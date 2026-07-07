package io.army.example.coder.config;

import io.army.generator.FieldGeneratorFactory;
import io.army.generator.StandaloneFieldGeneratorFactory;
import io.army.session.SyncSessionContext;
import io.army.session.SyncSessionFactory;
import io.army.spring.sync.ArmySyncLocalTransactionManager;
import io.army.spring.sync.ArmySyncSessionFactoryBean;
import io.army.spring.sync.DefaultArmyTransactionTemplate;
import io.army.spring.sync.TransactionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;


@Configuration
public class DataSourceConfiguration {


    @Bean
    public FieldGeneratorFactory coderFieldGeneratorFactory() {
        return StandaloneFieldGeneratorFactory.getInstance();
    }


    @Bean
    public ArmySyncSessionFactoryBean coderSyncSessionFactory(DataSource dataSource) {

        final ArmySyncSessionFactoryBean factoryBean = ArmySyncSessionFactoryBean.create();

        factoryBean
                .setFactoryName("army-coder")
                .setDataSource(dataSource)
                .setPackagesToScan(List.of("io.army.example.coder.domain"))
                .setFieldGeneratorFactory(coderFieldGeneratorFactory())
                .setFactoryAdviceCollection(List.of(new CoderSessionFactoryAdvisor()))
        ;

        return factoryBean;
    }

    @Bean
    public ArmySyncLocalTransactionManager coderSyncTransactionManager(SyncSessionFactory sessionFactory) {

        final ArmySyncLocalTransactionManager manager = ArmySyncLocalTransactionManager.create(sessionFactory);
        manager.setNestedTransactionAllowed(true);

        return manager.setUseDataSourceTimeout(true)
                .setUseTransactionName(true)
                .setUseTransactionLabel(true)
                .setPseudoTransactionAllowed(true);
    }


    @Bean
    public SyncSessionContext coderSyncSessionContext(ArmySyncLocalTransactionManager manager) {
        return manager.getSessionContext();
    }

    @Bean
    public TransactionTemplate coderTransactionTemplate(ArmySyncLocalTransactionManager manager) {
        return new DefaultArmyTransactionTemplate(manager, true);
    }

}
