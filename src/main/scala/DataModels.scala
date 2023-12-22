import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.numeric.*

import java.time.LocalDateTime
import java.time.LocalDate

object CarbonIntensities {
  opaque type CarbonIntensity = Float

  object CarbonIntensity {
    def apply(value: Float): CarbonIntensity = value
  }
}

object Percentages {
  opaque type Percentage = Float :| Interval.Closed[0, 100]

  object Percentage {
    def apply(value: Float :| Interval.Closed[0, 100]): Percentage = value
  }
}

object PowerValues {
  opaque type Power = Float

  object Power {
    def apply(value: Float): Power = value
  }
}

object ConsumptionValues {
  opaque type HourlyConsumption = Float
  opaque type MonthlyConsumption = Float

  object HourlyConsumption {
    def apply(value: Float): HourlyConsumption = value
  }

  object MonthlyConsumption {
    def apply(value: Float): MonthlyConsumption = value
  }
}

import CarbonIntensities._
import ConsumptionValues._
import Percentages._
import PowerValues._

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
                                    meanTemperature: Float,
                                    referenceTemperature: Float
                                   )

