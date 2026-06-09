package io.army.example.type.config;


import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class TestConfiguration {


    @Scope("prototype")
    @Bean(destroyMethod = "close")
    public SyncSession currentSession(SyncSessionContext sessionContext) {
        return sessionContext.currentSession();
    }

}
