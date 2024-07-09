package com.ksoot.spark.common;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * @author Rajveer Singh
 */
@Slf4j
@Order
class SparkDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {

  private static final String DEFAULT_PROPERTIES = "spark-defaults.conf";

  private static final String DEFAULT_PROPERTIES_SOURCE_NAME = "sparkDefaults";

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    log.debug("Adding Spark Default properties from : " + DEFAULT_PROPERTIES);

    Properties defaultProperties = new Properties();
    try {
      Resource extFile = new ClassPathResource(DEFAULT_PROPERTIES);
      if (extFile.exists()) {
        defaultProperties.putAll(ExternalPropertiesLoader.loadProperties(extFile));
        log.info("Default property file: " + DEFAULT_PROPERTIES + " added in property sources");
      } else {
        throw new ConfigException("File not found: " + DEFAULT_PROPERTIES);
      }
    } catch (IOException e) {
      throw new ConfigException(
          "Exception while reading default properties file: " + DEFAULT_PROPERTIES, e);
    }

    if (!defaultProperties.isEmpty()) {
      MutablePropertySources propertySources = environment.getPropertySources();
      PropertySource<?> existingDefaultProperties =
          propertySources.remove(DEFAULT_PROPERTIES_SOURCE_NAME);
      if (existingDefaultProperties != null) {
        Object src = existingDefaultProperties.getSource();
        if (ClassUtils.isAssignableValue(Map.class, src)) {
          defaultProperties.putAll((Map<?, ?>) src);
        } else {
          log.error(
              "Unknown default property source type: " + src.getClass() + ", handle accordingly");
        }
      } else {
        log.info("No default properties found before adding external default properties");
      }
      propertySources.addLast(
          new PropertiesPropertySource(DEFAULT_PROPERTIES_SOURCE_NAME, defaultProperties));
    } else {
      log.debug("No external default properties defined");
    }
  }
}
