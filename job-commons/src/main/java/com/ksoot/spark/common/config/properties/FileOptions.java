package com.ksoot.spark.common.config.properties;

import com.ksoot.spark.common.util.SparkOptions;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Validated
public class FileOptions {

  /** Output file format, csv or parquet. Default: csv */
  @NotEmpty private String format = "csv";

  /**
   * Whether to create header row in an output file. Column names would be the columns in Spark
   * dataset.
   */
  @NotNull private boolean header = true;

  /** Whether to merge output into a single file. Applicable only to file writers. Default: false */
  private boolean merge = false;

  /** Output file path. Applicable only to file writers. Default: output */
  @NotEmpty private String path = "spark-space/output";

  public Map<String, String> options() {
    return Map.of(SparkOptions.Common.HEADER, String.valueOf(this.header), "nullValue", "null");
  }

  public boolean isCsv() {
    return this.format.equals(SparkOptions.CSV.FORMAT);
  }

  public boolean isParquet() {
    return this.format.equals(SparkOptions.Parquet.FORMAT);
  }
}