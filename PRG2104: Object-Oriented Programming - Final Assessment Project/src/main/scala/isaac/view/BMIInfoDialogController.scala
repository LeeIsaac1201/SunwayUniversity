// Controller for the Body Mass Index (BMI) Information dialog, located in the 'isaac.view' package
package isaac.view
// Imports JavaFX stage handling and FXML injection
import javafx.fxml.FXML
import javafx.stage.Stage

// Defines the controller for the BMI info dialog
class BMIInfoDialogController {
  // Reference to the dialog's Stage (set from outside)
  private var dialogStage: Stage = _
  // Setter for the stage, called when dialog is created
  def setDialogStage(stage: Stage): Unit = dialogStage = stage
  // Closes the dialog when the user clicks the close button
  @FXML
  private def closeDialog(): Unit = {
    if (dialogStage != null) dialogStage.close()
  }
}
