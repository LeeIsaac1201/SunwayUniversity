// Declares the package for domain model classes representing core nutrition data entities
package isaac.model

import java.util.UUID

/**
 * Represents a recipe composed of food ingredients and preparation instructions.
 *
 * @param id Unique identifier for the recipe
 * @param name Name of the recipe
 * @param ingredients Map of FoodItem → grams used
 * @param instructions Ordered list of preparation steps
 */
case class Recipe(
                   id: UUID,
                   name: String,
                   ingredients: Map[FoodItem, Double], // measured in grams
                   instructions: List[String]
                 ) {

  /**
   * Aggregates the total nutrients from all ingredients.
   * Nutrient amounts are scaled proportionally to the weight used in the recipe.
   *
   * @return Combined list of nutrients across all ingredients, grouped and summed by name.
   */
  def totalNutrients: Seq[Nutrient] = {
    ingredients.toSeq
      .flatMap { case (foodItem, gramsUsed) =>
        val scale = gramsUsed / foodItem.servingSize
        foodItem.nutrients.map {
          case m: Macronutrient => Macronutrient(m.name, m.amount * scale)
          case m: Micronutrient => Micronutrient(m.name, m.amount * scale, m.unit)
          case f: Fibre         => Fibre(f.amount * scale)
          case s: Sugar         => Sugar(s.amount * scale)
        }
      }
      .groupBy(_.name)
      .map { case (name, nutrients) =>
        val totalAmount = nutrients.map(_.amount).sum
        nutrients.head match {
          case m: Macronutrient => Macronutrient(name, totalAmount)
          case m: Micronutrient => Micronutrient(name, totalAmount, m.unit)
          case _: Fibre         => Fibre(totalAmount)
          case _: Sugar         => Sugar(totalAmount)
        }
      }
      .toSeq
  }

  /**
   * Finds a nutrient by name in the total nutrient list.
   *
   * @param nutrientName Name of the nutrient to find
   * @return Option containing the nutrient if found, otherwise None
   */
  def findNutrient(nutrientName: String): Option[Nutrient] = {
    totalNutrients.find(_.name.equalsIgnoreCase(nutrientName))
  }
}
