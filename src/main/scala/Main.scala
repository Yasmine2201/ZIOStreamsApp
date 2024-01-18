import zio._
import zio.Console._

import DataLoader._
import UI._

object Main extends ZIOAppDefault {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Unit] = {

    for {
      _ <- printLine("Loading data...")

      // Load data
      data <- for {
        carbonIntensity <- loadCarbonIntensity
        ecoMix          <- loadEcoMix
        rawConso        <- loadRawConsos
        peakConso       <- loadPeakConso
      } yield LoadedData(
        carbonIntensity,
        ecoMix,
        rawConso,
        peakConso
      )

      // Print size of chunks
      _ <- printLine(
        s"Carbon intensity entries: ${data.hourlyCarbonIntensity.size}\n" +
          s"Eco mix entries: ${data.hourlyElectricityProductionAndConsumption.size}\n" +
          s"Raw conso entries: ${data.monthlyElectricityConsumption.size}\n" +
          s"Peak conso entries: ${data.dailyPowerPeakWithTemperature.size}"
      )

      // Run UI
      _ <- consoleLoop(data)
    } yield ()
  }
}
