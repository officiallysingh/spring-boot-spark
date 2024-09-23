package com.ksoot.spark.common.conf;

import com.ksoot.spark.common.error.SparkError;
import com.ksoot.spark.common.error.SparkProblem;
import com.ksoot.spark.common.util.DurationRepresentation;
import java.time.Duration;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.task.listener.annotation.AfterTask;
import org.springframework.cloud.task.listener.annotation.BeforeTask;
import org.springframework.cloud.task.listener.annotation.FailedTask;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Log4j2
@RequiredArgsConstructor
@Component
public class JobExecutionListener {

  private final MessageSource messageSource;

  @BeforeTask
  public void onJobStart(final TaskExecution taskExecution) {
    log.info(
        "Task: {} with  executionId: {} started at: {} with arguments: {}",
        taskExecution.getTaskName(),
        taskExecution.getExecutionId(),
        taskExecution.getStartTime(),
        taskExecution.getArguments());
  }

  @AfterTask
  public void onJobSuccess(final TaskExecution taskExecution) {
    if (taskExecution.getExitCode() == 0) {
      DurationRepresentation duration =
          DurationRepresentation.of(
              Duration.between(taskExecution.getStartTime(), taskExecution.getEndTime()));
      log.info(
          "Task: {} with executionId: {} completed at: {} with exitCode: {} and exitMessage: {}. "
              + "Total time taken: {}",
          taskExecution.getTaskName(),
          taskExecution.getExecutionId(),
          taskExecution.getEndTime(),
          taskExecution.getExitCode(),
          taskExecution.getExitMessage(),
          duration);
    }
  }

  @FailedTask
  public void onJobFailure(final TaskExecution taskExecution, final Throwable throwable) {
    DurationRepresentation duration =
        DurationRepresentation.of(
            Duration.between(taskExecution.getStartTime(), taskExecution.getEndTime()));
    log.error(
        "Task: {} with executionId: {} failed at: {} with exitCode: {} and exitMessage: {}. "
            + "Total time taken: {}",
        taskExecution.getTaskName(),
        taskExecution.getExecutionId(),
        taskExecution.getEndTime(),
        taskExecution.getExitCode(),
        taskExecution.getExitMessage(),
        duration);
    SparkProblem sparkProblem;
    if (throwable instanceof SparkProblem e) {
      sparkProblem = e;
    } else {
      sparkProblem = SparkProblem.of(SparkError.unknown()).cause(throwable).build();
    }

    final String code = sparkProblem.getErrorType().code();
    final String defaultTitle = sparkProblem.getErrorType().title();
    final String defaultMessage = sparkProblem.getErrorType().message();
    final Object[] args = sparkProblem.getArgs();

    final String title = this.getMessage("title." + code, defaultTitle);
    final String message = this.getMessage("message." + code, defaultMessage, args);

    log.error("Code: {}, Title: {}, Message: {}", code, title, message);
  }

  private String getMessage(final String messageCode, final String defaultMessage) {
    return this.messageSource.getMessage(messageCode, null, defaultMessage, Locale.getDefault());
  }

  private String getMessage(
      final String messageCode, final String defaultMessage, final Object... params) {
    return this.messageSource.getMessage(messageCode, params, defaultMessage, Locale.getDefault());
  }
}
