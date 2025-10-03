// Main application entry point located in the 'isaac' package
package isaac.view
// JavaFX imports for FXML, controls, stage, and events
import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.input.KeyCode
import javafx.util.StringConverter
import isaac.model.FoodItem
import isaac.util.FoodItemStorage
import scala.jdk.CollectionConverters._
import scala.util.Try

class AddIngredientController {

  @FXML private var foodCombo: ComboBox[FoodItem] = _
  @FXML private var gramsField: TextField = _
  @FXML private var okButton: Button = _
  @FXML private var cancelButton: Button = _

  // Result stored after OK button pressed (dialog consumer reads via getResult)
  private var resultOpt: Option[(FoodItem, Double)] = None

  // Master list and filtered list used by the ComboBox
  private val masterItems: ObservableList[FoodItem] = FXCollections.observableArrayList[FoodItem]()
  private val filtered: FilteredList[FoodItem] = new FilteredList[FoodItem](masterItems, (_: FoodItem) => true)

  // Optional injected list
  private var injectedItems: Option[List[FoodItem]] = None

  @FXML
  def initialize(): Unit = {
    if (foodCombo != null) {
      // Set the filtered list as the ComboBox items
      foodCombo.setEditable(true)
      foodCombo.setItems(filtered)

      // Converter: Display name in editor and button cell
      val converter = new StringConverter[FoodItem]() {
        override def toString(obj: FoodItem): String =
          if (obj == null) "" else Option(obj.name).getOrElse(obj.toString)

        override def fromString(string: String): FoodItem = {
          val list = injectedItems.getOrElse(loadFromStorage())
          list.find(_.name.equalsIgnoreCase(string)).orNull
        }
      }
      foodCombo.setConverter(converter)

      // Update the editor when selection changes (programmatic or user)
      foodCombo.getSelectionModel.selectedItemProperty().addListener((_, _, newSel) => {
        if (newSel != null) {
          Platform.runLater(() => {
            try Option(foodCombo.getEditor).foreach(_.setText(Option(newSel.name).getOrElse("")))
            catch { case _: Throwable => () }
          })
        }
      })

      // Customise the popup cells so mouse press selects and commits the item (avoid race with popup)
      foodCombo.setCellFactory(_ => {
        val cell = new ListCell[FoodItem] {
          override def updateItem(item: FoodItem, empty: Boolean): Unit = {
            super.updateItem(item, empty)
            if (empty || item == null) setText("")
            else setText(Option(item.name).getOrElse(item.toString))
          }
        }

        // Use MOUSE_PRESSED to ensure we act before popup/selection races
        cell.setOnMousePressed { (_: javafx.scene.input.MouseEvent) =>
          if (!cell.isEmpty) {
            val item = cell.getItem
            // Commit selection immediately and update editor on fx thread
            try {
              foodCombo.getSelectionModel.select(item)
              foodCombo.setValue(item)
            } catch { case _: Throwable => () }

            Platform.runLater(() => {
              try {
                Option(foodCombo.getEditor).foreach(_.setText(Option(item.name).getOrElse("")))
                // Hide popup after committing
                try foodCombo.hide() catch { case _: Throwable => () }
              } catch { case _: Throwable => () }
            })
          }
        }

        cell
      })

      // Button cell should show the selected name
      foodCombo.setButtonCell(new ListCell[FoodItem] {
        override def updateItem(item: FoodItem, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          if (empty || item == null) setText("") else setText(Option(item.name).getOrElse(item.toString))
        }
      })

      // Editor listeners: Filter on typed text
      val editor = foodCombo.getEditor
      if (editor != null) {
        editor.textProperty().addListener((_, _, newText) => {
          Platform.runLater(() => applyFilter(newText))
        })

        // Keyboard navigation: Down opens popup, Enter tries to commit (choose first match if none selected)
        editor.setOnKeyPressed { ke =>
          if (ke.getCode == KeyCode.DOWN) {
            try if (!foodCombo.isShowing) foodCombo.show() catch { case _: Throwable => () }
          } else if (ke.getCode == KeyCode.ENTER) {
            val sel = Option(foodCombo.getValue)
            if (sel.isEmpty) {
              if (!filtered.isEmpty) {
                val first = filtered.get(0)
                foodCombo.setValue(first)
                Option(editor).foreach(_.setText(Option(first.name).getOrElse("")))
                try foodCombo.hide() catch { case _: Throwable => () }
              }
            }
          }
        }
      }

      // When popup hides, try to resolve typed exact text to a value (final chance)
      foodCombo.showingProperty().addListener((_, _, showing) => {
        if (!showing) {
          try {
            val currentVal = Option(foodCombo.getValue)
            if (currentVal.isEmpty) {
              val typed = Option(foodCombo.getEditor).map(_.getText).getOrElse("").trim
              if (typed.nonEmpty) {
                masterItems.asScala.find(_.name.equalsIgnoreCase(typed)).foreach { matched =>
                  Platform.runLater(() => {
                    try {
                      foodCombo.setValue(matched)
                      Option(foodCombo.getEditor).foreach(_.setText(Option(matched.name).getOrElse("")))
                    } catch { case _: Throwable => () }
                  })
                }
              }
            }
          } catch { case _: Throwable => () }
        }
      })
    }

    // Load items into master list on init
    Platform.runLater(() => refreshItems())

    // Allow Enter in grams field to confirm
    if (gramsField != null) gramsField.setOnAction(_ => handleOk())
  }

  // Reload items from FoodItemStorage (reads from food_items.txt).
  def refreshItems(): Unit = {
    val items = injectedItems.getOrElse(loadFromStorage())
    masterItems.clear()
    masterItems.addAll(items.asJava)
    // reset filter to show all (but do NOT auto-select any)
    applyFilter("")
  }

  // Allow callers to inject an in-memory list of FoodItem (e.g. from FoodListController)
  def setItems(items: List[FoodItem]): Unit = {
    injectedItems = Some(items)
    refreshItems()
  }

  // Robust loader: handles Java/Scala collections or odd types returned by FoodItemStorage.loadAll()
  private def loadFromStorage(): List[FoodItem] = {
    try {
      val raw = FoodItemStorage.loadAll()
      raw match {
        case jc: java.util.Collection[_] => jc.asInstanceOf[java.util.Collection[FoodItem]].asScala.toList
        case seq: Seq[_] => seq.asInstanceOf[Seq[FoodItem]].toList
        case other if other != null =>
          try other.asInstanceOf[java.util.List[FoodItem]].asScala.toList
          catch { case _: Throwable =>
            try other.asInstanceOf[Seq[FoodItem]].toList
            catch { case _: Throwable => Nil }
          }
        case _ => Nil
      }
    } catch {
      case _: Throwable =>
        try {
          val j = FoodItemStorage.loadAll().asInstanceOf[java.util.Collection[FoodItem]]
          j.asScala.toList
        } catch { case _: Throwable => Nil }
    }
  }

  // Apply filter to the FilteredList based on typed text and show/hide popup accordingly.
  private def applyFilter(text: String): Unit = {
    val q = Option(text).map(_.trim.toLowerCase).getOrElse("")
    if (q.isEmpty) {
      filtered.setPredicate((_: FoodItem) => true)
    } else {
      filtered.setPredicate((fi: FoodItem) => Option(fi.name).getOrElse("").toLowerCase.contains(q))
    }
    Platform.runLater(() => {
      try {
        if (!filtered.isEmpty && foodCombo.isFocused && !foodCombo.isShowing) foodCombo.show()
        else if (filtered.isEmpty && foodCombo.isShowing) foodCombo.hide()
      } catch { case _: Throwable => () }
    })
  }

  // Called by FXML OK button. Validates input and closes the dialog.
  @FXML
  def handleOk(): Unit = {
    val comboValOpt: Option[FoodItem] = Option(foodCombo).flatMap(c => Option(c.getValue))

    val resolved: Option[FoodItem] = comboValOpt.orElse {
      val typed = Option(foodCombo).flatMap(c => Option(c.getEditor)).map(_.getText).getOrElse("").trim
      if (typed.nonEmpty) masterItems.asScala.find(_.name.equalsIgnoreCase(typed))
      else None
    }

    if (resolved.isEmpty) {
      showAlert(Alert.AlertType.WARNING, "No food selected", "Please select or type a valid food item (exact name).")
      return
    }

    val gramsText = Option(gramsField).map(_.getText).getOrElse("").trim
    val parsed = Try(gramsText.toDouble).toOption
    parsed match {
      case None =>
        showAlert(Alert.AlertType.WARNING, "Invalid quantity", "Please enter a numeric grams value (e.g. 120).")
        return
      case Some(v) if v <= 0.0 =>
        showAlert(Alert.AlertType.WARNING, "Invalid quantity", "Please enter a positive grams value.")
        return
      case Some(v) =>
        val chosen = resolved.get
        Platform.runLater(() => {
          try {
            foodCombo.setValue(chosen)
            Option(foodCombo.getEditor).foreach(_.setText(Option(chosen.name).getOrElse("")))
          } catch { case _: Throwable => () }
        })
        resultOpt = Some((chosen, v))
        Option(okButton).flatMap(b => Option(b.getScene)).flatMap(s => Option(s.getWindow)).foreach(w => w.hide())
    }
  }

  // Called by FXML Cancel button.
  @FXML
  def handleCancel(): Unit = {
    resultOpt = None
    Option(cancelButton).flatMap(b => Option(b.getScene)).flatMap(s => Option(s.getWindow)).foreach(w => w.hide())
  }

  // Called by consumers after dialog closes to get the chosen food and grams (if any).
  def getResult: Option[(FoodItem, Double)] = resultOpt

  private def showAlert(tp: Alert.AlertType, header: String, content: String): Unit = {
    val a = new Alert(tp)
    a.setHeaderText(header)
    a.setContentText(content)
    a.showAndWait()
  }
}
