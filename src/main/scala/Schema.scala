package server

import sangria.macros.derive._
import sangria.schema._
import slick.jdbc.MySQLProfile.api._

class CategoryRepo {
  val categories = TableQuery[Categories]
  def category(id: String) = {
    val query = categories.filter(_.category_id === id).result.headOption
    DB.db.run(query)
  }
}

object RecipeSchema {
  val CategoryType = deriveObjectType[Unit, Category](
    ObjectTypeDescription("The recipe category"),
  )

  val Id = Argument("id", StringType)

  val QueryType = ObjectType("Query", fields[CategoryRepo, Unit](
    Field(
      "category",
      OptionType(CategoryType),
      description = Some("Returns a product with specific `id`."),
      arguments = Id :: Nil,
      resolve = c => c.ctx.category(c arg Id)
    ),
  ))

  val schema = Schema(QueryType)
}
