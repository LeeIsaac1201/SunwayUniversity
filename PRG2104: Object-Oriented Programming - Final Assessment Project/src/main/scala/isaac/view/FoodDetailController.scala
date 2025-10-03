// Controller for the Food Detail dialog located in the 'isaac.view' package
package isaac.view
// Imports: FoodItem model, JavaFX FXML annotations, user interface (UI) controls, image handling, layout containers, window event handling, and File API for loading food images
import isaac.model.FoodItem
import javafx.fxml.FXML
import javafx.scene.control.{Button, DialogPane, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.VBox
import javafx.stage.{WindowEvent, Stage}
import java.io.File

// Handles the display of a single FoodItem's details, including name, category, serving size, nutrients, image, and sets the dialog window title to the food's name when possible.
class FoodDetailController {
  // UI components from the FoodDetailDialog.fxml file
  @FXML private var detailDialogPane: DialogPane = _
  @FXML private var foodImage: ImageView = _
  @FXML private var nameLabel: Label = _
  @FXML private var categoryLabel: Label = _
  @FXML private var servingLabel: Label = _
  @FXML private var nutrientsBox: VBox = _
  @FXML private var editButton: Button = _
  @FXML private var deleteButton: Button = _
  // NOTE: closeButton removed because the FXML no longer contains it.

  // Internal state: The current FoodItem and optional callbacks for edit and delete actions
  private var foodItem: FoodItem = _
  private var onEditCallback: Option[FoodItem => Unit] = None
  private var onDeleteCallback: Option[FoodItem => Unit] = None

  // Populates UI components with details from the given FoodItem
  def setFoodItem(item: FoodItem): Unit = {
    foodItem = item
    nameLabel.setText(item.name)
    categoryLabel.setText(item.category.label)
    servingLabel.setText(f"${item.servingSize}%.2f g")

    // Clear existing nutrient labels before adding new ones
    nutrientsBox.getChildren.clear()
    item.nutrients.foreach { nutrient =>
      val label = new Label(s"${nutrient.name}: ${nutrient.amount} ${nutrient.unit}")
      nutrientsBox.getChildren.add(label)
    }

    // Load image if present
    item.imagePath.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        val image = new Image(file.toURI.toString)
        foodImage.setImage(image)
      }
    }

    // Set the dialog window title to the food name if possible (or once the scene/window is available).
    setWindowTitleWhenAvailable(item.name)
  }

  // Register a callback function to be executed when the Edit button is clicked
  def setOnEdit(callback: FoodItem => Unit): Unit = {
    onEditCallback = Some(callback)
  }

  // Register a callback function to be executed when the Delete button is clicked
  def setOnDelete(callback: FoodItem => Unit): Unit = {
    onDeleteCallback = Some(callback)
  }

  /**
   * Ensure the Stage title is set to the provided title when the dialog's window is available.
   * If the window is already available, set it immediately. Otherwise, attach a short-lived listener
   * that sets the title once and then does nothing further.
   */
  private def setWindowTitleWhenAvailable(title: String): Unit = {
    if (detailDialogPane != null && detailDialogPane.getScene != null && detailDialogPane.getScene.getWindow != null) {
      val window = detailDialogPane.getScene.getWindow
      if (window.isInstanceOf[Stage]) window.asInstanceOf[Stage].setTitle(title)
    } else if (detailDialogPane != null) {
      detailDialogPane.sceneProperty().addListener((_, _, scene) => {
        if (scene != null && scene.getWindow != null && scene.getWindow.isInstanceOf[Stage]) {
          scene.getWindow.asInstanceOf[Stage].setTitle(title)
        }
      })
    }
  }

  @FXML
  def initialize(): Unit = {
    // Defensive: remove any ButtonTypes that may have been created from FXML (removes any default Close button).
    if (detailDialogPane != null) {
      detailDialogPane.getButtonTypes.clear()

      // Ensure the window's X (close) button hides the dialog (don't consume the event).
      // We set this via a sceneProperty listener because the scene/window may not yet be available at initialize time.
      detailDialogPane.sceneProperty().addListener((_, _, scene) => {
        if (scene != null) {
          val window = scene.getWindow
          if (window != null) {
            window.setOnCloseRequest((e: WindowEvent) => {
              // any cleanup (if desired) can go here
              window.hide() // hide/close the dialog window normally
              // do NOT call e.consume() here — allow the close to proceed
            })
          }
        }
      })
    }

    // Edit button: Run the edit callback, then close dialog
    if (editButton != null) {
      editButton.setOnAction(_ => {
        onEditCallback.foreach(cb => cb(foodItem))
        if (detailDialogPane != null && detailDialogPane.getScene != null && detailDialogPane.getScene.getWindow != null)
          detailDialogPane.getScene.getWindow.hide()
      })
    }

    // Delete button: Run the delete callback, then close dialog
    if (deleteButton != null) {
      deleteButton.setOnAction(_ => {
        onDeleteCallback.foreach(cb => cb(foodItem))
        if (detailDialogPane != null && detailDialogPane.getScene != null && detailDialogPane.getScene.getWindow != null)
          detailDialogPane.getScene.getWindow.hide()
      })
    }
  }
}
