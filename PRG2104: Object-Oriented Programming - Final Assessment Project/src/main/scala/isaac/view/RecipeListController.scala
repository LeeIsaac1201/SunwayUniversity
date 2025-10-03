// Controller for the recipe list view located in the 'isaac.view' package
package isaac.view
// Imports JavaFX FXML, UI controls, collections, layout, threading, and alert utilities
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.collections.FXCollections
import isaac.model.Recipe
import isaac.util.RecipeStorage
import scala.jdk.CollectionConverters._
import javafx.application.Platform
import javafx.scene.control.Alert.AlertType

// Controller that lists recipes, supports search, view, edit, delete and refresh operations
class RecipeListController {
  @FXML private var recipeListView: ListView[String] = _
  @FXML private var recipeCountLabel: Label = _
  @FXML private var refreshButton: Button = _
  @FXML private var deleteButton: Button = _
  @FXML private var viewButton: Button = _
  @FXML private var editButton: Button = _
  @FXML private var recipeDetailsArea: TextArea = _
  @FXML private var searchField: TextField = _
  // Store all recipes for filtering
  private var allRecipes: List[Recipe] = List.empty
  private var filteredRecipes: List[Recipe] = List.empty
  // Initialise the controller: set up list, search, buttons, and load recipes
  @FXML
  def initialize(): Unit = {
    println("[DEBUG] RecipeListController initializing...")
    try {
      setupRecipeList()
      setupSearchField()
      setupButtons()
      refreshRecipeList()
      println("[DEBUG] RecipeListController initialized successfully")
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        println(s"[ERROR] Failed to initialize RecipeListController: ${ex.getMessage}")
    }
  }
  // Configure the recipe list view with selection listener, placeholder, and button states
  private def setupRecipeList(): Unit = {
    if (recipeListView != null) {
      // Set up selection listener
      recipeListView.getSelectionModel.selectedItemProperty().addListener((_, _, selectedRecipe) => {
        updateRecipeDetails(selectedRecipe)
        updateButtonStates(selectedRecipe != null)
      })
      // Set placeholder
      recipeListView.setPlaceholder(new Label("No recipes found. Create your first recipe in the Recipe Editor!"))
      // Initially disable action buttons
      updateButtonStates(false)
    }
  }
  // Configure the search field with a prompt and filtering behaviour
  private def setupSearchField(): Unit = {
    if (searchField != null) {
      searchField.setPromptText("Search recipes...")
      searchField.textProperty().addListener((_, _, newValue) => {
        filterRecipes(newValue)
      })
    }
  }
  // Configure the button actions for refresh, delete, view, and edit
  private def setupButtons(): Unit = {
    if (refreshButton != null) refreshButton.setOnAction(_ => handleRefresh())
    if (deleteButton != null) deleteButton.setOnAction(_ => handleDelete())
    if (viewButton != null) viewButton.setOnAction(_ => handleView())
    if (editButton != null) editButton.setOnAction(_ => handleEdit())
  }
  // Enable or disable buttons based on whether a recipe is selected
  private def updateButtonStates(hasSelection: Boolean): Unit = {
    if (deleteButton != null) deleteButton.setDisable(!hasSelection)
    if (viewButton != null) viewButton.setDisable(!hasSelection)
    if (editButton != null) editButton.setDisable(!hasSelection)
  }
  // Handle refresh action: Reload recipes and update view
  @FXML
  def handleRefresh(): Unit = {
    println("[DEBUG] Refreshing recipe list...")
    refreshRecipeList()
  }
  // Handle delete action: Confirm and remove a recipe
  @FXML
  def handleDelete(): Unit = {
    val selectedRecipeName = recipeListView.getSelectionModel.getSelectedItem
    if (selectedRecipeName == null) return
    // Confirm deletion
    val alert = new Alert(AlertType.CONFIRMATION)
    alert.setTitle("Delete Recipe")
    alert.setHeaderText("Are you sure?")
    alert.setContentText(s"Do you want to delete the recipe '$selectedRecipeName'? This action cannot be undone.")
    val result = alert.showAndWait()
    if (result.isPresent && result.get() == ButtonType.OK) {
      // Find and delete the recipe
      allRecipes.find(_.name == selectedRecipeName) match {
        case Some(recipe) =>
          if (RecipeStorage.delete(recipe)) {
            showInfo("Recipe Deleted", s"Recipe '$selectedRecipeName' has been deleted successfully.")
            refreshRecipeList()
          } else {
            showError("Delete Failed", s"Failed to delete recipe '$selectedRecipeName'.")
          }
        case None =>
          showError("Recipe Not Found", s"Could not find recipe '$selectedRecipeName' to delete.")
      }
    }
  }
  // Handle view action: Open a detailed recipe dialog
  @FXML
  def handleView(): Unit = {
    val selectedRecipeName = recipeListView.getSelectionModel.getSelectedItem
    if (selectedRecipeName == null) return
    // Show detailed view in a new window/dialog
    allRecipes.find(_.name == selectedRecipeName) match {
      case Some(recipe) => showRecipeDetailsDialog(recipe)
      case None => showError("Recipe Not Found", s"Could not find recipe '$selectedRecipeName'.")
    }
  }
  // Handle edit action: Show a message to switch to the recipe editor
  @FXML
  def handleEdit(): Unit = {
    val selectedRecipeName = recipeListView.getSelectionModel.getSelectedItem
    if (selectedRecipeName == null) return
    // Load recipe in recipe editor
    allRecipes.find(_.name == selectedRecipeName) match {
      case Some(recipe) =>
        try {
          // Notify other components that a recipe should be edited
          println(s"[INFO] Edit recipe requested: ${recipe.name}")
          // For now, just show an info dialog
          val alert = new Alert(AlertType.INFORMATION)
          alert.setTitle("Edit Recipe")
          alert.setHeaderText("Switch to Recipe Editor")
          alert.setContentText(s"Please switch to the Recipe Editor tab to edit '${recipe.name}'.")
          alert.showAndWait()
        } catch {
          case ex: Exception =>
            showError("Edit Failed", s"Failed to open recipe for editing: ${ex.getMessage}")
        }
      case None =>
        showError("Recipe Not Found", s"Could not find recipe '$selectedRecipeName'.")
    }
  }
  // Refresh the recipe list: Load from storage, apply filters, update labels
  private def refreshRecipeList(): Unit = {
    try {
      // Load all recipes
      allRecipes = RecipeStorage.loadAll()
      // Apply current search filter
      val searchText = if (searchField != null) searchField.getText else ""
      filterRecipes(searchText)
      // Update count label
      updateRecipeCount()
      println(s"[INFO] Recipe list refreshed. Found ${allRecipes.length} recipes.")
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        showError("Load Failed", s"Failed to load recipes: ${ex.getMessage}")
        // Set empty list on failure
        allRecipes = List.empty
        filteredRecipes = List.empty
        updateRecipeListView()
        updateRecipeCount()
    }
  }
  // Filter recipes by search text (name or ingredient matches)
  private def filterRecipes(searchText: String): Unit = {
    if (searchText == null || searchText.trim.isEmpty) {
      filteredRecipes = allRecipes
    } else {
      val searchLower = searchText.toLowerCase
      filteredRecipes = allRecipes.filter { recipe =>
        recipe.name.toLowerCase.contains(searchLower) ||
          recipe.ingredients.exists(_._1.name.toLowerCase.contains(searchLower))
      }
    }

    updateRecipeListView()
  }
  // Update the list view with current filtered recipes
  private def updateRecipeListView(): Unit = {
    if (recipeListView != null) {
      Platform.runLater(() => {
        val recipeNames = filteredRecipes.map(_.name).asJava
        recipeListView.setItems(FXCollections.observableList(recipeNames))
      })
    }
  }
  // Update the recipe count label to show total and filtered counts
  private def updateRecipeCount(): Unit = {
    if (recipeCountLabel != null) {
      Platform.runLater(() => {
        val totalCount = allRecipes.length
        val displayedCount = filteredRecipes.length

        if (totalCount == displayedCount) {
          recipeCountLabel.setText(s"Total: $totalCount recipes")
        } else {
          recipeCountLabel.setText(s"Showing: $displayedCount of $totalCount recipes")
        }
      })
    }
  }
  // Update the recipe details area with selected recipe information
  private def updateRecipeDetails(recipeName: String): Unit = {
    if (recipeDetailsArea != null) {
      if (recipeName == null) {
        recipeDetailsArea.clear()
        return
      }

      allRecipes.find(_.name == recipeName) match {
        case Some(recipe) =>
          val details = buildRecipeDetailsText(recipe)
          Platform.runLater(() => recipeDetailsArea.setText(details))
        case None =>
          Platform.runLater(() => recipeDetailsArea.setText("Recipe details not available."))
      }
    }
  }
  // Build a formatted string with recipe details, ingredients, instructions, and nutrients
  private def buildRecipeDetailsText(recipe: Recipe): String = {
    val sb = new StringBuilder
    sb.append(s"Recipe: ${recipe.name}\n")
    sb.append(s"ID: ${recipe.id}\n\n")
    sb.append("Ingredients:\n")
    recipe.ingredients.foreach { case (food, quantity) =>
      sb.append(s"• ${food.name}: ${formatDouble(quantity)}g\n")
    }

    sb.append("\nInstructions:\n")
    recipe.instructions.zipWithIndex.foreach { case (instruction, index) =>
      sb.append(s"${index + 1}. $instruction\n")
    }

    sb.append("\nNutrition Summary:\n")
    val totalNutrients = recipe.totalNutrients
    totalNutrients.foreach { nutrient =>
      sb.append(s"${nutrient.name}: ${formatDouble(nutrient.amount)} ${nutrient.unit}\n")
    }

    sb.toString()
  }
  // Show a detailed recipe dialog with expandable content
  private def showRecipeDetailsDialog(recipe: Recipe): Unit = {
    val alert = new Alert(AlertType.INFORMATION)
    alert.setTitle(s"Recipe: ${recipe.name}")
    alert.setHeaderText(recipe.name)
    val details = buildRecipeDetailsText(recipe)
    val textArea = new TextArea(details)
    textArea.setEditable(false)
    textArea.setWrapText(true)
    textArea.setPrefSize(600, 400)
    alert.getDialogPane.setExpandableContent(textArea)
    alert.getDialogPane.setExpanded(true)
    alert.showAndWait()
  }
  // Utility method to show an error alert
  private def showError(title: String, message: String): Unit = {
    Platform.runLater(() => {
      val alert = new Alert(AlertType.ERROR)
      alert.setTitle(title)
      alert.setHeaderText("Error")
      alert.setContentText(message)
      alert.showAndWait()
    })
  }
  // Utility method to show an information alert
  private def showInfo(title: String, message: String): Unit = {
    Platform.runLater(() => {
      val alert = new Alert(AlertType.INFORMATION)
      alert.setTitle(title)
      alert.setHeaderText("Success")
      alert.setContentText(message)
      alert.showAndWait()
    })
  }
  // Utility method to format doubles to 2 decimal places or integer if whole
  private def formatDouble(d: Double): String = {
    val rounded = Math.round(d * 100.0) / 100.0
    if (rounded % 1 == 0) rounded.toInt.toString else f"$rounded%.2f"
  }

  // Public method to refresh from external sources (e.g., when a new recipe is saved)
  def notifyRecipeListChanged(): Unit = {
    Platform.runLater(() => refreshRecipeList())
  }
}
