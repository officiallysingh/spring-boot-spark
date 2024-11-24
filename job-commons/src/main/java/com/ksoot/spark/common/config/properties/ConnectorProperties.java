package com.ksoot.spark.common.config.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Validated
public abstract class ConnectorProperties {

  /** Connector type, where to write data. postgres or files */
//  @NotNull protected ConnectorType type = ConnectorType.file;

  protected JdbcOptions jdbcOptions = new JdbcOptions();

  protected MongoOptions mongoOptions = new MongoOptions();

  protected ArangoOptions arangoOptions = new ArangoOptions();

  protected KafkaOptions kafkaOptions = new KafkaOptions();

//  public enum ConnectorType {
//    file,
//    jdbc,
//    mongo,
//    arango,
//    kafka;
//  }
}
