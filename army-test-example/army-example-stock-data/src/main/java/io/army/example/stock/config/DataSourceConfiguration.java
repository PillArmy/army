package io.army.example.stock.config;

import io.army.generator.FieldGeneratorFactory;
import io.army.generator.StandaloneFieldGeneratorFactory;
import io.army.session.SyncSessionContext;
import io.army.session.SyncSessionFactory;
import io.army.spring.ArmyTransactionTemplate;
import io.army.spring.DefaultArmyTransactionTemplate;
import io.army.spring.sync.ArmySyncLocalTransactionManager;
import io.army.spring.sync.ArmySyncSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class DataSourceConfiguration {


    @Bean
    public FieldGeneratorFactory stockFieldGeneratorFactory() {
        return StandaloneFieldGeneratorFactory.getInstance();
    }


    @Bean
    public ArmySyncSessionFactoryBean stockSyncSessionFactory(DataSource dataSource) {

        final ArmySyncSessionFactoryBean factoryBean = ArmySyncSessionFactoryBean.create();

        factoryBean
                .setFactoryName("army-stock")
                .setDataSource(dataSource)
                .setPackagesToScan(List.of("io.army.example.stock.domain", "io.army.spring.ai.chat.memory"))
                .setFieldGeneratorFactory(stockFieldGeneratorFactory())
                .setFactoryAdviceCollection(List.of(new StockSessionFactoryAdvisor()))
        ;

        return factoryBean;
    }

    @Bean
    public ArmySyncLocalTransactionManager stockSyncTransactionManager(SyncSessionFactory sessionFactory) {

        final ArmySyncLocalTransactionManager manager = ArmySyncLocalTransactionManager.create(sessionFactory);
        manager.setNestedTransactionAllowed(true);

        return manager.setUseDataSourceTimeout(true)
                .setUseTransactionName(true)
                .setUseTransactionLabel(true)
                .setPseudoTransactionAllowed(true);
    }


    @Bean
    public SyncSessionContext stockSyncSessionContext(ArmySyncLocalTransactionManager manager) {
        return manager.getSessionContext();
    }

    @Bean
    public ArmyTransactionTemplate stockTransactionTemplate(ArmySyncLocalTransactionManager manager) {
        return new DefaultArmyTransactionTemplate(manager);
    }


}
