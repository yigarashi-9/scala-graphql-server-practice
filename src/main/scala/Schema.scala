package server

import sangria.macros.derive._
import sangria.schema._

class CategoryRepo {
  private val categories = Seq(
    Category("1", "example", ""),
    Category("2", "example", ""),
  )

  def category(id: String) = categories.find(_.id == id)
}

object Schema {
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
}
