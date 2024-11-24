package com.ksoot.spark.common.config.properties;

import com.ksoot.spark.common.util.SparkOptions;
import jakarta.validation.constraints.NotEmpty;
import java.util.Properties;
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
public class JdbcOptions {

  @NotEmpty private String url;

  @NotEmpty private String driver = "org.postgresql.Driver";

  @NotEmpty private String database;

  @NotEmpty private String username;

  @NotEmpty private String password;

  public String getUrl() {
    return this.url + "/" + this.database;
  }

  public Properties options() {
    Properties properties = new Properties(3);
    properties.put(SparkOptions.Jdbc.DRIVER, this.driver);
    properties.put(SparkOptions.Jdbc.USER, this.username);
    properties.put(SparkOptions.Jdbc.PASSWORD, this.password);
    return properties;
  }
}
