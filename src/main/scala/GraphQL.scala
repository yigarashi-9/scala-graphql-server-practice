package server

import sangria.execution._
import sangria.execution.WithViolations
import sangria.marshalling.circe._
import sangria.parser.{ QueryParser, SyntaxError }
import sangria.validation._
import cats.effect._
import cats.implicits._
import io.circe.{ Json, JsonObject }
import io.circe.optics.JsonPath.root
import scala.util.{ Success, Failure }

trait GraphQL[F[_]]{
  def query(request: Json): F[Either[Json, Json]]
}

object GraphQL {
  // Some circe lenses
  private val queryStringLens   = root.query.string
  private val operationNameLens = root.operationName.string
  private val variablesLens     = root.variables.obj

  // Format a SyntaxError as a GraphQL `errors`
  private def formatSyntaxError(e: SyntaxError): Json = Json.obj(
    "errors" -> Json.arr(Json.obj(
      "message"   -> Json.fromString(e.getMessage),
      "locations" -> Json.arr(Json.obj(
        "line"   -> Json.fromInt(e.originalError.position.line),
        "column" -> Json.fromInt(e.originalError.position.column))))))

  // Format a WithViolations as a GraphQL `errors`
  private def formatWithViolations(e: WithViolations): Json = Json.obj(
    "errors" -> Json.fromValues(e.violations.map {
      case v: AstNodeViolation => Json.obj(
        "message"   -> Json.fromString(v.errorMessage),
        "locations" -> Json.fromValues(v.locations.map(loc => Json.obj(
          "line"   -> Json.fromInt(loc.line),
          "column" -> Json.fromInt(loc.column)))))
      case v => Json.obj(
        "message" -> Json.fromString(v.errorMessage))}))

  // Format a String as a GraphQL `errors`
  private def formatString(s: String): Json = Json.obj(
    "errors" -> Json.arr(Json.obj(
      "message" -> Json.fromString(s))))

  // Format a Throwable as a GraphQL `errors`
  private def formatThrowable(e: Throwable): Json = Json.obj(
    "errors" -> Json.arr(Json.obj(
      "class"   -> Json.fromString(e.getClass.getName),
      "message" -> Json.fromString(e.getMessage))))

  def impl[F[_]](implicit F: Async[F]): GraphQL[F] = new GraphQL[F]{
    def query(request: Json): F[Either[Json, Json]] = {
        val queryString   = queryStringLens.getOption(request)
        val operationName = operationNameLens.getOption(request)
        val variables     = variablesLens.getOption(request).getOrElse(JsonObject())

        queryString match {
          case Some(qs) => query(qs, operationName, variables)
          case None     => fail(formatString("No 'query' property was present in the request."))
        }
    }

    // Parse `query` and execute.
    def query(query: String, operationName: Option[String], variables: JsonObject): F[Either[Json, Json]] =
      QueryParser.parse(query)  match {
        case Success(ast) => {
          implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
          F.async_[Json] { (cb: Either[Throwable, Json] => Unit) =>
            Executor.execute[GraphQLContext, Unit, Json](
              schema = RecipeSchema.schema,
              queryAst = ast,
              variables = Json.fromJsonObject(variables),
              operationName = operationName,
              userContext = GraphQLContext,
            ).onComplete {
              case Success(value) => cb(Right(value))
              case Failure(error) => cb(Left(error))
            }
          } .attempt.flatMap {
            case Right(json)               => F.pure(json.asRight)
            case Left(err: WithViolations) => fail(formatWithViolations(err))
            case Left(err)                 => fail(formatThrowable(err))
          }
        }
        case Failure(e @ SyntaxError(_, _, _)) => fail(formatSyntaxError(e))
        case Failure(e)                         => fail(formatThrowable(e))
      }

    // Lift a `Json` into the error side of our effect.
    def fail(j: Json): F[Either[Json, Json]] = F.pure(j.asLeft)
  }
}
