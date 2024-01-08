import zio.Chunk

final case class AnalysisModule(data: ChunkedData) {
  def getCarbonIntensityPerHour: Chunk[CarbonIntensityPerHour]                           = data.carbonIntensity
  def getElectricityProductionAndConsumption: Chunk[ElectricityProductionAndConsumption] = data.ecoMix
  def getElectricityConsumptionPerMonth: Chunk[ElectricityConsumptionPerMonth]           = data.rawConso
  def getPowerPeakWithTemperature: Chunk[PowerPeakWithTemperature]                       = data.peakConso
}
