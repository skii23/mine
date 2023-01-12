package com.fit2cloud.devops.config;


import com.fit2cloud.commons.server.security.SsoFilter;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager sessionManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(sessionManager);
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        shiroFilterFactoryBean.setSuccessUrl("/");

        shiroFilterFactoryBean.getFilters().put("sso", new SsoFilter());

        Map<String, String> filterChainDefinitionMap = shiroFilterFactoryBean.getFilterChainDefinitionMap();
        filterChainDefinitionMap.put("/resource/**", "anon");
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/eureka/**", "anon");
        filterChainDefinitionMap.put("/web-public/**", "anon");
        filterChainDefinitionMap.put("/project/**", "anon");
        //提供给jenkins caas更新配置接口
        filterChainDefinitionMap.put("/api/casc/jenkins.yaml", "anon");
        filterChainDefinitionMap.put("/403", "anon");
        filterChainDefinitionMap.put("/anonymous/**", "anon");
        filterChainDefinitionMap.put("/**", "sso, authc");
        return shiroFilterFactoryBean;
    }
}
