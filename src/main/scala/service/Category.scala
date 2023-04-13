package server
package service

import sangria.macros.derive.GraphQLField
import slick.jdbc.MySQLProfile.api._

import model.{Category, Categories}

trait CategoryService {
  val categories = TableQuery[Categories]

  def findCategoryById(id: String) = {
    val query = categories.filter(_.category_id === id).result.headOption
    DB.db.run(query)
  }

  @GraphQLField
  def addCategory(name: String, description: String) = {
    val id = DB.generateUUID()
    val category = Category(id.toString(), name, description)
    val query = categories += category
    DB.db.run(query)
    category
  }
}
