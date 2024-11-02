package com.ksoot.spark.common.conf;

import static org.apache.spark.sql.functions.callUDF;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.functions;

public class UserDefinedFunctions {

  public static final String EXPLODE_DATE_SEQ = "explodeDateSeq";

  public static final String VECTOR_TO_ARRAY = "vectorToArray";
  public static final String FIRST_VECTOR_ELEMENT = "firstVectorElement";

  static UDF2<Timestamp, Timestamp, List<Timestamp>> explodeDateSeq =
      (start, end) -> {
        LocalDate startDate = toLocalDate(start);
        LocalDate endDate = toLocalDate(end);
        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return Stream.iterate(startDate, date -> date.plusDays(1))
            .map(UserDefinedFunctions::toTimestamp)
            .limit(numOfDaysBetween)
            .toList();
      };

  public static Column explodeDateSeq(final Column startCol, final Column endCol) {
    return functions.explode(callUDF(UserDefinedFunctions.EXPLODE_DATE_SEQ, startCol, endCol));
  }

  public static LocalDate toLocalDate(final Timestamp timestamp) {
    return Objects.nonNull(timestamp)
        ? timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        : null;
  }

  public static Timestamp toTimestamp(final LocalDate localDate) {
    return Objects.nonNull(localDate) ? Timestamp.valueOf(localDate.atStartOfDay()) : null;
  }

  static UDF1<Vector, Double> firstVectorElement =
      (final Vector vector) -> {
        double firstValue = vector.toArray()[0];
        return Double.valueOf(firstValue);
      };

  public static Column getFirstVectorElement(final Column vector) {
    return callUDF(UserDefinedFunctions.FIRST_VECTOR_ELEMENT, vector);
  }
}
