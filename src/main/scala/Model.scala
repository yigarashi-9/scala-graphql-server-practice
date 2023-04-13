package server

import slick.jdbc.MySQLProfile.api._

trait Identifiable {
  def id: String
}

case class Category(id: String, name: String, description: String) extends Identifiable
class Categories(tag: Tag) extends Table[Category](tag, "category") {
  def category_id = column[String]("category_id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def description = column[String]("description")

  def * = (category_id, name, description) <> (Category.tupled, Category.unapply)
}
