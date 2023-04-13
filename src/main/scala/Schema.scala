package server

import sangria.macros.derive._
import sangria.schema._

trait MasterService extends service.CategoryService
object MasterService extends MasterService

trait GraphQLContext {
  val srv = MasterService
}
object GraphQLContext extends GraphQLContext

object RecipeSchema {
  val Id = Argument("id", StringType)
  implicit val CategoryType = deriveObjectType[GraphQLContext, model.Category](
    ObjectTypeDescription("The recipe category"),
  )

  val QueryType = ObjectType("Query", fields[GraphQLContext, Unit](
    Field(
      "category",
      OptionType(CategoryType),
      description = Some("Returns a product with specific `id`."),
      arguments = Id :: Nil,
      resolve = c => c.ctx.srv.findCategoryById(c arg Id)
    ),
  ))

  val MutationType = deriveContextObjectType[GraphQLContext, MasterService, Unit](_.srv)

  val schema = Schema[GraphQLContext, Unit](QueryType, Some(MutationType))
}
