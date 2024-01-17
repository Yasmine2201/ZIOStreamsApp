import zio.Chunk
import AnalysisModule.*
import Types.*
import java.time.LocalDate

extension [A](chunk: Chunk[A]) {
  def averageBy[B](f: A => B)(using n: Fractional[B]): B =
    n.div(chunk.map(f).sum, n.fromInt(chunk.size))
}

final case class GlobalStatisticsAnalysis(analysisModule: AnalysisModule) {

  // DailyPowerPeakWithTemperature Analysis
  def maxPowerPeakByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]]): Map[Int, (Power.MW, LocalDate)] = {
    powerPeakTemperatureGroupedByYear.map { case (year, dataList) =>
      val maxPowerEntry: (Power.MW, LocalDate) = dataList.map(line => (line.powerPeak, line.date)).maxBy(_._1)
      (year, maxPowerEntry)
    }
  }
  def minTempeartureByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]]): Map[Int, (Temperature.Celsius, LocalDate)] = {
    powerPeakTemperatureGroupedByYear.map { case (year, dataList) =>
      val maxTempEntry: (Temperature.Celsius, LocalDate) = dataList.map(line => (line.meanTemperature, line.date)).minBy(_._1)
      (year, maxTempEntry)
    }
  }
  // Prints DailyPowerPeakWithTemperature Analysis
  def printMaxPowerPeakByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]]): Unit = maxPowerPeakByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]]).foreach { case (year, (power, day)) =>
    println(s"Year $year: max Power Peak was: $power MW on: $day")
  }

  def printMinTempeartureByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]]): Unit = minTempeartureByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]]).foreach { case (year, (temperature, day)) =>
    println(s"Year $year: Tempearture Min was: $temperature°C on: $day")
  }
}
