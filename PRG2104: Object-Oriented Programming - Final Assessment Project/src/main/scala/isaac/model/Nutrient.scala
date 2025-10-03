// Declares the package for domain model classes representing core nutrition data entities
package isaac.model

// Represents the base interface for any nutrient component
trait Nutrient {
  def name: String
  def unit: String
  def amount: Double

  /** Render exactly `<Name>:<Amount><Unit>` with no extra spaces */
  def toLine: String
}

// Represents a macronutrient (e.g., protein, fat, carbohydrates), measured in grams
case class Macronutrient(
                          name: String,
                          amount: Double
                        ) extends Nutrient {
  val unit: String = "g"
  override def toLine: String = f"$name:${amount}%.1f$unit"
}

// Represents a micronutrient (e.g., vitamins, minerals), measured in milligrams or micrograms
case class Micronutrient(
                          name: String,
                          amount: Double,
                          unit: String
                        ) extends Nutrient {
  override def toLine: String = f"$name:${amount}%.1f$unit"
}

// Represents dietary fibre, measured in grams
case class Fibre(
                  amount: Double
                ) extends Nutrient {
  val name: String = "Fibre"
  val unit: String = "g"
  override def toLine: String = f"$name:${amount}%.1f$unit"
}

// Represents sugars, measured in grams
case class Sugar(
                  amount: Double
                ) extends Nutrient {
  val name: String = "Sugar"
  val unit: String = "g"
  override def toLine: String = f"$name:${amount}%.1f$unit"
}

// Provides utility methods for Nutrient serialization
object Nutrient {

  /**
   * Formats a Nutrient into a descriptive string, including type tag, name, amount, and unit.
   * Examples:
   *   Macronutrient: Protein: 0.3 g
   *   Micronutrient: Vitamin C: 12.0 mg
   *   Fibre: 1.0 g
   */
  def format(n: Nutrient): String = n match {
    case m: Macronutrient =>
      s"Macronutrient: ${m.name}: ${m.amount} ${m.unit}"
    case m: Micronutrient =>
      s"Micronutrient: ${m.name}: ${m.amount} ${m.unit}"
    case f: Fibre =>
      s"Fibre: ${f.amount} ${f.unit}"
    case s: Sugar =>
      s"Sugar: ${s.amount} ${s.unit}"
    case other =>
      // Provides a fallback serialization for any additional nutrient types
      s"${other.name}: ${other.amount} ${other.unit}"
  }
}
