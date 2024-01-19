import zio.test._
import zio.test.Assertion._
import zio.Chunk
import java.time.LocalDate

object PowerPeakAndTemperatureSpec extends ZIOSpecDefault {

  val testYear1 = Chunk(
    DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 1), 1.0f, 10.0, 10.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 2), 2.0f, 9.0f, 9.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 3), 3.0f, 8.0f, 8.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 4), 4.0f, 7.0f, 7.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 5), 5.0f, 6.0f, 6.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 6), 6.0f, 5.0f, 5.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 7), 7.0f, 4.0f, 4.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 8), 8.0f, 3.0f, 3.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 9), 9.0f, 2.0f, 2.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 10), 10.0f, 1.0f, 1.0f)
  )

  val testYear2 = Chunk(
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 1), 5.0f, 20.0, 20.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 2), 6.0f, 19.0f, 19.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 3), 7.0f, 18.0f, 18.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 4), 8.0f, 17.0f, 17.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 5), 9.0f, 16.0f, 16.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 6), 10.0f, 15.0f, 15.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 7), 11.0f, 14.0f, 14.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 8), 12.0f, 13.0f, 13.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 9), 13.0f, 12.0f, 12.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 10), 14.0f, 11.0f, 11.0f),
    DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 11), 15.0f, 10.0f, 10.0f)
  )

  val totalTestData = testYear1 ++ testYear2

  def spec: Spec[TestEnvironment, Any] = suite("PowerPeakAndTemperature")(
    test("maxPowerPeakAndMinTemperatureOfYear should return correct result") {
      val result = PowerTemperatureAnalysis.maxPowerPeakAndMinTemperatureOfYear(testYear1)
      assert(result)(equalTo((DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 10), 10.0f, 1.0f, 1.0f), DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 10), 10.0f, 1.0f, 1.0f))))
    },
    test("powerPeakTemperatureGroupedByYear should return correct result") {
      val data   = LoadedData(Chunk.empty, Chunk.empty, Chunk.empty, totalTestData)
      val result = PowerTemperatureAnalysis.powerPeakTemperatureGroupedByYear(data)
      assert(result)(equalTo(Map(2021 -> testYear1, 2022 -> testYear2)))
    },
    test("maxPowerPeaksAndMinTemperaturesByYear should return correct result") {
      val data   = LoadedData(Chunk.empty, Chunk.empty, Chunk.empty, totalTestData)
      val result = PowerTemperatureAnalysis.maxPowerPeaksAndMinTemperaturesByYear(data)
      assert(result)(
        equalTo(
          Chunk(
            (2021, DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 10), 10.0f, 1.0f, 1.0f), DailyPowerPeakWithTemperature(LocalDate.of(2021, 1, 10), 10.0f, 1.0f, 1.0f)),
            (2022, DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 11), 15.0f, 10.0f, 10.0f), DailyPowerPeakWithTemperature(LocalDate.of(2022, 1, 11), 15.0f, 10.0f, 10.0f))
          )
        )
      )
    }
  )
}
