import java.time.{LocalDate, LocalDateTime}
import Types.*

case class MonthYear(month: Int, year: Int)

/** Carbon intensity data for an hour. */
case class HourlyCarbonIntensity(
    dateTime: LocalDateTime,
    directIntensity: CarbonIntensity.gPerkWh,
    lcaIntensity: CarbonIntensity.gPerkWh, // life-cycle assessment intensity
    lowCarbonPercent: Percentage,
    renewablePercent: Percentage
)

/** Electricity consumption data for a month. */
case class MonthlyElectricityConsumption(
    month: MonthYear,
    rawConsumption: Consumption.MWh,
    correctedConsumption: Consumption.MWh
)

/** Supply chains for electricity production. */
enum SupplyChain:
  case Fuel, Coal, Gas, Nuclear, Wind, Solar, Hydro, Bio

/** Electricity production data for a supply chain. */
case class ElectricityProductionPerSupplyChain(
    supplyChain: SupplyChain,
    production: Power.MW
)

/** Electricity production and consumption by hour, with details on the production by supply chain. */
case class HourlyElectricityProductionAndConsumption(
    dateTime: LocalDateTime,
    production: Seq[ElectricityProductionPerSupplyChain],
    consumption: Power.MW // Mean power consumed
)

/** Max necessary power to cover peak consumption and temprature, dayly */
case class DailyPowerPeakWithTemperature(
    date: LocalDate,
    powerPeak: Power.MW,
    meanTemperature: Temperature.Celsius,
    referenceTemperature: Temperature.Celsius
)
