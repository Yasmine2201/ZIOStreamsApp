import zio.test._
import zio.test.Assertion._
import zio.Chunk
import GlobalStatisticsAnalysis._
import java.time.LocalDate

object GlobalStatsSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] = suite("GlobalStatistics")(
    test("getFieldsStatistics should return empty list if no data in period") {
      val data = Chunk(
        (LocalDate.of(2021, 1, 1), 1.0f),
        (LocalDate.of(2021, 1, 2), 2.0f)
      )
      val fields = List(
        FieldSelector("fieldName", (x: (LocalDate, Float)) => x._2)
      )
      val start  = LocalDate.of(2022, 1, 1)
      val end    = LocalDate.of(2022, 12, 31)
      val result = GlobalStatisticsAnalysis.getFieldsStatistics(data, fields, (x: (LocalDate, Float)) => x._1, start, end)
      assert(result)(equalTo(Nil))
    },
    test("getFieldsStatistics should return correct statistics") {
      val data = Chunk(
        (LocalDate.of(2021, 1, 1), 1.0f),
        (LocalDate.of(2021, 1, 2), 2.0f)
      )
      val fields = List(
        FieldSelector("fieldName", (x: (LocalDate, Float)) => x._2)
      )
      val start  = LocalDate.of(2021, 1, 1)
      val end    = LocalDate.of(2021, 1, 2)
      val result = GlobalStatisticsAnalysis.getFieldsStatistics(data, fields, (x: (LocalDate, Float)) => x._1, start, end)
      assert(result)(equalTo(List(Statistics("fieldName", 1.5, 0.5, 1.0, 2.0, 2))))
    },
    test("getFieldsStatistics should return empty list if empty data") {
      val data = Chunk.empty
      val fields = List(
        FieldSelector("fieldName", (x: (LocalDate, Float)) => x._2)
      )
      val start  = LocalDate.of(2021, 1, 1)
      val end    = LocalDate.of(2021, 1, 2)
      val result = GlobalStatisticsAnalysis.getFieldsStatistics(data, fields, (x: (LocalDate, Float)) => x._1, start, end)
      assert(result)(equalTo(Nil))
    }
  )
}
