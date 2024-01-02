import zio.*

import DataLoader._

object Main extends ZIOAppDefault {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Unit] = {
    for {
      _ <- ZIO.succeed(println("Loading data..."))

      // Load data

      carbonIntensity <- loadCarbonIntensity
      ecoMix          <- loadEcoMix
      rawConso        <- loadRawConsos
      peakConso       <- loadPeakConso

      // Print size of chunks

      _ <- ZIO.succeed(println(s"Carbon intensity entries: ${carbonIntensity.size}"))
      _ <- ZIO.succeed(println(s"Eco mix entries: ${ecoMix.size}"))
      _ <- ZIO.succeed(println(s"Raw conso entries: ${rawConso.size}"))
      _ <- ZIO.succeed(println(s"Peak conso entries: ${peakConso.size}"))
    } yield ()
  }
}
