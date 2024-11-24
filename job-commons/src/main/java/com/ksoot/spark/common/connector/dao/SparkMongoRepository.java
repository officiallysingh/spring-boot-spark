// package com.ksoot.spark.common.connector.dao;
//
// import com.ksoot.spark.common.config.SparkConnectorProperties;
// import com.ksoot.spark.common.util.SparkOptions;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.spark.sql.DataFrameReader;
// import org.apache.spark.sql.Dataset;
// import org.apache.spark.sql.Row;
// import org.apache.spark.sql.SparkSession;
// import org.springframework.util.Assert;
//
// @Slf4j
// @RequiredArgsConstructor
// public class SparkMongoRepository {
//
//  private final SparkSession sparkSession;
//
//  private final SparkConnectorProperties sparkConnectorProperties;
//
//  private DataFrameReader dataFrameReader() {
//    return this.sparkSession
//        .read()
//        .format(SparkOptions.Mongo.FORMAT)
//        .option(
//            SparkOptions.Mongo.READ_CONNECTION_URI,
//            this.sparkConnectorProperties.getMongo().getUrl());
//  }
//
//  public Dataset<Row> findAll(final String database, final String collection) {
//    log.info("Fetching MongoDB >> database: {}, collection: {}", database, collection);
//    Assert.hasText(database, "MongoDB database name required");
//    Assert.hasText(collection, "MongoDB collection name required");
//    return this.dataFrameReader()
//        .option(SparkOptions.Mongo.DATABASE, database)
//        .option(SparkOptions.Mongo.COLLECTION, collection)
//        .option(SparkOptions.Common.INFER_SCHEMA, true)
//        .load();
//  }
//
//  public Dataset<Row> find(
//      final String database, final String collection, final String aggregationPipeline) {
//    Assert.hasText(database, "MongoDB database name required");
//    Assert.hasText(collection, "MongoDB collection name required");
//    Assert.hasText(aggregationPipeline, "MongoDB aggregationPipeline required");
//    log.info(
//        "Fetching MongoDB >> database: {}, collection: {}, aggregationPipeline: {}",
//        database,
//        collection,
//        aggregationPipeline);
//    return this.dataFrameReader()
//        .option(SparkOptions.Mongo.DATABASE, database)
//        .option(SparkOptions.Mongo.COLLECTION, collection)
//        .option(SparkOptions.Mongo.AGGREGATION_PIPELINE, aggregationPipeline)
//        .option(SparkOptions.Common.INFER_SCHEMA, true)
//        .load();
//  }
// }
