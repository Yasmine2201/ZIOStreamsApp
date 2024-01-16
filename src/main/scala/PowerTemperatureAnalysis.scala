import PowerValues.*
import java.time.LocalDate
import TemperatureValues.*
import zio.Chunk

final case class PowerTemperatureAnalysis(powerPeakWithTemperature: Chunk[PowerPeakWithTemperature]) {

  def maxPowerPeakAndMinTemperatureByYear(yearData: (Int, List[PowerPeakWithTemperature])): (Int, PowerValues.Power, LocalDate, TemperatureValues.Temperature, LocalDate) = {
    val (year, dataList) = yearData

    val maxPowerEntry       = dataList.maxBy(_.power)
    val minTemperatureEntry = dataList.minBy(_.meanTemperature)

    (year, maxPowerEntry.power, maxPowerEntry.dateTime, minTemperatureEntry.meanTemperature, minTemperatureEntry.dateTime)
  }

  def printMaxPowerPeakAndMinTemperatureByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[PowerPeakWithTemperature]]): Unit = powerPeakTemperatureGroupedByYear.foreach { yearData =>
    val (year, maxPower, maxPowerDay, minTemperature, minTemperatureDay) = maxPowerPeakAndMinTemperatureByYear(yearData)
    println(s"Year $year: Max Power Peak was on: $maxPowerDay, Min Temperature was on: $minTemperatureDay")
  }

}
