import java.time.LocalDate
import Types._
import zio.Chunk
import ChunkMath._

object DailyStatisticsAnalysis {

  final case class DailyCarbonIntensityStatistics(
      date: LocalDate,
      minDirectIntensity: CarbonIntensity.gPerkWh,
      avgDirectIntensity: CarbonIntensity.gPerkWh,
      maxDirectIntensity: CarbonIntensity.gPerkWh,
      lowCarbonPercent: Percentage,
      renewablePercent: Percentage
  )

  final case class DailyStatistics(
      date: LocalDate,
      minDirectIntensity: Option[CarbonIntensity.gPerkWh],
      avgDirectIntensity: Option[CarbonIntensity.gPerkWh],
      maxDirectIntensity: Option[CarbonIntensity.gPerkWh],
      lowCarbonPercent: Option[Percentage],
      renewablePercent: Option[Percentage],
      avgProduction: Option[Chunk[ElectricityProductionPerSupplyChain]],
      powerPeak: Option[Power.MW],
      meanTemperature: Option[Temperature.Celsius]
  )

  /** Get the carbon intensity statistics of a given day
    *
    * @param data
    *   The data to analyse
    * @param date
    *   The date of the day
    * @return
    *   The carbon intensity statistics of the day
    */
  def carbonIntensityStatsOfDay(data: LoadedData, date: LocalDate): Option[DailyCarbonIntensityStatistics] = {
    val hourlyCarbonIntensityOfDay: Chunk[HourlyCarbonIntensity] = data.hourlyCarbonIntensity.filter(_.dateTime.toLocalDate == date)

    if hourlyCarbonIntensityOfDay.isEmpty
    then None
    else {

      val minDirectIntensity: CarbonIntensity.gPerkWh = hourlyCarbonIntensityOfDay.map(_.directIntensity).min
      val avgDirectIntensity: CarbonIntensity.gPerkWh = hourlyCarbonIntensityOfDay.averageBy(_.directIntensity)
      val maxDirectIntensity: CarbonIntensity.gPerkWh = hourlyCarbonIntensityOfDay.map(_.directIntensity).max

      val lowCarbonPercent: Percentage = hourlyCarbonIntensityOfDay.averageBy(_.lowCarbonPercent)
      val renewablePercent: Percentage = hourlyCarbonIntensityOfDay.averageBy(_.renewablePercent)

      Some(
        DailyCarbonIntensityStatistics(
          date,
          minDirectIntensity,
          avgDirectIntensity,
          maxDirectIntensity,
          lowCarbonPercent,
          renewablePercent
        )
      )
    }
  }

  /** Get the production statistics of a given day
    *
    * @param data
    *   The data to analyse
    * @param date
    *   The date of the day
    * @return
    *   The production statistics of the day
    */
  def productionStatsOfDay(data: LoadedData, date: LocalDate): Option[Chunk[ElectricityProductionPerSupplyChain]] = {

    val hourlyProductionOfDay: Chunk[HourlyElectricityProductionAndConsumption] =
      data.hourlyElectricityProductionAndConsumption.filter(_.dateTime.toLocalDate == date)

    if hourlyProductionOfDay.isEmpty
    then None
    else {

      val productionGroupedBySupplyChain: Map[SupplyChain, Seq[ElectricityProductionPerSupplyChain]] =
        hourlyProductionOfDay.map(_.production).toSeq.flatten.groupBy(_.supplyChain)

      val avgProductionPerSupplyChain: Seq[ElectricityProductionPerSupplyChain] =
        productionGroupedBySupplyChain.map { case (supplyChain, productionPerSupplyChain) =>
          val avgProduction: Power.MW = Chunk.fromIterable(productionPerSupplyChain).averageBy(_.production)
          ElectricityProductionPerSupplyChain(supplyChain, avgProduction)
        }.toSeq

      Some(Chunk.fromIterable(avgProductionPerSupplyChain))
    }
  }

  /** Get the power peak and temperature statistics of a given day
    *
    * @param data
    *   The data to analyse
    * @param date
    *   The date of the day
    * @return
    *   The power peak and temperature statistics of the day
    */
  def powerPeakAndTemperatureStatsOfDay(data: LoadedData, date: LocalDate): Option[DailyPowerPeakWithTemperature] =
    data.dailyPowerPeakWithTemperature.find(_.date == date)

    /** Get the statistics of a given day
      *
      * @param data
      *   The data to analyse
      * @param date
      *   The date of the day
      * @return
      *   The statistics of the day
      */
  def getStatsOfTheDay(data: LoadedData, date: LocalDate): DailyStatistics = {
    val carbonIntensityStats = carbonIntensityStatsOfDay(data, date)
    val productionStats      = productionStatsOfDay(data, date)
    val powerPeakAndTemperatureStats =
      powerPeakAndTemperatureStatsOfDay(data, date)

    DailyStatistics(
      date,
      carbonIntensityStats.map(_.minDirectIntensity),
      carbonIntensityStats.map(_.avgDirectIntensity),
      carbonIntensityStats.map(_.maxDirectIntensity),
      carbonIntensityStats.map(_.lowCarbonPercent),
      carbonIntensityStats.map(_.renewablePercent),
      productionStats,
      powerPeakAndTemperatureStats.map(_.powerPeak),
      powerPeakAndTemperatureStats.map(_.meanTemperature)
    )
  }
}
