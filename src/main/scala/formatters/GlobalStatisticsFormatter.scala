import GlobalStatisticsAnalysis._
import java.time.LocalDate

/** Contains the functions to format the results of the global statistics analysis.
  */
object GlobalStatisticsFormatter {

  /** Get a formatted table of statistics.
    *
    * @param title
    *   The title of the table.
    * @param statisticsList
    *   The list of statistics.
    * @param footer
    *   The footer of the table.
    * @return
    *   A formatted table of statistics.
    */
  def formatStatisticsTable(title: String, statisticsList: List[Statistics], footer: String = ""): String = {

    /** Format a statistics row.
      */
    def formatStatisticsRow(statistics: Statistics): String = {
      val fieldName = statistics.variableName
      val min       = statistics.min
      val max       = statistics.max
      val average   = statistics.average
      val stdDev    = statistics.standardDeviation
      val count     = statistics.numberOfPoints

      f"| $fieldName%-48s | $min%8.2f | $max%8.2f | $average%8.2f | $stdDev%8.2f | $count%8d |\n"
        + "+--------------------------------------------------+----------+----------+----------+----------+----------+\n"
    }

    val addPadding           = title.size % 2
    val sidePadding          = (102 - title.size) / 2
    val correctedSidePadding = if (sidePadding < 0) 0 else sidePadding
    val titleFormatted =
      "+---------------------------------------------------------------------------------------------------------+\n"
        + "| " + " " * (correctedSidePadding + addPadding) + title + " " * (correctedSidePadding + 1) + " |\n"

    val header =
      "+--------------------------------------------------+----------+----------+----------+----------+----------+\n"
        + "| Field                                            | Min      | Max      | Average  | Std Dev  | Nb pts   |\n"
        + "+--------------------------------------------------+----------+----------+----------+----------+----------+\n"

    val rows = statisticsList.map(formatStatisticsRow).mkString

    titleFormatted + header + rows + " " + footer
  }

  /** Get a formatted table of statistics about the carbon intensity of electricity production and consumption, given a period of time.
    *
    * @param data
    *   The data to analyse.
    * @param startDate
    *   The start date of the period.
    * @param endDate
    *   The end date of the period.
    * @return
    *   A formatted table
    */
  def carbonIntensityTab(data: LoadedData, startDate: LocalDate, endDate: LocalDate): String =
    formatStatisticsTable(
      "Environmental impact of electricity production and consumption",
      GlobalStatisticsAnalysis.getCarbonIntensityStatistics(data, startDate, endDate),
      s"Data between $startDate and $endDate"
    )

  /** Get a formatted table of statistics about the temperature and power consumption, given a period of time.
    *
    * @param data
    *   The data to analyse.
    * @param startDate
    *   The start date of the period.
    * @param endDate
    *   The end date of the period.
    * @return
    *   A formatted table
    */
  def consumptionAndTemperatureTab(data: LoadedData, startDate: LocalDate, endDate: LocalDate): String =
    formatStatisticsTable(
      "Electricty consumption and temperature",
      GlobalStatisticsAnalysis.getTemperatureAndConsumptionStatistics(data, startDate, endDate),
      s"Data between $startDate and $endDate"
    )

    /** Get a formatted table of statistics about the production by supply chain, given a period of time.
      *
      * @param data
      *   The data to analyse.
      * @param startDate
      *   The start date of the period.
      * @param endDate
      *   The end date of the period.
      * @return
      *   A formatted table
      */
  def productionBySupplyChainTab(data: LoadedData, startDate: LocalDate, endDate: LocalDate): String =
    formatStatisticsTable(
      "Production (MW) by supply chain",
      GlobalStatisticsAnalysis.getProductionBySupplyChain(data, startDate, endDate),
      s"Data between $startDate and $endDate"
    )
}
