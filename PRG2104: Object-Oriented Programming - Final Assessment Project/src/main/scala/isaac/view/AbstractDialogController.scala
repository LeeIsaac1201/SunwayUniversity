// Abstract base class for dialog controllers, located in the 'isaac.view' package
package isaac.view
// JavaFX imports for window stage and action events
import javafx.stage.Stage
import javafx.event.ActionEvent

// Abstract base class for all dialog controllers
abstract class AbstractDialogController {
  // Reference to the dialog's Stage (set when the window is created)
  protected var dialogStage: Stage = _

  // Allows external injection of stage
  def setDialogStage(stage: Stage): Unit = {
    dialogStage = stage
  }

  // Close the window (can be called by Save/Cancel buttons)
  protected def closeWindow(event: ActionEvent): Unit = {
    val stage = event.getSource
      .asInstanceOf[javafx.scene.Node]
      .getScene
      .getWindow
      .asInstanceOf[Stage]
    stage.close()
  }
}
