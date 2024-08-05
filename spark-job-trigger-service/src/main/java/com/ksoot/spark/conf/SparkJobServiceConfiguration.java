package com.ksoot.spark.conf;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SparkSubmitProperties.class)
@RequiredArgsConstructor
class SparkJobServiceConfiguration {

  private final SparkSubmitProperties sparkSubmitProperties;
}
