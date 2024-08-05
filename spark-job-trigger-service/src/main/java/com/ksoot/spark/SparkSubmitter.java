package com.ksoot.spark;

import static com.ksoot.spark.common.util.StringUtils.substitute;

import com.ksoot.spark.conf.SparkJobProperties;
import com.ksoot.spark.conf.SparkSubmitProperties;
import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
public class SparkSubmitter {

  private static final String SPARK_SUBMIT_SCRIPT = "spark-job-submit.sh";
  private static final String SPARK_DRIVER_EXTRA_JAVA_OPTIONS = "spark.driver.extraJavaOptions";

  private static final String JOB_NAME = "jobName";
  private static final String MAIN_CLASS_NAME = "mainClassName";
  private static final String CONF_PROPERTY = "confProperty";
  private static final String DEPLOY_MODE = "spark.submit.deployMode";
  private static final String DEPLOY_MODE_CLIENT = "client";

  private static final String MODEL_SCHEMA_KEY_VAR = "modelSchemaKey";
  private static final String MODEL_EXECUTION_TYPE_VAR = "modelExecutionType";
  private static final String MODEL_SCHEMA_JAVA_OPTIONS_TEMPLATE =
      "-DMODEL_SCHEMA_KEY=${"
          + MODEL_SCHEMA_KEY_VAR
          + "} -DMODEL_EXECUTION_TYPE=${"
          + MODEL_EXECUTION_TYPE_VAR
          + "}";

  //  private static final String SUBMIT_CMD_TEMPLATE =
  //      "./bin/spark-submit --name ${" + JOB_NAME + "} --class ${" + MAIN_CLASS_NAME + "} ";
  private static final String SUBMIT_CMD_TEMPLATE =
      "./bin/spark-submit --class ${" + MAIN_CLASS_NAME + "} ";
  private static final String SUBMIT_CMD_CONF_TEMPLATE = "--conf ${confProperty}";

  private static final ExecutorService executor = Executors.newCachedThreadPool();

  private final Properties sparkProperties;

  private final SparkSubmitProperties sparkSubmitProperties;

  SparkSubmitter(
      @Qualifier("sparkProperties") final Properties sparkProperties,
      final SparkSubmitProperties sparkSubmitProperties) {
    this.sparkProperties = sparkProperties;
    this.sparkSubmitProperties = sparkSubmitProperties;
  }

  public void submit(final String jobName) throws InterruptedException, IOException {
    Assert.hasText(jobName, "jobName is required");

    log.info("============================================================");

    final SparkJobProperties sparkJobProperties = this.sparkSubmitProperties.getJobs().get(jobName);
    final Properties jobSparkConf =
        this.confProperties(this.sparkProperties, sparkJobProperties.getConf());

    final String baseCommand =
        substitute(
            SUBMIT_CMD_TEMPLATE,
            JOB_NAME,
            jobName,
            MAIN_CLASS_NAME,
            sparkJobProperties.getMainClassName());
    final String confCommand = this.getConfCommand(jobSparkConf);
    final String jobSubmitCommand =
        baseCommand + confCommand + " " + sparkJobProperties.getJarFile();
    log.info("spark-submit command: {}", jobSubmitCommand);

    final File directory = new File(this.sparkSubmitProperties.getSparkHome());

    final String sparkSubmitScriptPath = this.getSparkSubmitScriptPath();

    final ProcessBuilder processBuilder =
        new ProcessBuilder(sparkSubmitScriptPath).directory(directory);

    final Map<String, String> environment = processBuilder.environment();
    environment.put("JOB_SUBMIT_COMMAND", jobSubmitCommand);

    final Process process;
    final int exitCode;
    // Start the process
    if (this.sparkSubmitProperties.isCaptureJobLogs()) {
      process = processBuilder.inheritIO().start();
      final StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), log::info);
      final StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), log::error);
      executor.submit(outputGobbler);
      executor.submit(errorGobbler);
    } else {
      process = processBuilder.start();
    }

    // Wait for the process to complete
    exitCode = process.waitFor();

    // Get the inputs.waitFor();
    log.info(
        "spark-submit completed with code: {}, status: {}",
        exitCode,
        (exitCode == 0 ? "SUCCESS" : "FAILURE"));
    log.info("============================================================");
  }

  private String getSparkSubmitScriptPath() throws IOException {
    String sparkSubmitScriptPath;
    try {
      // A Heck to get spark-job-submit.sh path at project root while running in local
      final Resource resource =
          new ClassPathResource("META-INF/additional-spring-configuration-metadata.json");
      final Path currentPath = resource.getFile().toPath();
      sparkSubmitScriptPath =
          currentPath.getParent().getParent().getParent().getParent().toString()
              + "/"
              + SPARK_SUBMIT_SCRIPT;
    } catch (final FileNotFoundException e) {
      sparkSubmitScriptPath = "./bin/" + SPARK_SUBMIT_SCRIPT;
    }
    log.info("spark-job-submit.sh file path: {}", sparkSubmitScriptPath);
    return sparkSubmitScriptPath;
  }

  private Properties confProperties(
      final Properties sparkProperties, final Properties sparkJobConfProperties) {
    final Properties confProperties = new Properties();
    confProperties.putAll(sparkProperties);
    confProperties.putAll(sparkJobConfProperties);
    if (!confProperties.containsKey(DEPLOY_MODE)) {
      log.warn(DEPLOY_MODE + " not specified, falling back to: " + DEPLOY_MODE_CLIENT);
      confProperties.putIfAbsent(DEPLOY_MODE, DEPLOY_MODE_CLIENT);
    }
    return confProperties.entrySet().stream()
        .filter(
            property -> property != null && StringUtils.isNotBlank(property.getValue().toString()))
        .collect(
            Collectors.toMap(
                entry -> entry.getKey().toString(),
                entry -> entry.getValue(),
                (v1, v2) -> v1,
                Properties::new));
  }

  private String getConfCommand(final Properties jobSpecificSparkConf) {
    final String confCommand =
        jobSpecificSparkConf.entrySet().stream()
            .map(
                property ->
                    property.getKey().toString().startsWith(SPARK_DRIVER_EXTRA_JAVA_OPTIONS)
                        ? property.getKey()
                            + "="
                            + StringUtils.wrap(String.valueOf(property.getValue()), '"')
                        : property.getKey() + "=" + property.getValue())
            .map(confProperty -> substitute(SUBMIT_CMD_CONF_TEMPLATE, CONF_PROPERTY, confProperty))
            .collect(Collectors.joining(" "));
    return confCommand;
  }

  static class StreamGobbler implements Runnable {
    private final InputStream inputStream;
    private final Consumer<String> consumer;

    StreamGobbler(final InputStream inputStream, final Consumer<String> consumer) {
      this.inputStream = inputStream;
      this.consumer = consumer;
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
    }
  }
}
