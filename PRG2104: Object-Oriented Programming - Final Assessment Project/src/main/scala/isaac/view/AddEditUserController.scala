// Controller for adding or editing user profiles, located in the 'isaac.view' package
package isaac.view

// Imports user profile model and storage helpers
import isaac.model.UserProfile
import isaac.model.UserProfileStorage
// Imports JavaFX user interface (UI) components
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{TextField, ComboBox, DatePicker, Alert, ButtonType, Label}
import javafx.scene.Scene
import javafx.stage.{Stage, Modality}

class AddEditUserController {

  // FXML-injected UI components
  @FXML private var lblDialogTitle: Label = _
  @FXML private var tfUsername: TextField = _
  @FXML private var tfEmail: TextField = _
  @FXML private var dpDob: DatePicker = _
  @FXML private var cbSex: ComboBox[String] = _
  @FXML private var tfHeight: TextField = _
  @FXML private var tfWeight: TextField = _
  @FXML private var tfBmi: TextField = _
  @FXML private var cbActivityLevel: ComboBox[String] = _
  @FXML private var tfTargetWeight: TextField = _
  @FXML private var tfDietaryPreferences: TextField = _
  @FXML private var tfAllergies: TextField = _
  @FXML private var tfHealthFlags: TextField = _

  // State variables
  private var dialogStage: Stage = _
  private var saved: Boolean = false
  private var currentProfile: Option[UserProfile] = None
  private var isEditMode: Boolean = false
  private var originalUsername: String = ""

  // Attach the dialog stage for later control
  def setDialogStage(stage: Stage): Unit = dialogStage = stage

  // Switch between Add and Edit modes and update the UI accordingly
  def setEditMode(editMode: Boolean): Unit = {
    isEditMode = editMode
    if (isEditMode) {
      lblDialogTitle.setText("Edit Existing User")
      if (dialogStage != null) dialogStage.setTitle("Edit User")
    } else {
      lblDialogTitle.setText("Add New User")
      if (dialogStage != null) dialogStage.setTitle("Add User")
    }
  }

  // Pre-fill the form with values from an existing profile
  def setProfile(profile: UserProfile): Unit = {
    currentProfile = Some(profile)
    originalUsername = profile.username
    setEditMode(true)

    tfUsername.setText(profile.username)
    tfUsername.setDisable(false)
    tfEmail.setText(profile.email.getOrElse(""))
    dpDob.setValue(profile.dob.orNull)
    cbSex.setValue(profile.gender.orNull)
    tfHeight.setText(profile.heightCm.map(_.toString).getOrElse(""))
    tfWeight.setText(profile.weightKg.map(_.toString).getOrElse(""))
    cbActivityLevel.setValue(profile.activityLevel.orNull)
    tfTargetWeight.setText(profile.targetWeightKg.map(_.toString).getOrElse(""))
    tfDietaryPreferences.setText(profile.dietaryPreferences.mkString(", "))
    tfAllergies.setText(profile.allergies.mkString(", "))
    tfHealthFlags.setText(profile.healthFlags.mkString(", "))
    updateBmi()
  }

  // Return the result profile if saved
  def getResult: Option[UserProfile] = if (saved) currentProfile else None
  def isSaved: Boolean = saved

  @FXML
  def initialize(): Unit = {
    // Populate dropdowns
    cbSex.getItems.addAll("Male", "Female")
    cbActivityLevel.getItems.addAll(
      "Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extra Active"
    )

    // Update Body Mass Index (BMI) whenever height or weight is changed
    tfHeight.textProperty().addListener((_, _, _) => updateBmi())
    tfWeight.textProperty().addListener((_, _, _) => updateBmi())

    // Validate username input dynamically
    tfUsername.textProperty().addListener((_, _, newValue) => validateUsername(newValue))

    // Start in Add mode by default
    setEditMode(false)
  }

  // Validate that the username is unique and follows rules
  private def validateUsername(username: String): Unit = {
    val trimmedUsername = username.trim

    // Skip validation if empty or unchanged in edit mode
    if (trimmedUsername.isEmpty || (isEditMode && trimmedUsername == originalUsername)) {
      tfUsername.setStyle("")
      return
    }

    // Check if username already exists
    try {
      val existingProfile = UserProfileStorage.load(trimmedUsername)
      if (existingProfile.isDefined)
        tfUsername.setStyle("-fx-border-color: red; -fx-border-width: 2px;")
      else
        tfUsername.setStyle("-fx-border-color: green; -fx-border-width: 2px;")
    } catch {
      // Reset style on error
      case _: Exception => tfUsername.setStyle("")
    }
  }

  // Calculate and update BMI field
  private def updateBmi(): Unit = {
    val heightOpt = parseDoubleOpt(tfHeight.getText.trim, "Height")
    val weightOpt = parseDoubleOpt(tfWeight.getText.trim, "Weight")
    val bmi = for {
      h <- heightOpt
      w <- weightOpt
      if h > 0
    } yield {
      val meters = h / 100.0
      val raw = w / (meters * meters)
      BigDecimal(raw).setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
    tfBmi.setText(bmi.map(_.toString).getOrElse(""))
  }

  // Show a separate BMI information dialog
  @FXML
  private def showBmiInfo(): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/isaac/view/BMIInfoDialog.fxml"))
      val root = loader.load[javafx.scene.layout.VBox]()
      val controller = loader.getController[BMIInfoDialogController]

      val stage = new Stage()
      stage.setTitle("Body Mass Index (BMI) Information")
      stage.setScene(new Scene(root))
      stage.setResizable(false)
      stage.initModality(Modality.APPLICATION_MODAL)
      if (dialogStage != null) stage.initOwner(dialogStage)

      controller.setDialogStage(stage)
      stage.showAndWait()
    } catch {
      case ex: Exception => showError(s"Failed to open the Body Mass Index (BMI) information dialog: ${ex.getMessage}")
    }
  }

  // Handle Save button click
  @FXML
  private def onSave(): Unit = {
    val username = tfUsername.getText.trim
    val email = Option(tfEmail.getText.trim).filter(_.nonEmpty)
    val dob = Option(dpDob.getValue)
    val gender = Option(cbSex.getValue).filter(_.nonEmpty)
    val height = parseDoubleOpt(tfHeight.getText.trim, "Height")
    val weight = parseDoubleOpt(tfWeight.getText.trim, "Weight")
    val activityLevel = Option(cbActivityLevel.getValue).filter(_.nonEmpty)
    val targetWeight = parseDoubleOpt(tfTargetWeight.getText.trim, "Target Weight")
    val dietaryPreferences = tfDietaryPreferences.getText.split(",").map(_.trim).filter(_.nonEmpty)
    val allergies = tfAllergies.getText.split(",").map(_.trim).filter(_.nonEmpty)
    val healthFlags = tfHealthFlags.getText.split(",").map(_.trim).filter(_.nonEmpty)

    if (username.isEmpty) { showError("Username is required."); return }
    if (!username.matches("^[A-Za-z0-9_]+$")) { showError("Username can only contain letters, numbers, and underscores."); return }

    // Ensure username is unique (unless unchanged in edit mode)
    if (!(isEditMode && username == originalUsername)) {
      try {
        val existingProfile = UserProfileStorage.load(username)
        if (existingProfile.isDefined) {
          showError("Username already exists. Please choose a different username.")
          return
        }
      } catch {
        case ex: Exception =>
          showError(s"Failed to validate username: ${ex.getMessage}")
          return
      }
    }

    // Validate numeric fields
    if (height.isEmpty && tfHeight.getText.nonEmpty) return
    if (weight.isEmpty && tfWeight.getText.nonEmpty) return
    if (targetWeight.isEmpty && tfTargetWeight.getText.nonEmpty) return

    // Construct the UserProfile instance
    val profile = UserProfile(
      username = username,
      email = email,
      dob = dob,
      gender = gender,
      heightCm = height,
      weightKg = weight,
      activityLevel = activityLevel,
      targetWeightKg = targetWeight,
      dailyCalorieGoal = parseDoubleOpt(tfTargetWeight.getText.trim, "Daily Calorie Goal").map(_.toInt),
      dietaryPreferences = dietaryPreferences,
      allergies = allergies,
      healthFlags = healthFlags
    )

    try {
      // Save profile (overwrites if already exists)
      UserProfileStorage.save(profile)
      saved = true
      currentProfile = Some(profile)
      if (dialogStage != null) dialogStage.close()
    } catch {
      case ex: Exception => showError(s"Failed to save profile: ${ex.getMessage}")
    }
  }

  // Handle Cancel button click
  @FXML private def onCancel(): Unit = {
    saved = false
    if (dialogStage != null) dialogStage.close()
  }

  // Parse Double from a field with error handling
  private def parseDoubleOpt(value: String, field: String): Option[Double] = {
    if (value.isEmpty) None
    else try Some(value.toDouble)
    catch { case _: NumberFormatException => showError(s"$field must be a valid number."); None }
  }

  // Show error message in an alert dialog
  private def showError(msg: String): Unit = {
    val alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK)
    if (dialogStage != null) alert.initOwner(dialogStage)
    alert.showAndWait()
  }
}
