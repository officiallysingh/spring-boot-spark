package com.ksoot.spark.common.connector;

import com.ksoot.spark.common.config.properties.ReaderProperties;
import com.ksoot.spark.common.config.properties.WriterProperties;
import com.ksoot.spark.common.util.SparkOptions;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.types.StructType;

@Slf4j
@RequiredArgsConstructor
public class FileConnector {

  protected final SparkSession sparkSession;

  protected final ReaderProperties readerProperties;

  protected final WriterProperties writerProperties;

  public Dataset<Row> read(final String format, final String file, final StructType schema) {
    return this.sparkSession
        .read()
        .format(format)
        .schema(schema)
        .options(this.readerProperties.getFileOptions().options())
        .load(file);
  }

  public Dataset<Row> read(final String format, final String file) {
    return this.sparkSession
        .read()
        .format(format)
        .option(SparkOptions.Common.INFER_SCHEMA, true)
        .options(this.readerProperties.getFileOptions().options())
        .load(file);
  }

  public void write(final Dataset<Row> dataset, final Map<String, String> options) {
    Dataset<Row> result = dataset;
    result =
        this.writerProperties.getFileOptions().isMerge()
            ? result.coalesce(1) // Write to a single file
            : result;
    log.info(
        "Writing output at location: {} in {} format.",
        this.writerProperties.getFileOptions().getPath(),
        this.writerProperties.getFileOptions().getFormat());
    result
        .write()
        .mode(writerProperties.getSaveMode())
        .format(this.writerProperties.getFileOptions().getFormat())
        .options(options)
        .save(this.writerProperties.getFileOptions().getPath());
  }

  public DataStreamWriter<Row> writeStream(
      final Dataset<Row> dataset, final Map<String, String> options) {
    Dataset<Row> result = dataset;
    result =
        this.writerProperties.getFileOptions().isMerge()
            ? result.coalesce(1) // Write to a single file
            : result;
    log.info(
        "Streaming output at location: {} in {} format.",
        this.writerProperties.getFileOptions().getPath(),
        this.writerProperties.getFileOptions().getFormat());
    return result
        .writeStream()
        .outputMode(this.writerProperties.outputMode())
        .format(this.writerProperties.getFileOptions().getFormat())
        .options(this.writerProperties.getFileOptions().options())
        .options(options)
        .option(
            SparkOptions.Common.CHECKPOINT_LOCATION, this.writerProperties.getCheckpointLocation());
  }
}
