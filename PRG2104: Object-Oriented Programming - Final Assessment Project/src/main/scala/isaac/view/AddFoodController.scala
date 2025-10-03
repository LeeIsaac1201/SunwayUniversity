// Controller for adding new food items, located in the 'isaac.view' package
package isaac.view
// JavaFX imports for FXML, controls, stage, and events
import isaac.model.Nutrient
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.stage.{FileChooser, Stage}
import javafx.event.ActionEvent
// Model classes for food categories and nutrients
import isaac.model.{Category, FoodItem, Macronutrient, Micronutrient, Fibre, Sugar}
// Storage utility for saving food items
import isaac.util.FoodItemStorage
import scala.util.Try
import java.io.File

class AddFoodController {
  // FXML-injected user interface (UI) elements
  @FXML private var nameField: TextField = _
  @FXML private var categoryChoice: ChoiceBox[Category] = _
  @FXML private var servingField: TextField = _
  @FXML private var nutrientsArea: TextArea = _
  @FXML private var imageField: TextField = _
  @FXML private var saveButton: Button = _
  @FXML private var cancelButton: Button = _

  // Button for selecting an image file from disk
  @FXML private var selectImageButton: Button = _

  // Store the created FoodItem, if saved
  private var result: Option[FoodItem] = None

  def getResult: Option[FoodItem] = result

  @FXML
  def initialize(): Unit = {
    // Populate category choice options
    categoryChoice.getItems.addAll(
      Category.Fruit, Category.Vegetable, Category.Meat,
      Category.Grain, Category.Dairy, Category.Seafood, Category.Oil
    )
    categoryChoice.getSelectionModel.selectFirst()

    // Attach handler for image selection button
    if (selectImageButton != null) {
      selectImageButton.setOnAction(_ => handleSelectImage())
    }
  }

  // Handle file selection for the food item image
  private def handleSelectImage(): Unit = {
    try {
      val fileChooser = new FileChooser()
      fileChooser.setTitle("Select Image")
      fileChooser.getExtensionFilters.addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
      )
      val window = if (selectImageButton.getScene != null) selectImageButton.getScene.getWindow else null
      val selectedFile: File = fileChooser.showOpenDialog(window)
      if (selectedFile != null) {
        imageField.setText(selectedFile.getAbsolutePath)
      }
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        val alert = new Alert(Alert.AlertType.ERROR)
        alert.setHeaderText("Failed to select image")
        alert.setContentText(ex.getMessage)
        alert.showAndWait()
    }
  }

  // Parse nutrients from input string (format: name:amountUnit)
  private def parseNutrientsFromInput(nutrientsStr: String): List[Nutrient] = {
    nutrientsStr
      .split(",")
      .toList
      .flatMap { token =>
        token.split(":").map(_.trim) match {
          case Array(nm, amtWithUnit) =>
            val unitPattern = "([0-9.]+)([a-zA-Zµμ]*)".r
            amtWithUnit match {
              case unitPattern(valueStr, unitRaw) if Try(valueStr.toDouble).isSuccess =>
                val amt = valueStr.toDouble
                val unit = unitRaw match {
                  case "µg" | "μg" => "µg"
                  case "mg"       => "mg"
                  case "g"        => "g"
                  case _          => "g" // Default to grams if no unit specified
                }

                val lowerName = nm.toLowerCase
                unit match {
                  case "mg" | "µg" =>
                    Some(Micronutrient(nm, amt, unit))
                  case "g" =>
                    lowerName match {
                      case "fibre" => Some(Fibre(amt))
                      case "sugar" => Some(Sugar(amt))
                      case _       => Some(Macronutrient(nm, amt))
                    }
                  case _ => None
                }
              case _ => None
            }
          case _ => None
        }
      }
  }

  // Handle Save button click
  @FXML
  def handleSave(event: ActionEvent): Unit = {
    val maybeServing = Try(servingField.getText.toDouble).toOption

    // Parse nutrients from the text area using the helper method
    val nutrients = parseNutrientsFromInput(Option(nutrientsArea.getText).getOrElse(""))

    // Handle optional image field
    val imageOpt = Option(imageField.getText).map(_.trim).filter(_.nonEmpty)

    // Construct and save FoodItem if input is valid
    (for {
      serving <- maybeServing
      cat     <- Option(categoryChoice.getValue)
      name    <- Option(nameField.getText).map(_.trim).filter(_.nonEmpty)
    } yield {
      val foodItem = FoodItem(name, cat, serving, nutrients, imageOpt)
      result = Some(foodItem)

      // Debug: Print what we're about to save
      println(s"[DEBUG] Saving FoodItem: $name")
      println(s"[DEBUG] Nutrients being saved: ${nutrients.map(Nutrient.format).mkString(", ")}")

      FoodItemStorage.save(foodItem)

      // Show success message
      val alert = new Alert(Alert.AlertType.INFORMATION)
      alert.setHeaderText("Success")
      alert.setContentText(s"Food item '$name' saved successfully!")
      alert.showAndWait()

    }).getOrElse {
      // Show error message for invalid input
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setHeaderText("Invalid Input")
      alert.setContentText("Please fill in all required fields (name, serving size) and ensure serving size is a valid number.")
      alert.showAndWait()
    }

    closeWindow(event)
  }

  // Handle Cancel button click
  @FXML
  def handleCancel(event: ActionEvent): Unit =
    closeWindow(event)

  // Close the current window
  private def closeWindow(event: ActionEvent): Unit = {
    val stage = event.getSource
      .asInstanceOf[javafx.scene.Node]
      .getScene
      .getWindow
      .asInstanceOf[Stage]
    stage.close()
  }
}
