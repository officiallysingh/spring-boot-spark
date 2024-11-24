package com.ksoot.spark.common.config.properties;

import jakarta.validation.constraints.NotEmpty;
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
public class KafkaOptions {

  /** Kafka Bootstrap servers. Default: */
  @NotEmpty private String[] bootstrapServers = {"localhost:9092"};

  /** Kafka topic. Default: */
  @NotEmpty private String topic;

  /** Kafka starting offsets. Default: latest */
  @NotEmpty private String startingOffsets = "latest";

  public String bootstrapServers() {
    return String.join(",", this.bootstrapServers);
  }
}
