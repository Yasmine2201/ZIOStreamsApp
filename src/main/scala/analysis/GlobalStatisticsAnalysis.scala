import zio.Chunk
import Types.*
import java.time.LocalDate
import scala.math.Ordering

/** Contains the functions to analyse the global statistics.
  */
object GlobalStatisticsAnalysis {

  /** Statistics on a variable : average, standard deviation, min, max, number of points.
    */
  final case class Statistics(
      variableName: String,
      average: Double,
      standardDeviation: Double,
      min: Double,
      max: Double,
      numberOfPoints: Int
  )

  /** Structure to select a field in a structure of type A.
    *
    * @param fieldName
    *   The name of the field
    * @param fieldSelector
    *   The function to select the field
    */
  final case class FieldSelector[A](
      fieldName: String,
      fieldSelector: A => Float
  )

  /** Get global statistics on given fields, during a given period.
    *
    * @param data:
    *   LoadedData. The data to analyse.
    * @param fields:
    *   The fields to analyse.
    * @param dateSelector:
    *   The function to select the date from the data.
    * @param start:
    *   LocalDate. The start date of the period.
    * @param end:
    *   LocalDate. The end date of the period.
    * @return
    *   The global statistics on the period.
    */
  def getFieldsStatistics[A](
      data: Chunk[A],
      fields: List[FieldSelector[A]],
      dateSelector: A => LocalDate,
      start: LocalDate,
      end: LocalDate
  ): List[Statistics] = {

    val filteredData = data.filter { x =>
      val date = dateSelector(x)
      (date.isAfter(start) || date.isEqual(start)) && (date.isBefore(end) || date.isEqual(end))
    }

    if (filteredData.isEmpty)
      Nil: List[Statistics]
    else
      fields.map { field =>
        val fieldName = field.fieldName
        val values    = filteredData.map(field.fieldSelector)
        val average   = ChunkMath.average(values)
        val stdDev    = ChunkMath.standardDeviation(values)
        val min       = values.min
        val max       = values.max
        val count     = values.size

        Statistics(fieldName, average, stdDev, min, max, count)
      }.toList
  }

  /** Get global statistics on the carbon intensity of electricity production and consumption.
    *
    * @param data:
    *   The data to analyse.
    * @param start:
    *   The start date of the period.
    * @param end:
    *   The end date of the period.
    * @return
    *   The global statistics on the period.
    */
  def getCarbonIntensityStatistics(data: LoadedData, start: LocalDate, end: LocalDate): List[Statistics] = {
    val fields = List(
      FieldSelector("Direct Carbon intensity (gCO2 eq / kWh)", (_: HourlyCarbonIntensity).directIntensity),
      FieldSelector("LCA Intensity (gCO2 eq / kWh)", (_: HourlyCarbonIntensity).lcaIntensity),
      FieldSelector("Low carbon percent", (_: HourlyCarbonIntensity).lowCarbonPercent),
      FieldSelector("Renewable percent", (_: HourlyCarbonIntensity).renewablePercent)
    )

    getFieldsStatistics(data.hourlyCarbonIntensity, fields, (_: HourlyCarbonIntensity).dateTime.toLocalDate, start, end)
  }

  /** Get global statistics on the production by supply chain.
    *
    * @param data
    *   The data to analyse.
    * @param start
    *   The start date of the period.
    * @param end
    *   The end date of the period.
    * @return
    *   The global statistics on the period.
    */
  def getProductionBySupplyChain(
      data: LoadedData,
      start: LocalDate,
      end: LocalDate
  ): List[Statistics] = {

    val supplyChains = List(
      SupplyChain.Fuel,
      SupplyChain.Coal,
      SupplyChain.Gas,
      SupplyChain.Nuclear,
      SupplyChain.Wind,
      SupplyChain.Solar,
      SupplyChain.Hydro,
      SupplyChain.Bio
    )

    val productionBySupplyChainFields = supplyChains.map { supplyChain =>
      FieldSelector(
        supplyChain.toString,
        (x: HourlyElectricityProductionAndConsumption) => x.production.filter(_.supplyChain == supplyChain).head.production
      )
    }.toList

    getFieldsStatistics(
      data.hourlyElectricityProductionAndConsumption,
      productionBySupplyChainFields,
      (_: HourlyElectricityProductionAndConsumption).dateTime.toLocalDate,
      start,
      end
    )
  }

  /** Get global statistics on the temperature and power consumption.
    *
    * @param data
    *   The data to analyse.
    * @param start
    *   The start date of the period.
    * @param end
    *   The end date of the period.
    * @return
    *   The global statistics on the period.
    */
  def getTemperatureAndConsumptionStatistics(
      data: LoadedData,
      start: LocalDate,
      end: LocalDate
  ): List[Statistics] = {

    val tempratureAndPowerPeakFields = List(
      FieldSelector("Daily mean temperature (C)", (_: DailyPowerPeakWithTemperature).meanTemperature),
      FieldSelector("Daily peak of power (MW)", (_: DailyPowerPeakWithTemperature).powerPeak)
    )

    val consumptionFields = List(
      FieldSelector("Power consumption (MW)", (_: HourlyElectricityProductionAndConsumption).consumption)
    )

    getFieldsStatistics(
      data.dailyPowerPeakWithTemperature,
      tempratureAndPowerPeakFields,
      (_: DailyPowerPeakWithTemperature).date,
      start,
      end
    ) ++ getFieldsStatistics(
      data.hourlyElectricityProductionAndConsumption,
      consumptionFields,
      (_: HourlyElectricityProductionAndConsumption).dateTime.toLocalDate,
      start,
      end
    )
  }
}
