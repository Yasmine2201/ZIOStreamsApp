import zio.Chunk
import java.time.LocalDate

final case class CarbonIntensityAnalysis(analysisModule: AnalysisModule) {
  def carbonIntensityGroupedByDay: Map[LocalDate, List[HourlyCarbonIntensity]] = {
    val carbonIntensityList: List[HourlyCarbonIntensity] = analysisModule.hourlyCarbonIntensity.toList
    val groupedByDay: Map[LocalDate, List[HourlyCarbonIntensity]] = carbonIntensityList.groupBy { ci =>
      ci.dateTime.toLocalDate
    }
    groupedByDay
  }

  def printCarbonIntensityGroupedByDay: Unit = {
    carbonIntensityGroupedByDay.foreach { case (date, intensityList) =>
      println(s"Date: $date")
      intensityList.foreach { intensity =>
        println(s"  Time: ${intensity.dateTime.toLocalTime}, Direct Intensity: ${intensity.directIntensity}, LCA Intensity: ${intensity.lcaIntensity}")

      }
      println()
    }
  }
}
