package com.ksoot.spark.common.error;

public interface ErrorType {

  ErrorType DEFAULT = SparkError.unknown();

  String code();

  String title();

  String message();
}
