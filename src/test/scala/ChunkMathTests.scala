import zio.test._
import zio.test.Assertion._
import zio.Chunk
import ChunkMath.*

object ChunkMathSpec extends ZIOSpecDefault {
  case class Foo(a: Float, b: Float)

  def spec: Spec[TestEnvironment, Any] = suite("ChunkMath")(
    test("average") {
      val chunk   = Chunk(1f, 2f, 3f, 4f, 5f)
      val average = ChunkMath.average(chunk)
      assert(average)(equalTo(3))
    },
    test("variance") {
      val chunk = Chunk(1f, 2f, 3f, 4f, 5f)
      assert(ChunkMath.variance(chunk))(equalTo(2))
    },
    test("standardDeviation") {
      val chunk = Chunk(1f, 2f, 3f, 4f, 5f)
      assert(ChunkMath.standardDeviation(chunk))(equalTo(math.sqrt(2)))
    },
    test("averageBy") {
      val chunk = Chunk(Foo(1f, 0f), Foo(2f, 0f), Foo(3f, 0f), Foo(4f, 0f), Foo(5f, 0f))
      assert(chunk.averageBy(foo => foo.a))(equalTo(3))
    },
    test("varianceBy") {
      val chunk = Chunk(Foo(1f, 0f), Foo(2f, 0f), Foo(3f, 0f), Foo(4f, 0f), Foo(5f, 0f))
      assert(chunk.varianceBy(foo => foo.a))(equalTo(2))
    },
    test("standardDeviationBy") {
      val chunk = Chunk(Foo(1f, 0f), Foo(2f, 0f), Foo(3f, 0f), Foo(4f, 0f), Foo(5f, 0f))
      assert(chunk.standardDeviationBy(foo => foo.a))(equalTo(math.sqrt(2)))
    }
  )
}
