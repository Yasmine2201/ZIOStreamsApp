import zio.Chunk
import AnalysisModule.*
import PowerValues.*
import java.time.LocalDate
import TemperatureValues.*
import CarbonIntensities.*

final case class GlobalStatisticsAnalysis(analysisModule: AnalysisModule) {
  
  // PowerPeakWithTemperature Analysis
  def maxPowerPeakByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[PowerPeakWithTemperature]]): Map[Int, (Power, LocalDate)] = {
    powerPeakTemperatureGroupedByYear.map { case (year, dataList) =>
      val maxPowerEntry: (Power, LocalDate) = dataList.map(pp => (pp.power, pp.dateTime)).maxBy(_._1)
      (year, maxPowerEntry)
    }
  }
  def minTempeartureByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[PowerPeakWithTemperature]]): Map[Int, (Temperature, LocalDate)] = {
    powerPeakTemperatureGroupedByYear.map { case (year, dataList) =>
      val maxTempEntry: (Temperature, LocalDate) = dataList.map(pp => (pp.meanTemperature, pp.dateTime)).minBy(_._1)
      (year, maxTempEntry)
    }
  }
   // Prints PowerPeakWithTemperature Analysis
  def printMaxPowerPeakByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[PowerPeakWithTemperature]]): Unit = maxPowerPeakByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[PowerPeakWithTemperature]]).foreach { case (year, (power, day)) =>
    println(s"Year $year: max Power Peak was: $power MW on: $day")
  }

  def printMinTempeartureByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[PowerPeakWithTemperature]]): Unit = minTempeartureByYear(powerPeakTemperatureGroupedByYear: Map[Int, List[PowerPeakWithTemperature]]).foreach { case (year, (temperature, day)) =>
    println(s"Year $year: Tempearture Min was: $temperature°C on: $day")
  }
  // CarbonIntensity Analysis
 
 
  // CarbonIntensity Prints
}
   


