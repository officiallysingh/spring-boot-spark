package com.ksoot.spark.common.conf;

import jakarta.validation.Valid;
import java.time.Duration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.hadoop.shaded.org.checkerframework.checker.index.qual.Positive;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Validated
public class SparkConnectorProperties {

  /** MongoDB Configurations */
  private Mongo mongo = new Mongo();

  /** ArangoDB Configurations */
  private Arango arango = new Arango();

  @Getter
  @Setter
  @NoArgsConstructor
  @ToString
  @Valid
  public static class Mongo {

    /** MongoDB URL to connect to, Default: mongodb://localhost:27017 */
    private String url = "mongodb://localhost:27017";

    /** Features Database name, Default: sales_db */
    private String database = "sales_db";
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @ToString
  @Valid
  public static class Arango {

    /** List of ArangoDB endpoints, Default: localhost:8529 */
    private String[] endpoints = {"localhost:8529"};

    /** ArangoDB database name, Default: _system */
    private String database = "_system";

    /** ArangoDB username, Default root: */
    private String username = "root";

    /** ArangoDB password, Default: "" */
    private String password = "";

    /** Enable SSL for ArangoDB connection, Default: false */
    private boolean sslEnabled;

    /** ArangoDB SSL certificate value, Default: "" */
    private String sslCertValue = "";

    /**
     * Cursor time to live in seconds, see the ISO 8601 standard for java.time.Duration String
     * patterns, Default: 30 seconds
     */
    @Positive private Duration cursorTtl = Duration.ofSeconds(30);

    public long cursorTtl() {
      return this.cursorTtl.toSeconds();
    }

    public String endpoints() {
      return String.join(",", this.endpoints);
    }
  }
}