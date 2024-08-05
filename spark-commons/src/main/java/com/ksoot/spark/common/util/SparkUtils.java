package com.ksoot.spark.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
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

  public static Column[] toSparkColumns(final Collection<String> columnNames) {
    Assert.notEmpty(columnNames, "columnNames is required");
    return columnNames.stream().map(functions::col).toArray(Column[]::new);
  }

  public static Column[] toSparkColumns(final String... columnNames) {
    Assert.notEmpty(columnNames, "columnNames is required");
    return Arrays.stream(columnNames).map(functions::col).toArray(Column[]::new);
  }

  public static boolean containColumns(final Dataset<Row> dataset, final List<String> columnNames) {
    Assert.notEmpty(columnNames, "columnNames required");
    final String[] datasetColumns = dataset.columns();
    return columnNames.stream().allMatch(col -> ArrayUtils.contains(datasetColumns, col));
  }

  public static boolean containColumns(final Dataset<Row> dataset, final String... columnNames) {
    Assert.notEmpty(columnNames, "columnNames required");
    final String[] datasetColumns = dataset.columns();
    return Arrays.stream(columnNames).allMatch(col -> ArrayUtils.contains(datasetColumns, col));
  }

  public static boolean containsColumn(final Dataset<Row> dataset, final String columnName) {
    return Arrays.stream(dataset.columns()).anyMatch(col -> col.equals(columnName));
  }

  public static boolean doesNotContainColumn(final Dataset<Row> dataset, final String columnName) {
    return Arrays.stream(dataset.columns()).noneMatch(col -> col.equals(columnName));
  }

  public static void logDataset(final String datasetName, final Dataset<Row> dataset) {
    logDataset(datasetName, dataset, 20);
  }

  public static void logDataset(
      final String datasetName, final Dataset<Row> dataset, final int numRows) {
    if (Objects.nonNull(dataset)) {
      log.debug("----------- Dataset: {} -----------", datasetName);
      dataset.printSchema();
      dataset.show(numRows, false);
      log.debug("..................................................");
    } else {
      log.debug("----------- Dataset: {} is null -----------", datasetName);
    }
  }
}
