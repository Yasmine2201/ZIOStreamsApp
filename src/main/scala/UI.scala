import zio._
import zio.Console._
import java.time.LocalDate
import scala.util.Try
import java.time.format.DateTimeFormatter
import PowerTemperatureAnalysis._

/** Object containing all the UI logic
  */
object UI {

  /** Menu options with their associated actions to perform
    * @return
    *   a chunk of tuples (name, action), action being a function that takes LoadedData and returns a ZIO effect that will return Unit
    */
  val menuOptions: Chunk[(String, (LoadedData) => ZIO[Any, Any, Unit])] = Chunk(
    "Get stats for a specific day" -> ((data) =>
      for {
        _    <- printLine("Please enter a date (dd/MM/yyyy): ")
        date <- readDate
        _    <- printLine(s"Stats for $date:")
      } yield ()
    ),
    "Global statistics for a given period" -> ((data) =>
      for {
        _         <- printLine("Please enter a start date (dd/MM/yyyy): ")
        startDate <- readDate
        _         <- printLine("Please enter an end date (dd/MM/yyyy): ")
        endDate   <- readDate
        _ <- printLine(
          GlobalStatisticsAnalysis.formatStatisticsTable(
            "Environmental impact of electricity production and consumption",
            GlobalStatisticsAnalysis.getCarbonIntensityStatistics(data, startDate, endDate),
            s"Data between $startDate and $endDate"
          )
        )
        _ <- printLine(
          "\n\n" +
            GlobalStatisticsAnalysis.formatStatisticsTable(
              "Electricty consumption and temperature",
              GlobalStatisticsAnalysis.getTemperatureAndConsumptionStatistics(data, startDate, endDate),
              s"Data between $startDate and $endDate"
            )
        )
        _ <- printLine(
          "\n\n" +
            GlobalStatisticsAnalysis.formatStatisticsTable(
              "Production (MW) by supply chain",
              GlobalStatisticsAnalysis.getProductionBySupplyChain(data, startDate, endDate),
              s"Data between $startDate and $endDate"
            )
        )
      } yield ()
    ),
    "Case study: Temperature vs power peak" -> ((data) => printLine(formatPowerPeakAndTemperature(data)))
  )

  /** Retrieves the information about maximum power peak and minimum temperature entries for each year and formats it into a newline-separated string.
    *
    * @param data
    *   The loaded data containing daily power peak with temperature information.
    * @return
    *   A formatted string containing information about maximum power peak and minimum temperature entries for each year, with each entry on a new line.
    */
  def formatPowerPeakAndTemperature(data: LoadedData): String = {
    val title = "+-------------------------------------------------------------------------+\n" +
      "|                Case Study : Power and Temperature Summary               |\n" +
      "+-------------------------------------------------------------------------+\n" +
      "| We want to know the maximum power peak and the minimum temperature for  |\n" +
      "| each year.                                                              |\n" +
      "+-------------------------------------------------------------------------+\n"

    val output = powerPeakTemperatureGroupedByYear(data)
      .flatMap {
        case (year, yearData) => {
          val (maxPowerEntry, minTemperatureEntry) = maxPowerPeakAndMinTemperature(yearData)
          Some(
            f"|                              Year $year                                  |\n" +
              "+-------------------------------------------------------------------------+\n" +
              f"| Max Power Peak:  ${maxPowerEntry.powerPeak}%10.2f MW on ${maxPowerEntry.date}                            |\n" +
              f"| Min Temperature: ${minTemperatureEntry.meanTemperature}%10.2f C on ${minTemperatureEntry.date}                             |\n" +
              "+-------------------------------------------------------------------------+\n".stripMargin.trim
          )
        }
      }
      .mkString("\n")

    val pearsonCoef = temperatureAndPowerPeakPearsonCorrelation(data.dailyPowerPeakWithTemperature)
    val pearsonCoefFormatted =
      f"\n| Temperature And PowerPeak Pearson Correlation: $pearsonCoef%5.2f                    |\n" +
        "+-------------------------------------------------------------------------+\n"

    val conclusion =
      "|                            Final Conclusion                             |\n" +
        "+-------------------------------------------------------------------------+\n" +
        "| - The correlation coefficient indicates a strong correlation.           |\n" +
        "| - As temperatures decrease, power peak tends to increase.               |\n" +
        "| - People tend to use more electricity for heating purposes when         |\n" +
        "|   temperatures fall                                                     |\n" +
        "+-------------------------------------------------------------------------+\n"

    title + output + pearsonCoefFormatted + conclusion
  }

  val choiceMenu: String =
    "Welcome to our energy analysis tool!\n\nHere are all the interesting interactions you can have with it:\n"
      + menuOptions.zipWithIndex
        .map { case ((name, _), index) =>
          s"${index + 1}. $name"
        }
        .mkString("\n")
      + "\n\nPlease enter your choice: "

  /** Main ZIO console loop
    * @return
    *   a ZIO effect that will return Unit
    */
  def consoleLoop(data: LoadedData): ZIO[Any, Any, Unit] = {
    for {
      _     <- printLine(choiceMenu)
      input <- readLine
      _ <- menuOptions.lift(input.toInt - 1) match {
        case Some((_, action)) => action(data)
        case None              => printLineError("Invalid choice")
      }
      _ <- consoleLoop(data)
    } yield ()
  }

  /** Reads a date from the console
    * @return
    *   a ZIO effect that will return a LocalDate
    */
  def readDate: ZIO[Any, Any, LocalDate] =
    for {
      input <- readLine
      date <- Try(LocalDate.parse(input, DateTimeFormatter.ofPattern("dd/MM/yyyy"))).toOption match {
        case Some(date) => ZIO.succeed(date)
        case None       => printLineError("You entered an invalid date, retry.\n") *> readDate
      }
    } yield date
}
