import zio._
import zio.Console._

import DataLoader._
import UI._

object Main extends ZIOAppDefault {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Unit] = {
    for {
      data <- loadData
      _    <- consoleLoop(data)
    } yield ()
  }
}
