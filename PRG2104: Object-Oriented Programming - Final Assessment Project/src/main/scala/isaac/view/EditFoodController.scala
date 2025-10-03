// Controller for editing an existing FoodItem, located in the 'isaac.view' package
package isaac.view
// Imports JavaFX user interface (UI) controls, event handling, stage management, and application-specific models
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.stage.{Stage, FileChooser}
import javafx.event.ActionEvent
import isaac.model._
import scala.util.Try
import java.io.File

// Controller for the Edit Food dialog, allowing users to modify an existing FoodItem
class EditFoodController {
  @FXML private var nameField: TextField = _
  @FXML private var categoryChoice: ChoiceBox[Category] = _
  @FXML private var servingField: TextField = _
  @FXML private var nutrientsArea: TextArea = _
  @FXML private var imageField: TextField = _
  @FXML private var saveButton: Button = _
  @FXML private var cancelButton: Button = _
  @FXML private var selectImageButton: Button = _

  private var updatedItem: Option[FoodItem] = None
  private var originalItem: FoodItem = _

  @FXML
  def initialize(): Unit = {
    categoryChoice.getItems.addAll(
      Category.Fruit, Category.Vegetable, Category.Meat,
      Category.Grain, Category.Dairy, Category.Seafood, Category.Oil
    )
  }

  // Pre-fills form fields with values from the given FoodItem
  def setFoodItem(item: FoodItem): Unit = {
    originalItem = item
    nameField.setText(item.name)
    categoryChoice.getSelectionModel.select(item.category)
    servingField.setText(item.servingSize.toString)
    nutrientsArea.setText(
      item.nutrients.map {
        case m: Micronutrient => s"${m.name}:${m.amount}${m.unit}"
        case f: Fibre => s"${f.name}:${f.amount}g"
        case s: Sugar  => s"${s.name}:${s.amount}g"
        case m: Macronutrient => s"${m.name}:${m.amount}g"
      }.mkString(", ")
    )
    imageField.setText(item.imagePath.getOrElse(""))
  }

  // Returns the updated food item, if any
  def getResult: Option[FoodItem] = updatedItem

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

  @FXML
  def handleSave(event: ActionEvent): Unit = {
    val maybeServing = Try(servingField.getText.toDouble).toOption

    // Parse nutrients using the helper method
    val nutrients = parseNutrientsFromInput(nutrientsArea.getText)

    val imagePath = Option(imageField.getText.trim).filter(_.nonEmpty)

    // If inputs are valid, create an updated FoodItem
    for {
      name    <- Option(nameField.getText).map(_.trim).filter(_.nonEmpty)
      cat     <- Option(categoryChoice.getValue)
      serving <- maybeServing
    } yield {
      updatedItem = Some(originalItem.copy(
        name = name,
        category = cat,
        servingSize = serving,
        nutrients = nutrients,
        imagePath = imagePath
      ))
    }

    closeWindow(event) // Close dialog after save
  }

  @FXML
  def handleCancel(event: ActionEvent): Unit = {
    updatedItem = None // Discard changes
    closeWindow(event)
  }

  // Opens a FileChooser to let the user pick an image and fills the imageField with the file's absolute path.
  @FXML
  def handleBrowse(event: ActionEvent): Unit = {
    try {
      val chooser = new FileChooser()
      chooser.setTitle("Select Image")
      chooser.getExtensionFilters.addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
      )

      // Use the dialog's window as owner if available, otherwise null
      val owner = if (imageField != null && imageField.getScene != null) imageField.getScene.getWindow else null
      val file: File = chooser.showOpenDialog(owner)

      if (file != null) {
        // Set the field to the absolute filesystem path (requested)
        imageField.setText(file.getAbsolutePath)
      }
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        val alert = new Alert(Alert.AlertType.ERROR)
        alert.setHeaderText("Image selection failed")
        alert.setContentText(ex.getMessage)
        alert.showAndWait()
    }
  }

  // Closes the dialog window
  private def closeWindow(event: ActionEvent): Unit = {
    val stage = event.getSource.asInstanceOf[javafx.scene.Node]
      .getScene.getWindow.asInstanceOf[Stage]
    stage.close()
  }
}
