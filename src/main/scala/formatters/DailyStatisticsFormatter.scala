import DailyStatisticsFormatter._
import java.time.LocalDate
import DailyStatisticsAnalysis._
import zio.Chunk

object DailyStatisticsFormatter {

  def formatOption[T](option: Option[T]): String = option match {
    case Some(value) => value.toString
    case None        => "No data available for this day"
  }

  def formatOptionWithUnit[T](option: Option[T], unit: String): String = option match {
    case Some(value) => value.toString + " " + unit
    case None        => "No data available for this day"
  }

  def formatDailyStatistics(dailyStatistics: DailyStatistics): String = {
    def formatSupplyChain(supplyChainProduction: ElectricityProductionPerSupplyChain): String =
      f"${supplyChainProduction.supplyChain}%-35s: ${supplyChainProduction.production}%-10.2f MW"

    val date          = dailyStatistics.date
    val formattedDate = s"${date.getDayOfMonth}/${date.getMonthValue}/${date.getYear}"
    val top3Production = dailyStatistics.avgProduction.map(
      _.sortBy(_.production)
        .take(3)
        .map(formatSupplyChain(_))
        .mkString("\n")
    )

    s"=== Daily statistics for $formattedDate ===\n" +
      s"Minimum direct intensity:          ${formatOptionWithUnit(dailyStatistics.minDirectIntensity, "gCO2eq/kWh")}\n" +
      s"Average direct intensity:          ${formatOptionWithUnit(dailyStatistics.avgDirectIntensity, "gCO2eq/kWh")}\n" +
      s"Maximum direct intensity:          ${formatOptionWithUnit(dailyStatistics.maxDirectIntensity, "gCO2eq/kWh")}\n" +
      s"Average low carbon percentage:     ${formatOptionWithUnit(dailyStatistics.lowCarbonPercent, "%")}\n" +
      s"Average renewable percentage:      ${formatOptionWithUnit(dailyStatistics.renewablePercent, "%")}\n" +
      s"Average production per supply chain (Top 3):\n${formatOption(top3Production)}\n" +
      s"Power peak:                        ${formatOptionWithUnit(dailyStatistics.powerPeak, "MW")}\n" +
      s"Mean temperature:                  ${formatOptionWithUnit(dailyStatistics.meanTemperature, "C")}\n"
  }
}
