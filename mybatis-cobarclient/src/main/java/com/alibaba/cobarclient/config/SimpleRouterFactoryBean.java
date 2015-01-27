package com.alibaba.cobarclient.config;

import com.alibaba.cobarclient.Shard;
import com.alibaba.cobarclient.config.vo.InternalRule;
import com.alibaba.cobarclient.config.vo.InternalRules;
import com.alibaba.cobarclient.expr.MVELExpression;
import com.alibaba.cobarclient.route.Route;
import com.alibaba.cobarclient.route.Router;
import com.alibaba.cobarclient.route.SimpleMybatisRouter;
import com.thoughtworks.xstream.XStream;
import java.util.*;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

public class SimpleRouterFactoryBean implements FactoryBean, InitializingBean {

    private Resource configLocation;
    private Resource[] configLocations;
    private SimpleMybatisRouter router = null;
    private Map<String, Object> functions;
    private Set<Shard> shards;

    public void afterPropertiesSet() throws Exception {

        List<InternalRule> allRules = new ArrayList<InternalRule>();
        if (getConfigLocation() != null) {
            List<InternalRule> rules = loadRules(configLocation);
            if (!CollectionUtils.isEmpty(rules)) {
                allRules.addAll(rules);
            }
        }

        if (!ObjectUtils.isEmpty(getConfigLocations())) {
            for (Resource res : getConfigLocations()) {
                List<InternalRule> rules = loadRules(res);
                if (!CollectionUtils.isEmpty(rules)) {
                    allRules.addAll(rules);
                }
            }
        }

        //构造生成router需要的routes参数
        if (!CollectionUtils.isEmpty(allRules)) {
            Map<String, Shard> shardMap = convertShardMap(shards);
            Set<Route> routes = new LinkedHashSet<Route>();
            Route route;
            Set<Shard> subShard;
            for (InternalRule rule : allRules) {
                String sqlmap = rule.getSqlmap();
                if (sqlmap == null || sqlmap.equals("")) {
                    sqlmap = rule.getNamespace();
                }
                //通常这里size == 1
                String[] shardArr = rule.getShards().split(",");
                subShard = new LinkedHashSet<Shard>();
                for (String shardId : shardArr) {
                    Shard tempShard = shardMap.get(shardId);
                    if (tempShard == null) {
                        throw new NullPointerException("shard:" + shardId + " is not exists");
                    }
                    subShard.add(tempShard);
                }
                if (null != rule.getShardingExpression()) {
                    if (null == functions) {
                        functions = new HashMap<String, Object>();
                    }
                    route = new Route(sqlmap, new MVELExpression(rule.getShardingExpression(), functions), subShard);
                } else {
                    route = new Route(sqlmap, null, subShard);
                }
                routes.add(route);
            }

            router = new SimpleMybatisRouter(routes);
        }
    }

    private Map<String, Shard> convertShardMap(Set<Shard> shards) {
        Map<String, Shard> shardMap = new HashMap<String, Shard>();
        for (Shard shard : shards) {
            shardMap.put(shard.getId(), shard);
        }
        return shardMap;
    }

    private List<InternalRule> loadRules(Resource configLocation) throws Exception {
        XStream xstream = new XStream();
        xstream.alias("rules", InternalRules.class);
        xstream.alias("rule", InternalRule.class);
        xstream.addImplicitCollection(InternalRules.class, "rules");
        xstream.useAttributeFor(InternalRule.class, "merger");

        InternalRules internalRules = (InternalRules) xstream
                .fromXML(configLocation.getInputStream());
        return internalRules.getRules();
    }

    public Router getObject() throws Exception {
        return router;
    }

    public Class getObjectType() {
        return Router.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public Resource getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    public Resource[] getConfigLocations() {
        return configLocations;
    }

    public void setConfigLocations(Resource[] configLocations) {
        this.configLocations = configLocations;
    }

    public Map<String, Object> getFunctions() {
        return functions;
    }

    public void setFunctions(Map<String, Object> functions) {
        this.functions = functions;
    }

    public Set<Shard> getShards() {
        return shards;
    }

    public void setShards(Set<Shard> shards) {
        this.shards = shards;
    }

}
