import zio.Chunk
import java.time.LocalDate
 
final case class AnalysisModule(data: ChunkedData) {
  def getCarbonIntensityPerHour: Chunk[CarbonIntensityPerHour]                           = data.carbonIntensity
  def getElectricityProductionAndConsumption: Chunk[ElectricityProductionAndConsumption] = data.ecoMix
  def getElectricityConsumptionPerMonth: Chunk[ElectricityConsumptionPerMonth]           = data.rawConso
  def getPowerPeakWithTemperature: Chunk[PowerPeakWithTemperature]                       = data.peakConso
  
  val powerPeakTemperatureGroupedByYear: Map[Int, List[PowerPeakWithTemperature]] = {
    val powerPeakTemperatureList: List[PowerPeakWithTemperature] = getPowerPeakWithTemperature.toList
    powerPeakTemperatureList.groupBy(pp => pp.dateTime.getYear)
  }
  def printpowerPeakTemperatureGroupedByYear = this.powerPeakTemperatureGroupedByYear.foreach { case (year, data) =>
    println(s"Year $year:")
    data.foreach(pp => println(s"  Date: ${pp.dateTime}, Power: ${pp.power}, Mean Temperature: ${pp.meanTemperature}, Reference Temperature: ${pp.referenceTemperature}"))
    println()
}
def carbonIntensityGroupedByDay: Map[LocalDate, List[CarbonIntensityPerHour]] = {
  val carbonIntensityList: List[CarbonIntensityPerHour] = getCarbonIntensityPerHour.toList
   val groupedByDay: Map[LocalDate, List[CarbonIntensityPerHour]] = carbonIntensityList.groupBy { ci =>
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