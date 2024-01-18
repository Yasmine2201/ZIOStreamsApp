import zio._
import zio.Console._
import java.time.LocalDate
import scala.util.Try
import java.time.format.DateTimeFormatter

object UI {

  val menuOptions: Chunk[(String, (AnalysisModule) => ZIO[Any, Any, Unit])] = Chunk(
    "Get stats for a specific day" -> ((mod) =>
      for {
        date <- askForDate
        _    <- printLine(s"Stats for $date:")
      } yield ()
    ),
    "Global statistics for a given period"       -> ((mod) => ZIO.succeed(mod.carbonIntensityAnalysis.printCarbonIntensityGroupedByDay)),
    "Case study: Temperature vs power peak"      -> ((mod) => ZIO.succeed(mod.powerTemperatureAnalysis.printMaxPowerPeakAndMinTemperatureByYear)),
    "Case study: Carbon intensity vs power peak" -> ((mod) => ZIO.succeed(mod.powerTemperatureAnalysis.printMaxPowerPeakByYear))
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
    */
  def consoleLoop(analysisModule: AnalysisModule): ZIO[Any, Any, Unit] = {
    for {
      _     <- printLine(choiceMenu)
      input <- readLine
      _ <- menuOptions.lift(input.toInt - 1) match {
        case Some((_, action)) => action(analysisModule)
        case None              => printLineError("Invalid choice")
      }
      _ <- consoleLoop(analysisModule)
    } yield ()
  }

  def askForDate: ZIO[Any, Any, LocalDate] =
    for {
      _     <- printLine("Please enter a date (dd/MM/yyyy): ")
      input <- readLine
      date <- Try(LocalDate.parse(input, DateTimeFormatter.ofPattern("dd/MM/yyyy"))).toOption match {
        case Some(date) => ZIO.succeed(date)
        case None       => printLineError("You entered an invalid date, retry.\n") *> askForDate
      }
    } yield date
}
