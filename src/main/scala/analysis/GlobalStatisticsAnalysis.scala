import zio.Chunk
import AnalysisModule.*
import Types.*
import java.time.LocalDate

extension [A](chunk: Chunk[A]) {
  def averageBy[B](f: A => B)(using n: Fractional[B]): B =
    n.div(chunk.map(f).sum, n.fromInt(chunk.size))
}

final case class GlobalStatisticsAnalysis(analysisModule: AnalysisModule) {}
