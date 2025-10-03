// Controller for the main dashboard located in the 'isaac.view' package
package isaac.view
// Imports JavaFX FXML utilities, layout components, user interface (UI) controls, input/output (I/O) handling, storage helpers, model classes, and Scala utilities for collections and error handling
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.layout.AnchorPane
import javafx.scene.Node
import java.io.IOException
import javafx.scene.control.{TabPane, ListView}
import isaac.util.{FoodItemStorage, RecipeStorage}
import isaac.model.Recipe
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

// DashboardController loads the main subviews into the dashboard tabs, loads FXML subviews, and wires controllers for food, recipes, and user profiles
class DashboardController {
  @FXML
  private var tabPane: TabPane = _

  @FXML
  private var foodListContainer: AnchorPane = _

  @FXML
  private var recipeListContainer: AnchorPane = _

  @FXML
  private var recipeBuilderContainer: AnchorPane = _

  @FXML
  private var userProfileContainer: AnchorPane = _

  // Keep references to controllers/roots for reuse and programmatic interactions
  private var foodListControllerOpt: Option[Any] = None
  private var recipeListControllerOpt: Option[Any] = None
  private var recipeEditorControllerOpt: Option[Any] = None
  private var recipeEditorRootOpt: Option[Node] = None
  private var userProfileControllerOpt: Option[Any] = None

  // Toggle this to enable auto-opening editor when selecting an item from the Recipe List
  private val enableAutoOpenEditorOnListSelect: Boolean = false

  @FXML
  def initialize(): Unit = {
    // Load food list view
    val foodLoad = loadViewWithController("/isaac/view/FoodListView.fxml", foodListContainer)
    foodListControllerOpt = foodLoad.map(_._1)

    foodListControllerOpt.foreach { ctrl =>
      Try {
        val storedItems = FoodItemStorage.loadAll()
        invokeIfExists(
          ctrl,
          "setInitialFoodItems",
          Array(storedItems.asJava),
          classOf[java.util.List[_]].asInstanceOf[Class[_]]
        )
      } match {
        case Failure(ex) => ex.printStackTrace()
        case Success(_)  => // OK
      }
    }

    // Load recipe list (browser)
    val recipeListLoad = loadViewWithController("/isaac/view/RecipeListView.fxml", recipeListContainer)
    recipeListControllerOpt = recipeListLoad.map(_._1)

    // Load recipe editor (builder)
    val recipeEditorLoad = loadViewWithController("/isaac/view/RecipeEditorView.fxml", recipeBuilderContainer)
    recipeEditorControllerOpt = recipeEditorLoad.map(_._1)
    recipeEditorRootOpt = recipeEditorLoad.map(_._2)

    // Load user profile
    val userProfileLoad = loadViewWithController("/isaac/view/UserProfile.fxml", userProfileContainer)
    userProfileControllerOpt = userProfileLoad.map(_._1)

    // Optionally wire selection in recipe list to load into editor
    if (enableAutoOpenEditorOnListSelect) {
      tryWireRecipeListSelection()
    }
  }

  // Public helper methods
  // Refresh all dashboard subviews
  def refreshAllViews(): Unit = {
    refreshFoodList()
    refreshRecipeList()
    refreshEditor()
    refreshUserProfile()
  }
  // Refresh the food list view
  def refreshFoodList(): Unit = {
    foodListControllerOpt.foreach { ctrl =>
      if (!invokeIfExists(ctrl, "refresh", Array.empty[AnyRef])) {
        Try {
          val storedItems = FoodItemStorage.loadAll()
          invokeIfExists(
            ctrl,
            "setInitialFoodItems",
            Array(storedItems.asJava),
            classOf[java.util.List[_]].asInstanceOf[Class[_]]
          )
        }.failed.foreach(_.printStackTrace())
      }
    }
  }
  // Refresh the recipe list view
  def refreshRecipeList(): Unit = {
    recipeListControllerOpt.foreach { ctrl =>
      val tried = invokeIfExists(ctrl, "refreshSavedRecipesList", Array.empty[AnyRef])
      if (!tried) {
        invokeIfExists(ctrl, "refreshList", Array.empty[AnyRef])
        invokeIfExists(ctrl, "loadAll", Array.empty[AnyRef])
      }
    }
  }
  // Refresh the recipe editor view
  def refreshEditor(): Unit = {
    recipeEditorControllerOpt.foreach { ctrl =>
      invokeIfExists(ctrl, "refresh", Array.empty[AnyRef])
    }
  }
  // Refresh the user profile view
  def refreshUserProfile(): Unit = {
    userProfileControllerOpt.foreach { ctrl =>
      invokeIfExists(ctrl, "refresh", Array.empty[AnyRef])
    }
  }
  // Open the recipe editor for a given recipe, falling back if controller method is missing
  def openEditorForRecipe(recipe: Recipe): Unit = {
    selectTabContaining(recipeBuilderContainer)
    val result = for {
      editorCtrl <- recipeEditorControllerOpt
      editorRoot <- recipeEditorRootOpt
    } yield {
      val invoked = invokeIfExists(
        editorCtrl,
        "loadRecipe",
        Array(recipe.asInstanceOf[AnyRef]),
        classOf[Recipe].asInstanceOf[Class[_]]
      )
      if (!invoked) {
        fallbackPopulateEditor(editorRoot, recipe)
      }
    }
    if (recipeEditorControllerOpt.isEmpty && recipeEditorRootOpt.nonEmpty) {
      fallbackPopulateEditor(recipeEditorRootOpt.get, recipe)
    }
  }

  // Internal wiring helpers
  // Wire recipe list selection to auto-open editor
  private def tryWireRecipeListSelection(): Unit = {
    try {
      val listViewNode = Option(recipeListContainer).flatMap { c =>
        Option(c.lookup("#recipeList"))
      }
      listViewNode match {
        case Some(node) if node.isInstanceOf[ListView[_]] =>
          val lv = node.asInstanceOf[ListView[String]]
          lv.getSelectionModel.selectedItemProperty().addListener((_, _, newVal) => {
            if (newVal != null) {
              val maybeRecipe = RecipeStorage.loadAll().find(_.name == newVal)
              maybeRecipe.foreach(openEditorForRecipe)
            }
          })
        case _ =>
      }
    } catch {
      case ex: Throwable => ex.printStackTrace()
    }
  }
  // Selects the tab containing a given container
  private def selectTabContaining(container: AnchorPane): Unit = {
    Option(tabPane).foreach { tp =>
      val tabs = tp.getTabs
      val idx = (0 until tabs.size()).find { i =>
        val t = tabs.get(i)
        Option(t.getContent).exists(_ == container)
      }
      idx.foreach(i => tp.getSelectionModel.select(i))
    }
  }
  // Load an FXML view into a container and return its controller and root node
  private def loadViewWithController(fxmlPath: String, container: AnchorPane): Option[(Any, Node)] = {
    try {
      val loader = new FXMLLoader(getClass.getResource(fxmlPath))
      val view: Node = loader.load()
      container.getChildren.setAll(view)
      AnchorPane.setTopAnchor(view, 0.0)
      AnchorPane.setBottomAnchor(view, 0.0)
      AnchorPane.setLeftAnchor(view, 0.0)
      AnchorPane.setRightAnchor(view, 0.0)
      val ctrl = loader.getController
      try {
        println(
          s"[DEBUG] loadViewWithController loaded fxml=$fxmlPath controller=${if (ctrl == null) "null" else ctrl.getClass.getName + "@" + System.identityHashCode(ctrl)} view=${System.identityHashCode(view)}"
        )
      } catch { case _: Throwable => () }
      Some((ctrl, view))
    } catch {
      case ex: IOException =>
        ex.printStackTrace()
        None
      case ex: Throwable =>
        ex.printStackTrace()
        None
    }
  }
  // Invoke a method on a controller if it exists, matching by name and parameter types
  private def invokeIfExists(target: Any, methodName: String, args: Array[AnyRef], paramTypes: Class[_]*): Boolean = {
    Try {
      val methodOpt = target.getClass.getMethods.find(m =>
        m.getName == methodName && (paramTypes.isEmpty || {
          val mParamTypes = m.getParameterTypes
          mParamTypes.length == paramTypes.length &&
            mParamTypes.zip(paramTypes).forall { case (a, b) => a.isAssignableFrom(b) }
        })
      )
      methodOpt match {
        case Some(m) =>
          m.invoke(target, args: _*)
          true
        case None =>
          false
      }
    } match {
      case Success(v) => v
      case Failure(ex) =>
        ex.printStackTrace()
        false
    }
  }
  // Populate editor UI with recipe details if the controller method is unavailable
  private def fallbackPopulateEditor(editorRoot: Node, recipe: Recipe): Unit = {
    try {
      val nameNode = editorRoot.lookup("#recipeNameField")
      if (nameNode != null && nameNode.isInstanceOf[javafx.scene.control.TextField]) {
        nameNode.asInstanceOf[javafx.scene.control.TextField].setText(recipe.name)
      }
      val instrNode = editorRoot.lookup("#instructionsArea")
      if (instrNode != null && instrNode.isInstanceOf[javafx.scene.control.TextArea]) {
        instrNode.asInstanceOf[javafx.scene.control.TextArea].setText(recipe.instructions.mkString("\n"))
      }
      val tableNode = editorRoot.lookup("#ingredientTable")
      if (tableNode != null && tableNode.isInstanceOf[javafx.scene.control.TableView[_]]) {

      }
    } catch {
      case _: Throwable => // Ignore
    }
  }
}
