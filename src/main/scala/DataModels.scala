import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.numeric.*


import java.time.{LocalDate, LocalDateTime}

import scala.Conversion

object CarbonIntensities {
  opaque type CarbonIntensity = Float

  object CarbonIntensity {
    def apply(value: Float): CarbonIntensity = value
    def div (v1: Float, v2:Float): CarbonIntensity = CarbonIntensity(v1/v2)
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
  implicit val powerOrdering: Ordering[Power] = Ordering.Int
}

object ConsumptionValues {
  opaque type Consumption        = Float
  opaque type HourlyConsumption  = Consumption
  opaque type MonthlyConsumption = Consumption

  // allow conversion from int to float for consumption values

  object Consumption {
    def apply(value: Float | Int): HourlyConsumption | MonthlyConsumption = {
      value match {
        case value: Float => value
        case value: Int   => value.toFloat
      }
    }
  }

  given Conversion[Float, HourlyConsumption]  = Consumption(_)
  given Conversion[Float, MonthlyConsumption] = Consumption(_)
  given Conversion[Int, HourlyConsumption]    = Consumption(_)
  given Conversion[Int, MonthlyConsumption]   = Consumption(_)
}

object TemperatureValues {
  opaque type Temperature = Float

  object Temperature {
    def apply(value: Float): Temperature = value
  }

  given Conversion[Float, Temperature] = Temperature(_)
   implicit val temperatureOrdering: Ordering[Temperature] = (x: Temperature, y: Temperature) =>
    java.lang.Float.compare(x.toFloat, y.toFloat)

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
