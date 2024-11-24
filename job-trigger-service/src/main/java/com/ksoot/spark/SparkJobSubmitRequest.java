package com.ksoot.spark;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ksoot.spark.common.util.JobConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Valid
public class SparkJobSubmitRequest {

  @Schema(
      description = "Spark Job name, must be present in application.yml spark-submit.jobs",
      example = "daily-sales-report-job",
      defaultValue = "daily-sales-report-job",
      hidden = true)
  @JsonIgnore
  @NotEmpty
  private String jobName = JobConstants.JOB_NAME_SPARK_STATEMENT_JOB;
}
