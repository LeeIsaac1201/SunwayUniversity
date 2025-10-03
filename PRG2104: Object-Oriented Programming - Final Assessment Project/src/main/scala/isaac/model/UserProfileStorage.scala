package isaac.model

import java.nio.file.{Files, Path, Paths}
import java.time.LocalDate
import java.util.Properties

object UserProfileStorage {
  // Base directory: ./data/users
  private val baseDir: Path = Paths.get("data", "users")

  // Username validation regex
  private val UsernamePattern = "^[A-Za-z0-9_]+$".r

  private def ensureDir(): Unit = {
    if (!Files.exists(baseDir)) {
      Files.createDirectories(baseDir)
    }
  }

  private def validateUsername(username: String): Unit = {
    if (!UsernamePattern.matches(username)) {
      throw new IllegalArgumentException(
        s"Invalid username '$username'. Only letters, numbers, and underscores are allowed."
      )
    }
  }

  def profilePathFor(username: String): Path = {
    validateUsername(username.trim)
    ensureDir()
    baseDir.resolve(s"${username.trim}.txt")
  }

  def save(profile: UserProfile): Unit = {
    validateUsername(profile.username)

    val props = new Properties()
    props.setProperty("username", profile.username)
    profile.email.foreach(e => props.setProperty("email", e))
    profile.dob.foreach(d => props.setProperty("dob", d.toString))
    profile.gender.foreach(g => props.setProperty("gender", g))
    profile.heightCm.foreach(h => props.setProperty("heightCm", h.toString))
    profile.weightKg.foreach(w => props.setProperty("weightKg", w.toString))
    profile.activityLevel.foreach(a => props.setProperty("activityLevel", a))
    profile.targetWeightKg.foreach(t => props.setProperty("targetWeightKg", t.toString))
    profile.dailyCalorieGoal.foreach(c => props.setProperty("dailyCalorieGoal", c.toString))
    if (profile.dietaryPreferences.nonEmpty) props.setProperty("dietaryPreferences", profile.dietaryPreferences.mkString(","))
    if (profile.allergies.nonEmpty) props.setProperty("allergies", profile.allergies.mkString(","))
    if (profile.healthFlags.nonEmpty) props.setProperty("healthFlags", profile.healthFlags.mkString(","))

    val path = profilePathFor(profile.username)
    val out = Files.newOutputStream(path)
    try props.store(out, "UserProfile")
    finally out.close()
  }

  def load(username: String): Option[UserProfile] = {
    validateUsername(username)

    val path = profilePathFor(username)
    if (!Files.exists(path)) return None
    val props = new Properties()
    val in = Files.newInputStream(path)
    try props.load(in)
    finally in.close()

    def opt(k: String): Option[String] = Option(props.getProperty(k)).filter(_.nonEmpty)

    val profile = UserProfile(
      username = props.getProperty("username"),
      email = opt("email"),
      dob = opt("dob").map(LocalDate.parse),
      gender = opt("gender"),
      heightCm = opt("heightCm").map(_.toDouble),
      weightKg = opt("weightKg").map(_.toDouble),
      activityLevel = opt("activityLevel"),
      targetWeightKg = opt("targetWeightKg").map(_.toDouble),
      dailyCalorieGoal = opt("dailyCalorieGoal").map(_.toInt),
      dietaryPreferences = opt("dietaryPreferences")
        .map(_.split(",").toSeq.map(_.trim).filter(_.nonEmpty))
        .getOrElse(Seq.empty),
      allergies = opt("allergies")
        .map(_.split(",").toSeq.map(_.trim).filter(_.nonEmpty))
        .getOrElse(Seq.empty),
      healthFlags = opt("healthFlags")
        .map(_.split(",").toSeq.map(_.trim).filter(_.nonEmpty))
        .getOrElse(Seq.empty)
    )

    Some(profile)
  }
}
