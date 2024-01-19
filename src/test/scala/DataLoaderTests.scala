import zio.test._
import zio.test.Assertion._
import zio.Chunk

object DataLoaderSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] = suite("DataLoader")(
    test("loadCarbonIntensity should load carbon intensity per hour data") {
      for (result <- DataLoader.loadCarbonIntensity)
        yield assert(result)(isSubtype[Chunk[HourlyCarbonIntensity]](anything))
    },
    test("loadCarbonIntensityFromUrl should load carbon intensity per hour data") {
      for (result <- DataLoader.loadCarbonIntensityFromFileName("carbon-intensity.csv"))
        yield assert(result)(isSubtype[Chunk[HourlyCarbonIntensity]](anything))
    },
    test("loadCarbonIntensityFromUrl should load right number of entries") {
      for (result <- DataLoader.loadCarbonIntensityFromFileName("FR_2021_hourly.csv"))
        yield assert(result.size)(equalTo(8760))
    },
    test("loadEcoMix should load eco mix data") {
      for (result <- DataLoader.loadEcoMix)
        yield assert(result)(isSubtype[Chunk[HourlyElectricityProductionAndConsumption]](anything))
    },
    test("loadEcoMixFromUrl should load eco mix data") {
      for (result <- DataLoader.loadEcoMixFromFileName("ecomix.csv"))
        yield assert(result)(isSubtype[Chunk[HourlyElectricityProductionAndConsumption]](anything))
    },
    test("loadEcoMixFromUrl should load right number of entries") {
      for (result <- DataLoader.loadEcoMixFromFileName("ecomix.csv"))
        yield assert(result.size)(equalTo(10))
    },
    test("loadRawConsos should load raw consos data") {
      for (result <- DataLoader.loadRawConsos)
        yield assert(result)(isSubtype[Chunk[MonthlyElectricityConsumption]](anything))
    },
    test("loadRawConsosFromUrl should load raw consos data") {
      for (result <- DataLoader.loadRawConsosFromFileName("conso-brute.csv"))
        yield assert(result)(isSubtype[Chunk[MonthlyElectricityConsumption]](anything))
    },
    test("loadRawConsosFromUrl should load right number of entries") {
      for (result <- DataLoader.loadRawConsosFromFileName("conso-brute.csv"))
        yield assert(result.size)(equalTo(10))
    },
    test("loadPeakConso should load peak conso data") {
      for (result <- DataLoader.loadPeakConso)
        yield assert(result)(isSubtype[Chunk[DailyPowerPeakWithTemperature]](anything))
    },
    test("loadPeakConsoFromUrl should load peak conso data") {
      for (result <- DataLoader.loadPeakConsoFromFileName("pic-conso.csv"))
        yield assert(result)(isSubtype[Chunk[DailyPowerPeakWithTemperature]](anything))
    },
    test("loadPeakConsoFromUrl should load right number of entries") {
      for (result <- DataLoader.loadPeakConsoFromFileName("pic-conso.csv"))
        yield assert(result.size)(equalTo(18))
    }
  )
}
