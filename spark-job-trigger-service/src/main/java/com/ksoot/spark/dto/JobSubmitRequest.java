package com.ksoot.spark.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.*;

@Getter
@ToString
@Valid
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "jobName",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(
      value = DailySalesReportJobSubmitRequest.class,
      name = "daily-sales-report-job"),
  @JsonSubTypes.Type(value = LogsAnalysisJobSubmitRequest.class, name = "logs-analysis-job"),
})
public abstract class JobSubmitRequest {

  @Schema(
      description = "Spark Job name, must be present in application.yml spark-submit.jobs",
      example = "daily-sales-report-job")
  @NotEmpty
  protected final String jobName;

  /**
   * Spark conf properties for this job.
   *
   * @see <a
   *     href="https://spark.apache.org/docs/3.5.3/configuration.html#available-properties">Spark
   *     configurations</a>
   * @see <a
   *     href="https://spark.apache.org/docs/3.5.3/running-on-kubernetes.html#configuration">Spark
   *     Kubernetes configurations</a>
   */
  protected final Map<String, Object> sparkConfigs;

  protected JobSubmitRequest(final String jobName) {
    this.jobName = jobName;
    this.sparkConfigs = new LinkedHashMap<>();
  }

  protected JobSubmitRequest(final String jobName, final Map<String, Object> sparkConfigs) {
    this.jobName = jobName;
    this.sparkConfigs = sparkConfigs;
  }

  public abstract Map<String, String> jobVMOptions();

  public String correlationId() {
    return UUID.randomUUID().toString();
  }
}
