package com.ksoot.spark.common.error;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SparkError implements ErrorType {

  public static String BAD_REQUEST_TITLE = "Bad Request";
  private static final String DEFAULT_ERROR_CODE = "spark.unknown.error";
  private static final String DEFAULT_TITLE = "Spark Error";
  private static final String DEFAULT_MESSAGE = "Something went wrong in Spark pipeline";

  private final String code;
  private final String title;
  private final String message;

  public static SparkError of(final String code, final String title, final String message) {
    Assert.hasText(code, "code required");
    Assert.hasText(title, "title required");
    Assert.hasText(message, "message required");
    return new SparkError(code, title, message);
  }

  public static SparkError of(final String title, final String message) {
    return of(DEFAULT_ERROR_CODE, title, message);
  }

  public static SparkError of(final String message) {
    return of(DEFAULT_ERROR_CODE, DEFAULT_TITLE, message);
  }

  public static SparkError unknown() {
    return of(DEFAULT_ERROR_CODE, DEFAULT_TITLE, DEFAULT_MESSAGE);
  }

  @Override
  public String code() {
    return this.code;
  }

  @Override
  public String title() {
    return this.title;
  }

  @Override
  public String message() {
    return this.message;
  }

  @Override
  public String toString() {
    return "SparkError{"
        + "code: '"
        + code
        + '\''
        + ", title: '"
        + code
        + '\''
        + ", message: '"
        + message
        + '\''
        + '}';
  }
}
