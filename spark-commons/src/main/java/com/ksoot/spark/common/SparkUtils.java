package com.ksoot.spark.common;

import static com.ksoot.spark.common.JobConstants.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.springframework.util.Assert;

@Slf4j
@UtilityClass
public class SparkUtils {

  public static Column toSparkColumn(final String columnName) {
    Assert.hasText(columnName, "columnName is required");
    return functions.col(columnName);
  }

  public static Column[] toSparkColumns(final List<String> columnNames) {
    Assert.notEmpty(columnNames, "columnNames is required");
    return columnNames.stream().map(functions::col).toArray(Column[]::new);
  }

  public static Column[] toSparkColumns(final String... columnNames) {
    Assert.notEmpty(columnNames, "columnNames is required");
    return Arrays.stream(columnNames).map(functions::col).toArray(Column[]::new);
  }

  public static void logDataset(final String datasetName, final Dataset<Row> dataset) {
    logDataset(datasetName, dataset, 20);
  }

  public static void logDataset(
      final String datasetName, final Dataset<Row> dataset, final int numRows) {
    if (Objects.nonNull(dataset)) {
      log.info("----------- Dataset: {} -----------", datasetName);
      dataset.printSchema();
      dataset.show(numRows, false);
      log.info("..................................................", datasetName);
    } else {
      log.info("----------- Dataset: {} is null -----------", datasetName);
    }
  }
}
