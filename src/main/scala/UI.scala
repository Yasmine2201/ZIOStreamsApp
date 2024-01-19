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
        _         <- printLine(s"Stats for $startDate to $endDate:")
      } yield ()
    ),
    "Case study: Temperature vs power peak"      -> ((data) => 
      printLine(maxPowerPeakAndMinTemperatureByYear(data))
    ),
  )

  /**
 * Retrieves the information about maximum power peak and minimum temperature entries for each year
 * and formats it into a newline-separated string.
 *
 * @param data The loaded data containing daily power peak with temperature information.
 * @return A formatted string containing information about maximum power peak and minimum temperature
 *         entries for each year, with each entry on a new line.
 */
 def maxPowerPeakAndMinTemperatureByYear(data: LoadedData): String = {
  val output = powerPeakTemperatureGroupedByYear(data)
    .flatMap { case (year, yearData) =>
      val (maxPowerEntry, minTemperatureEntry) = maxPowerPeakAndMinTemperature(yearData)
      Some(
        s"""|+------------------------------------------------------------------------+
            ||                Power and Temperature Summary - Year $year               |
            |+------------------------------------------------------------------------+
            || Max Power Peak:   ${maxPowerEntry.powerPeak} MW on ${maxPowerEntry.date}
            || Min Temperature:  ${minTemperatureEntry.meanTemperature} °C on ${minTemperatureEntry.date}
            |+------------------------------------------------------------------------+""".stripMargin.trim
      )
    }
    .mkString("\n")

  val conclusion = "+------------------------------------------------------------------------+\n" +
    "|                           Final Conclusion                             |\n" +
    "+------------------------------------------------------------------------+\n" +
    "| They are very close! It's logical. When it's colder, people use more   |\n" +
    "| electricity for heat.                                                  |\n" +
    "+------------------------------------------------------------------------+\n"

  s"\n$output\n$conclusion\n"
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
