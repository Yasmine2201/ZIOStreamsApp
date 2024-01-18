import zio.Chunk
import math.Fractional.Implicits.infixFractionalOps

object ChunkMath {

  def average[A](chunk: Chunk[A])(using F: Fractional[A]): A =
    chunk.sum / F.fromInt(chunk.size)

  def variance[A](chunk: Chunk[A])(using F: Fractional[A]): A = {
    val mean = average(chunk)
    val n    = F.fromInt(chunk.size)

    val sumOfSquares = chunk.map { x =>
      (x - mean) * (x - mean)
    }.sum

    sumOfSquares / n
  }

  def standardDeviation[A](chunk: Chunk[A])(using F: Fractional[A]): Double =
    math.sqrt(F.toDouble(variance(chunk)))

  extension [A](chunk: Chunk[A]) {
    def averageBy[B](f: A => B)(using F: Fractional[B]): B =
      average(chunk.map(f))

    def varianceBy[B](f: A => B)(using F: Fractional[B]): B =
      variance(chunk.map(f))

    def standardDeviationBy[B](f: A => B)(using F: Fractional[B]): Double =
      standardDeviation(chunk.map(f))
  }
}
