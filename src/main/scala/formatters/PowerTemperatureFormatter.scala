import PowerTemperatureAnalysis._
import java.time.LocalDate
import zio.Chunk

/** Contains the functions to format the results of the power and temperature analysis.
  */
object PowerTemperatureFormatter {

  /** Retrieves the information about maximum power peak and minimum temperature entries for each year and formats it into a newline-separated string.
    *
    * @param data
    *   The loaded data containing daily power peak with temperature information.
    * @return
    *   A formatted string containing information about maximum power peak and minimum temperature entries for each year, with each entry on a new line.
    */
  def formatPowerPeakAndTemperature(yearsData: Chunk[(Int, DailyPowerPeakWithTemperature, DailyPowerPeakWithTemperature)], correlationCoef: Double): String = {
    val title = "+-------------------------------------------------------------------------+\n" +
      "|                Case Study : Power and Temperature Summary               |\n" +
      "+-------------------------------------------------------------------------+\n" +
      "| We want to know the maximum power peak and the minimum temperature for  |\n" +
      "| each year.                                                              |\n" +
      "+-------------------------------------------------------------------------+\n"

    val yearsSummaries = yearsData
      .map((year, maxPowerEntry, minTemperatureEntry) =>
        f"|                              Year $year                                  |\n" +
          "+-------------------------------------------------------------------------+\n" +
          f"| Max Power Peak:  ${maxPowerEntry.powerPeak}%10.2f MW on ${maxPowerEntry.date}                            |\n" +
          f"| Min Temperature: ${minTemperatureEntry.meanTemperature}%10.2f C on ${minTemperatureEntry.date}                             |\n" +
          "+-------------------------------------------------------------------------+\n".stripMargin.trim
      )
      .mkString("\n")

    val pearsonCoefFormatted =
      f"\n| Temperature And PowerPeak Pearson Correlation: $correlationCoef%5.2f                    |\n" +
        "+-------------------------------------------------------------------------+\n"

    val conclusion =
      "|                            Final Conclusion                             |\n" +
        "+-------------------------------------------------------------------------+\n" +
        "| - The correlation coefficient indicates a strong enough correlation.    |\n" +
        "| - As daily temperatures decrease, daily power peak tends to increase.   |\n" +
        "| - People tend to use more electricity for heating purposes when         |\n" +
        "|   temperatures fall                                                     |\n" +
        "+-------------------------------------------------------------------------+\n"

    title + yearsSummaries + pearsonCoefFormatted + conclusion
  }
}
