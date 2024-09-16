package com.ksoot.spark.sales;

import com.ksoot.spark.common.conf.SparkConnectorProperties;
import com.ksoot.spark.common.dao.SparkMongoRepository;
import com.ksoot.spark.common.executor.Executor;
import com.ksoot.spark.common.executor.publish.JobOutput;
import com.ksoot.spark.sales.conf.JobProperties;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SparkPipelineExecutor {

  private final SparkSession sparkSession;

  private final JobProperties jobProperties;

  private final SparkConnectorProperties connectorProperties;

  private final Executor<Dataset<Row>, JobOutput> publishExecutor;

  private final SparkMongoRepository sparkMongoRepository;

  public void execute() {
    log.info("Spark Pipeline Executor Started at: " + LocalDateTime.now());
    final StopWatch stopWatch = StopWatch.createStarted();

    Dataset<Row> datatset =
        this.sparkMongoRepository.findAll(connectorProperties.getMongo().getDatabase(), "sales");
    this.publishExecutor.execute(datatset);

    stopWatch.stop();
    log.info(
        "Spark Pipeline Executor completed at: {} successfully. Time taken: {}",
        LocalDateTime.now(),
        stopWatch.formatTime());
  }
}
