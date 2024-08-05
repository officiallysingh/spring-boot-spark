package com.ksoot.spark;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/v1/spark-jobs")
@Tag(name = "Spark Job", description = "APIs")
@Slf4j
@RequiredArgsConstructor
class SparkJobController {

  private final SparkSubmitter sparkSubmitter;

  @Operation(operationId = "submit-spark-job", summary = "Submit Spark Job")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Spark Job submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping("/submit")
  ResponseEntity<String> submitSparkJob(
      @RequestBody @Valid SparkJobSubmitRequest sparkJobSubmitRequest)
      throws IOException, InterruptedException {

    this.sparkSubmitter.submit(sparkJobSubmitRequest.getJobName());

    return ResponseEntity.accepted()
        .body("Spark Job: '" + sparkJobSubmitRequest.getJobName() + "'");
  }
}
