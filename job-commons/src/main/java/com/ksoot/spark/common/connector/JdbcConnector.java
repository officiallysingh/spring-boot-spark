package com.ksoot.spark.common.connector;

import com.ksoot.spark.common.config.properties.ReaderProperties;
import com.ksoot.spark.common.config.properties.WriterProperties;
import com.ksoot.spark.common.util.SparkOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.types.StructType;

@Slf4j
@RequiredArgsConstructor
public class JdbcConnector {

  protected final SparkSession sparkSession;

  protected final ReaderProperties readerProperties;

  protected final WriterProperties writerProperties;

  public Dataset<Row> read(final String table) {
    Dataset<Row> dataFrame =
        this.sparkSession
            .read()
            .jdbc(
                this.readerProperties.getJdbcOptions().getUrl(),
                table,
                this.readerProperties.getJdbcOptions().options());
    return dataFrame;
  }

  public Dataset<Row> read(final String table, final StructType schema) {
    Dataset<Row> dataFrame =
        this.sparkSession
            .read()
            .schema(schema)
            .jdbc(
                this.readerProperties.getJdbcOptions().getUrl(),
                table,
                this.readerProperties.getJdbcOptions().options());
    return dataFrame;
  }

  public void write(final Dataset<Row> dataset, final String table) {
    dataset
        .write()
        .mode(this.writerProperties.getSaveMode())
        .jdbc(
            this.writerProperties.getJdbcOptions().getUrl(),
            table,
            this.writerProperties.getJdbcOptions().options());
  }

  public DataStreamWriter<Row> writeStream(final Dataset<Row> dataset, final String table) {
    log.info(
        "Streaming data to database: {} table: {}",
        this.writerProperties.getJdbcOptions().getDatabase(),
        table);
    // Write each micro-batch to PostgreSQL
    return dataset
        .writeStream()
        .outputMode(this.writerProperties.outputMode())
        .foreachBatch(
            (batchDataset, batchId) -> {
              batchDataset
                  .write()
                  .mode(this.writerProperties.getSaveMode())
                  .jdbc(
                      this.writerProperties.getJdbcOptions().getUrl(),
                      table,
                      this.writerProperties.getJdbcOptions().options());
            })
        .option(
            SparkOptions.Common.CHECKPOINT_LOCATION, this.writerProperties.getCheckpointLocation());
  }
}