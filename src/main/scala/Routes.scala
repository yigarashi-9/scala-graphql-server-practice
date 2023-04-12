package server

import io.circe.Json
import cats.effect.Async
import cats.implicits._
import org.http4s.circe._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object Routes {
  def graphqlRoutes[F[_]: Async](graphQL: GraphQL[F]): HttpRoutes[F] = {
    object dsl extends Http4sDsl[F]; import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "graphql" =>
        req.as[Json].flatMap(graphQL.query).flatMap {
          case Right(json) => Ok(json)
          case Left(json)  => BadRequest(json)
        }
    }
  }
}
