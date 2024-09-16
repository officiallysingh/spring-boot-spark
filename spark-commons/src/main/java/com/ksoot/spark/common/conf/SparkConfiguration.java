package com.ksoot.spark.common.conf;

import com.ksoot.spark.common.dao.SparkArangoRepository;
import com.ksoot.spark.common.dao.SparkMongoRepository;
import com.ksoot.spark.common.executor.Executor;
import com.ksoot.spark.common.executor.publish.JobOutput;
import com.ksoot.spark.common.executor.publish.PublishExecutor;
import com.ksoot.spark.common.executor.publish.PublishProperties;
import com.ksoot.spark.common.logging.DebugLogAspect;
import com.ksoot.spark.common.logging.DebugLogProperties;
import com.mongodb.spark.sql.connector.MongoCatalog;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.arangodb.datasource.ArangoTable;
import org.apache.spark.sql.types.DataTypes;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Log4j2
@RequiredArgsConstructor
class SparkConfiguration {

  private static final String SPARK_PREFIX = "spark.";

  @Bean
  Properties sparkProperties(final Environment environment) {
    if (environment instanceof ConfigurableEnvironment) {
      final List<PropertySource<?>> propertySources =
          ((ConfigurableEnvironment) environment)
              .getPropertySources().stream().collect(Collectors.toList());
      final List<String> sparkPropertyNames =
          propertySources.stream()
              .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
              .map(propertySource -> (EnumerablePropertySource) propertySource)
              .map(EnumerablePropertySource::getPropertyNames)
              .flatMap(Arrays::stream)
              .distinct()
              .filter(key -> key.startsWith(SPARK_PREFIX))
              .collect(Collectors.toList());

      return sparkPropertyNames.stream()
          .collect(
              Properties::new,
              (props, key) -> props.put(key, environment.getProperty(key)),
              Properties::putAll);
    } else {
      return new Properties();
    }
  }

  @ConditionalOnClass(SparkSession.class)
  @ConditionalOnMissingBean(SparkSession.class)
  static class SparkSessionConfiguration {

    @Bean
    @ConfigurationProperties("ksoot.connector")
    SparkConnectorProperties sparkConnectorProperties() {
      return new SparkConnectorProperties();
    }

    @Bean(name = "sparkSession", destroyMethod = "stop")
    SparkSession sparkSession(final SparkConf sparkConf) {
      SparkSession sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
      sparkSession
          .udf()
          .register(
              UserDefinedFunctions.EXPLODE_DATE_SEQ,
              UserDefinedFunctions.explodeDateSeq,
              DataTypes.createArrayType(DataTypes.TimestampType));
      sparkSession
          .udf()
          .register(
              UserDefinedFunctions.FIRST_VECTOR_ELEMENT,
              UserDefinedFunctions.firstVectorElement,
              DataTypes.DoubleType);
      //      sparkSession.stop();
      return sparkSession;
    }
  }

  @ConditionalOnClass(value = {SparkSession.class, ProceedingJoinPoint.class, Advisor.class})
  static class AOPConfiguration {

    @Bean
    DebugLogAspect debugLogAspect(
        final DebugLogProperties properties, final ConfigurableBeanFactory beanFactory) {
      return new DebugLogAspect(properties, beanFactory);
    }

    @Bean
    @ConfigurationProperties("ksoot.debug-log")
    DebugLogProperties debugLogProperties() {
      return new DebugLogProperties();
    }
  }

  @ConditionalOnClass(SparkConf.class)
  @ConditionalOnMissingBean(SparkConf.class)
  static class SparkConfConfiguration {

    @Bean
    SparkConf sparkConf(@Qualifier("sparkProperties") final Properties sparkProperties) {
      final SparkConf sparkConf = new SparkConf();
      sparkProperties.forEach((key, value) -> sparkConf.set(key.toString(), value.toString()));
      return sparkConf;
    }
  }

  @ConditionalOnClass(SparkSession.class)
  static class ExecutorConfiguration {

    @Bean
    @ConfigurationProperties("ksoot.publish")
    PublishProperties publishProperties() {
      return new PublishProperties();
    }

    @Bean
    Executor<Dataset<Row>, JobOutput> publishExecutor(final PublishProperties publishProperties) {
      return new PublishExecutor(publishProperties);
    }
  }

  @ConditionalOnClass(MongoCatalog.class)
  static class MongoConnectorConfiguration {

    @Bean
    SparkMongoRepository sparkMongoRepository(
        final SparkSession sparkSession, final SparkConnectorProperties sparkConnectorProperties) {
      log.info(
          "MongoDB Configurations >> Url: {}, Database: {}",
          sparkConnectorProperties.getMongo().getUrl(),
          sparkConnectorProperties.getMongo().getDatabase());
      return new SparkMongoRepository(sparkSession, sparkConnectorProperties);
    }
  }

  @ConditionalOnClass(ArangoTable.class)
  static class ArangoConnectorConfiguration {

    @Bean
    SparkArangoRepository sparkArangoRepository(
        final SparkSession sparkSession, final SparkConnectorProperties sparkConnectorProperties) {
      log.info(
          "ArangoDB Configurations >> Endpoints: {}, Database: {}, Username: {}",
          sparkConnectorProperties.getArango().endpoints(),
          sparkConnectorProperties.getArango().getDatabase(),
          sparkConnectorProperties.getArango().getUsername());
      return new SparkArangoRepository(sparkSession, sparkConnectorProperties);
    }
  }
}
