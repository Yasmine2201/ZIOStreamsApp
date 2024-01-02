import zio.ZIO
import zio.stream.ZStream
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

import CarbonIntensities._
import ConsumptionValues._
import Percentages._
import PowerValues._
import TemperatureValues._
import java.time.format.DateTimeFormatter
import scala.util.Try
import com.github.tototoshi.csv.CSVReader
import com.github.tototoshi.csv.CSVFormat
import com.github.tototoshi.csv.DefaultCSVFormat
import com.github.tototoshi.csv.defaultCSVFormat
import scala.annotation.static
import java.time.LocalDate

object DataLoader {

  private object SemiColonFormat extends DefaultCSVFormat {
    override val delimiter: Char = ';'
  }

  private def loadCsv(filename: String)(implicit format: CSVFormat): CSVReader = {
    val url = getClass.getClassLoader.getResource(filename)
    CSVReader.open(url.getFile)(format)
  }

  def loadCarbonIntensity: ZIO[Any, Throwable, zio.Chunk[CarbonIntensityPerHour]] = {
    for {
      file_2021 <- ZIO.succeed(loadCsv("FR_2021_hourly.csv"))
      file_2022 <- ZIO.succeed(loadCsv("FR_2022_hourly.csv"))
      stream <- ZStream
        .fromIterator[Seq[String]](file_2021.iterator)
        .drop(1)
        .merge(ZStream.fromIterator[Seq[String]](file_2022.iterator).drop(1))
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
      _ <- ZIO.succeed(file_2021.close())
      _ <- ZIO.succeed(file_2022.close())
    } yield (stream)
  }

  def loadEcoMix: ZIO[Any, Throwable, zio.Chunk[ElectricityProductionAndConsumption]] = {
    for {
      file <- ZIO.succeed(loadCsv("eco2mix-national-tr.csv")(SemiColonFormat))
      stream <- ZStream
        .fromIterator[Seq[String]](file.iterator)
        .drop(1)
        .map[Option[ElectricityProductionAndConsumption]](line =>
          val dateTime = Try(LocalDateTime.parse(line(4), DateTimeFormatter.ISO_DATE_TIME)).toOption

          val consumption = line(5).toIntOption

          val fuelPower    = line(8).toIntOption
          val coalPower    = line(9).toIntOption
          val gasPower     = line(10).toIntOption
          val nuclearPower = line(11).toIntOption
          val windPower    = line(12).toIntOption
          val solarPower   = line(15).toIntOption
          val hydroPower   = line(16).toIntOption
          val bioPower     = line(18).toIntOption

          for {
            dateTime     <- dateTime
            consumption  <- consumption
            fuelPower    <- fuelPower
            coalPower    <- coalPower
            gasPower     <- gasPower
            nuclearPower <- nuclearPower
            windPower    <- windPower
            solarPower   <- solarPower
            hydroPower   <- hydroPower
            bioPower     <- bioPower
          } yield ElectricityProductionAndConsumption(
            dateTime,
            Seq(
              ElectricityProductionPerSupplyChain(SupplyChain.Fuel, fuelPower),
              ElectricityProductionPerSupplyChain(SupplyChain.Coal, coalPower),
              ElectricityProductionPerSupplyChain(SupplyChain.Gas, gasPower),
              ElectricityProductionPerSupplyChain(SupplyChain.Nuclear, nuclearPower),
              ElectricityProductionPerSupplyChain(SupplyChain.Wind, windPower),
              ElectricityProductionPerSupplyChain(SupplyChain.Solar, solarPower),
              ElectricityProductionPerSupplyChain(SupplyChain.Hydro, hydroPower),
              ElectricityProductionPerSupplyChain(SupplyChain.Bio, bioPower)
            ),
            consumption
          )
        )
        .collectSome[ElectricityProductionAndConsumption]
        .runCollect
      _ <- ZIO.succeed(file.close())
    } yield (stream)
  }

  def loadRawConsos: ZIO[Any, Throwable, zio.Chunk[ElectricityConsumptionPerMonth]] = {
    for {
      file <- ZIO.succeed(loadCsv("conso_brute_corrigee_client_direct.csv")(SemiColonFormat))
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

  def loadPeakConso: ZIO[Any, Throwable, zio.Chunk[PowerPeakWithTemperature]] = {
    for {
      file <- ZIO.succeed(loadCsv("pic-journalier-consommation-brute.csv")(SemiColonFormat))
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
