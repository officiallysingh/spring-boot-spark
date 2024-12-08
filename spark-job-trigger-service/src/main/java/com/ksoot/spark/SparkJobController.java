package com.ksoot.spark;

import com.ksoot.spark.dto.JobSubmitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.cloud.task.repository.TaskExplorer;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

  @Autowired @Lazy private TaskExplorer taskExplorer;

  @Operation(operationId = "submit-spark-job", summary = "Submit Spark Job")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "202",
            description = "Spark Job submitted successfully asynchronously"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping("/submit")
  ResponseEntity<String> submitSparkJob(@RequestBody @Valid JobSubmitRequest jobSubmitRequest)
      throws IOException, InterruptedException {
    log.info("Submitting Spark Job: {}", jobSubmitRequest.getJobName());
    Page<TaskExecution> tasks = taskExplorer.findAll(PageRequest.ofSize(10));
    System.out.println(tasks);
    this.sparkSubmitter.submit(jobSubmitRequest);

    return ResponseEntity.accepted()
        .body(
            "Spark Job: '"
                + jobSubmitRequest.getJobName()
                + "' submitted for execution asynchronously");
  }
}
