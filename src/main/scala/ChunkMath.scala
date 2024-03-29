import zio.Chunk
import math.Fractional.Implicits.infixFractionalOps

/** A collection of functions for calculating statistics on a chunk of data.
  */
object ChunkMath {

  /** Calculates the average of a chunk of data.
    *
    * @param chunk
    *   the chunk of data
    * @param F
    *   the fractional type class
    * @return
    *   the average of the chunk
    */
  def average[A](chunk: Chunk[A])(using F: Fractional[A]): A =
    chunk.sum / F.fromInt(chunk.size)

  /** Calculates the variance of a chunk of data.
    *
    * @param chunk
    *   the chunk of data
    * @param F
    *   the fractional type class
    * @return
    *   the variance of the chunk
    */
  def variance[A](chunk: Chunk[A])(using F: Fractional[A]): A = {
    val mean = average(chunk)
    val n    = F.fromInt(chunk.size)

    val sumOfSquares = chunk.map { x =>
      (x - mean) * (x - mean)
    }.sum

    sumOfSquares / n
  }

  /** Calculates the standard deviation of a chunk of data.
    *
    * @param chunk
    *   the chunk of data
    * @param F
    *   the fractional type class
    * @return
    *   the standard deviation of the chunk
    */
  def standardDeviation[A](chunk: Chunk[A])(using F: Fractional[A]): Double =
    math.sqrt(F.toDouble(variance(chunk)))

  /** Calculates the linear correlation coefficient of two chunks of data using the Pearson correlation coefficient. The two chunks must be of the same size.
    *
    * @param chunkX
    *   the first chunk of data
    * @param chunkY
    *   the second chunk of data
    * @param F
    *   the fractional type class
    * @return
    *   the linear correlation coefficient of the two chunks of data
    */
  def linearCorrelationCoefficient[A](chunkX: Chunk[A], chunkY: Chunk[A])(using F: Fractional[A]): Double = {
    assert(chunkX.size == chunkY.size)

    val n = F.fromInt(chunkX.size)
    val sumOfProducts = chunkX
      .zip(chunkY)
      .map { case (x, y) =>
        x * y
      }
      .sum

    val xAverage = average(chunkX);
    val yAverage = average(chunkY);

    val numerator   = (sumOfProducts - (n * xAverage * yAverage)).toDouble
    val denominator = n.toDouble * standardDeviation(chunkX) * standardDeviation(chunkY)

    if (denominator != 0) numerator / denominator else 0
  }

  extension [A](chunk: Chunk[A]) {

    /** Calculates the average of a the values returned by a function applied to each element of the chunk.
      *
      * @param B
      *   the type of the values returned by the function
      * @param f
      *   the function to apply to each element of the chunk
      * @param F
      *   the fractional type class
      * @return
      *   the average of the values returned by the function applied to each element of the chunk
      */
    def averageBy[B](f: A => B)(using F: Fractional[B]): B =
      average(chunk.map(f))

    /** Calculates the variance of a the values returned by a function applied to each element of the chunk.
      *
      * @param B
      *   the type of the values returned by the function
      * @param f
      *   the function to apply to each element of the chunk
      * @param F
      *   the fractional type class
      * @return
      *   the variance of the values returned by the function applied to each element of the chunk
      */
    def varianceBy[B](f: A => B)(using F: Fractional[B]): B =
      variance(chunk.map(f))

    /** Calculates the standard deviation of a the values returned by a function applied to each element of the chunk.
      *
      * @param B
      *   the type of the values returned by the function
      * @param f
      *   the function to apply to each element of the chunk
      * @param F
      *   the fractional type class
      * @return
      *   the standard deviation of the values returned by the function applied to each element of the chunk
      */
    def standardDeviationBy[B](f: A => B)(using F: Fractional[B]): Double =
      standardDeviation(chunk.map(f))
  }
}
