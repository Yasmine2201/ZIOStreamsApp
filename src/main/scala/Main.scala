import zio.*
import zio.stream.ZStream
import com.github.tototoshi.csv.{CSVFormat, DefaultCSVFormat, *}
import io.github.iltotore.iron.autoRefine

import java.time.LocalDateTime
import java.time.LocalDate

import CarbonIntensities._
import ConsumptionValues._
import Percentages._
import PowerValues._
import TemperatureValues._

object Main extends ZIOAppDefault {

  private object SemiColonFormat extends DefaultCSVFormat {
    override val delimiter: Char = ';'
  }

  private def loadCsv(filename: String)(implicit format: CSVFormat): ZStream[Any, Throwable, Seq[String]] = {
    val url = getClass.getClassLoader.getResource(filename)
    val source = CSVReader.open(url.getFile)(format)
    ZStream.fromIterator[Seq[String]](source.iterator)
  }

//  private def loadCarbonIntensity(): ZStream[Any, Throwable, CarbonIntensityPerHour] = {
//    for {
//      stream <- loadCsv("FR_2021_hourly.csv")
//        .merge(loadCsv("FR_2022_hourly.csv"))
//        .drop(1)
//        .map[Option[CarbonIntensityPerHour]](line =>
//          try {
//            val dateTime = LocalDateTime.parse(line.head)
//            val directIntensity = CarbonIntensity(line(1).toFloat)
//            val lcaIntensity = CarbonIntensity(line(2).toFloat)
//            val lowCarbonPercent = Percentage(line(3).toFloat)
//            val renewablePercent = Percentage(line(4).toFloat)
//
//            Some(
//              CarbonIntensityPerHour(
//                dateTime,
//                directIntensity,
//                lcaIntensity,
//                lowCarbonPercent,
//                renewablePercent
//              )
//            )
//          } catch {
//            case _: Throwable => None
//          }
//        )
//    } yield stream
//  }

//  private def loadEcoMix() = {
//    for {
//      stream <- loadCsv("eco2mix-national-tr.csv", SemiColonFormat)
//        .drop(1)
//        .map[Option[ElectricityProductionAndConsumption]] (line => {
//          val date = LocalDate.parse(line(2),)
//        }
//
//        )
//    } yield()
//  }

//  private def loadRawConso() = {
//    for {
//      stream <- loadCsv("conso_brute_corrigee_client_direct.csv", SemiColonFormat)
//        .drop(1)
//        .map[Option[ElectricityConsumptionPerMonth]](line =>
//          line match
//      )
//    } yield ()
//  }

//  private def loadPeakConso() = {
//    for {
//      stream <- loadCsv(("pic-journalier-consommation-brute.csv", SemiColonFormat)
//        .map[Option[PowerPeakWithTemperature]](line)
//    } yield ()
//  }

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Unit] = {
    for {
      url <- ZIO.succeed(loadCsv("conso_brute_corrigee_client_direct.csv"))

    } yield ()
  }
}