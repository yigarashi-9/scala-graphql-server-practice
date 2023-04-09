package server

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = ServerServer.run[IO]
}
