package com.ksoot.spark.common.dao;

import static org.apache.commons.lang3.StringUtils.*;

import com.ksoot.spark.common.JobConstants;
import com.ksoot.spark.common.conf.SparkConnectorProperties;
import com.ksoot.spark.common.util.SparkOptions;
import com.ksoot.spark.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.util.Assert;

@Slf4j
@RequiredArgsConstructor
public class SparkArangoRepository {
  public static final String VAR_COLLECTION = "collection";
  private static final String VAR_FILTER = "filter";
  private static final String VAR_PROJECTION = "projection";

  private static final String SELECT_TEMPLATE =
      "FOR ${" + VAR_COLLECTION + "} IN ${" + VAR_COLLECTION + "}";
  private static final String FILTER_TEMPLATE = "FILTER ${" + VAR_FILTER + "}";
  private static final String RETURN_PROJECTION_TEMPLATE = "RETURN {${" + VAR_PROJECTION + "}}";
  private static final String RETURN_COLLECTION_TEMPLATE = "RETURN ${" + VAR_COLLECTION + "}";

  private final SparkSession sparkSession;

  private final SparkConnectorProperties sparkConnectorProperties;

  private DataFrameReader dataFrameReader() {
    return this.sparkSession
        .read()
        .format(SparkOptions.Arango.FORMAT)
        .option(
            SparkOptions.Arango.ENDPOINTS, this.sparkConnectorProperties.getArango().endpoints())
        .option(
            SparkOptions.Arango.DATABASE, this.sparkConnectorProperties.getArango().getDatabase())
        .option(
            SparkOptions.Arango.USERNAME, this.sparkConnectorProperties.getArango().getUsername())
        .option(
            SparkOptions.Arango.PASSWORD, this.sparkConnectorProperties.getArango().getPassword())
        .option(
            SparkOptions.Arango.SSL_ENABLED,
            this.sparkConnectorProperties.getArango().isSslEnabled())
        .option(
            SparkOptions.Arango.CURSOR_TIME_TO_LIVE,
            this.sparkConnectorProperties.getArango().cursorTtl());
  }

  public Dataset<Row> findAll(final String collection) {
    log.info("Fetching Knowledge >> collection: {}", collection);
    Assert.hasText(collection, "ArangoDB collection name required");
    return this.dataFrameReader()
        .option(SparkOptions.Arango.TABLE, collection)
        .option(SparkOptions.Common.INFER_SCHEMA, true)
        .load();
  }

  public Dataset<Row> find(final String query) {
    log.info("Fetching Knowledge >> query: {}", query);
    Assert.hasText(query, "ArangoDB query required");
    return this.dataFrameReader()
        .option(SparkOptions.Arango.QUERY, query)
        .option(SparkOptions.Common.INFER_SCHEMA, true)
        .load();
  }

  public Dataset<Row> find(final String collection, final String filter, final String projection) {
    Assert.hasText(collection, "ArangoDB collection name required");
    log.info(
        "Fetching Knowledge >> collection: {}, filter: {}, projection: {}",
        collection,
        filter,
        projection);
    if (isBlank(filter) && isBlank(projection)) {
      return this.findAll(collection);
    } else {
      final String selectClause =
          StringUtils.substitute(SELECT_TEMPLATE, VAR_COLLECTION, collection);
      final String filterClause =
          isNotBlank(filter)
              ? StringUtils.substitute(FILTER_TEMPLATE, VAR_FILTER, filter)
              : JobConstants.BLANK;

      final String returnClause;
      if (isNotBlank(projection)) {
        final String finalProjection =
            StringUtils.substitute(projection, VAR_COLLECTION, collection);
        returnClause =
            StringUtils.substitute(RETURN_PROJECTION_TEMPLATE, VAR_PROJECTION, finalProjection);
      } else {
        returnClause =
            StringUtils.substitute(RETURN_COLLECTION_TEMPLATE, VAR_COLLECTION, collection);
      }

      final String query = this.createQuery(selectClause, filterClause, returnClause);
      return this.find(query);
    }
  }

  private String createQuery(
      final String selectClause, final String filterClause, final String returnClause) {
    return (selectClause.trim()
            + StringUtils.prependIfNotBlank(filterClause, SPACE)
            + StringUtils.prependIfNotBlank(returnClause, SPACE))
        .trim();
  }
}
