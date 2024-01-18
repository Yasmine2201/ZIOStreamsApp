import Types.*
import java.time.LocalDate
import zio.Chunk

object PowerTemperatureAnalysis {

  def maxPowerPeakByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]]): Map[Int, (Power.MW, LocalDate)] = {
    powerPeakTemperatureGroupedByYear.map { case (year, dataList) =>
      val maxPowerEntry: (Power.MW, LocalDate) = dataList.map(line => (line.powerPeak, line.date)).maxBy(_._1)
      (year, maxPowerEntry)
    }
  }

  def minTemperatureByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]]): Map[Int, (Temperature.Celsius, LocalDate)] = {
    powerPeakTemperatureGroupedByYear.map { case (year, dataList) =>
      val maxTempEntry: (Temperature.Celsius, LocalDate) = dataList.map(line => (line.meanTemperature, line.date)).minBy(_._1)
      (year, maxTempEntry)
    }
  }

  def powerPeakTemperatureGroupedByYear(data: LoadedData): Map[Int, List[DailyPowerPeakWithTemperature]] = {
    data.dailyPowerPeakWithTemperature.toList.groupBy(pp => pp.date.getYear)
  }

  def maxPowerPeakAndMinTemperature(yearData: List[DailyPowerPeakWithTemperature]): (DailyPowerPeakWithTemperature, DailyPowerPeakWithTemperature) =
    (yearData.maxBy(_.powerPeak), yearData.minBy(_.meanTemperature))

  // Prints DailyPowerPeakWithTemperature Analysis
  def printMaxPowerPeakByYear(data: LoadedData): Unit =
    maxPowerPeakByYear(powerPeakTemperatureGroupedByYear(data)).foreach { case (year, (power, day)) =>
      println(s"Year $year: max Power Peak was: $power MW on: $day")
    }

  def printMinTempeartureByYear(data: LoadedData): Unit =
    minTemperatureByYear(powerPeakTemperatureGroupedByYear(data)).foreach { case (year, (temperature, day)) =>
      println(s"Year $year: Tempearture Min was: $temperature°C on: $day")
    }

  // def printMaxPowerPeakAndMinTemperatureByYear(data: LoadedData): Unit = {
  //   powerPeakTemperatureGroupedByYear(data).foreach { yearData =>
  //     val (year, maxPowerEntry, minTemperatureEntry) = maxPowerPeakAndMinTemperatureByYear(yearData)
  //     println(s"Year $year: Max Power Peak was on: ${maxPowerEntry.date}, Min Temperature was on: ${minTemperatureEntry.date}")
  //   }
  // }
}
