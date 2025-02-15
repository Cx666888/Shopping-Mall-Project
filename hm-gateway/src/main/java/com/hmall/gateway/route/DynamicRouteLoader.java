package com.hmall.gateway.route;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteLoader {
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final NacosConfigManager nacosConfigManager;
    // 路由配置文件的id和分组
    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";
    private  final Set<String> routeIds  =new HashSet<>();
    @PostConstruct
    public void initRouteConfigListener () throws NacosException {
        // 1.项目启动，先拉取一次配置并添加监听器
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }
//监听配置变更，更新路由表
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        updateConfigInfo(configInfo);
                    }
                });
        //首次读取到配置，也需要更新路由表
        updateConfigInfo(configInfo);
    }

    private void updateConfigInfo(String configInfo) {
        log.debug("监听到路由配置变更，{}", configInfo);
        if(CollUtil.isNotEmpty(routeIds)){
           /* for (String routeId:
                    routeIds) {
                routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
            }*/
            routeIds.forEach(routeId->routeDefinitionWriter.delete(Mono.just(routeId)).subscribe());
        }
        routeIds.clear();
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
       /* for (RouteDefinition routeDefinition:
             routeDefinitions) {
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
routeIds.add(routeDefinition.getId());
        }*/
routeDefinitions.forEach(routeDefinition->{
    routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
    routeIds.add(routeDefinition.getId());
});
    }

}
