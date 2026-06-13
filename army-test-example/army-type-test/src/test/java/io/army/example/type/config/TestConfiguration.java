package io.army.example.type.config;


import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.domain.PostgreTypes;
import io.army.mapping.MappingEnv;
import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import io.army.session.SyncSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class TestConfiguration {


    @Scope("prototype")
    @Bean(name = "currentSession", destroyMethod = "close")
    public SyncSession currentSession(SyncSessionContext sessionContext) {
        return sessionContext.currentSession();
    }

    @Bean
    public MappingEnv mappingEnv(SyncSessionFactory sessionFactory) {
        return sessionFactory.mappingEnv();
    }

    @Scope("prototype")
    @Bean(name = "newPostgreTypeId")
    public Long newPostgreTypeId(@CurrentSession SyncSession session) {
        final PostgreTypes o = new PostgreTypes();
        session.save(o);
        return o.id;
    }

}
