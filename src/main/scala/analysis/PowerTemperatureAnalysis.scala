import Types.*
import java.time.LocalDate
import zio.Chunk
import ChunkMath._

object PowerTemperatureAnalysis {

  /** Groups the given daily power peak with temperature data by year.
    *
    * @param data
    *   The loaded data containing daily power peak with temperature information.
    * @return
    *   A Map where the key is the year and the value is a List of daily power peak with temperature entries for that year.
    */
  def powerPeakTemperatureGroupedByYear(data: LoadedData): Map[Int, Chunk[DailyPowerPeakWithTemperature]] = {
    data.dailyPowerPeakWithTemperature.groupBy(pp => pp.date.getYear)
  }

  /** Finds the maximum power peak and minimum temperature entries in the given list of daily power peak with temperature data for a specific year.
    *
    * @param yearData
    *   The list of daily power peak with temperature entries for a specific year.
    * @return
    *   A tuple containing the maximum power peak entry and the minimum temperature entry for the specified year.
    */
  def maxPowerPeakAndMinTemperatureOfYear(yearData: Chunk[DailyPowerPeakWithTemperature]): (DailyPowerPeakWithTemperature, DailyPowerPeakWithTemperature) =
    (yearData.maxBy(_.powerPeak), yearData.minBy(_.meanTemperature))

  /** Retrieves the information about maximum power peak and minimum temperature entries for each year.
    *
    * @param data
    *   The loaded data containing daily power peak with temperature information.
    * @return
    *   A chunk of tuples containing the year, the maximum power peak entry for that year and the minimum temperature entry for that year.
    */
  def maxPowerPeaksAndMinTemperaturesByYear(data: LoadedData): Chunk[(Int, DailyPowerPeakWithTemperature, DailyPowerPeakWithTemperature)] =
    Chunk
      .fromIterable(
        powerPeakTemperatureGroupedByYear(data)
          .map((year, yearData) => {
            val (maxPowerPeak, minTemperature) = maxPowerPeakAndMinTemperatureOfYear(yearData)
            (year, maxPowerPeak, minTemperature)
          })
      )
      .sortBy(_._1)

  /** Calculates the Pearson correlation coefficient between the given power peaks and temperatures.
    *
    * @param data
    *   The loaded chunk of data containing daily power peaks with temperature information.
    * @return
    *   The Pearson correlation coefficient between the given power peaks and temperatures.
    */
  def temperatureAndPowerPeakPearsonCorrelation(data: LoadedData): Double =
    val temperatures: Chunk[Temperature.Celsius] = data.dailyPowerPeakWithTemperature.map(line => line.meanTemperature)
    val powerPeaks: Chunk[Power.MW]              = data.dailyPowerPeakWithTemperature.map(line => line.powerPeak)
    linearCorrelationCoefficient(powerPeaks, temperatures)

    /** Retrieves the information about maximum power peak and minimum temperature entries for each year and formats it into a newline-separated string.
      *
      * @param data
      *   The loaded data containing daily power peak with temperature information.
      * @return
      *   A formatted string containing information about maximum power peak and minimum temperature entries for each year, with each entry on a new line.
      */
  def formatPowerPeakAndTemperature(yearsData: Chunk[(Int, DailyPowerPeakWithTemperature, DailyPowerPeakWithTemperature)], correlationCoef: Double): String = {
    val title = "\n+-------------------------------------------------------------------------+\n" +
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
        "+-------------------------------------------------------------------------+"

    title + yearsSummaries + pearsonCoefFormatted + conclusion
  }
}
