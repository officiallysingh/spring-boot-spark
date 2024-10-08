package com.ksoot.spark.conf;

import jakarta.validation.constraints.NotEmpty;
import java.util.LinkedHashMap;
import java.util.Map;
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
@ConfigurationProperties(prefix = "spark-submit")
public class SparkSubmitProperties {

  /** Spark installation path. */
  @NotEmpty private String sparkHome = System.getenv("SPARK_HOME");

  /** Whether to collect the spark submitted Job's logs in this service. Default: false */
  private boolean captureJobLogs = false;

  private Map<@NotEmpty String, SparkJobProperties> jobs = new LinkedHashMap();
}
