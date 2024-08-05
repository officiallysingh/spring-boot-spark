package com.ksoot.spark.common.logging;

import static com.ksoot.spark.common.JobConstants.BLANK;
import static com.ksoot.spark.common.JobConstants.SLASH;

import com.ksoot.spark.common.logging.annotation.DebugLog;
import com.ksoot.spark.common.logging.annotation.LogDataset;
import com.ksoot.spark.common.logging.annotation.LogVar;
import com.ksoot.spark.common.util.SparkOptions;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SaveMode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Log4j2
@Aspect
@Component
public class DebugLogAspect {

  private static final String RESULT_EXPRESSION_PREFIX = "#result";
  private static final String RESULT_VARIABLE = "result";

  private final DebugLogProperties properties;

  private final BeanExpressionContext beanExpressionContext;

  private final BeanExpressionResolver beanExpressionResolver;

  private final ParameterNameDiscoverer parameterNameDiscoverer;

  private final ExpressionParser expressionParser;

  private final Map<AnnotatedElementKey, DebugLogOperation> logOperations;

  private final Map<AnnotatedElementKey, Integer> counters;

  public DebugLogAspect(
      final DebugLogProperties properties, final ConfigurableBeanFactory beanFactory) {
    this.properties = properties;
    this.beanExpressionContext =
        beanFactory == null ? null : new BeanExpressionContext(beanFactory, null);
    this.beanExpressionResolver = new StandardBeanExpressionResolver();
    this.parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    this.expressionParser = new SpelExpressionParser();
    this.logOperations = new ConcurrentHashMap<>();
    this.counters = new ConcurrentHashMap<>();
  }

  //  @Around("execution (@com.telos.spark.common.logging.annotation.DebugLog * *.*(..))")
  @Around("@annotation(com.ksoot.spark.common.logging.annotation.DebugLog)")
  public Object doLogging(final ProceedingJoinPoint pjp) throws Throwable {
    final Method method = ((MethodSignature) pjp.getSignature()).getMethod();
    final AnnotatedElementKey annotatedElementKey =
        new AnnotatedElementKey(method, pjp.getTarget().getClass());

    final DebugLogOperation debugLogOperation = this.parseDebugLogAnnotation(pjp);
    final int counter =
        this.counters.compute(annotatedElementKey, (k, v) -> (v == null) ? 1 : v + 1);

    log.info(
        "Started {} - {} ({})",
        debugLogOperation.getClassName(),
        debugLogOperation.getMethodName(),
        counter);
    log.info(debugLogOperation.getDescription());

    final Object returnValue;
    if (debugLogOperation.isEnabled()) {
      final EvaluationContext methodArgsEvalContext =
          new MethodBasedEvaluationContext(
              pjp.getTarget(), method, pjp.getArgs(), this.parameterNameDiscoverer);
      final Pair<Map<String, Object>, Map<String, Dataset>> evaluatedArgs =
          this.evaluate(methodArgsEvalContext, debugLogOperation.getArgs());
      this.logValues(evaluatedArgs.getLeft());
      this.logDataset(debugLogOperation, evaluatedArgs.getRight(), counter);

      returnValue = pjp.proceed();
      if (Objects.nonNull(returnValue)) {
        final StandardEvaluationContext resultEvaluationContext = new StandardEvaluationContext();
        resultEvaluationContext.setVariable(RESULT_VARIABLE, returnValue);
        final Pair<Map<String, Object>, Map<String, Dataset>> evaluatedResults =
            this.evaluate(resultEvaluationContext, debugLogOperation.getResults());

        this.logValues(evaluatedResults.getLeft());
        this.logDataset(debugLogOperation, evaluatedResults.getRight(), counter);
      }
    } else {
      returnValue = pjp.proceed();
    }

    log.info(
        "Completed {} - {} ({})",
        debugLogOperation.getClassName(),
        debugLogOperation.getMethodName(),
        counter);
    return returnValue;
  }

  private Pair<Map<String, Object>, Map<String, Dataset>> evaluate(
      final EvaluationContext evaluationContext, final Map<String, String> params) {
    final Map<String, Object> args = new LinkedHashMap<>();
    final Map<String, Dataset> datasets = new LinkedHashMap<>();

    for (final Map.Entry<String, String> entry : params.entrySet()) {
      final Object argValue;
      if (entry.getValue().startsWith(StandardBeanExpressionResolver.DEFAULT_EXPRESSION_PREFIX)) {
        try {
          argValue =
              this.beanExpressionResolver.evaluate(entry.getValue(), this.beanExpressionContext);
        } catch (final Exception e) {
          log.error("Exception while evaluating: " + entry.getValue(), e);
          continue;
        }
      } else {
        try {
          argValue =
              this.expressionParser.parseExpression(entry.getValue()).getValue(evaluationContext);
        } catch (final Exception e) {
          log.error("Exception while evaluating: " + entry.getValue(), e);
          continue;
        }
      }
      if (argValue instanceof Dataset dataset) {
        datasets.put(entry.getKey(), dataset);
      } else {
        args.put(entry.getKey(), argValue);
      }
    }

    return Pair.of(args, datasets);
  }

  private void logValues(final Map<String, Object> args) {
    args.forEach((key, value) -> log.info("{} = {}", key, value));
  }

  private void logDataset(
      final DebugLogOperation debugLogOperation,
      final Map<String, Dataset> datasets,
      final int counter) {
    for (final Map.Entry<String, Dataset> entry : datasets.entrySet()) {
      if (Objects.nonNull(entry.getValue())) {
        if (debugLogOperation.isPrintSchema()) {
          log.info("----------- Dataset Schema: {} -----------", entry.getKey());
          try {
            final String schema = entry.getValue().schema().treeString();
            log.info("\n" + schema);
          } catch (final Exception e) {
            log.error("Exception while logging dataset schema for: " + entry.getKey(), e);
          }
          log.info("..................................................");
        }
        if (debugLogOperation.isLogDataset()) {
          log.info("----------- Dataset: {} -----------", entry.getKey());
          try {
            entry.getValue().show(debugLogOperation.getNumRows(), false);
            log.info("..................................................");
          } catch (final Exception e) {
            log.error("Exception while logging dataset for: " + entry.getKey(), e);
          }
        }
        if (debugLogOperation.isDumpDataset()) {
          final String outputLocation =
              this.properties.getDataset().getDump().getLocation()
                  + (this.properties.getDataset().getDump().getLocation().endsWith(SLASH)
                      ? BLANK
                      : SLASH)
                  + debugLogOperation.getClassName()
                  + SLASH
                  + debugLogOperation.getMethodName()
                  + SLASH
                  + "execution_"
                  + counter
                  + SLASH
                  + entry.getKey();
          try {
            entry
                .getValue()
                .limit(debugLogOperation.getNumRows())
                .coalesce(1)
                .write()
                .mode(SaveMode.Overwrite)
                .option(SparkOptions.Common.HEADER, true) // Include header
                .option("nullValue", "null")
                .csv(outputLocation);
          } catch (final Exception e) {
            log.error("Exception while writing dataset to location: " + outputLocation, e);
          }
        }
      } else {
        log.info("----------- Dataset: {} is null -----------", entry.getKey());
      }
    }
  }

  private DebugLogOperation parseDebugLogAnnotation(final ProceedingJoinPoint pjp) {
    final Method method = ((MethodSignature) pjp.getSignature()).getMethod();
    final AnnotatedElementKey annotatedElementKey =
        new AnnotatedElementKey(method, pjp.getTarget().getClass());
    if (this.logOperations.containsKey(annotatedElementKey)) {
      return this.logOperations.get(annotatedElementKey);
    } else {
      final DebugLog debugLog = method.getAnnotation(DebugLog.class);
      final Map<String, String> args =
          Arrays.stream(debugLog.values())
              .filter(logVar -> !logVar.value().startsWith(RESULT_EXPRESSION_PREFIX))
              .collect(Collectors.toMap(LogVar::name, LogVar::value));
      final Map<String, String> results =
          Arrays.stream(debugLog.values())
              .filter(logVar -> logVar.value().startsWith(RESULT_EXPRESSION_PREFIX))
              .collect(Collectors.toMap(LogVar::name, LogVar::value));

      final String className = pjp.getTarget().getClass().getSimpleName();
      final String methodName = pjp.getSignature().getName();
      final LogDataset logDataset = debugLog.dataset();
      final DebugLogOperation debugLogOperation =
          DebugLogOperation.builder()
              .enabled(this.properties.isEnabled() && debugLog.enabled())
              .className(className)
              .methodName(methodName)
              .description(debugLog.description())
              .logDataset(this.properties.getDataset().isShow() && logDataset.show())
              .numRows(
                  logDataset.numRows() > 0
                      ? logDataset.numRows()
                      : this.properties.getDataset().getNumRows())
              .printSchema(this.properties.getDataset().isPrintSchema() && logDataset.printSchema())
              .dumpDataset(this.properties.getDataset().getDump().isEnabled() && logDataset.dump())
              .args(args)
              .results(results)
              .build();
      this.logOperations.put(annotatedElementKey, debugLogOperation);
      return debugLogOperation;
    }
  }
}
