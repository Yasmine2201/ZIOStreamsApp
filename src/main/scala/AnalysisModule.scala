import zio.Chunk
import java.time.LocalDate

final case class AnalysisModule(data: ChunkedData) {
  val hourlyCarbonIntensity: Chunk[HourlyCarbonIntensity]                                         = data.carbonIntensity
  val hourlyElectricityProductionAndConsumption: Chunk[HourlyElectricityProductionAndConsumption] = data.ecoMix
  val monthlyElectricityConsumption: Chunk[MonthlyElectricityConsumption]                         = data.rawConso
  val dailyPowerPeakWithTemperature: Chunk[DailyPowerPeakWithTemperature]                         = data.peakConso

  def getDailyPowerPeakWithTemperature(date: LocalDate): Option[DailyPowerPeakWithTemperature] = {
    dailyPowerPeakWithTemperature.find(_.date == date)
  }

  val powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]] = {
    val powerPeakTemperatureList: List[DailyPowerPeakWithTemperature] = dailyPowerPeakWithTemperature.toList
    powerPeakTemperatureList.groupBy(pp => pp.date.getYear)
  }

  def printpowerPeakTemperatureGroupedByYear = this.powerPeakTemperatureGroupedByYear.foreach { case (year, data) =>
    println(s"Year $year:")
    data.foreach(pp => println(s"  Date: ${pp.date}, Power: ${pp.powerPeak}, Mean Temperature: ${pp.meanTemperature}, Reference Temperature: ${pp.referenceTemperature}"))
    println()
  }

  def carbonIntensityGroupedByDay: Map[LocalDate, List[HourlyCarbonIntensity]] = {
    val carbonIntensityList: List[HourlyCarbonIntensity] = hourlyCarbonIntensity.toList
    val groupedByDay: Map[LocalDate, List[HourlyCarbonIntensity]] = carbonIntensityList.groupBy { ci =>
      ci.dateTime.toLocalDate
    }
    groupedByDay
  }

  def printCarbonIntensityGroupedByDay: Unit = {
    carbonIntensityGroupedByDay.foreach { case (date, intensityList) =>
      println(s"Date: $date")
      intensityList.foreach { intensity =>
        println(s"  Time: ${intensity.dateTime.toLocalTime}, Direct Intensity: ${intensity.directIntensity}, LCA Intensity: ${intensity.lcaIntensity}")

      }
      println()
    }
  }
}
