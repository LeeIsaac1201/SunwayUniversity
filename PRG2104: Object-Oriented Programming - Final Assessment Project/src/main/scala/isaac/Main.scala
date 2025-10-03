// Main application entry point located in the 'isaac' package
package isaac
// Loads JavaFX FXML files, defines ScalaFX application components, and imports the storage helper to load saved items at startup
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import isaac.util.FoodItemStorage
// Entry point: Load the data and launch the welcome view
object Main extends JFXApp3:
  override def start(): Unit =
    FoodItemStorage.loadAll() // Load persisted food items (debug print occurs in loadAll)
    val loader = FXMLLoader(getClass.getResource("/isaac/view/WelcomeView.fxml")) // Retrieves the FXML resource from the resources folder
    val root: Parent = loader.load() // Loads the FXML content as a JavaFX Parent node
    // Sets up the main window (PrimaryStage) with a title and the loaded scene
    stage = new PrimaryStage:
      title = "NutriTrack: A Nutrition Information Database"
      scene = new Scene(root)
end Main
