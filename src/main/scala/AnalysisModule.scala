import zio.Chunk
import PowerValues.*
import java.time.LocalDate
import TemperatureValues.* 

final case class AnalysisModule(data: ChunkedData) {
  def getCarbonIntensityPerHour: Chunk[CarbonIntensityPerHour]                           = data.carbonIntensity
  def getElectricityProductionAndConsumption: Chunk[ElectricityProductionAndConsumption] = data.ecoMix
  def getElectricityConsumptionPerMonth: Chunk[ElectricityConsumptionPerMonth]           = data.rawConso
  def getPowerPeakWithTemperature: Chunk[PowerPeakWithTemperature]                       = data.peakConso

  //analysis PowerPeakWithTemperature
  
private val groupedByYear: Map[Int, List[PowerPeakWithTemperature]] = {
  val list: List[PowerPeakWithTemperature] = data.peakConso.toList
  list.groupBy(pp => pp.dateTime.getYear)
}

def PeakOfConsumPerYear: Map[Int, (Power, LocalDate)] = {
  groupedByYear.map { case (year, dataList) =>
    val maxPowerEntry: (Power, LocalDate) = dataList.map(pp => (pp.power, pp.dateTime)).maxBy(_._1)
    (year, maxPowerEntry)
  }
}
def MinOfTempearturePerYear : Map[Int, (Temperature, LocalDate)] ={
   groupedByYear.map { case (year, dataList) =>
    val maxTempEntry: (Temperature, LocalDate) = dataList.map(pp => (pp.meanTemperature, pp.dateTime)).minBy(_._1)
    (year, maxTempEntry)
  }
}
def printPeakOfConsumptionPerYear: Unit = PeakOfConsumPerYear.foreach {
  case (year, (power, day)) =>
    println(s"Year $year: Consumption Peak was: $power on: $day")
}

def printMinOfTemperaturePerYear: Unit = MinOfTempearturePerYear.foreach {
  case (year, (temperature, day)) =>
    println(s"Year $year: Tempearture Min was: $temperature on: $day")
}

def printGroupByYear = this.groupedByYear.foreach { case (year, data) =>
  println(s"Year $year:")
  data.foreach(pp =>
    println(s"  Date: ${pp.dateTime}, Power: ${pp.power}, Mean Temperature: ${pp.meanTemperature}, Reference Temperature: ${pp.referenceTemperature}")
  )
  println() 
}
}
