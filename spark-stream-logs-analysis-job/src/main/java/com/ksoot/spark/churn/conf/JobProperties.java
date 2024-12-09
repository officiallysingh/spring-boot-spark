package com.ksoot.spark.churn.conf;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Validated
@ConfigurationProperties(prefix = "ksoot.job", ignoreInvalidFields = true)
public class JobProperties {

  /** Unique correlation id for each Job execution. */
  @NotEmpty private String correlationId;
}
