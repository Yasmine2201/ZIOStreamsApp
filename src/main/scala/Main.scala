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
      // Create GlobalStatisticsAnalysis
      globalStatisticsAnalysis = GlobalStatisticsAnalysis(analysisModule)
      // Create PowerTemperatureAnalysis
      powerTemperatureAnalysis = PowerTemperatureAnalysis(analysisModule.dailyPowerPeakWithTemperature)

      _         <- printLine("Hello, what is you name?")
      name      <- readLine
      _         <- printLine(s"Hello $name, welcome to ZIO! ${analysisModule.hourlyCarbonIntensity.size}")
      _         <- printLine("Let's see what is the maximum power peak reached for each year :\n")
      _         <- ZIO.succeed(globalStatisticsAnalysis.printMaxPowerPeakByYear(analysisModule.powerPeakTemperatureGroupedByYear))
      _         <- printLine("\n")
      _         <- printLine("Let's see what is the minimum temperature reached for each year :\n")
      _         <- ZIO.succeed(globalStatisticsAnalysis.printMinTempeartureByYear(analysisModule.powerPeakTemperatureGroupedByYear))
      _         <- printLine("\n")
      _         <- printLine("Let's display them together to see if the date of maximum power peak is close to the one of minimum temperature :")
      _         <- printLine("\n")
      _         <- ZIO.succeed(powerTemperatureAnalysis.printMaxPowerPeakAndMinTemperatureByYear(analysisModule.powerPeakTemperatureGroupedByYear))
      _         <- printLine("\n")
      _         <- printLine(s"what is your deduction $name?")
      deduction <- readLine
      _         <- printLine("Yes you got it! They are very close!!! It's logical.\nWhen it is cold, people use more electricity for heat.")
      _         <- readLine("Thanks for your interaction, keep going!\n")
      _         <- printLine("Let's see power peak temperature grouped by year :\n")
      _         <- ZIO.succeed(analysisModule.printpowerPeakTemperatureGroupedByYear)
      _         <- ZIO.succeed(analysisModule.printCarbonIntensityGroupedByDay)
    } yield ()
  }
}
