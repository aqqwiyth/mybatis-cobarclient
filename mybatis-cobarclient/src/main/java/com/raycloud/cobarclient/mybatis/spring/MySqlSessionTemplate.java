/*
 *    Copyright 2010-2012 The MyBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.raycloud.cobarclient.mybatis.spring;

import com.alibaba.cobarclient.Shard;
import com.alibaba.cobarclient.route.Router;
import com.alibaba.cobarclient.transaction.MultipleCauseException;
import com.alibaba.mtc.threadpool.MtContextExecutors;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.TransactionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionHolder;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.CollectionUtils;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.ibatis.reflection.ExceptionUtil.unwrapThrowable;
import static org.springframework.util.Assert.notNull;

public class MySqlSessionTemplate implements SqlSession, InitializingBean, DisposableBean {
    private Log log = LogFactory.getLog(MySqlSessionTemplate.class);
    protected Map<String, Environment> environmentMap = new HashMap<String, Environment>();
    protected Set<Shard> shards;
    protected Router router;

    private TransactionFactory transactionFactory = new SpringManagedTransactionFactory();


    private final SqlSessionFactory sqlSessionFactory;

    private final ExecutorType executorType;

    private final SqlSession sqlSessionProxy;

    private final PersistenceExceptionTranslator exceptionTranslator;

    private boolean useDefaultExecutor = false;
    private ExecutorService executor = Executors.newFixedThreadPool(20);

    /**
     * Constructs a Spring managed SqlSession with the {@code SqlSessionFactory}
     * provided as an argument.
     *
     * @param sqlSessionFactory
     */
    public MySqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType());
    }

    /**
     * Constructs a Spring managed SqlSession with the {@code SqlSessionFactory}
     * provided as an argument and the given {@code ExecutorType}
     * {@code ExecutorType} cannot be changed once the {@code SqlSessionTemplate}
     * is constructed.
     *
     * @param sqlSessionFactory
     * @param executorType
     */
    public MySqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) {
        this(sqlSessionFactory, executorType,
                new MyBatisExceptionTranslator(
                        sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true));
    }

    /**
     * Constructs a Spring managed {@code SqlSession} with the given
     * {@code SqlSessionFactory} and {@code ExecutorType}.
     * A custom {@code SQLExceptionTranslator} can be provided as an
     * argument so any {@code PersistenceException} thrown by MyBatis
     * can be custom translated to a {@code RuntimeException}
     * The {@code SQLExceptionTranslator} can also be null and thus no
     * exception translation will be done and MyBatis exceptions will be
     * thrown
     *
     * @param sqlSessionFactory
     * @param executorType
     * @param exceptionTranslator
     */
    public MySqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
                                PersistenceExceptionTranslator exceptionTranslator) {
        notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
        notNull(executorType, "Property 'executorType' is required");

        this.sqlSessionFactory = sqlSessionFactory;
        this.executorType = executorType;
        this.exceptionTranslator = exceptionTranslator;
        this.sqlSessionProxy = (SqlSession) newProxyInstance(
                SqlSessionFactory.class.getClassLoader(),
                new Class[]{SqlSession.class},
                new SqlSessionInterceptor());
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return this.sqlSessionFactory;
    }

    public ExecutorType getExecutorType() {
        return this.executorType;
    }

    public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {
        return this.exceptionTranslator;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T selectOne(String statement) {
        return this.sqlSessionProxy.<T>selectOne(statement);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T selectOne(String statement, Object parameter) {
        return this.sqlSessionProxy.<T>selectOne(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.sqlSessionProxy.<K, V>selectMap(statement, mapKey);
    }

    /**
     * {@inheritDoc}
     */
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.sqlSessionProxy.<K, V>selectMap(statement, parameter, mapKey);
    }

    /**
     * {@inheritDoc}
     */
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return this.sqlSessionProxy.<K, V>selectMap(statement, parameter, mapKey, rowBounds);
    }

    /**
     * {@inheritDoc}
     */
    public <E> List<E> selectList(String statement) {
        return this.sqlSessionProxy.<E>selectList(statement);
    }

    /**
     * {@inheritDoc}
     */
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.sqlSessionProxy.<E>selectList(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.<E>selectList(statement, parameter, rowBounds);
    }

    /**
     * {@inheritDoc}
     */
    public void select(String statement, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, handler);
    }

    /**
     * {@inheritDoc}
     */
    public void select(String statement, Object parameter, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, handler);
    }

    /**
     * {@inheritDoc}
     */
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, rowBounds, handler);
    }

    /**
     * {@inheritDoc}
     */
    public int insert(String statement) {
        return this.sqlSessionProxy.insert(statement);
    }

    /**
     * {@inheritDoc}
     */
    public int insert(String statement, Object parameter) {
        if (parameter instanceof Collection) {
            Collection collection = (Collection) parameter;
            if (CollectionUtils.isEmpty(collection)) {
                return 0;
            } else {
                if (collection.size() <= 100) {
                    return batchSync(statement, collection);
                } else {
                    return batchAsync(statement, collection);
                }
            }
        }
        return this.sqlSessionProxy.insert(statement, parameter);
    }


    /**
     * 同步方式批量提交
     *
     * @param statement
     * @param collection
     * @param <T>
     * @return
     */
    private <T extends Object> int batchSync(final String statement, Collection<T> collection) {
        Map<Shard, List<T>> classifiedEntities = classify(statement, collection);
        final MultipleCauseException throwables = new MultipleCauseException();
        int counter = 0;
        for (final Map.Entry<Shard, List<T>> entry : classifiedEntities.entrySet()) {
            Environment environment = environmentMap.get(entry.getKey().getId());
            //打开一个批量操作
            final SqlSession sqlSession = SqlSessionUtils.getSqlSession(
                    MySqlSessionTemplate.this.sqlSessionFactory,
                    ExecutorType.BATCH,
                    MySqlSessionTemplate.this.exceptionTranslator, environment);
            try {
                for (T item : entry.getValue()) {
                    sqlSession.update(statement, item);
                }
                List<BatchResult> results = sqlSession.flushStatements();
                int[] updateCounts = results.get(0).getUpdateCounts();
                for (int i = 0; i < updateCounts.length; i++) {
                    int value = updateCounts[i];
                    counter += value;
                }
            } catch (Throwable e) {
                Throwable unwrapped = unwrapThrowable(e);
                if (MySqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                    Throwable translated = MySqlSessionTemplate.this.exceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
                    if (translated != null) {
                        unwrapped = translated;
                    }
                }
                throwables.add(unwrapped);
            } finally {
                SqlSessionUtils.closeSqlSession(sqlSession, MySqlSessionTemplate.this.sqlSessionFactory);
            }
        }
        if (!throwables.getCauses().isEmpty()) {
            throw new TransientDataAccessResourceException("one or more errors when performing data access operations  against multiple shards", throwables);
        }
        return counter;
    }

    /**
     * TODO 使用异步方式会有Multiple Thread Transaction，无法回滚
     *
     * @param statement
     * @param collection
     * @param <T>
     * @return
     */
    private final <T extends Object> int batchAsync(final String statement, Collection<T> collection) {
        Map<Shard, List<T>> classifiedEntities = classify(statement, collection);
        final CountDownLatch latch = new CountDownLatch(classifiedEntities.size());
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
        final MultipleCauseException throwables = new MultipleCauseException();
        ExecutorService _executor = MtContextExecutors.getMtcExecutorService(executor);
        SqlSessionHolder holder = SqlSessionUtils.currentSqlSessionHolder(MySqlSessionTemplate.this.sqlSessionFactory);
        for (final Map.Entry<Shard, List<T>> entry : classifiedEntities.entrySet()) {
            futures.add(_executor.submit(new BatchAsyncCallable(entry, statement, latch, throwables, holder)));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new ConcurrencyFailureException(
                    "interrupted when processing data access request in concurrency", e);
        }
        if (!throwables.getCauses().isEmpty()) {
            throw new TransientDataAccessResourceException("one or more errors when performing data access operations" +
                    " against multiple shards", throwables);
        }
        return counter(getFutureResults(futures));
    }

    class BatchAsyncCallable<T> implements Callable<Integer> {
        private Map.Entry<Shard, List<T>> entry;
        private String statement;
        private CountDownLatch latch;
        private MultipleCauseException throwables;
        private SqlSessionHolder sqlSessionHolder;

        BatchAsyncCallable(Map.Entry<Shard, List<T>> entry,
                           String statement,
                           CountDownLatch latch,
                           MultipleCauseException throwables, SqlSessionHolder sqlSessionHolder) {
            this.entry = entry;
            this.statement = statement;
            this.latch = latch;
            this.throwables = throwables;
            this.sqlSessionHolder = sqlSessionHolder;
        }

        public Integer call() throws Exception {
            Environment environment = environmentMap.get(entry.getKey().getId());
            final SqlSession sqlSession = SqlSessionUtils.getSqlSession(
                    MySqlSessionTemplate.this.sqlSessionFactory,
                    ExecutorType.BATCH/**BaseExecutor**/,
                    MySqlSessionTemplate.this.exceptionTranslator,
                    environment,
                    sqlSessionHolder/**主线程的sqlSessionHolder**/);
            int counter = 0;
            try {
                for (Object item : entry.getValue()) {
                    sqlSession.update(statement, item);
                }
                List<BatchResult> results = sqlSession.flushStatements();
                if (results.size() != 1) {
                    throw new InvalidDataAccessResourceUsageException("Batch execution returned invalid results. Expected 1 but number of BatchResult objects returned was " + results.size());
                }
                int[] updateCounts = results.get(0).getUpdateCounts();
                for (int i = 0; i < updateCounts.length; i++) {
                    int value = updateCounts[i];
                    counter += value;
                }
                return counter;
            } catch (Throwable e) {
                Throwable unwrapped = unwrapThrowable(e);
                if (MySqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                    Throwable translated = MySqlSessionTemplate.this.exceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
                    if (translated != null) {
                        unwrapped = translated;
                    }
                }
                throwables.add(unwrapped);
            } finally {
                SqlSessionUtils.closeSqlSession(sqlSession, MySqlSessionTemplate.this.sqlSessionFactory);
                latch.countDown();
            }
            return counter;
        }
    }

    private int counter(List<Integer> result) {
        int counter = 0;
        for (Integer row : result) {
            counter += row;
        }
        return counter;
    }

    private <T> List<T> getFutureResults(List<Future<T>> futures) {
        List<T> result = new ArrayList<T>();
        for (Future<T> future : futures) {
            try {
                result.add(future.get());
            } catch (InterruptedException e) {
                throw new ConcurrencyFailureException(
                        "interrupted when processing data access request in concurrency", e);
            } catch (ExecutionException e) {
                throw new ConcurrencyFailureException("something goes wrong in processing", e);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */

    public int update(String statement) {
        return this.sqlSessionProxy.update(statement);
    }

    /**
     * {@inheritDoc}
     */
    public int update(String statement, Object parameter) {
        if (parameter instanceof Collection) {
            Collection collection = (Collection) parameter;
            if (CollectionUtils.isEmpty(collection)) {
                return 0;
            } else {
                return batchSync(statement, collection);
            }
        }
        return this.sqlSessionProxy.update(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    public int delete(String statement) {
        return this.sqlSessionProxy.delete(statement);
    }

    /**
     * {@inheritDoc}
     */
    public int delete(String statement, Object parameter) {
        return this.sqlSessionProxy.delete(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T getMapper(Class<T> type) {
        return getConfiguration().getMapper(type, this);
    }

    /**
     * {@inheritDoc}
     */
    public void commit() {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    public void commit(boolean force) {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    public void rollback() {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    public void rollback(boolean force) {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        throw new UnsupportedOperationException("Manual close is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    public void clearCache() {
        this.sqlSessionProxy.clearCache();
    }

    /**
     * {@inheritDoc}
     */
    public Configuration getConfiguration() {
        return this.sqlSessionFactory.getConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    public Connection getConnection() {
        return this.sqlSessionProxy.getConnection();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0.2
     */
    public List<BatchResult> flushStatements() {
        return this.sqlSessionProxy.flushStatements();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /****HOOK***/
        if (shards == null || shards.isEmpty())
            throw new IllegalArgumentException("'shards' argument is required.");
        if (router == null) throw new IllegalArgumentException("'router' argument is required");
        if (executor == null) {
            useDefaultExecutor = true;
            executor = Executors.newCachedThreadPool(new ThreadFactory() {
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "Sql-Executor-thread");
                }
            });
        }
        for (Shard shard : shards) {
            Environment environment = new Environment(shard.getId(), this.transactionFactory, shard.getDataSource());
            environmentMap.put(shard.getId(), environment);
        }
    }

    /**
     * Proxy needed to route MyBatis method calls to the proper SqlSession got
     * from Spring's Transaction Manager
     * It also unwraps exceptions thrown by {@code Method#invoke(Object, Object...)} to
     * pass a {@code PersistenceException} to the {@code PersistenceExceptionTranslator}.
     */
    private class SqlSessionInterceptor implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Set<Shard> shards;
            final SqlSession sqlSession;
            if (args.length == 1) {
                shards = router.route(args[0].toString(), null);
            } else {
                shards = router.route(args[0].toString(), args[1]);
            }
            if (shards.isEmpty()) {
                log.warn(args[0] + "没有找到路由规则,选用默认数据源");
                sqlSession = SqlSessionUtils.getSqlSession(
                        MySqlSessionTemplate.this.sqlSessionFactory,
                        MySqlSessionTemplate.this.executorType,
                        MySqlSessionTemplate.this.exceptionTranslator
                );
            } else if (shards.size() == 1) {
                Environment environment = environmentMap.get(shards.iterator().next().getId());
                sqlSession = SqlSessionUtils.getSqlSession(
                        MySqlSessionTemplate.this.sqlSessionFactory,
                        MySqlSessionTemplate.this.executorType,
                        MySqlSessionTemplate.this.exceptionTranslator, environment);
            } else {
                throw new RuntimeException("暂时不支持多读或者多写操作...");
            }
            try {
                Object result = method.invoke(sqlSession, args);
                if (!SqlSessionUtils.isSqlSessionTransactional(sqlSession, MySqlSessionTemplate.this.sqlSessionFactory)) {
                    // force commit even on non-dirty sessions because some databases require
                    // a commit/rollback before calling close()
                    sqlSession.commit(true);
                }
                return result;
            } catch (Throwable t) {
                Throwable unwrapped = unwrapThrowable(t);
                if (MySqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                    Throwable translated = MySqlSessionTemplate.this.exceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
                    if (translated != null) {
                        unwrapped = translated;
                    }
                }
                throw unwrapped;
            } finally {
                SqlSessionUtils.closeSqlSession(sqlSession, MySqlSessionTemplate.this.sqlSessionFactory);
            }
        }
    }

    /**
     * 批量处理的时候把同一个shard的放到一个集合里批量操作
     *
     * @param statementName
     * @param entities
     * @return
     */
    private <T extends Object> Map<Shard, List<T>> classify(String statementName, Collection<T> entities) {
        Map<Shard, List<T>> shardEntityMap = new HashMap<Shard, List<T>>();
        for (Object entity : entities) {
            Set<Shard> shards = router.route(statementName, entity);
            for (Shard shard : shards) {
                List shardEntities = shardEntityMap.get(shard);
                if (null == shardEntities) {
                    shardEntities = new ArrayList<Object>();
                    shardEntityMap.put(shard, shardEntities);
                }
                shardEntities.add(entity);
            }
        }
        if (shardEntityMap.size() == 0) throw new RuntimeException("没有找到对应的路由信息");
        return shardEntityMap;
    }


    public void setRouter(Router router) {
        this.router = router;
    }

    public void setShards(Set<Shard> shards) {
        this.shards = shards;
    }

    public void setTransactionFactory(TransactionFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
    }

    public void destroy() throws Exception {
        if (useDefaultExecutor) {
            executor.shutdown();
        }
    }
}
