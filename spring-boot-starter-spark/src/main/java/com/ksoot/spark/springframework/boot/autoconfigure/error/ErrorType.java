package com.ksoot.spark.springframework.boot.autoconfigure.error;

public interface ErrorType {

  ErrorType DEFAULT = JobErrorType.unknown();

  String code();

  String title();

  String message();
}
