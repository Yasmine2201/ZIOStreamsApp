import Types.*

import com.github.tototoshi.csv.{CSVReader, CSVFormat, DefaultCSVFormat, defaultCSVFormat}

import zio.ZIO
import zio.stream.ZStream
import zio.Chunk

import scala.util.Try
import scala.annotation.static

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.net.URL
import java.io.File
import scala.io.Source

/** Acts as a data container for all the data we need to perform our analysis
  * @param data
  *   The data coming from the dataloader
  */
final case class LoadedData(
    hourlyCarbonIntensity: Chunk[HourlyCarbonIntensity],
    hourlyElectricityProductionAndConsumption: Chunk[HourlyElectricityProductionAndConsumption],
    monthlyElectricityConsumption: Chunk[MonthlyElectricityConsumption],
    dailyPowerPeakWithTemperature: Chunk[DailyPowerPeakWithTemperature]
)

object DataLoader {

  /** CSVFormat that uses ';' as a delimiter
    */
  private object SemiColonFormat extends DefaultCSVFormat {
    override val delimiter: Char = ';'
  }

  /** Loads a CSV file from the loaded resources in the jar
    *
    * @param fileName
    *   name of the file
    * @param format
    *   CSVFormat to use
    * @return
    *   A CSVReader
    */
  private def loadCsv(fileName: String)(implicit format: CSVFormat): CSVReader = {
    CSVReader.open(Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(fileName)))(format)
  }

  /** Loads the carbon intensity data from the resources folder
    *
    * @return
    *   a chunk of HourlyCarbonIntensity
    */
  def loadCarbonIntensity: ZIO[Any, Throwable, zio.Chunk[HourlyCarbonIntensity]] = {
    for {
      chunk2021 <- loadCarbonIntensityFromUrl("FR_2021_hourly.csv")
      chunk2022 <- loadCarbonIntensityFromUrl("FR_2022_hourly.csv")
      merged    <- ZStream.fromChunks(chunk2021, chunk2022).runCollect
    } yield (merged)
  }

  /** Loads the carbon intensity data from an URL
    *
    * @param filename
    *   name of the file
    * @return
    *   a chunk of HourlyCarbonIntensity
    */
  def loadCarbonIntensityFromUrl(fileName: String): ZIO[Any, Throwable, zio.Chunk[HourlyCarbonIntensity]] = {
    for {
      file <- ZIO.succeed(loadCsv(fileName))
      stream <- ZStream
        .fromIterator[Seq[String]](file.iterator)
        .drop(1)
        .map[Option[HourlyCarbonIntensity]](line =>
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
          } yield HourlyCarbonIntensity(
            dateTime,
            directIntensity,
            lcaIntensity,
            lowCarbonPercent,
            renewablePercent
          )
        )
        .collectSome[HourlyCarbonIntensity]
        .runCollect
      _ <- ZIO.succeed(file.close)
    } yield (stream)
  }

  /** Loads the eco mix data from the resources folder.
    *
    * @return
    *   a chunk of HourlyElectricityProductionAndConsumption
    */
  def loadEcoMix: ZIO[Any, Throwable, zio.Chunk[HourlyElectricityProductionAndConsumption]] = {
    loadEcoMixFromFileName("eco2mix-national-tr.csv")
  }

  /** Loads the eco mix data from an URL.
    *
    * @param filename
    * @return
    */
  def loadEcoMixFromFileName(fileName: String): ZIO[Any, Throwable, zio.Chunk[HourlyElectricityProductionAndConsumption]] = {
    implicit class SupplyChainSeqOperations(val seq: Seq[ElectricityProductionPerSupplyChain]) {
      def maybeAppendToSeq(maybeValue: String, supplyChain: SupplyChain): Seq[ElectricityProductionPerSupplyChain] = {
        maybeValue.toFloatOption match {
          case Some(value) => seq :+ ElectricityProductionPerSupplyChain(supplyChain, value)
          case None        => seq
        }
      }
    }

    for {
      file <- ZIO.succeed(loadCsv(fileName)(SemiColonFormat))
      stream <- ZStream
        .fromIterator[Seq[String]](file.iterator)
        .drop(1)
        .map[Option[HourlyElectricityProductionAndConsumption]](line =>
          val dateTime    = Try(LocalDateTime.parse(line(4), DateTimeFormatter.ISO_DATE_TIME)).toOption
          val consumption = line(5).toFloatOption

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
          } yield HourlyElectricityProductionAndConsumption(
            dateTime,
            production,
            consumption
          )
        )
        .collectSome[HourlyElectricityProductionAndConsumption]
        .runCollect
      _ <- ZIO.succeed(file.close)
    } yield (stream)
  }

  /** Loads the raw consumption data from the resources folder
    *
    * @return
    *   a chunk of MonthlyElectricityConsumption
    */
  def loadRawConsos: ZIO[Any, Throwable, zio.Chunk[MonthlyElectricityConsumption]] = {
    loadRawConsosFromFileName("conso_brute_corrigee_client_direct.csv")
  }

  /** Loads the raw consumption data from an URL
    *
    * @param filename
    *   name of the file
    * @return
    *   a chunk of MonthlyElectricityConsumption
    */
  def loadRawConsosFromFileName(fileName: String): ZIO[Any, Throwable, zio.Chunk[MonthlyElectricityConsumption]] = {
    for {
      file <- ZIO.succeed(loadCsv(fileName)(SemiColonFormat))
      stream <- ZStream
        .fromIterator[Seq[String]](file.iterator)
        .drop(1)
        .map[Option[MonthlyElectricityConsumption]](line =>
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
          } yield MonthlyElectricityConsumption(
            MonthYear(month, year),
            rawConsumption,
            correctedConsumption
          )
        )
        .collectSome[MonthlyElectricityConsumption]
        .runCollect
      _ <- ZIO.succeed(file.close)
    } yield (stream)
  }

  /** Loads the peak consumption data and temperature data from the resources folder
    *
    * @return
    *   a chunk of DailyPowerPeakWithTemperature
    */
  def loadPeakConso: ZIO[Any, Throwable, zio.Chunk[DailyPowerPeakWithTemperature]] = {
    loadPeakConsoFromFileName("pic-journalier-consommation-brute.csv")
  }

  /** Loads the peak consumption and temperature data from an URL
    *
    * @param filename
    *   name of the file
    * @return
    *   a chunk of DailyPowerPeakWithTemperature
    */
  def loadPeakConsoFromFileName(fileName: String): ZIO[Any, Throwable, zio.Chunk[DailyPowerPeakWithTemperature]] = {
    for {
      file <- ZIO.succeed(loadCsv(fileName)(SemiColonFormat))
      stream <- ZStream
        .fromIterator[Seq[String]](file.iterator)
        .drop(1)
        .map[Option[DailyPowerPeakWithTemperature]](line =>
          val date     = Try(LocalDate.parse(line(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toOption
          val peak     = line(1).toFloatOption
          val meanTemp = line(2).toFloatOption
          val tempRef  = line(3).toFloatOption

          for {
            date     <- date
            peak     <- peak
            meanTemp <- meanTemp
            tempRef  <- tempRef
          } yield DailyPowerPeakWithTemperature(
            date,
            peak,
            meanTemp,
            tempRef
          )
        )
        .collectSome[DailyPowerPeakWithTemperature]
        .runCollect
      _ <- ZIO.succeed(file.close)
    } yield (stream)
  }

  /** Loads all the data into a LoadedData object
    *
    * @return
    *   a ZIO effect that will return a LoadedData object
    */
  def loadData: ZIO[Any, Throwable, LoadedData] = {
    for {
      hourlyCarbonIntensity                     <- loadCarbonIntensity
      hourlyElectricityProductionAndConsumption <- loadEcoMix
      monthlyElectricityConsumption             <- loadRawConsos
      dailyPowerPeakWithTemperature             <- loadPeakConso
    } yield LoadedData(
      hourlyCarbonIntensity,
      hourlyElectricityProductionAndConsumption,
      monthlyElectricityConsumption,
      dailyPowerPeakWithTemperature
    )
  }
}
