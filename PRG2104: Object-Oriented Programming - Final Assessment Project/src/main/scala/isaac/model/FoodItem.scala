package isaac.model

// Represents a food item with its nutritional information
case class FoodItem(
                     name: String,
                     category: Category,
                     servingSize: Double, // Standard serving size in grams
                     nutrients: List[Nutrient],
                     imagePath: Option[String] = None // Optional path to an image
                   ) {

  // Returns the amount of a specific nutrient by name, if available
  def getNutrientAmount(nutrientName: String): Option[Double] =
    nutrients.find(_.name.equalsIgnoreCase(nutrientName)).map(_.amount)

  // Returns a summary string of the food item's nutrient contents
  def nutrientSummary: String =
    nutrients.map(n => s"${n.name}: ${n.amount} ${n.unit}").mkString(", ")

  // Converts the food item to a pipe-delimited line suitable for text-based storage
  def toLine: String = {
    val nutrientStr = nutrients.map(Nutrient.format).mkString(", ")
    val imageStr = imagePath.getOrElse("")
    s"$name|${category.label}|$servingSize|$nutrientStr|$imageStr"
  }
}
