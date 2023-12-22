import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.numeric.*

import java.time.LocalDateTime
import java.time.LocalDate
import scala.Conversion

object CarbonIntensities {
  opaque type CarbonIntensity = Float

  object CarbonIntensity {
    def apply(value: Float): CarbonIntensity = value
  }

  given Conversion[Float, CarbonIntensity] = CarbonIntensity(_)
}

object Percentages {
  opaque type Percentage = Float

  object Percentage {
    def apply(value: Float): Percentage = value
  }

  given Conversion[Float, Percentage] = Percentage(_)
}

object PowerValues {
  opaque type Power = Int

  object Power {
    def apply(value: Int): Power = value
  }

  given Conversion[Int, Power] = Power(_)
}

object ConsumptionValues {
  opaque type HourlyConsumption  = Int
  opaque type MonthlyConsumption = Int

  object HourlyConsumption {
    def apply(value: Int): HourlyConsumption = value
  }

  object MonthlyConsumption {
    def apply(value: Int): MonthlyConsumption = value
  }

  given Conversion[Int, HourlyConsumption]  = HourlyConsumption(_)
  given Conversion[Int, MonthlyConsumption] = MonthlyConsumption(_)
}

object TemperatureValues {
  opaque type Temperature = Float

  object Temperature {
    def apply(value: Float): Temperature = value
  }
}

import CarbonIntensities._
import ConsumptionValues._
import Percentages._
import PowerValues._
import TemperatureValues._

// Intensité carbone de l'électricité en France, par heure, en 2021 et 2022

case class CarbonIntensityPerHour(
    dateTime: LocalDateTime,
    directIntensity: CarbonIntensity,
    lcaIntensity: CarbonIntensity, // life-cycle assessment intensity
    lowCarbonPercent: Percentage,
    renewablePercent: Percentage
)

// Consommation mensuelle d'électricité en France

case class MonthYear(month: Int, year: Int)

case class ElectricityConsumptionPerMonth(
    month: MonthYear,
    rawConsumption: MonthlyConsumption,
    correctedConsumption: MonthlyConsumption
)

enum SupplyChain:
  case Fuel, Coal, Gas, Nuclear, Wind, Solar, Hydro, Bio

case class ElectricityProductionPerSupplyChain(
    supplyChain: SupplyChain,
    production: Power
)

case class ElectricityProductionAndConsumption(
    dateTime: LocalDateTime,
    production: Seq[ElectricityProductionPerSupplyChain],
    consumption: HourlyConsumption
)

case class PowerPeakWithTemperature(
    dateTime: LocalDate,
    power: Power,
    meanTemperature: Temperature,
    referenceTemperature: Temperature
)
