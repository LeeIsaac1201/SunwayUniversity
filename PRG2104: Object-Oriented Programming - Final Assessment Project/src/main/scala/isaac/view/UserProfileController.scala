// Controller for the User Profile tab located in the 'isaac.view' package
package isaac.view
// Imports model classes for user profiles and storage utilities
import isaac.model.{UserProfile, UserProfileStorage}
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.stage.{Stage, Modality}
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.collections.{FXCollections, ObservableList}
// Imports Scala collection converters and input/output (I/O) utilities
import scala.jdk.CollectionConverters._
import java.io.IOException
import javafx.geometry.Insets
import javafx.scene.layout.VBox
import javafx.scene.text.Font

// Manages the User Profile tab: displays saved profiles and supports add, edit, and delete operations
class UserProfileController {

  // FXML fields bound to user interface (UI) elements
  @FXML private var userTable: TableView[UserProfile] = _
  @FXML private var usernameColumn: TableColumn[UserProfile, String] = _
  @FXML private var emailColumn: TableColumn[UserProfile, String] = _
  @FXML private var dobColumn: TableColumn[UserProfile, String] = _
  @FXML private var sexColumn: TableColumn[UserProfile, String] = _
  @FXML private var heightColumn: TableColumn[UserProfile, String] = _
  @FXML private var weightColumn: TableColumn[UserProfile, String] = _
  @FXML private var bmiColumn: TableColumn[UserProfile, String] = _
  @FXML private var activityLevelColumn: TableColumn[UserProfile, String] = _
  @FXML private var targetWeightColumn: TableColumn[UserProfile, String] = _
  @FXML private var dailyCalorieGoalColumn: TableColumn[UserProfile, String] = _
  @FXML private var dietaryPreferencesColumn: TableColumn[UserProfile, String] = _
  @FXML private var allergiesColumn: TableColumn[UserProfile, String] = _
  @FXML private var healthFlagsColumn: TableColumn[UserProfile, String] = _
  @FXML private var addButton: Button = _
  @FXML private var editButton: Button = _
  @FXML private var deleteButton: Button = _

  // Observable list that backs the user table
  private val userList: ObservableList[UserProfile] =
    FXCollections.observableArrayList[UserProfile]()

  // Called automatically by FXMLLoader after UI injection: configures columns and loads users
  def initialize(): Unit = {
    // Bind table columns to UserProfile properties
    usernameColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.username))
    emailColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.email.getOrElse("")))
    dobColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.dob.map(_.toString).getOrElse("")))
    sexColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.gender.getOrElse("")))
    heightColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.heightCm.map(_.toString).getOrElse("")))
    weightColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.weightKg.map(_.toString).getOrElse("")))
    bmiColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.bmi.map(_.toString).getOrElse("")))
    activityLevelColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.activityLevel.getOrElse("")))
    targetWeightColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.targetWeightKg.map(_.toString).getOrElse("")))
    dailyCalorieGoalColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.dailyCalorieGoal.map(_.toString).getOrElse("")))
    dietaryPreferencesColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.dietaryPreferences.mkString(", ")))
    allergiesColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.allergies.mkString(", ")))
    healthFlagsColumn.setCellValueFactory(c => new javafx.beans.property.SimpleStringProperty(c.getValue.healthFlags.mkString(", ")))
    // Set the table’s data source
    userTable.setItems(userList)

    // Enable/disable Edit and Delete based on selection
    userTable.getSelectionModel.selectedItemProperty.addListener((_, _, selected) => {
      val hasSelection = selected != null
      editButton.setDisable(!hasSelection)
      deleteButton.setDisable(!hasSelection)
    })

    loadUsers()
  }

  // Load all saved users from storage into the table
  private def loadUsers(): Unit = {
    userList.clear()
    val dir = java.nio.file.Paths.get("data", "users").toFile
    if (dir.exists && dir.isDirectory) {
      dir.listFiles((_, name) => name.endsWith(".txt")).foreach { f =>
        val username = f.getName.stripSuffix(".txt")
        UserProfileStorage.load(username).foreach(userList.add)
      }
    }
  }

  // Button handlers
  @FXML private def onAddUser(): Unit = {
    val newUser = showUserDialog(None)
    newUser.foreach { profile =>
      UserProfileStorage.save(profile)
      loadUsers()
    }
  }

  @FXML private def onEditUser(): Unit = {
    val selected = userTable.getSelectionModel.getSelectedItem
    if (selected != null) {
      val updatedUser = showUserDialog(Some(selected))
      updatedUser.foreach { profile =>
        UserProfileStorage.save(profile)
        loadUsers()
      }
    }
  }

  @FXML private def onDeleteUser(): Unit = {
    val selected = userTable.getSelectionModel.getSelectedItem
    if (selected != null) {
      // Create styled confirmation alert
      val confirm = new Alert(Alert.AlertType.CONFIRMATION) {
        initModality(Modality.APPLICATION_MODAL)
        setTitle("Confirm Delete")
        setHeaderText("Delete User")
        setContentText(null)
      }

      // Custom content with styled message
      val content = new VBox(10)
      content.setPadding(new Insets(10))
      val message = new Label(s"Are you sure you want to delete user '${selected.username}'?")
      message.setFont(new Font("Segoe UI", 14))
      content.getChildren.add(message)
      confirm.getDialogPane.setContent(content)

      // Custom buttons
      val okayButton = new ButtonType("Okay", ButtonBar.ButtonData.YES)
      val cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
      confirm.getButtonTypes.setAll(okayButton, cancelButton)

      // Style buttons
      confirm.getDialogPane.lookupButton(okayButton).setStyle(
        "-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 16 6 16;"
      )
      confirm.getDialogPane.lookupButton(cancelButton).setStyle(
        "-fx-background-color: #f0f0f0; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 6 16 6 16;"
      )

      // Run dialog and delete if confirmed
      val result = confirm.showAndWait()
      if (result.isPresent && result.get == okayButton) {
        val path = UserProfileStorage.profilePathFor(selected.username)
        java.nio.file.Files.deleteIfExists(path)
        loadUsers()
      }
    }
  }

  // Show the add/edit dialog for a user profile and return the result
  private def showUserDialog(existing: Option[UserProfile]): Option[UserProfile] = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/isaac/view/AddEditUserDialog.fxml"))
      val root = loader.load[javafx.scene.layout.Pane]()
      val controller = loader.getController[AddEditUserController]
      // If editing, pre-fill fields with the existing profile
      existing.foreach(controller.setProfile)
      // Configure the dialog window
      val dialogStage = new Stage()
      dialogStage.initModality(Modality.APPLICATION_MODAL)
      dialogStage.setTitle(if (existing.isDefined) "Edit User" else "Add User")
      dialogStage.setScene(new Scene(root))
      controller.setDialogStage(dialogStage)
      // Show the dialog and wait for user confirmation
      dialogStage.showAndWait()

      controller.getResult
    } catch {
      case ex: IOException =>
        ex.printStackTrace()
        new Alert(Alert.AlertType.ERROR, "Failed to open user dialog.").showAndWait()
        None
    }
  }
}
