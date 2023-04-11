package server

trait Identifiable {
  def id: String
}

case class Category(id: String, name: String, description: String) extends Identifiable
