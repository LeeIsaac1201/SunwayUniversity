package isaac.util

import isaac.model._
import java.io.IOException
import java.nio.file.{Files, Paths, Path, StandardOpenOption, StandardCopyOption}
import scala.io.Source
import scala.util.Try
import scala.jdk.CollectionConverters._

object FoodItemStorage {

  private val folderPath: Path = Paths.get("data")
  private val storagePath: Path = folderPath.resolve("food_items.txt")
  private val imagesPath: Path = Paths.get("images")

  // Saves a single FoodItem to the default file (appends) and handles image copying
  def save(food: FoodItem): Unit = {
    println(s"[DEBUG] Working directory: ${new java.io.File(".").getAbsolutePath}")
    try {
      ensureFileAndFolderExist()
      ensureImagesFolderExists()

      // Handle image copying if an image path is provided
      val updatedFood = food.imagePath match {
        case Some(originalImagePath) if originalImagePath.nonEmpty =>
          copyImageToImagesFolder(originalImagePath, food.name) match {
            case Some(newImagePath) => food.copy(imagePath = Some(newImagePath))
            case None => food // Keep original if copy failed
          }
        case _ => food // No image to copy
      }

      Files.writeString(
        storagePath,
        updatedFood.toLine + "\n",
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND
      )
      println(s"[INFO] Saved food item to ${storagePath.toAbsolutePath}")
      println(s"[DEBUG] Saved line: ${updatedFood.toLine}")
    } catch {
      case ex: IOException =>
        println(s"[ERROR] Failed to write food item: ${ex.getMessage}")
    }
  }

  // Ensures both the data folder and file exist
  private def ensureFileAndFolderExist(): Unit = {
    if (!Files.exists(folderPath)) {
      Files.createDirectories(folderPath)
      println(s"[INFO] Created folder: ${folderPath.toAbsolutePath}")
    }
    if (!Files.exists(storagePath)) {
      Files.createFile(storagePath)
      println(s"[INFO] Created file: ${storagePath.toAbsolutePath}")
    }
  }

  // Ensures the images folder exists
  private def ensureImagesFolderExists(): Unit = {
    if (!Files.exists(imagesPath)) {
      Files.createDirectories(imagesPath)
      println(s"[INFO] Created images folder: ${imagesPath.toAbsolutePath}")
    }
  }

  // Copies an image file to the images folder and returns the new relative path
  private def copyImageToImagesFolder(originalPath: String, foodName: String): Option[String] = {
    try {
      val sourcePath = Paths.get(originalPath)
      if (!Files.exists(sourcePath)) {
        println(s"[WARNING] Source image file does not exist: $originalPath")
        return None
      }

      // Get file extension
      val fileName = sourcePath.getFileName.toString
      val extension = if (fileName.contains(".")) {
        fileName.substring(fileName.lastIndexOf("."))
      } else {
        ""
      }

      // Create filename based on food name only
      val sanitizedFoodName = foodName.replaceAll("[^a-zA-Z0-9_-]", "_")
      val newFileName = s"${sanitizedFoodName}${extension}"
      val destinationPath = imagesPath.resolve(newFileName)

      // Copy the file
      Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING)
      println(s"[INFO] Copied image from $originalPath to ${destinationPath.toAbsolutePath}")

      // Return the relative path from project root
      Some(s"images${System.getProperty("file.separator")}$newFileName")
    } catch {
      case ex: Exception =>
        println(s"[ERROR] Failed to copy image file: ${ex.getMessage}")
        None
    }
  }

  // Loads all FoodItems from file
  def loadAll(): List[FoodItem] = {
    println(s"[DEBUG] Attempting to load food items from: ${storagePath.toAbsolutePath}")
    if (!Files.exists(storagePath)) {
      println("[DEBUG] Storage file not found. Returning empty list.")
      return Nil
    }

    val source = Source.fromFile(storagePath.toFile)
    try {
      val lines = source.getLines().toList
      val items = lines.flatMap { line =>
        val trimmed = Option(line).map(_.trim).getOrElse("")
        if (trimmed.isEmpty) None
        else {
          println(s"[DEBUG] Parsing line: $trimmed")
          trimmed.split("\\|", -1).toList match {
            case name :: catStr :: servingStr :: nutrientsStr :: imagePathOpt =>
              for {
                serving  <- Try(servingStr.trim.toDouble).toOption
                category <- parseCategory(catStr.trim)
                nutrients = if (nutrientsStr.trim.isEmpty) Nil else parseNutrients(nutrientsStr)
              } yield {
                val imagePath = imagePathOpt.headOption.map(_.trim).filter(_.nonEmpty)
                val item = FoodItem(name.trim, category, serving, nutrients, imagePath)
                println(s"[DEBUG] Parsed item: ${item.name} with ${nutrients.length} nutrients")
                item
              }
            case _ =>
              println(s"[WARNING] Could not parse line: $trimmed")
              None
          }
        }
      }
      println(s"[DEBUG] Loaded ${items.length} food item(s).")
      items
    } finally {
      source.close()
    }
  }

  private def parseCategory(label: String): Option[Category] = {
    label.toLowerCase match {
      case "fruit"     => Some(Category.Fruit)
      case "vegetable" => Some(Category.Vegetable)
      case "meat"      => Some(Category.Meat)
      case "grain"     => Some(Category.Grain)
      case "dairy"     => Some(Category.Dairy)
      case "seafood"   => Some(Category.Seafood)
      case "oil"       => Some(Category.Oil)
      case _           => None
    }
  }

  /**
   * FIXED: Now handles both formats - the prefixed format from Nutrient.format() 
   * AND the simple format from user input parsing
   */
  private def parseNutrients(nutrientsStr: String): List[Nutrient] = {
    println(s"[DEBUG] Parsing nutrients string: $nutrientsStr")

    nutrientsStr
      .split(",")
      .toList
      .flatMap { token =>
        val trimmedToken = token.trim
        println(s"[DEBUG] Processing nutrient token: '$trimmedToken'")

        // Check if it's the prefixed format (e.g., "Macronutrient: Protein: 10.0 g")
        if (trimmedToken.contains("Macronutrient:") || trimmedToken.contains("Micronutrient:")) {
          val parts = trimmedToken.split(":", 3).map(_.trim)
          parts match {
            // Prefixed macronutrient: ignore unit, only name & amount
            case Array("Macronutrient", name, amtUnit) =>
              parseAmount(name, amtUnit) { (n, v, _) =>
                Macronutrient(n, v)
              }

            // Prefixed micronutrient: pass all three
            case Array("Micronutrient", name, amtUnit) =>
              parseAmount(name, amtUnit)(Micronutrient(_, _, _))

            case _ =>
              println(s"[WARNING] Could not parse prefixed nutrient: $trimmedToken")
              None
          }
        }
        // Check for special cases (Fibre, Sugar)
        else if (trimmedToken.startsWith("Fibre:") || trimmedToken.startsWith("Sugar:")) {
          val parts = trimmedToken.split(":", 2).map(_.trim)
          parts match {
            case Array(name, amtUnit) =>
              parseTwoPart(name, amtUnit)
            case _ =>
              println(s"[WARNING] Could not parse special nutrient: $trimmedToken")
              None
          }
        }
        // Handle simple format (e.g., "Protein:10g" or "Protein: 10.0 g")
        else {
          val parts = trimmedToken.split(":", 2).map(_.trim)
          parts match {
            case Array(name, amtUnit) =>
              parseTwoPart(name, amtUnit)
            case _ =>
              println(s"[WARNING] Could not parse simple nutrient: $trimmedToken")
              None
          }
        }
      }
  }

  // Splits "<Name>:<amount><unit>" and applies a constructor to build a Nutrient
  private def parseAmount(
                           name: String,
                           amtUnit: String
                         )(ctor: (String, Double, String) => Nutrient): Option[Nutrient] = {
    val unitPattern = """([\d.]+)\s*([a-zA-Zµμ%]*)""".r
    amtUnit match {
      case unitPattern(valueStr, unitRaw) if Try(valueStr.toDouble).isSuccess =>
        val v = valueStr.toDouble
        val u = unitRaw match {
          case "µg" | "μg" => "µg"
          case "mg"        => "mg"
          case "g"         => "g"
          case ""          => "g"  // Default to grams if no unit
          case other       => other
        }
        val result = Some(ctor(name.trim, v, u))
        println(s"[DEBUG] Parsed nutrient: ${result.get}")
        result
      case _ =>
        println(s"[WARNING] Could not parse amount from: '$amtUnit'")
        None
    }
  }

  // Handles entries without explicit prefix: decides between Macro/Micro/Fibre/Sugar
  private def parseTwoPart(name: String, amtUnit: String): Option[Nutrient] = {
    parseAmount(name, amtUnit) { (n, v, u) =>
      u match {
        case "mg" | "µg" =>
          Micronutrient(n, v, u)
        case "g" | "" =>
          n.toLowerCase match {
            case "fibre" => Fibre(v)
            case "sugar" => Sugar(v)
            case _       => Macronutrient(n, v)
          }
        case _ =>
          // treat any unknown unit as gram-based macro
          Macronutrient(n, v)
      }
    }
  }

  // Deletes a FoodItem from the file and removes associated image
  def delete(foodToDelete: FoodItem): Unit = {
    if (!Files.exists(storagePath)) {
      println("[WARNING] Tried to delete from non-existent file.")
      return
    }

    // Delete associated image file if it exists in the images folder
    foodToDelete.imagePath.foreach { imagePath =>
      deleteImageFile(imagePath)
    }

    val lines = Files.readAllLines(storagePath).asScala.toList
    val kept  = lines.filterNot { line =>
      val parts = line.split("\\|", -1)
      parts.lift(0).exists(_.trim == foodToDelete.name) &&
        parts.lift(1).exists(_.trim == foodToDelete.category.label)
    }

    try {
      ensureFileAndFolderExist()
      Files.write(
        storagePath,
        kept.mkString("\n").getBytes,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      )
      println(s"[INFO] Deleted item '${foodToDelete.name}' from file.")
    } catch {
      case ex: IOException =>
        println(s"[ERROR] Failed to delete food item: ${ex.getMessage}")
    }
  }

  // Helper method to delete an image file
  private def deleteImageFile(imagePath: String): Unit = {
    try {
      val imageFile = Paths.get(imagePath)
      if (Files.exists(imageFile)) {
        Files.delete(imageFile)
        println(s"[INFO] Deleted image file: $imagePath")
      }
    } catch {
      case ex: Exception =>
        println(s"[WARNING] Failed to delete image file $imagePath: ${ex.getMessage}")
    }
  }

  /**
   * Updates an existing FoodItem by replacing its raw line (matching name|category)
   * with the new serialized line. Handles image copying and cleanup.
   */
  def update(oldItem: FoodItem, updatedItem: FoodItem): Unit = {
    if (!Files.exists(storagePath)) {
      println("[WARNING] Tried to update in non-existent file.")
      return
    }

    ensureImagesFolderExists()

    // Handle image changes
    val finalUpdatedItem = (oldItem.imagePath, updatedItem.imagePath) match {
      // New image provided - copy it
      case (_, Some(newImagePath)) if !newImagePath.startsWith("images") =>
        copyImageToImagesFolder(newImagePath, updatedItem.name) match {
          case Some(copiedPath) =>
            // Delete old image if it exists and is different
            oldItem.imagePath.filter(_ != copiedPath).foreach(deleteImageFile)
            updatedItem.copy(imagePath = Some(copiedPath))
          case None => updatedItem
        }

      // Image removed - delete old image
      case (Some(oldImagePath), None) =>
        deleteImageFile(oldImagePath)
        updatedItem

      // Same image or image already in images folder
      case _ => updatedItem
    }

    val allLines = Files.readAllLines(storagePath).asScala.toList

    val filtered = allLines.filterNot { line =>
      val parts = line.split("\\|", -1)
      parts.lift(0).exists(_.trim == oldItem.name) &&
        parts.lift(1).exists(_.trim == oldItem.category.label)
    }

    val newLines = filtered :+ finalUpdatedItem.toLine

    try {
      ensureFileAndFolderExist()
      Files.write(
        storagePath,
        newLines.mkString("\n").getBytes,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      )
      println(s"[INFO] Updated item '${oldItem.name}' in file.")
    } catch {
      case ex: IOException =>
        println(s"[ERROR] Failed to update food item: ${ex.getMessage}")
    }
  }
}
