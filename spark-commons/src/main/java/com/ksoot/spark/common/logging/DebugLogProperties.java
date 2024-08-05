package com.ksoot.spark.common.logging;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Validated
public class DebugLogProperties {

  private boolean enabled = false;

  @NotNull private Dataset dataset = new Dataset();

  @Getter
  @Setter
  @NoArgsConstructor
  @ToString
  @Valid
  public static class Dataset {

    private boolean show = false;

    private boolean printSchema = false;

    @NotNull private Dump dump = new Dump();

    @Min(1)
    private int numRows = 8;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @Valid
    public static class Dump {

      private boolean enabled = false;

      @NotEmpty private String location = "spark-output/dump";
    }
  }
}
