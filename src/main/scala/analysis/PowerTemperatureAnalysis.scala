import Types.*
import java.time.LocalDate
import zio.Chunk
import ChunkMath._

/** Contains the functions to analyse the power and temperature data.
  */
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
}
