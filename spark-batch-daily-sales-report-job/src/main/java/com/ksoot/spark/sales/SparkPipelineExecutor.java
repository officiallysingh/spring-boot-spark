package com.ksoot.spark.sales;

import com.ksoot.spark.common.connector.FileConnector;
import com.ksoot.spark.common.connector.MongoConnector;
import com.ksoot.spark.sales.conf.JobProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  private final FileConnector fileConnector;

  private final MongoConnector mongoConnector;

  public void execute() {
    Dataset<Row> datatset = this.mongoConnector.read("sales");
    this.fileConnector.write(datatset);
  }
}
