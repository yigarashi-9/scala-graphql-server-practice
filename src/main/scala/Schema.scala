package server

import sangria.macros.derive._
import sangria.schema._
import slick.jdbc.MySQLProfile.api._

object RecipeSchema {

  class CategoryRepo {
    val categories = TableQuery[Categories]

    def findCategoryById(id: String) = {
      val query = categories.filter(_.category_id === id).result.headOption
      DB.db.run(query)
    }

    @GraphQLField
    def addCategory(name: String, description: String) = {
      val category = Category("3", name, description)
      val query = categories += category
      DB.db.run(query)
      category
    }
  }

  case class MyCtx(categoryRepo: CategoryRepo)

  implicit val CategoryType = deriveObjectType[MyCtx, Category](
    ObjectTypeDescription("The recipe category"),
  )

  val Id = Argument("id", StringType)

  val MutationType = deriveContextObjectType[MyCtx, CategoryRepo, Unit](_.categoryRepo)

  val QueryType = ObjectType("Query", fields[MyCtx, Unit](
    Field(
      "category",
      OptionType(CategoryType),
      description = Some("Returns a product with specific `id`."),
      arguments = Id :: Nil,
      resolve = c => c.ctx.categoryRepo.findCategoryById(c arg Id)
    ),
  ))

  val schema = Schema[MyCtx, Unit](QueryType, Some(MutationType))
}
