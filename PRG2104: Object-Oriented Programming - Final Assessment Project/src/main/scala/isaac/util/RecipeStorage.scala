// Updated RecipeStorage.scala
package isaac.util

import isaac.model.{FoodItem, Recipe}

import java.io.{BufferedWriter, File, FileWriter, PrintWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Try

object RecipeStorage {

  // Base data directory
  private val DATA_DIR = "data"
  private val RECIPES_DIR = s"$DATA_DIR/recipes"

  // Ensure the recipes directory exists
  private def ensureRecipesDirectoryExists(): Unit = {
    val recipesPath = Paths.get(RECIPES_DIR)
    if (!Files.exists(recipesPath)) {
      try {
        Files.createDirectories(recipesPath)
        println(s"[INFO] Created recipes directory: $RECIPES_DIR")
      } catch {
        case ex: Exception =>
          println(s"[ERROR] Failed to create recipes directory: ${ex.getMessage}")
          throw ex
      }
    }
  }

  // Sanitize recipe name for filename
  private def sanitizeFileName(recipeName: String): String = {
    val sanitized = recipeName
      .toLowerCase
      .replaceAll("[^a-zA-Z0-9\\s-]", "") // Remove special characters except spaces and hyphens
      .replaceAll("\\s+", "-") // Replace spaces with hyphens
      .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
      .stripPrefix("-").stripSuffix("-") // Remove leading/trailing hyphens

    if (sanitized.isEmpty) {
      // Fallback to UUID if name becomes empty after sanitization
      s"recipe-${UUID.randomUUID().toString.take(8)}"
    } else {
      s"recipe-$sanitized"
    }
  }

  // Get the file path for a recipe
  private def getRecipeFilePath(recipe: Recipe): Path = {
    ensureRecipesDirectoryExists()
    val fileName = sanitizeFileName(recipe.name) + ".txt"
    Paths.get(RECIPES_DIR, fileName)
  }

  // Parse ingredients from text lines
  private def parseIngredients(lines: List[String]): Map[FoodItem, Double] = {
    val ingredients = collection.mutable.Map[FoodItem, Double]()

    lines.foreach { line =>
      val parts = line.split("\\|")
      if (parts.length >= 3) {
        val foodName = parts(0).trim
        val quantity = Try(parts(1).trim.toDouble).getOrElse(0.0)
        val unit = parts(2).trim

        // Look up the food item from FoodItemStorage
        // For now, create a basic FoodItem - you may want to enhance this
        // to actually look up from your food database
        try {
          val foodItems = FoodItemStorage.loadAll()
          val matchingFood = foodItems.find(_.name.equalsIgnoreCase(foodName))

          matchingFood match {
            case Some(food) => ingredients += (food -> quantity)
            case None =>
              println(s"[WARNING] Food item '$foodName' not found in database, skipping")
          }
        } catch {
          case ex: Exception =>
            println(s"[ERROR] Failed to look up food item '$foodName': ${ex.getMessage}")
        }
      }
    }

    ingredients.toMap
  }

  // Parse instructions from text lines
  private def parseInstructions(lines: List[String]): List[String] = {
    lines.map(_.replaceFirst("^\\d+\\.\\s*", "")).filter(_.nonEmpty) // Remove step numbers
  }

  // Save a recipe to individual file
  def save(recipe: Recipe): Unit = {
    try {
      val filePath = getRecipeFilePath(recipe)
      val writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath.toFile, StandardCharsets.UTF_8)))

      try {
        // Write recipe header
        writer.println(s"RECIPE: ${recipe.name}")
        writer.println(s"ID: ${recipe.id}")
        writer.println()

        // Write ingredients
        writer.println("INGREDIENTS:")
        recipe.ingredients.foreach { case (food, quantity) =>
          writer.println(s"${food.name}|$quantity|g")
        }
        writer.println()

        // Write instructions
        writer.println("INSTRUCTIONS:")
        recipe.instructions.zipWithIndex.foreach { case (instruction, index) =>
          writer.println(s"${index + 1}. $instruction")
        }
        writer.println()

        // Write nutrition summary
        writer.println("NUTRITION_SUMMARY:")
        val totalNutrients = recipe.totalNutrients
        totalNutrients.foreach { nutrient =>
          writer.println(s"${nutrient.name}: ${formatDouble(nutrient.amount)} ${nutrient.unit}")
        }

        println(s"[INFO] Recipe '${recipe.name}' saved to: ${filePath.toAbsolutePath}")

      } finally {
        writer.close()
      }

    } catch {
      case ex: Exception =>
        println(s"[ERROR] Failed to save recipe '${recipe.name}': ${ex.getMessage}")
        ex.printStackTrace()
        throw ex
    }
  }

  // Load a single recipe from file
  def loadRecipe(filePath: Path): Option[Recipe] = {
    if (!Files.exists(filePath)) {
      println(s"[WARNING] Recipe file does not exist: $filePath")
      return None
    }

    try {
      val lines = Source.fromFile(filePath.toFile, "UTF-8").getLines().toList
      parseRecipeFromLines(lines)
    } catch {
      case ex: Exception =>
        println(s"[ERROR] Failed to load recipe from $filePath: ${ex.getMessage}")
        None
    }
  }

  // Parse recipe from file lines
  private def parseRecipeFromLines(lines: List[String]): Option[Recipe] = {
    try {
      var recipeName = ""
      var recipeId: UUID = UUID.randomUUID()
      var currentSection = ""
      val ingredientLines = ListBuffer[String]()
      val instructionLines = ListBuffer[String]()

      lines.foreach { line =>
        val trimmedLine = line.trim

        if (trimmedLine.startsWith("RECIPE:")) {
          recipeName = trimmedLine.substring(7).trim
        } else if (trimmedLine.startsWith("ID:")) {
          try {
            recipeId = UUID.fromString(trimmedLine.substring(3).trim)
          } catch {
            case _: IllegalArgumentException =>
              recipeId = UUID.randomUUID() // Generate new UUID if invalid
          }
        } else if (trimmedLine == "INGREDIENTS:") {
          currentSection = "ingredients"
        } else if (trimmedLine == "INSTRUCTIONS:") {
          currentSection = "instructions"
        } else if (trimmedLine == "NUTRITION_SUMMARY:") {
          currentSection = "nutrition" // We'll ignore this section as we calculate it dynamically
        } else if (trimmedLine.nonEmpty && !trimmedLine.startsWith("NUTRITION_SUMMARY:")) {
          currentSection match {
            case "ingredients" => ingredientLines += trimmedLine
            case "instructions" => instructionLines += trimmedLine
            case _ => // Ignore other sections
          }
        }
      }

      if (recipeName.nonEmpty) {
        val ingredients = parseIngredients(ingredientLines.toList)
        val instructions = parseInstructions(instructionLines.toList)

        Some(Recipe(recipeId, recipeName, ingredients, instructions))
      } else {
        println("[ERROR] Recipe name not found in file")
        None
      }

    } catch {
      case ex: Exception =>
        println(s"[ERROR] Failed to parse recipe: ${ex.getMessage}")
        ex.printStackTrace()
        None
    }
  }

  // Load all recipes from the recipes directory
  def loadAll(): List[Recipe] = {
    ensureRecipesDirectoryExists()

    try {
      val recipesDir = new File(RECIPES_DIR)
      if (!recipesDir.exists() || !recipesDir.isDirectory) {
        println(s"[WARNING] Recipes directory does not exist: $RECIPES_DIR")
        return List.empty
      }

      val recipeFiles = recipesDir.listFiles()
        .filter(_.isFile)
        .filter(_.getName.toLowerCase.endsWith(".txt"))
        .toList

      println(s"[INFO] Found ${recipeFiles.length} recipe files")

      val recipes = recipeFiles.flatMap { file =>
        loadRecipe(file.toPath)
      }

      println(s"[INFO] Successfully loaded ${recipes.length} recipes")
      recipes.sortBy(_.name) // Sort alphabetically by name

    } catch {
      case ex: Exception =>
        println(s"[ERROR] Failed to load recipes: ${ex.getMessage}")
        ex.printStackTrace()
        List.empty
    }
  }

  // Delete a recipe file
  def delete(recipe: Recipe): Boolean = {
    try {
      val filePath = getRecipeFilePath(recipe)
      if (Files.exists(filePath)) {
        Files.delete(filePath)
        println(s"[INFO] Deleted recipe file: ${filePath.toAbsolutePath}")
        true
      } else {
        println(s"[WARNING] Recipe file not found for deletion: ${filePath.toAbsolutePath}")
        false
      }
    } catch {
      case ex: Exception =>
        println(s"[ERROR] Failed to delete recipe '${recipe.name}': ${ex.getMessage}")
        false
    }
  }

  // Check if a recipe exists
  def exists(recipe: Recipe): Boolean = {
    val filePath = getRecipeFilePath(recipe)
    Files.exists(filePath)
  }

  // Get list of all recipe names (for Recipe List tab)
  def getRecipeNames(): List[String] = {
    loadAll().map(_.name)
  }

  // Load recipe by name
  def loadByName(name: String): Option[Recipe] = {
    loadAll().find(_.name.equalsIgnoreCase(name))
  }

  // Helper method to format doubles
  private def formatDouble(d: Double): String = {
    val rounded = Math.round(d * 100.0) / 100.0
    if (rounded % 1 == 0) rounded.toInt.toString else f"$rounded%.2f"
  }

  // Get recipes directory path (useful for UI)
  def getRecipesDirectory(): String = RECIPES_DIR

  // Get recipe count
  def getRecipeCount(): Int = {
    try {
      ensureRecipesDirectoryExists()
      val recipesDir = new File(RECIPES_DIR)
      if (recipesDir.exists() && recipesDir.isDirectory) {
        recipesDir.listFiles().count(f => f.isFile && f.getName.toLowerCase.endsWith(".txt"))
      } else {
        0
      }
    } catch {
      case _: Exception => 0
    }
  }
}
