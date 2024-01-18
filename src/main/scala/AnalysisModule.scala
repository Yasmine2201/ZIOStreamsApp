import zio.Chunk
import java.time.LocalDate

/** Acts as a data container for all the data we need to perform our analysis
  * @param data
  *   The data coming from the dataloader
  */
final case class AnalysisModule(data: ChunkedData) {

  // Global Analysis Data (parsed from the dataloader)

  val hourlyCarbonIntensity: Chunk[HourlyCarbonIntensity]                                         = data.carbonIntensity
  val hourlyElectricityProductionAndConsumption: Chunk[HourlyElectricityProductionAndConsumption] = data.ecoMix
  val monthlyElectricityConsumption: Chunk[MonthlyElectricityConsumption]                         = data.rawConso
  val dailyPowerPeakWithTemperature: Chunk[DailyPowerPeakWithTemperature]                         = data.peakConso

  // Declare Analysis Modules

  val globalStatisticsAnalysis = GlobalStatisticsAnalysis(this)
  val powerTemperatureAnalysis = PowerTemperatureAnalysis(this)
  val carbonIntensityAnalysis  = CarbonIntensityAnalysis(this)

  val powerPeakTemperatureGroupedByYear: Map[Int, List[DailyPowerPeakWithTemperature]] = {
    val powerPeakTemperatureList: List[DailyPowerPeakWithTemperature] = dailyPowerPeakWithTemperature.toList
    powerPeakTemperatureList.groupBy(pp => pp.date.getYear)
  }

  def printPowerPeakTemperatureGroupedByYear = powerPeakTemperatureGroupedByYear.foreach { case (year, data) =>
    println(s"Year $year:")
    data.foreach(pp => println(s"  Date: ${pp.date}, Power: ${pp.powerPeak}, Mean Temperature: ${pp.meanTemperature}, Reference Temperature: ${pp.referenceTemperature}"))
    println()
  }

  def getDailyPowerPeakWithTemperature(date: LocalDate): Option[DailyPowerPeakWithTemperature] = {
    dailyPowerPeakWithTemperature.find(_.date == date)
  }
}
