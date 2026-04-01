package com.example.permission.config;

import com.pluginpolicyengine.core.FeatureFlagService;
import com.pluginpolicyengine.core.FlagStore;
import com.pluginpolicyengine.core.store.InMemoryFlagStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthzPolicyEngineConfig {

    @Bean
    public InMemoryFlagStore authzPolicyFlagStore() {
        return new InMemoryFlagStore();
    }

    @Bean
    public FeatureFlagService authzPolicyEngine(FlagStore authzPolicyFlagStore) {
        return new FeatureFlagService(authzPolicyFlagStore);
    }
}
