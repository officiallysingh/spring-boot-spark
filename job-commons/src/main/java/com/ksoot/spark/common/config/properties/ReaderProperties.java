package com.ksoot.spark.common.config.properties;

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
public class ReaderProperties extends ConnectorProperties {

    private ReadFileOptions fileOptions = new ReadFileOptions();

}
