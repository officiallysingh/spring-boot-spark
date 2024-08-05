package com.ksoot.spark.common.logging.annotation;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface LogDataset {

  boolean show() default true;

  boolean printSchema() default true;

  boolean dump() default true;

  int numRows() default 8;
}
