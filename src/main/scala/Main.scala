import zio._
import zio.Console._

import DataLoader._
import UI._

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

      // Run UI
      _ <- consoleLoop(analysisModule)
    } yield ()
  }
}
