import CarbonIntensities._
import ConsumptionValues._
import Percentages._
import PowerValues._
import TemperatureValues._

import com.github.tototoshi.csv.{CSVReader, CSVFormat, DefaultCSVFormat, defaultCSVFormat}

import zio.ZIO
import zio.stream.ZStream

import scala.util.Try
import scala.annotation.static

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.net.URL

/** Containts loaded data from the app, as chunks
  *
  * @param carbonIntensity
  * @param ecoMix
  * @param rawConso
  * @param peakConso
  */
final case class ChunkedData(
    carbonIntensity: zio.Chunk[CarbonIntensityPerHour],
    ecoMix: zio.Chunk[ElectricityProductionAndConsumption],
    rawConso: zio.Chunk[ElectricityConsumptionPerMonth],
    peakConso: zio.Chunk[PowerPeakWithTemperature]
)

object DataLoader {

  /** CSVFormat that uses ';' as a delimiter
    */
  private object SemiColonFormat extends DefaultCSVFormat {
    override val delimiter: Char = ';'
  }

  /** Returns the full path of a file in the resources folder
    *
    * @param filename
    *   name of the file
    * @return
    */
  private def getFullPath(filename: String): String = {
    getClass.getClassLoader.getResource(filename).getFile()
  }

  /** Loads a CSV file from the resources folder
    *
    * @param filename
    *   name of the file
    * @param format
    *   CSVFormat to use
    * @return
    *   A CSVReader
    */
  private def loadCsv(filename: String)(implicit format: CSVFormat): CSVReader = {
    CSVReader.open(filename)(format)
  }

  /** Loads the carbon intensity data from the resources folder
    *
    * @return
    *   a chunk of CarbonIntensityPerHour
    */
  def loadCarbonIntensity: ZIO[Any, Throwable, zio.Chunk[CarbonIntensityPerHour]] = {
    val url2021 = getFullPath("FR_2021_hourly.csv")
    val url2022 = getFullPath("FR_2022_hourly.csv")
    for {
      chunk2021 <- loadCarbonIntensityFromUrl(url2021)
      chunk2022 <- loadCarbonIntensityFromUrl(url2022)
      merged    <- ZStream.fromChunks(chunk2021, chunk2022).runCollect
    } yield (merged)
  }

  /** Loads the carbon intensity data from an URL
    *
    * @param filename
    *   name of the file
    * @return
    *   a chunk of CarbonIntensityPerHour
    */
  def loadCarbonIntensityFromUrl(filename: String): ZIO[Any, Throwable, zio.Chunk[CarbonIntensityPerHour]] = {
    for {
      file <- ZIO.succeed(loadCsv(filename))
      stream <- ZStream
        .fromIterator[Seq[String]](file.iterator)
        .drop(1)
        .map[Option[CarbonIntensityPerHour]](line =>
          val dateTime         = Try(LocalDateTime.parse(line.head, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).toOption
          val directIntensity  = line(4).toFloatOption
          val lcaIntensity     = line(5).toFloatOption
          val lowCarbonPercent = line(6).toFloatOption
          val renewablePercent = line(7).toFloatOption

          for {
            dateTime         <- dateTime
            directIntensity  <- directIntensity
            lcaIntensity     <- lcaIntensity
            lowCarbonPercent <- lowCarbonPercent
            renewablePercent <- renewablePercent
          } yield CarbonIntensityPerHour(
            dateTime,
            directIntensity,
            lcaIntensity,
            lowCarbonPercent,
            renewablePercent
          )
        )
        .collectSome[CarbonIntensityPerHour]
        .runCollect
      _ <- ZIO.succeed(file.close())
    } yield (stream)
  }

  /** Loads the eco mix data from the resources folder. Eco mix data is the production and consumption of electricity, with details on the production by supply chain.
    *
    * @return
    *   a chunk of ElectricityProductionAndConsumption
    */
  def loadEcoMix: ZIO[Any, Throwable, zio.Chunk[ElectricityProductionAndConsumption]] = {
    loadEcoMixFromUrl(getFullPath("eco2mix-national-tr.csv"))
  }

  /** Loads the eco mix data from an URL. Eco mix data is the production and consumption of electricity, with details on the production by supply chain.
    *
    * @param filename
    * @return
    */
  def loadEcoMixFromUrl(filename: String): ZIO[Any, Throwable, zio.Chunk[ElectricityProductionAndConsumption]] = {
    implicit class SupplyChainSeqOperations(val seq: Seq[ElectricityProductionPerSupplyChain]) {
      def maybeAppendToSeq(maybeValue: String, supplyChain: SupplyChain): Seq[ElectricityProductionPerSupplyChain] = {
        maybeValue.toIntOption match {
          case Some(value) => seq :+ ElectricityProductionPerSupplyChain(supplyChain, value)
          case None        => seq
        }
      }
    }

    for {
      file <- ZIO.succeed(loadCsv(filename)(SemiColonFormat))
      stream <- ZStream
        .fromIterator[Seq[String]](file.iterator)
        .drop(1)
        .map[Option[ElectricityProductionAndConsumption]](line =>
          val dateTime    = Try(LocalDateTime.parse(line(4), DateTimeFormatter.ISO_DATE_TIME)).toOption
          val consumption = line(5).toIntOption

          val production = (Nil: Seq[ElectricityProductionPerSupplyChain])
            .maybeAppendToSeq(line(8), SupplyChain.Fuel)
            .maybeAppendToSeq(line(9), SupplyChain.Coal)
            .maybeAppendToSeq(line(10), SupplyChain.Gas)
            .maybeAppendToSeq(line(11), SupplyChain.Nuclear)
            .maybeAppendToSeq(line(12), SupplyChain.Wind)
            .maybeAppendToSeq(line(15), SupplyChain.Solar)
            .maybeAppendToSeq(line(16), SupplyChain.Hydro)
            .maybeAppendToSeq(line(18), SupplyChain.Bio)

          for {
            dateTime    <- dateTime
            consumption <- consumption
          } yield ElectricityProductionAndConsumption(
            dateTime,
            production,
            consumption
          )
        )
        .collectSome[ElectricityProductionAndConsumption]
        .runCollect
      _ <- ZIO.succeed(file.close())
    } yield (stream)
  }

  /** Loads the raw consumption data from the resources folder
    *
    * @return
    *   a chunk of ElectricityConsumptionPerMonth
    */
  def loadRawConsos: ZIO[Any, Throwable, zio.Chunk[ElectricityConsumptionPerMonth]] = {
    loadRawConsosFromUrl(getFullPath("conso_brute_corrigee_client_direct.csv"))
  }

  /** Loads the raw consumption data from an URL
    *
    * @param filename
    *   name of the file
    * @return
    *   a chunk of ElectricityConsumptionPerMonth
    */
  def loadRawConsosFromUrl(filename: String): ZIO[Any, Throwable, zio.Chunk[ElectricityConsumptionPerMonth]] = {
    for {
      file <- ZIO.succeed(loadCsv(filename)(SemiColonFormat))
      stream <- ZStream
        .fromIterator[Seq[String]](file.iterator)
        .drop(1)
        .map[Option[ElectricityConsumptionPerMonth]](line =>
          val monthYear = line(0).split("-")
          val month     = monthYear.head.toIntOption
          val year      = monthYear.last.toIntOption

          val rawConsumption       = line(1).toFloatOption
          val correctedConsumption = line(2).toFloatOption

          for {
            month                <- month
            year                 <- year
            rawConsumption       <- rawConsumption
            correctedConsumption <- correctedConsumption
          } yield ElectricityConsumptionPerMonth(
            MonthYear(month, year),
            rawConsumption,
            correctedConsumption
          )
        )
        .collectSome[ElectricityConsumptionPerMonth]
        .runCollect
      _ <- ZIO.succeed(file.close())
    } yield (stream)
  }

  /** Loads the peak consumption data and temperature data from the resources folder
    *
    * @return
    *   a chunk of PowerPeakWithTemperature
    */
  def loadPeakConso: ZIO[Any, Throwable, zio.Chunk[PowerPeakWithTemperature]] = {
    loadPeakConsoFromUrl(getFullPath("pic-journalier-consommation-brute.csv"))
  }

  /** Loads the peak consumption and temperature data from an URL
    *
    * @param filename
    *   name of the file
    * @return
    *   a chunk of PowerPeakWithTemperature
    */
  def loadPeakConsoFromUrl(filename: String): ZIO[Any, Throwable, zio.Chunk[PowerPeakWithTemperature]] = {
    for {
      file <- ZIO.succeed(loadCsv(filename)(SemiColonFormat))
      stream <- ZStream
        .fromIterator[Seq[String]](file.iterator)
        .drop(1)
        .map[Option[PowerPeakWithTemperature]](line =>
          val date     = Try(LocalDate.parse(line(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toOption
          val peak     = line(1).toFloatOption.map(_.toInt)
          val meanTemp = line(2).toFloatOption
          val tempRef  = line(3).toFloatOption

          for {
            date     <- date
            peak     <- peak
            meanTemp <- meanTemp
            tempRef  <- tempRef
          } yield PowerPeakWithTemperature(
            date,
            peak,
            meanTemp,
            tempRef
          )
        )
        .collectSome[PowerPeakWithTemperature]
        .runCollect
      _ <- ZIO.succeed(file.close())
    } yield (stream)
  }
}
