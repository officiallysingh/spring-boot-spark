package com.ksoot.spark.wordcount;

import com.ksoot.spark.wordcount.conf.JobProperties;
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
public class SparkWordCountJob {

  public static void main(String[] args) {
    SpringApplication.run(SparkWordCountJob.class, args);
  }

  @PostConstruct
  public void init() {
    log.info("Initializing SparkWordCountJob ...");
  }

  @Bean
  public ApplicationRunner applicationRunner(final SparkPipelineExecutor sparkPipelineExecutor) {
    return new SparkWordCountJobRunner(sparkPipelineExecutor);
  }

  @Slf4j
  static class SparkWordCountJobRunner implements ApplicationRunner {

    private final SparkPipelineExecutor sparkPipelineExecutor;

    public SparkWordCountJobRunner(final SparkPipelineExecutor sparkPipelineExecutor) {
      this.sparkPipelineExecutor = sparkPipelineExecutor;
    }

    @Override
    public void run(final ApplicationArguments args) {
      this.sparkPipelineExecutor.execute();
    }
  }
}
