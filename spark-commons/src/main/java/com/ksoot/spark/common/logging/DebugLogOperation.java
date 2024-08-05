package com.ksoot.spark.common.logging;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DebugLogOperation {

  private boolean enabled;

  private String className;

  private String methodName;

  private String description;

  private boolean logDataset;

  private int numRows;

  private boolean printSchema;

  private boolean dumpDataset;

  private Map<String, String> args;

  private Map<String, String> results;
}
