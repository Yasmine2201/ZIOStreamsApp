import Types.*
import java.time.LocalDate
import zio.Chunk

final case class PowerTemperatureAnalysis(dailyPowerPeakWithTemperature: Chunk[DailyPowerPeakWithTemperature]) {

  def maxPowerPeakAndMinTemperatureByYear(yearData: (Int, List[DailyPowerPeakWithTemperature])): (Int, DailyPowerPeakWithTemperature, DailyPowerPeakWithTemperature) = {
    val (year, dataList) = yearData

    val maxPowerEntry       = dataList.maxBy(_.powerPeak)
    val minTemperatureEntry = dataList.minBy(_.meanTemperature)

    (year, maxPowerEntry, minTemperatureEntry)
  }

  def printMaxPowerPeakAndMinTemperatureByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]]): Unit = {
    powerPeakTemperatureGroupedByYear.foreach { yearData =>
      val (year, maxPowerEntry, minTemperatureEntry) = maxPowerPeakAndMinTemperatureByYear(yearData)
      println(s"Year $year: Max Power Peak was on: ${maxPowerEntry.date}, Min Temperature was on: ${minTemperatureEntry.date}")
    }
  }
}
