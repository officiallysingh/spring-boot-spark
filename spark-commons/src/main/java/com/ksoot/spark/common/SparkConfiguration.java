package com.ksoot.spark.common;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
class SparkConfiguration {

  private static final String SPARK_PREFIX = "spark.";

  @Bean
  Properties sparkProperties(final Environment environment) {
    if (environment instanceof ConfigurableEnvironment) {
      List<PropertySource<?>> propertySources =
          ((ConfigurableEnvironment) environment)
              .getPropertySources().stream().collect(Collectors.toList());
      List<String> sparkPropertyNames =
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

    @Bean(name = "sparkSession", destroyMethod = "stop")
    SparkSession sparkSession(final SparkConf sparkConf) {
      return SparkSession.builder().config(sparkConf).getOrCreate();
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
}
