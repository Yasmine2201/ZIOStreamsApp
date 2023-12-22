import zio.*

object Main extends ZIOAppDefault {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Unit] = {
    for {
      url <- ZIO.succeed(loadCsv("conso_brute_corrigee_client_direct.csv"))

    } yield ()
  }
}