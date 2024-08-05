package com.ksoot.spark.common.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DebugLog {

  boolean enabled() default true;

  String description() default "";

  LogDataset dataset() default @LogDataset;

  LogVar[] values() default {};
}
