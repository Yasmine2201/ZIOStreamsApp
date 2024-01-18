import zio.Chunk
import java.time.LocalDate

object CarbonIntensityAnalysis {
  def carbonIntensityGroupedByDay(data: LoadedData): Map[LocalDate, List[HourlyCarbonIntensity]] = {
    val carbonIntensityList: List[HourlyCarbonIntensity] = data.hourlyCarbonIntensity.toList
    val groupedByDay: Map[LocalDate, List[HourlyCarbonIntensity]] = carbonIntensityList.groupBy { ci =>
      ci.dateTime.toLocalDate
    }
    groupedByDay
  }

  def printCarbonIntensityGroupedByDay(data: LoadedData): Unit = {
    carbonIntensityGroupedByDay(data).foreach { case (date, intensityList) =>
      println(s"Date: $date")
      intensityList.foreach { intensity =>
        println(s"  Time: ${intensity.dateTime.toLocalTime}, Direct Intensity: ${intensity.directIntensity}, LCA Intensity: ${intensity.lcaIntensity}")

      }
      println()
    }
  }
}
