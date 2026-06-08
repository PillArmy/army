package io.army.example.type.config;

import io.army.generator.FieldGeneratorFactory;
import io.army.generator.StandaloneFieldGeneratorFactory;
import io.army.session.SyncSessionContext;
import io.army.session.SyncSessionFactory;
import io.army.spring.DefaultArmyTransactionTemplate;
import io.army.spring.TransactionTemplate;
import io.army.spring.sync.ArmySyncLocalTransactionManager;
import io.army.spring.sync.ArmySyncSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class TypeDataSourceConfig {

    @Bean
    public FieldGeneratorFactory typeFieldGeneratorFactory() {
        return StandaloneFieldGeneratorFactory.getInstance();
    }

    @Bean
    public ArmySyncSessionFactoryBean typeSyncSessionFactory(DataSource dataSource) {
        final ArmySyncSessionFactoryBean factoryBean = ArmySyncSessionFactoryBean.create();
        factoryBean
                .setFactoryName("army-type")
                .setDataSource(dataSource)
                .setPackagesToScan(List.of("io.army.example.type.domain"))
                .setFieldGeneratorFactory(typeFieldGeneratorFactory());
        return factoryBean;
    }

    @Bean
    public ArmySyncLocalTransactionManager typeSyncTransactionManager(SyncSessionFactory sessionFactory) {
        return ArmySyncLocalTransactionManager.create(sessionFactory)
                .setUseDataSourceTimeout(true)
                .setUseTransactionName(true)
                .setUseTransactionLabel(true)
                .setPseudoTransactionAllowed(true);
    }

    @Bean
    public SyncSessionContext typeSyncSessionContext(ArmySyncLocalTransactionManager manager) {
        return manager.getSessionContext();
    }

    @Bean
    public TransactionTemplate typeTransactionTemplate(ArmySyncLocalTransactionManager manager) {
        return new DefaultArmyTransactionTemplate(manager, true);
    }

}
