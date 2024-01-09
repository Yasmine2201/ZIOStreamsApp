import zio._
import zio.Console._

import DataLoader._

object Main extends ZIOAppDefault {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Unit] = {
    for {
      _ <- printLine("Loading data...")

      // Load data

      carbonIntensity <- loadCarbonIntensity
      ecoMix          <- loadEcoMix
      rawConso        <- loadRawConsos
      peakConso       <- loadPeakConso

      // Print size of chunks

      _ <- printLine(s"Carbon intensity entries: ${carbonIntensity.size}")
      _ <- printLine(s"Eco mix entries: ${ecoMix.size}")
      _ <- printLine(s"Raw conso entries: ${rawConso.size}")
      _ <- printLine(s"Peak conso entries: ${peakConso.size}")

      // Create analysis module

      analysisModule = AnalysisModule(
        ChunkedData(
          carbonIntensity,
          ecoMix,
          rawConso,
          peakConso
        )
      )

      _    <- printLine("Hello, what is you name?")
      name <- readLine
      _    <- printLine(s"Hello $name, welcome to ZIO! ${analysisModule.getCarbonIntensityPerHour.size}")
      _    <- ZIO.succeed(analysisModule.printPeakOfConsumptionPerYear)
      _    <- printLine("Let's see for Temperatures")
      _    <- ZIO.succeed(analysisModule.printMinOfTemperaturePerYear)
      _    <- printLine("Let's see if the date of Peak of Cons is close to the one of Min Temp")
      _    <- ZIO.succeed(analysisModule.printPeakConsAndMinTempPerYear)
      _    <- printLine(s"what is your deduction $name?")
      deduction   <- readLine
      _    <- printLine("Yes you got it! They are very close!!! The same month for most of them! It's logical.\n When it is clod, people use more electricity for heat.")
      _    <- readLine("Thanks for your interaction, keep going!")
      _    <- printLine("All PeakCons GroupedBy Year ")
      //_    <- ZIO.succeed(analysisModule.printGroupByYear)
    } yield ()
  }
}
