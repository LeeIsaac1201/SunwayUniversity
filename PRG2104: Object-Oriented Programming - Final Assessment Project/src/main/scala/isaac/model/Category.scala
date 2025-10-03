// Declares the package for domain model classes representing core nutrition data entities
package isaac.model

// Represents a classification for food items, such as fruits, vegetables, meats, etc.
sealed trait Category {
  def label: String
}

// Concrete categories for classifying food items into standardised groups
object Category {
  case object Fruit     extends Category { val label = "Fruit" }
  case object Vegetable extends Category { val label = "Vegetable" }
  case object Meat      extends Category { val label = "Meat" }
  case object Grain    extends Category { val label = "Grain" }
  case object Dairy     extends Category { val label = "Dairy" }
  case object Seafood   extends Category { val label = "Seafood" }
  case object Oil      extends Category { val label = "Oil" }
}
