package com.ksoot.spark.statement;

import com.ksoot.spark.statement.conf.JobProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;

@Slf4j
@EnableTask
@SpringBootApplication
@EnableConfigurationProperties(JobProperties.class)
public class SparkStatementJob {

  public static void main(String[] args) {
    SpringApplication.run(SparkStatementJob.class, args);
  }

  @PostConstruct
  public void init() {
    log.info("Initializing SparkStatementJob ...");
  }

  @Bean
  public ApplicationRunner applicationRunner(final SparkPipelineExecutor sparkPipelineExecutor) {
    return new SparkStatementJobRunner(sparkPipelineExecutor);
  }

  @Slf4j
  static class SparkStatementJobRunner implements ApplicationRunner {

    private final SparkPipelineExecutor sparkPipelineExecutor;

    public SparkStatementJobRunner(final SparkPipelineExecutor sparkPipelineExecutor) {
      this.sparkPipelineExecutor = sparkPipelineExecutor;
    }

    @Override
    public void run(final ApplicationArguments args) {
      this.sparkPipelineExecutor.execute();
    }
  }
}
