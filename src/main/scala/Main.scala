import zio._
import zio.Console._

import DataLoader._
import UI._
import java.time.LocalDate

object Main extends ZIOAppDefault {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Unit] = {
    for {
      data <- loadData
      _    <- printLine(formatPowerPeakAndTemperature(data))
    } yield ()
  }
}
