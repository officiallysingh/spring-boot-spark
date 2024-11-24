package com.ksoot.spark.common.config;

import com.ksoot.spark.common.connector.dao.SparkArangoRepository;
import com.ksoot.spark.common.connector.dao.SparkMongoRepository;
import com.ksoot.spark.common.executor.Executor;
import com.ksoot.spark.common.executor.publish.JobOutput;
import com.ksoot.spark.common.executor.publish.PublishExecutor;
import com.ksoot.spark.common.executor.publish.PublishProperties;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.context.MessageSource;
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
    SparkConnectorConfiguration sparkConnectorProperties() {
      return new SparkConnectorConfiguration();
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

  @ConditionalOnClass(TaskExecution.class)
  static class JobExecutionListenerConfiguration {

    @Bean
    JobExecutionListener jobExecutionListener(final MessageSource messageSource) {
      return new JobExecutionListener(messageSource);
    }
  }
}
