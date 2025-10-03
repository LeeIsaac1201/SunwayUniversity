// Food list controller located in the 'isaac.view' package
package isaac.view
// JavaFX imports for FXML, user interface (UI) controls, layouts, stages, events, database, models, and file storage
import javafx.application.Platform
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control._
import javafx.scene.control.cell.TextFieldTableCell
import javafx.collections.{FXCollections, ObservableList}
import javafx.beans.property.{SimpleDoubleProperty, SimpleObjectProperty, SimpleStringProperty}
import javafx.beans.value.ObservableValue
import javafx.stage.{Modality, Stage, FileChooser}
import javafx.scene.{Parent, Scene}
import javafx.scene.image.{Image, ImageView}
import isaac.model._
import isaac.util.RecipeStorage
import isaac.util.FoodItemStorage
import java.util.UUID
import scala.jdk.CollectionConverters._
import javafx.util.converter.DoubleStringConverter
import scala.collection.mutable
import java.io.File

// Additional imports used for styling dialog buttons
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.Node
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.DialogPane
import javafx.scene.control.ButtonType
import javafx.scene.control.Label

//noinspection VarCouldBeVal
class RecipeEditorController {
  // UI components linked to FXML
  @FXML private var savedRecipesList: ListView[String] = _
  @FXML private var recipeNameField: TextField = _
  @FXML private var removeButton: Button = _
  @FXML private var saveButton: Button = _
  @FXML private var selectImageButton: Button = _
  @FXML private var recipeImageView: ImageView = _
  @FXML private var ingredientTable: TableView[IngredientRow] = _
  @FXML private var foodColumn: TableColumn[IngredientRow, String] = _
  @FXML private var qtyColumn: TableColumn[IngredientRow, java.lang.Double] = _
  @FXML private var nutrientsColumn: TableColumn[IngredientRow, String] = _
  @FXML private var totalCaloriesLabel: Label = _
  @FXML private var summaryArea: TextArea = _
  @FXML private var instructionsArea: TextArea = _

  // Backing list of ingredient rows
  private val data: ObservableList[IngredientRow] = FXCollections.observableArrayList[IngredientRow]()

  // Keep track of attached listeners so they can be removed
  private val listenerMap: mutable.Map[IngredientRow, javafx.beans.value.ChangeListener[Number]] = mutable.Map.empty

  // Store optional recipe image path when user selects one (not persisted unless Recipe model is extended)
  private var recipeImagePath: Option[String] = None

  // Flag to track if initialisation is completed successfully
  private var isInitialized = false

  // Stable wrapper class for each table row, keeps properties reactive
  case class IngredientRow(food: FoodItem, gramsProp: SimpleDoubleProperty) {
    // Stable properties (don't need to create new SimpleStringProperty each time)
    val nameProperty: SimpleStringProperty = new SimpleStringProperty(food.name)
    val gramsProperty: SimpleDoubleProperty = gramsProp
    val nutrientsProperty: SimpleStringProperty = new SimpleStringProperty(computeNutrients())

    // When grams change, update nutrientsProperty
    gramsProperty.addListener((_, _, _) => nutrientsProperty.set(computeNutrients()))

    private def computeNutrients(): String = {
      val qty = gramsProperty.get()
      val scale: Double =
        if (food.servingSize <= 0.0) 0.0 else qty / food.servingSize
      val scaled: Seq[Nutrient] = food.nutrients.map {
        case m: Macronutrient => m.copy(amount = m.amount * scale)
        case m: Micronutrient => m.copy(amount = m.amount * scale)
        case f: Fibre         => f.copy(amount = f.amount * scale)
        case s: Sugar         => s.copy(amount = s.amount * scale)
        case other            => other
      }
      scaled.map(n => s"${n.name}: ${formatDouble(n.amount)} ${n.unit}").mkString(", ")
    }
  }

  private def formatDouble(d: Double): String = {
    val rounded = Math.round(d * 100.0) / 100.0
    if (rounded % 1 == 0) rounded.toInt.toString else f"$rounded%.2f"
  }

  @FXML
  def initialize(): Unit = {
    println(s"[DEBUG] RecipeEditorController.initialize() called. ingredientTable null? ${ingredientTable == null}")

    // Ensure we don't double-initialize
    if (isInitialized) {
      println("[DEBUG] Controller has already been initialised, skipping.")
      return
    }

    try {
      initializeTable()
      initializeComponents()
      isInitialized = true
      println("[DEBUG] RecipeEditorController initialisation completed successfully.")
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        println(s"[ERROR] Failed to initialise RecipeEditorController: ${ex.getMessage}")
    }
  }

  private def initializeTable(): Unit = {
    // Set up table safely if present
    if (ingredientTable != null) {
      println("[DEBUG] Setting up ingredientTable...")
      ingredientTable.setEditable(true)
      // Ensure the table uses backing list
      ingredientTable.setItems(data)

      // Food name column - use stable nameProperty
      if (foodColumn != null) {
        foodColumn.setCellValueFactory(
          (cell: TableColumn.CellDataFeatures[IngredientRow, String]) =>
            // SimpleStringProperty implements ObservableValue[String]
            cell.getValue.nameProperty.asInstanceOf[ObservableValue[String]]
        )
      }

      // qty column: Bind to gramsProperty.asObject so edits reflect on the property
      if (qtyColumn != null) {
        qtyColumn.setCellValueFactory(
          (cell: TableColumn.CellDataFeatures[IngredientRow, java.lang.Double]) =>
            try {
              // SimpleDoubleProperty.asObject returns ObservableValue[java.lang.Double]
              cell.getValue.gramsProperty.asObject().asInstanceOf[ObservableValue[java.lang.Double]]
            } catch {
              case _: Throwable =>
                new SimpleObjectProperty[java.lang.Double](Double.box(cell.getValue.gramsProperty.get())).asInstanceOf[ObservableValue[java.lang.Double]]
            }
        )
        // Use DoubleStringConverter for editing doubles
        qtyColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()).asInstanceOf[javafx.util.Callback[TableColumn[IngredientRow, java.lang.Double], TableCell[IngredientRow, java.lang.Double]]])
        qtyColumn.setOnEditCommit((evt: TableColumn.CellEditEvent[IngredientRow, java.lang.Double]) => {
          val row = evt.getRowValue
          val newValPrimitive: Double =
            Option(evt.getNewValue).map(_.doubleValue()).orElse(Option(evt.getOldValue).map(_.doubleValue())).getOrElse(0.0)
          val sanitized: Double = if (newValPrimitive < 0.0) 0.0 else newValPrimitive
          row.gramsProperty.set(sanitized)
          safeRefreshTable()
          updateSummary()
        })
      }

      // Nutrients column - stable property
      if (nutrientsColumn != null) {
        nutrientsColumn.setCellValueFactory(
          (cell: TableColumn.CellDataFeatures[IngredientRow, String]) =>
            cell.getValue.nutrientsProperty.asInstanceOf[ObservableValue[String]]
        )
      }

      // Listen for changes to data: attach/detach listeners
      data.addListener(new javafx.collections.ListChangeListener[IngredientRow] {
        override def onChanged(c: javafx.collections.ListChangeListener.Change[_ <: IngredientRow]): Unit = {
          while (c.next()) {
            c.getRemoved.asScala.foreach { removedRow =>
              listenerMap.get(removedRow).foreach { cl =>
                removedRow.gramsProperty.removeListener(cl)
              }
              listenerMap -= removedRow
            }

            // Added: Create and attach listener (keeps summary / refresh in sync)
            c.getAddedSubList.asScala.foreach { addedRow =>
              val cl = new javafx.beans.value.ChangeListener[Number] {
                override def changed(obs: javafx.beans.value.ObservableValue[_ <: Number], oldV: Number, newV: Number): Unit = {
                  safeRefreshTable()
                  updateSummary()
                }
              }
              addedRow.gramsProperty.addListener(cl)
              listenerMap += (addedRow -> cl)
            }
          }
          updateSummary()
        }
      })

      ingredientTable.setPlaceholder(new Label("No ingredients added. Click on the Add Ingredient button to start."))
      println("[DEBUG] ingredientTable setup completed successfully.")
    } else {
      println("[ERROR] RecipeEditorController.initialize() ingredientTable is NULL.")
      throw new IllegalStateException("ingredientTable was not injected properly by FXML.")
    }
  }

  // ---------------------------
  // Styling helpers (buttons + dialog buttons)
  // ---------------------------
  private def applyButtonStyle(btn: Button, baseColor: String, hoverColor: String): Unit = {
    if (btn == null) return
    val baseStyle =
      s"-fx-background-color: $baseColor; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12 6 12;"
    val hoverStyle =
      s"-fx-background-color: $hoverColor; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12 6 12;"
    btn.setStyle(baseStyle)
    btn.setOnMouseEntered(_ => btn.setStyle(hoverStyle))
    btn.setOnMouseExited(_ => btn.setStyle(baseStyle))
  }

  private def styleAlertDialog(alert: Alert, primaryButton: ButtonType, primaryColor: String): Unit = {
    // dialog pane background + padding
    val pane: DialogPane = alert.getDialogPane
    pane.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 12;")

    // style content label if present
    val contentNode = pane.lookup(".content.label")
    if (contentNode != null && contentNode.isInstanceOf[Label]) {
      val lbl = contentNode.asInstanceOf[Label]
      lbl.setWrapText(true)
      lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-alignment: center; -fx-padding: 6 0 8 0;")
    }

    // style primary button
    val primaryBtnNode: Node = pane.lookupButton(primaryButton)
    if (primaryBtnNode != null) {
      primaryBtnNode.setStyle(s"-fx-background-color: $primaryColor; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12;")
      // add hover/pressed effects (simple)
      primaryBtnNode.setOnMouseEntered(_ => primaryBtnNode.setStyle(s"-fx-background-color: ${darken(primaryColor)}; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12;"))
      primaryBtnNode.setOnMouseExited(_ => primaryBtnNode.setStyle(s"-fx-background-color: $primaryColor; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12;"))
    }

    // style cancel button (if present)
    val cancelBtnNode = pane.lookupButton(ButtonType.CANCEL)
    if (cancelBtnNode != null) {
      cancelBtnNode.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #495057; -fx-font-weight: bold; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-background-radius: 6; -fx-padding: 6 12;")
      cancelBtnNode.setOnMouseEntered(_ => cancelBtnNode.setStyle("-fx-background-color: #f1f3f5; -fx-text-fill: #495057; -fx-font-weight: bold; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-background-radius: 6; -fx-padding: 6 12;"))
      cancelBtnNode.setOnMouseExited(_ => cancelBtnNode.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #495057; -fx-font-weight: bold; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-background-radius: 6; -fx-padding: 6 12;"))
    }
  }

  // Tiny helper to darken a hex color roughly (very simple; assumes #RRGGBB)
  private def darken(hex: String, amount: Double = 0.12): String = {
    try {
      val h = hex.stripPrefix("#")
      val r = Integer.parseInt(h.substring(0, 2), 16)
      val g = Integer.parseInt(h.substring(2, 4), 16)
      val b = Integer.parseInt(h.substring(4, 6), 16)
      def clamp(x: Int) = Math.max(0, Math.min(255, x))
      val nr = clamp((r * (1.0 - amount)).toInt)
      val ng = clamp((g * (1.0 - amount)).toInt)
      val nb = clamp((b * (1.0 - amount)).toInt)
      f"#${nr}%02x${ng}%02x${nb}%02x"
    } catch {
      case _: Throwable => hex
    }
  }

  /**
   * Show a styled alert. Returns the chosen ButtonType if present.
   *
   * - alertType: type of alert (INFORMATION, WARNING, ERROR, CONFIRMATION)
   * - header: header text (can be null)
   * - content: main content text
   * - primaryText: text for the primary button (default "Okay")
   * - primaryColor: hex color for primary button background
   * - includeCancel: whether to include a Cancel button
   */
  private def showStyledAlert(alertType: AlertType, header: String, content: String,
                              primaryText: String = "Okay", primaryColor: String = "#4CAF50",
                              includeCancel: Boolean = false): Option[ButtonType] = {
    val primary = new ButtonType(primaryText, ButtonData.OK_DONE)
    val types = if (includeCancel) Array(primary, ButtonType.CANCEL) else Array(primary)
    val alert = new Alert(alertType, "", types: _*)
    alert.setHeaderText(header)
    alert.setContentText(content)

    // Apply styling using our helper
    styleAlertDialog(alert, primary, primaryColor)

    val res = alert.showAndWait()
    if (res.isPresent) Some(res.get()) else None
  }

  // ---------------------------
  // End styling helpers
  // ---------------------------

  private def initializeComponents(): Unit = {
    // Saved recipes list
    if (savedRecipesList != null) {
      refreshSavedRecipesList()
      savedRecipesList.getSelectionModel.selectedItemProperty().addListener((_, _, newValue) => {
        if (newValue != null) loadRecipeByName(newValue)
      })
    }

    // Wire buttons
    if (removeButton != null) {
      removeButton.setOnAction(_ => handleRemoveSelected())
      // apply red style for remove
      applyButtonStyle(removeButton, "#f44336", "#d32f2f")
    }
    if (selectImageButton != null) {
      selectImageButton.setOnAction(_ => handleSelectImage())
      // slightly purple style for image picker
      applyButtonStyle(selectImageButton, "#9C27B0", "#7B1FA2")
    }
    if (saveButton != null) {
      saveButton.setOnAction(_ => handleSaveRecipe())
      // apply green style for save
      applyButtonStyle(saveButton, "#4CAF50", "#43A047")
    }
    if (totalCaloriesLabel != null) totalCaloriesLabel.setText("Total calories: Not available.")
    if (summaryArea != null) summaryArea.setText("")
  }

  // Safe method to refresh table that checks for null
  private def safeRefreshTable(): Unit = {
    Option(ingredientTable).foreach { table =>
      try {
        table.refresh()
      } catch {
        case ex: Exception =>
          println(s"[WARNING] Failed to refresh table: ${ex.getMessage}")
      }
    }
  }

  // Method to verify controller state before operations
  private def ensureInitialized(): Boolean = {
    if (!isInitialized || ingredientTable == null) {
      println("[ERROR] Controller not properly initialised or ingredientTable is null.")

      // Try to show error to user using styled dialog
      Platform.runLater(() => {
        showStyledAlert(Alert.AlertType.ERROR, "Controller Error",
          "Recipe editor is not properly initialised. Please close and reopen the recipe editor.",
          "Okay", "#e74c3c", includeCancel = false)
      })
      return false
    }
    true
  }

  // User Actions

  @FXML
  def handleAddIngredient(): Unit = {
    println(s"[DEBUG] handleAddIngredient called. ingredientTable null? ${ingredientTable == null}")
    if (!ensureInitialized()) return
    val resourceCandidates = Seq("/isaac/view/AddIngredientView.fxml", "/isaac/view/AddIngredientDialog.fxml")
    val loaderOpt = resourceCandidates.view.flatMap { rp =>
      Option(getClass.getResource(rp)).map(_ => new FXMLLoader(getClass.getResource(rp)))
    }.headOption

    loaderOpt match {
      case None =>
        showStyledAlert(Alert.AlertType.ERROR, "Dialog not found.",
          "Add ingredient dialog FXML not found. Expected /isaac/view/AddIngredientView.fxml or AddIngredientDialog.fxml",
          "Okay", "#e74c3c", includeCancel = false)
      case Some(loader) =>
        try {
          val root: Parent = loader.load()
          val dialog = new Stage()
          dialog.initModality(Modality.APPLICATION_MODAL)
          dialog.setTitle("Add Ingredient")
          dialog.setScene(new Scene(root))

          // Get parent window for proper modal behaviour
          if (ingredientTable.getScene != null && ingredientTable.getScene.getWindow != null) {
            dialog.initOwner(ingredientTable.getScene.getWindow)
          }

          val ctrl = loader.getController.asInstanceOf[AddIngredientController]

          // Inject current food items (from FoodItemStorage or other in-memory source)
          try {
            val items = loadFoodItems()
            val anyCtrl: AnyRef = ctrl.asInstanceOf[AnyRef]
            val cls = anyCtrl.getClass
            val setItemsM = cls.getMethods.find(m => m.getName == "setItems" && m.getParameterCount == 1).orNull
            if (setItemsM != null) {
              try setItemsM.invoke(anyCtrl, items) catch { case _: Throwable => () }
            } else {
              val refreshM = cls.getMethods.find(m => m.getName == "refreshItems" && m.getParameterCount == 0).orNull
              if (refreshM != null) try refreshM.invoke(anyCtrl) catch { case _: Throwable => () }
            }
          } catch {
            case ex: Throwable =>
              println(s"[WARNING] Failed to inject food items: ${ex.getMessage}.")
              ex.printStackTrace()
          }

          dialog.showAndWait()

          // Get result to ensure still initialised after dialog
          if (ensureInitialized()) {
            try {
              val res = ctrl.getResult
              res.foreach { case (food, grams) =>
                handleAddIngredientResult(food, grams)
              }
            } catch {
              case ex: Throwable =>
                println(s"[ERROR] Failed to process dialog result: ${ex.getMessage}.")
                ex.printStackTrace()
            }
          }
        } catch {
          case ex: Exception =>
            println(s"[ERROR] Failed to open add ingredient dialog: ${ex.getMessage}.")
            ex.printStackTrace()
            showStyledAlert(Alert.AlertType.ERROR, "Dialog Error",
              s"Failed to open add ingredient dialog: ${ex.getMessage}", "Okay", "#e74c3c", includeCancel = false)
        }
    }
  }

  // Remove currently selected ingredient row (if any).
  @FXML
  def handleRemoveSelected(): Unit = {
    if (!ensureInitialized()) return

    try {
      val sel = ingredientTable.getSelectionModel.getSelectedItem
      if (sel == null) {
        showStyledAlert(Alert.AlertType.INFORMATION, "No selection.",
          "Please select an ingredient row to remove.", "Okay", "#f44336", includeCancel = false)
        return
      }
      data.remove(sel)
      safeRefreshTable()
      updateSummary()
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        showStyledAlert(Alert.AlertType.ERROR, "Error removing ingredient.",
          ex.getMessage, "Okay", "#e74c3c", includeCancel = false)
    }
  }

  @FXML
  def handleClearRecipe(): Unit = {
    if (!ensureInitialized()) return

    // Confirm with user before clearing, with styled dialog (primary = red "Okay")
    val resp = showStyledAlert(Alert.AlertType.CONFIRMATION, "Clear Recipe",
      "Are you sure you want to clear the current recipe? This action cannot be undone.",
      "Okay", "#f44336", includeCancel = true)

    if (resp.isDefined && resp.get.getButtonData == ButtonData.OK_DONE) {
      // Clear all fields
      Option(recipeNameField).foreach(_.clear())
      Option(instructionsArea).foreach(_.clear())
      Option(summaryArea).foreach(_.clear())
      Option(totalCaloriesLabel).foreach(_.setText("Total calories: Not available."))
      Option(recipeImageView).foreach(_.setImage(null))

      // Clear ingredients data
      data.clear()
      safeRefreshTable()

      // Clear image path
      recipeImagePath = None

      println("[DEBUG] Recipe cleared successfully.")
    }
  }

  @FXML
  def handleSaveRecipe(): Unit = {
    val nameOpt = Option(recipeNameField).flatMap(r => Option(r.getText)).map(_.trim).filter(_.nonEmpty)
    if (nameOpt.isEmpty) {
      showStyledAlert(Alert.AlertType.WARNING, "Missing recipe name",
        "Please enter a recipe name before saving.", "Okay", "#f39c12", includeCancel = false)
      return
    }
    if (data.isEmpty) {
      showStyledAlert(Alert.AlertType.WARNING, "No ingredients",
        "Add at least one ingredient before saving.", "Okay", "#f39c12", includeCancel = false)
      return
    }

    val ingredientsMap: Map[FoodItem, Double] = data.asScala.toList.map(r => r.food -> r.gramsProperty.get()).toMap

    val instructionsList: List[String] =
      Option(instructionsArea).map(_.getText).getOrElse("").split("\\r?\\n").map(_.trim).filter(_.nonEmpty).toList

    val recipe = Recipe(UUID.randomUUID(), nameOpt.get, ingredientsMap, instructionsList)

    RecipeStorage.save(recipe)
    Option(savedRecipesList).foreach(_.setItems(FXCollections.observableArrayList(RecipeStorage.loadAll().map(_.name).sorted.asJava)))
    showStyledAlert(Alert.AlertType.INFORMATION, "Saved",
      s"Recipe ${recipe.name} saved to disk.", "Okay", "#4CAF50", includeCancel = false)

    try {
      AppContext.notifyFoodListChanged()
    } catch {
      case _: Throwable => ()
    }
  }

  // Image selector
  private def handleSelectImage(): Unit = {
    try {
      val fc = new FileChooser()
      fc.setTitle("Select recipe image")
      fc.getExtensionFilters.addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
      )
      val win = if (selectImageButton != null && selectImageButton.getScene != null) selectImageButton.getScene.getWindow else null
      val file: File = if (win != null) fc.showOpenDialog(win) else fc.showOpenDialog(null)
      if (file != null) {
        recipeImagePath = Some(file.getAbsolutePath)
        if (recipeImageView != null) {
          recipeImageView.setImage(new Image(file.toURI.toString))
        }
      }
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        showStyledAlert(Alert.AlertType.ERROR, "Image selection failed", ex.getMessage, "Okay", "#e74c3c", includeCancel = false)
    }
  }

  // Helpers

  private def refreshSavedRecipesList(): Unit = {
    val names = RecipeStorage.loadAll().map(_.name).sorted
    Option(savedRecipesList).foreach(_.setItems(FXCollections.observableArrayList(names.asJava)))
  }

  private def loadRecipeByName(name: String): Unit = {
    if (!ensureInitialized()) return

    val opt = RecipeStorage.loadAll().find(_.name == name)
    opt.foreach { recipe =>
      Option(recipeNameField).foreach(_.setText(recipe.name))
      data.clear()
      recipe.ingredients.foreach { case (food, grams) =>
        data.add(IngredientRow(food, new SimpleDoubleProperty(grams)))
      }
      ingredientTable.setItems(data)
      safeRefreshTable()
      Option(instructionsArea).foreach(_.setText(recipe.instructions.mkString("\n")))
      updateSummary()
    }
  }

  private def updateSummary(): Unit = {
    val ingredientsMap = data.asScala.toList.map(r => r.food -> r.gramsProperty.get()).toMap
    val tempRecipe = Recipe(UUID.randomUUID(), Option(recipeNameField).map(_.getText).getOrElse(""), ingredientsMap, Nil)
    val totals: Seq[Nutrient] = tempRecipe.totalNutrients
    val sb = new StringBuilder
    totals.foreach { n =>
      sb.append(s"${n.name}: ${formatDouble(n.amount)} ${n.unit}\n")
    }
    Option(summaryArea).foreach(_.setText(sb.toString()))

    val caloriesOpt: Option[Double] = {
      totals.find(n => n.name.toLowerCase.contains("calor") || Option(n.unit).exists(_.toLowerCase == "kcal")).map(_.amount)
        .orElse {
          def findByName(names: Seq[String]) = totals.find(t => names.exists(nm => t.name.equalsIgnoreCase(nm))).map(_.amount).getOrElse(0.0)
          val protein = findByName(Seq("Protein", "protein", "Proteins"))
          val fat     = findByName(Seq("Fat", "fat", "Total lipid (fat)"))
          val carbs   = findByName(Seq("Carbohydrate", "Carbohydrates", "Carbs", "carbohydrate", "carbohydrates"))
          val estimated = (protein * 4.0) + (carbs * 4.0) + (fat * 9.0)
          if (estimated > 0.0) Some(estimated) else None
        }
    }

    caloriesOpt match {
      case Some(cals) => Option(totalCaloriesLabel).foreach(_.setText(s"Total calories: ${formatDouble(cals)} kcal"))
      case None => Option(totalCaloriesLabel).foreach(_.setText("Total calories: N/A"))
    }
  }

  private def handleAddIngredientResult(food: FoodItem, grams: Double): Unit = {
    println(s"[DEBUG] handleAddIngredientResult called with food: ${food.name}, grams: $grams")
    println(s"[DEBUG] ingredientTable null? ${ingredientTable == null}")
    println(s"[DEBUG] data list size before: ${data.size()}")

    if (!ensureInitialized()) {
      println("[ERROR] Controller not initialised in handleAddIngredientResult.")
      return
    }

    if (grams <= 0) {
      showStyledAlert(Alert.AlertType.WARNING, "Invalid quantity", "Please enter a positive grams value.", "Okay", "#f39c12", includeCancel = false)
    } else {
      val existing = data.asScala.find(_.food == food)
      existing match {
        case Some(row) =>
          println(s"[DEBUG] Found existing row for ${food.name}, updating quantity.")
          row.gramsProperty.set(row.gramsProperty.get() + grams)
          Platform.runLater(() => {
            safeRefreshTable()
            updateSummary()
          })
        case None =>
          println(s"[DEBUG] Adding new row for ${food.name}")
          val row = IngredientRow(food, new SimpleDoubleProperty(grams))
          Platform.runLater(() => {
            println(s"[DEBUG] Adding row to data list. Current size: ${data.size()}.")
            data.add(row)
            println(s"[DEBUG] Data list size after adding: ${data.size()}.")

            // Ensure the visible table is using our list and refresh/select the new row
            ingredientTable.setItems(data)
            try {
              ingredientTable.getSelectionModel.select(row)
              ingredientTable.scrollTo(row)
            } catch {
              case ex: Exception =>
                println(s"[WARNING] Failed to select/scroll to new row: ${ex.getMessage}.")
            }
            safeRefreshTable()
            updateSummary()

            println(s"[DEBUG] Table items count: ${ingredientTable.getItems.size()}")
          })
      }
    }
  }

  private def loadFoodItems(): List[FoodItem] = {
    try {
      val raw = FoodItemStorage.loadAll()
      raw match {
        case jl: java.util.Collection[_] => jl.asInstanceOf[java.util.Collection[FoodItem]].asScala.toList
        case seq: Seq[_] => seq.asInstanceOf[Seq[FoodItem]].toList
        case _ =>
          try {
            raw.asInstanceOf[java.util.List[FoodItem]].asScala.toList
          } catch {
            case _: Throwable => Nil
          }
      }
    } catch {
      case _: Throwable =>
        try {
          val j = FoodItemStorage.loadAll().asInstanceOf[java.util.List[FoodItem]]
          j.asScala.toList
        } catch {
          case _: Throwable => Nil
        }
    }
  }

  // Public methods for external access (if needed)
  def getIngredients: List[(FoodItem, Double)] = {
    data.asScala.toList.map(r => r.food -> r.gramsProperty.get())
  }

  def addIngredient(food: FoodItem, grams: Double): Unit = {
    handleAddIngredientResult(food, grams)
  }

  def isTableInitialized: Boolean = isInitialized && ingredientTable != null
}
