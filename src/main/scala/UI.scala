import zio._
import zio.Console._
import java.time.LocalDate
import scala.util.Try
import java.time.format.DateTimeFormatter
import PowerTemperatureAnalysis._

/** Object containing all the UI logic
  */
object UI {

  /** Main ZIO console loop
    * @return
    *   a ZIO effect that will return Unit
    */
  def consoleLoop(data: LoadedData): ZIO[Any, Any, Unit] = {
    for {
      _      <- printLine(menuString)
      number <- readNumber
      _ <-
        if (number == menuChoices.length) ZIO.unit
        else
          menuChoices.lift(number - 1) match {
            case Some((_, action)) => action(data)
            case None              => printLineError("Invalid choice")
          }
      _ <- if (number == menuChoices.length) ZIO.unit else consoleLoop(data)
    } yield ()
  }

  /** Menu options with their associated actions to perform
    * @return
    *   a chunk of tuples (name, action), action being a function that takes LoadedData and returns a ZIO effect that will return Unit
    */
  val menuChoices: Chunk[(String, (LoadedData) => ZIO[Any, Any, Unit])] =
    Chunk(
      ("Get stats for a specific day", printDailyStats),
      ("Global statistics for a given period", printGlobalStats),
      ("Case study: Temperature vs power peak", printCaseStudy),
      ("Exit", (_) => ZIO.unit)
    )

  /** Menu string
    * @return
    *   a String containing the menu and its options
    */
  val menuString: String =
    "Welcome to our energy analysis tool!\n\nHere are all the interesting interactions you can have with it:\n"
      + menuChoices.zipWithIndex
        .map { case ((name, _), index) =>
          s"${index + 1}. $name"
        }
        .mkString("\n")
      + "\n\nPlease enter your choice:"

  /** Reads a date from the console
    * @return
    *   a ZIO effect that will return a LocalDate
    */
  def readDate: ZIO[Any, Any, LocalDate] =
    for {
      input <- readLine
      date <- Try(LocalDate.parse(input, DateTimeFormatter.ofPattern("dd/MM/yyyy"))).toOption match {
        case Some(date) => ZIO.succeed(date)
        case None       => printLineError("You entered an invalid date, retry:") *> readDate
      }
    } yield date

  /** Reads a number from the console
    *
    * @return
    *   a ZIO effect that will return an Int
    */
  def readNumber: ZIO[Any, Any, Int] =
    for {
      input <- readLine
      number <- Try(input.toInt).toOption match {
        case Some(number) => ZIO.succeed(number)
        case None         => printLineError("You entered an invalid number, retry:") *> readNumber
      }
    } yield number

  def printDailyStats(data: LoadedData): ZIO[Any, Any, Unit] = {
    for {
      _    <- printLine("Please enter a date (dd/MM/yyyy): ")
      date <- readDate
      _    <- printLine(s"Stats for $date:")
    } yield ()
  }

  def printGlobalStats(data: LoadedData): ZIO[Any, Any, Unit] = {
    for {
      _         <- printLine("Please enter a start date (dd/MM/yyyy):")
      startDate <- readDate
      _         <- printLine("Please enter an end date (dd/MM/yyyy):")
      endDate   <- readDate

      _ <- printLine("\n\n" + GlobalStatisticsAnalysis.carbonIntensityTab(data, startDate, endDate))
      _ <- printLine("\n\n" + GlobalStatisticsAnalysis.productionBySupplyChainTab(data, startDate, endDate))
      _ <- printLine("\n\n" + GlobalStatisticsAnalysis.consumptionAndTemperatureTab(data, startDate, endDate) + "\n")
    } yield ()
  }

  def printCaseStudy(data: LoadedData): ZIO[Any, Any, Unit] = {
    for {
      _ <- printLine(
        formatPowerPeakAndTemperature(
          maxPowerPeaksAndMinTemperaturesByYear(data),
          temperatureAndPowerPeakPearsonCorrelation(data)
        )
      )
      _ <- printLine("\nPress enter to go back to the menu...")
      _ <- readLine
    } yield ()
  }
}
