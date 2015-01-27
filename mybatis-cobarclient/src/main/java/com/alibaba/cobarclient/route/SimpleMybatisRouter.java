package com.alibaba.cobarclient.route;

import com.alibaba.cobarclient.Shard;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class SimpleMybatisRouter implements Router {

    protected Logger logger = Logger.getLogger("SimpleMybatisRouter");

    private Map<String, RouteGroup> routes = new HashMap<String, RouteGroup>();

    private Set<Shard> EMPTY_SHARD_SET = new HashSet<Shard>();

    public SimpleMybatisRouter(Set<Route> routeSet) {
        if (!(routeSet == null || routeSet.isEmpty())) {
            for (Route route : routeSet) {
                if (!routes.containsKey(route.getSqlmap())) routes.put(route.getSqlmap(), new RouteGroup());
                if (route.getExpression() == null)
                    routes.get(route.getSqlmap()).setFallbackRoute(route);
                else
                    routes.get(route.getSqlmap()).getSpecificRoutes().add(route);
            }
        }
    }

    public Set<Shard> route(String action, Object argument) {
        Route resultRoute = findRoute(action, argument);
        if (resultRoute == null) {
            if (action != null) {
                String namespace = action.substring(0, action.lastIndexOf("."));
                resultRoute = findRoute(namespace, argument);
            }
            //兼容mybatis通过注解的方式如 接口类名+接口方法名:com.raycloud.demo.dao.ShopMapper.getShopByShopName
            if (resultRoute == null) {
                String array[] = action.split("\\.");
                if (array.length >= 2) {
                    String namespace = array[array.length - 2];
                    resultRoute = findRoute(namespace, argument);
                }
            }
        }
        if (resultRoute == null) {
            return EMPTY_SHARD_SET;
        } else {
            return resultRoute.getShards();
        }
    }


    protected Route findRoute(String action, Object argument) {
        if (routes.containsKey(action)) {
            RouteGroup routeGroup = routes.get(action);
            for (Route route : routeGroup.getSpecificRoutes()) {
                if (route.apply(action, argument)) {
                    return route;
                }
            }
            if (routeGroup.getFallbackRoute() != null && routeGroup.getFallbackRoute().apply(action, argument))
                return routeGroup.getFallbackRoute();
        }
        return null;
    }

    public static void main(String[] args) {
        String action1 = "com.raycloud.demo.dao.ShopMapper.getShopByShopName";
        String action2 = "com.raycloud.demo.dao.ShopMapper";
        String action3 = "com.raycloud.demo.dao";
        String action4 = "ShopMapper.getShopByShopName";
        String array[] = action1.split("\\.");
        System.out.println(array[array.length - 2]);

        String array1[] = action4.split("\\.");
        System.out.println(array1.length);
        System.out.println(array1[array1.length - 2]);
    }
}

