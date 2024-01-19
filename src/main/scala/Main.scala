import zio._
import zio.Console._

import DataLoader._
import UI._
import java.time.LocalDate

object Main extends ZIOAppDefault {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Unit] = {
    for {
      data <- loadData
      _ <- printLine(
        GlobalStatisticsAnalysis.formatStatisticsTable(
          "Environmental impact of electricity production and consumption",
          GlobalStatisticsAnalysis.getCarbonIntensityStatistics(data, LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31))
        )
      )
      _ <- printLine(
        "\n\n" +
          GlobalStatisticsAnalysis.formatStatisticsTable(
            "Electricty consumption and temperature",
            GlobalStatisticsAnalysis.getTemperatureAndConsumptionStatistics(data, LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31))
          )
      )
      _ <- printLine(
        "\n\n" +
          GlobalStatisticsAnalysis.formatStatisticsTable(
            "Production (MW) by supply chain",
            GlobalStatisticsAnalysis.getProductionBySupplyChain(data, LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31))
          )
      )
    } yield ()
  }
}
