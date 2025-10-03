// Controller for the confirmation delete dialog, located in the 'isaac.view' package
package isaac.view
// Imports JavaFX controls, events, and stage handling
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.stage.Stage
import javafx.event.ActionEvent
import javafx.application.Platform
import javafx.scene.Node

// Handles user confirmation (Yes/Cancel) for deletion actions
class ConfirmDeleteController {
  @FXML private var messageLabel: Label = _ // Label to display confirmation message
  private var confirmed = false // Tracks whether user confirmed deletion

  @FXML
  def initialize(): Unit = {
    // ensure label wraps text if FXML already injected it
    if (messageLabel != null) {
      messageLabel.setWrapText(true)
    }
  }

  // Set the confirmation message text
  def setMessage(message: String): Unit = {
    // reset confirmed state each time a new message is applied
    confirmed = false

    // If label already injected, set immediately; otherwise schedule update on FX thread
    if (messageLabel != null) {
      messageLabel.setText(message)
    } else {
      Platform.runLater(() => if (messageLabel != null) messageLabel.setText(message))
    }
  }

  // Returns true if user confirmed deletion
  def isConfirmed: Boolean = confirmed

  // Handles 'Yes' button click: confirm and close dialog
  @FXML
  def handleYes(event: ActionEvent): Unit = {
    confirmed = true
    closeDialog(event)
  }

  // Handles 'Cancel' button click: cancel and close dialog
  @FXML
  def handleCancel(event: ActionEvent): Unit = {
    confirmed = false
    closeDialog(event)
  }

  // Closes the dialog window
  private def closeDialog(event: ActionEvent): Unit = {
    val node = event.getSource.asInstanceOf[Node]
    val stage = node.getScene.getWindow.asInstanceOf[Stage]
    // Hide instead of close to be a little gentler, which is consistent with common dialog patterns
    stage.hide()
  }
}
