package com.ksoot.spark.sales.conf;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.YearMonth;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Validated
@ConfigurationProperties(prefix = "ksoot.job")
public class JobProperties {

  /** Statement month * */
  @NotNull @PastOrPresent private YearMonth month = YearMonth.now();

  /** Unique correlation id for each Job execution. */
  @NotEmpty
  @Max(50)
  private String correlationId;
}
