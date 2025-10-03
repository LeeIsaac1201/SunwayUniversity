// Food list controller located in the 'isaac.view' package
package isaac.view
// JavaFX core user interface (UI) imports: FXML, stage handling, and scene graph components
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.{Parent, Scene}
import javafx.scene.control._
import javafx.stage.{Modality, Stage}
import javafx.collections.{FXCollections, ObservableList}
import javafx.collections.transformation.FilteredList
import javafx.beans.property.{SimpleObjectProperty, SimpleStringProperty}
import isaac.db.DatabaseProvider
import isaac.model.FoodItem
import isaac.util.FoodItemStorage
import scalafx.scene.control.Alert.AlertType
import javafx.scene.input.MouseButton
import javafx.scene.control.{Dialog, DialogPane}
import scala.jdk.CollectionConverters._

// Additional imports for styled alert buttons/lookup
import javafx.scene.control.ButtonType
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.Node
import javafx.scene.control.Label

// Suppress IntelliJ's "var could be a val" warnings since FXML requires var
@SuppressWarnings(Array("unchecked", "unused"))
//noinspection VarCouldBeVal
class FoodListController {
  // Table and UI elements injected from FXML
  @FXML private var foodTable: TableView[FoodItem] = _
  @FXML private var nameColumn: TableColumn[FoodItem, String] = _
  @FXML private var categoryColumn: TableColumn[FoodItem, String] = _
  @FXML private var servingColumn: TableColumn[FoodItem, java.lang.Double] = _
  @FXML private var nutrientColumn: TableColumn[FoodItem, String] = _
  @FXML private var addButton: Button = _
  @FXML private var editButton: Button = _
  @FXML private var deleteButton: Button = _
  @FXML private var searchField: TextField = _
  @FXML private var searchButton: Button = _
  // Observable list holding all items from the database and file storage
  private val masterData: ObservableList[FoodItem] =
    FXCollections.observableArrayList[FoodItem](loadCombinedData(): _*)
  // Filtered list for handling search functionality
  private val filteredData = new FilteredList[FoodItem](masterData, (_: FoodItem) => true)
  // Loads items from both the database and file storage, avoiding duplicates
  private def loadCombinedData(): List[FoodItem] = {
    val dbItems = DatabaseProvider.db.all()
    val fileItems = FoodItemStorage.loadAll()

    val dbSet = dbItems.map(item => (item.name, item.category)).toSet
    val uniqueFromFile = fileItems.filterNot(f => dbSet.contains((f.name, f.category)))

    val combined = (dbItems ++ uniqueFromFile).toList
    println(s"[DEBUG] Combined food items: ${combined.size} (DB: ${dbItems.size}, File: ${uniqueFromFile.size} new)")
    combined
  }

  // Initialisation logic for setting up table bindings, double-click handling, and search filtering
  @FXML
  def initialize(): Unit = {
    nameColumn.setCellValueFactory { cell =>
      new SimpleStringProperty(cell.getValue.name)
    }
    categoryColumn.setCellValueFactory { cell =>
      new SimpleStringProperty(cell.getValue.category.label)
    }
    servingColumn.setCellValueFactory { cell =>
      new SimpleObjectProperty[java.lang.Double](cell.getValue.servingSize: java.lang.Double)
    }
    nutrientColumn.setCellValueFactory { cell =>
      new SimpleStringProperty(cell.getValue.nutrientSummary)
    }

    foodTable.setItems(filteredData)

    // allow multi-select if desired
    foodTable.getSelectionModel.setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE)

    // Initially, disable edit and delete buttons since no item is selected
    editButton.setDisable(true)
    deleteButton.setDisable(true)

    // Enable/disable buttons based on selection
    foodTable.getSelectionModel.selectedItemProperty().addListener { (_, _, newValue) =>
      val hasSelection = newValue != null
      editButton.setDisable(!hasSelection)
      deleteButton.setDisable(!hasSelection)
    }

    // Double-click opens detail dialog
    foodTable.setRowFactory { _ =>
      val row = new TableRow[FoodItem]()
      row.setOnMouseClicked { event =>
        if (!row.isEmpty && event.getButton == MouseButton.PRIMARY && event.getClickCount == 2) {
          val item = row.getItem
          showDetailDialog(item)
        }
      }
      row
    }
    // Live filtering based on search field input
    searchField.textProperty().addListener(new javafx.beans.value.ChangeListener[String] {
      override def changed(obs: javafx.beans.value.ObservableValue[_ <: String], old: String, newVal: String): Unit = {
        val lower = if (newVal != null) newVal.toLowerCase else ""
        filteredData.setPredicate(item => item.name.toLowerCase.contains(lower))
      }
    })
  }

  // Handles the Add button by opening a modal dialog for adding a new food item
  @FXML
  def handleAdd(): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/isaac/view/AddFoodView.fxml"))
    val root: Parent = loader.load()
    val dialog = new Stage()
    dialog.initModality(Modality.APPLICATION_MODAL)
    dialog.setTitle("Add New Food Item")
    dialog.setScene(new Scene(root))
    dialog.showAndWait()

    val ctrl = loader.getController.asInstanceOf[AddFoodController]
    ctrl.getResult.foreach { item =>
      DatabaseProvider.db.add(item)
      masterData.setAll(loadCombinedData(): _*)
    }
  }
  // Handles the Edit button by opening the edit dialog for the selected item
  @FXML
  def handleEdit(): Unit = {
    Option(foodTable.getSelectionModel.getSelectedItem).foreach { selected =>
      showEditDialog(selected)
    }
  }
  // Displays the edit dialog and applies changes to both the database and file storage
  def showEditDialog(selected: FoodItem): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/isaac/view/EditFoodView.fxml"))
    val root: Parent = loader.load()
    val ctrl = loader.getController.asInstanceOf[EditFoodController]
    ctrl.setFoodItem(selected)

    val dialog = new Stage()
    dialog.initModality(Modality.APPLICATION_MODAL)
    dialog.setTitle(s"Edit Food Item – ${selected.name}")
    dialog.setScene(new Scene(root))
    dialog.showAndWait()

    ctrl.getResult.foreach { updated =>
      // Use the new two-argument update so the DB replaces the exact stored item
      DatabaseProvider.db.update(selected, updated)

      // Update the file storage (this replaces the matching file line)
      FoodItemStorage.update(selected, updated)

      // helpful debug output to confirm state
      println("[DEBUG] DB items after update:")
      DatabaseProvider.db.all().foreach(i => println(s"  DB -> ${i.name} | ${i.category}"))
      println("[DEBUG] FILE items after update:")
      FoodItemStorage.loadAll().foreach(i => println(s"  FILE -> ${i.name} | ${i.category}"))

      // Refresh UI
      masterData.setAll(loadCombinedData(): _*)
    }
  }

  @FXML
  def handleDelete(): Unit = {
    val selectedItems = foodTable.getSelectionModel.getSelectedItems.asScala.toList

    if (selectedItems.isEmpty) {
      val alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING)
      alert.setTitle("No Selection")
      alert.setHeaderText("No Food Item Selected")
      alert.setContentText("Please select one or more food items to delete.")
      alert.initOwner(foodTable.getScene.getWindow)
      alert.showAndWait()
      return
    }

    // Handle single vs multiple items
    val message = if (selectedItems.size == 1) {
      s"Are you sure you want to delete '${selectedItems.head.name}'?\n\nThis action cannot be undone."
    } else {
      s"Are you sure you want to delete these ${selectedItems.size} items?\n\nThis action cannot be undone."
    }

    // Create custom ButtonType with text "Okay"
    val okayButtonType = new ButtonType("Okay", ButtonData.OK_DONE)
    val cancelButtonType = ButtonType.CANCEL

    // Build alert with custom button types
    val confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION, "", okayButtonType, cancelButtonType)
    confirmAlert.initOwner(foodTable.getScene.getWindow)
    confirmAlert.initModality(Modality.APPLICATION_MODAL)
    confirmAlert.setTitle("Confirm Deletion")
    confirmAlert.setHeaderText(null)
    confirmAlert.setContentText(message)

    // Style the dialog content and buttons inline to match FXML styling
    val pane = confirmAlert.getDialogPane
    // background + padding for content area
    pane.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 14;")

    // Style the content label if lookup returns it
    val contentNode = pane.lookup(".content.label")
    if (contentNode != null && contentNode.isInstanceOf[Label]) {
      val lbl = contentNode.asInstanceOf[Label]
      lbl.setWrapText(true)
      lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-alignment: center; -fx-padding: 6 0 8 0;")
    }

    // Style the buttons (Okay = red, Cancel = light)
    val okButtonNode = pane.lookupButton(okayButtonType)
    if (okButtonNode != null) {
      okButtonNode.setStyle(
        "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500; -fx-padding: 6 12; -fx-background-radius: 6; -fx-border-color: #c82333; -fx-border-width: 1; -fx-border-radius: 6;"
      )
    }
    val cancelNode = pane.lookupButton(cancelButtonType)
    if (cancelNode != null) {
      cancelNode.setStyle(
        "-fx-background-color: #f8f9fa; -fx-text-fill: #495057; -fx-font-size: 13px; -fx-font-weight: 500; -fx-padding: 6 12; -fx-background-radius: 6; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 6;"
      )
    }

    val result = confirmAlert.showAndWait()
    if (result.isPresent && result.get() == okayButtonType) {
      selectedItems.foreach { item =>
        DatabaseProvider.db.delete(item)
        FoodItemStorage.delete(item)
      }
      masterData.setAll(loadCombinedData(): _*)
    }
  }
  // Shows the detailed dialog for a food item, including edit and delete actions
  def showDetailDialog(item: FoodItem): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/isaac/view/FoodDetailDialog.fxml"))
    val dialogPane = loader.load[DialogPane]()
    val controller = loader.getController[FoodDetailController]()

    controller.setFoodItem(item)
    // Configure edit action from the detail dialog
    controller.setOnEdit { food =>
      showEditDialog(food)
      masterData.setAll(loadCombinedData(): _*)
    }
    // Configure delete action from the detail dialog
    controller.setOnDelete { food =>
      val message = s"Are you sure you want to delete '${food.name}'?\n\nThis action cannot be undone."

      val okayButtonType = new ButtonType("Okay", ButtonData.OK_DONE)
      val cancelButtonType = ButtonType.CANCEL

      val confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION, "", okayButtonType, cancelButtonType)
      confirmAlert.initOwner(foodTable.getScene.getWindow)
      confirmAlert.setTitle("Confirm Deletion")
      confirmAlert.setHeaderText(null)
      confirmAlert.setContentText(message)

      // Apply same inline styling as above
      val pane = confirmAlert.getDialogPane
      pane.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 14;")

      val contentNode = pane.lookup(".content.label")
      if (contentNode != null && contentNode.isInstanceOf[Label]) {
        val lbl = contentNode.asInstanceOf[Label]
        lbl.setWrapText(true)
        lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-alignment: center; -fx-padding: 6 0 8 0;")
      }

      val okButtonNode = pane.lookupButton(okayButtonType)
      if (okButtonNode != null) {
        okButtonNode.setStyle(
          "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500; -fx-padding: 6 12; -fx-background-radius: 6; -fx-border-color: #c82333; -fx-border-width: 1; -fx-border-radius: 6;"
        )
      }
      val cancelNode = pane.lookupButton(cancelButtonType)
      if (cancelNode != null) {
        cancelNode.setStyle(
          "-fx-background-color: #f8f9fa; -fx-text-fill: #495057; -fx-font-size: 13px; -fx-font-weight: 500; -fx-padding: 6 12; -fx-background-radius: 6; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 6;"
        )
      }

      val result = confirmAlert.showAndWait()
      if (result.isPresent && result.get() == okayButtonType) {
        DatabaseProvider.db.delete(food)
        FoodItemStorage.delete(food)
        masterData.setAll(loadCombinedData(): _*)
      }
    }

    val dialog = new Dialog[Unit]()
    dialog.setDialogPane(dialogPane)
    dialog.setResizable(false)
    dialog.initOwner(foodTable.getScene.getWindow)

    // Disable "X" close
    dialog.setOnCloseRequest(_.consume())

    dialog.showAndWait()
  }
  // Placeholder for search button (search handled by text listener)
  @FXML def handleSearch(): Unit = {}

  // Adds additional items into the master list and logs injection
  def setInitialFoodItems(items: List[FoodItem]): Unit = {
    val newItems = items.filterNot(masterData.contains)
    masterData.addAll(newItems: _*)
    println(s"[DEBUG] Injected ${newItems.length} items from storage.")
  }
}
