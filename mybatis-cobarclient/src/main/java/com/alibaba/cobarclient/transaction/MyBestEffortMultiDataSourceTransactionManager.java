package com.alibaba.cobarclient.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.alibaba.cobarclient.Shard;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.*;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class MyBestEffortMultiDataSourceTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

    private Set<Shard> shards;

    protected List<AbstractPlatformTransactionManager> transactionManagers;

    public MyBestEffortMultiDataSourceTransactionManager() {
    }

    public MyBestEffortMultiDataSourceTransactionManager(Set<Shard> shards) {
        this.shards = shards;
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return new ArrayList<DefaultTransactionStatus>();
    }

    @Override
    protected void doBegin(Object o, TransactionDefinition transactionDefinition) throws TransactionException {
        List<TransactionStatus> statusList = (List<TransactionStatus>) o;
        try {
            for (AbstractPlatformTransactionManager transactionManager : transactionManagers) {
                statusList.add(transactionManager.getTransaction(transactionDefinition));
            }
        } catch (Throwable e) {
            //多个库开启事务成功,某个数据库闪断会导致开启失败的场景下. 回收已开启的状态
            logger.error("存在数据库开启事务失败,可能是数据库闪断或者无法连接,强制回滚之前开启的事务:" + e.getMessage(), e);
            for (int i = 0; i < statusList.size(); i++) {
                AbstractPlatformTransactionManager transactionManager = transactionManagers.get(i);
                TransactionStatus status = statusList.get(i);
                try {
                    transactionManager.rollback(status);
                } catch (TransactionException ex) {
                    ;
                }
            }
            if (e instanceof TransactionException) {
                throw (TransactionException) e;
            } else {
                throw new TransactionSystemException("存在数据库开启事务失败,强制回滚:" + e.getMessage(), e);
            }
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus defaultTransactionStatus) throws TransactionException {
        MultipleCauseException ex = new MultipleCauseException();
        List<TransactionStatus> statusList = (List<TransactionStatus>) defaultTransactionStatus.getTransaction();
        for (int i = transactionManagers.size() - 1; i >= 0; i--) {
            AbstractPlatformTransactionManager transactionManager = transactionManagers.get(i);
            TransactionStatus status = statusList.get(i);
            try {
                transactionManager.commit(status);
            } catch (TransactionException e) {
                ex.add(e);
            }
        }
        if (!ex.getCauses().isEmpty())
            throw new HeuristicCompletionException(HeuristicCompletionException.STATE_UNKNOWN, ex);

    }

    @Override
    protected void doRollback(DefaultTransactionStatus defaultTransactionStatus) throws TransactionException {
        MultipleCauseException ex = new MultipleCauseException();
        List<TransactionStatus> statusList = (List<TransactionStatus>) defaultTransactionStatus.getTransaction();
        for (int i = transactionManagers.size() - 1; i >= 0; i--) {
            AbstractPlatformTransactionManager transactionManager = transactionManagers.get(i);
            TransactionStatus status = statusList.get(i);
            try {
                transactionManager.rollback(status);
            } catch (TransactionException e) {
                e.printStackTrace();
                ex.add(e);
            }
        }
        if (!ex.getCauses().isEmpty())
            throw new UnexpectedRollbackException("one or more error on rolling back the transaction", ex);
    }

    public Set<Shard> getShards() {
        return shards;
    }

    public void setShards(Set<Shard> shards) {
        this.shards = shards;
    }

    public void afterPropertiesSet() throws Exception {
        if (shards == null || shards.isEmpty()) throw new IllegalArgumentException("'shards' is required.");
        transactionManagers = new ArrayList<AbstractPlatformTransactionManager>();
        for (Shard shard : shards) {
            DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(shard.getDataSource());
            transactionManager.setDefaultTimeout(getDefaultTimeout());
            transactionManager.setTransactionSynchronization(getTransactionSynchronization());
            transactionManagers.add(transactionManager);
        }
    }
}
