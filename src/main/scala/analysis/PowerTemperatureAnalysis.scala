import Types.*
import java.time.LocalDate
import zio.Chunk

final case class PowerTemperatureAnalysis(analysisModule: AnalysisModule) {

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

  def maxPowerPeakAndMinTemperatureByYear(yearData: (Int, List[DailyPowerPeakWithTemperature])): (Int, DailyPowerPeakWithTemperature, DailyPowerPeakWithTemperature) = {
    val (year, dataList) = yearData

    val maxPowerEntry       = dataList.maxBy(_.powerPeak)
    val minTemperatureEntry = dataList.minBy(_.meanTemperature)

    (year, maxPowerEntry, minTemperatureEntry)
  }

  // Prints DailyPowerPeakWithTemperature Analysis
  def printMaxPowerPeakByYear: Unit =
    maxPowerPeakByYear(analysisModule.powerPeakTemperatureGroupedByYear).foreach { case (year, (power, day)) =>
      println(s"Year $year: max Power Peak was: $power MW on: $day")
    }

  def printMinTempeartureByYear: Unit =
    minTempeartureByYear(analysisModule.powerPeakTemperatureGroupedByYear).foreach { case (year, (temperature, day)) =>
      println(s"Year $year: Tempearture Min was: $temperature°C on: $day")
    }

  def printMaxPowerPeakAndMinTemperatureByYear: Unit = {
    analysisModule.powerPeakTemperatureGroupedByYear.foreach { yearData =>
      val (year, maxPowerEntry, minTemperatureEntry) = maxPowerPeakAndMinTemperatureByYear(yearData)
      println(s"Year $year: Max Power Peak was on: ${maxPowerEntry.date}, Min Temperature was on: ${minTemperatureEntry.date}")
    }
  }
}
