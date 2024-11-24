package com.ksoot.spark.common.config;

import com.ksoot.spark.common.config.properties.ReaderProperties;
import com.ksoot.spark.common.config.properties.WriterProperties;
import com.ksoot.spark.common.connector.MongoConnector;
import com.mongodb.spark.sql.connector.MongoCatalog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.arangodb.datasource.ArangoTable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Slf4j
@Validated
public class SparkConnectorConfiguration {

  @Bean
  @ConfigurationProperties("ksoot.connector.reader")
  ReaderProperties readerProperties() {
    return new ReaderProperties();
  }

  @Bean
  @ConfigurationProperties("ksoot.connector.writer")
  WriterProperties writerProperties() {
    return new WriterProperties();
  }


  @Slf4j
  @ConditionalOnClass(MongoCatalog.class)
  static class MongoConnectorConfiguration {

    @Bean
    MongoConnector sparkMongoRepository(
            final SparkSession sparkSession, final ReaderProperties readerProperties, final WriterProperties writerProperties) {
      return new MongoConnector(sparkSession, readerProperties, writerProperties);
    }
  }

  @Slf4j
  @ConditionalOnClass(ArangoTable.class)
  static class ArangoConnectorConfiguration {

    @Bean
    SparkArangoRepository sparkArangoRepository(
            final SparkSession sparkSession, final SparkConnectorConfiguration sparkConnectorConfiguration) {
      log.info(
              "ArangoDB Configurations >> Endpoints: {}, Database: {}, Username: {}",
              sparkConnectorConfiguration.getArango().endpoints(),
              sparkConnectorConfiguration.getArango().getDatabase(),
              sparkConnectorConfiguration.getArango().getUsername());
      return new SparkArangoRepository(sparkSession, sparkConnectorConfiguration);
    }
  }

}
