import zio._
import zio.Console._
import java.time.LocalDate
import scala.util.Try
import java.time.format.DateTimeFormatter

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
    "Case study: Temperature vs power peak"      -> ((data) => printLine("Not implemented yet")),
    "Case study: Carbon intensity vs power peak" -> ((data) => printLine("Not implemented yet"))
  )

  val choiceMenu: String =
    "\nWelcome to our energy analysis tool!\n\nHere are all the interesting interactions you can have with it:\n"
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
