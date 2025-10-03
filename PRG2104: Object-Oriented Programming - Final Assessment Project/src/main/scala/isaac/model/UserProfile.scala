package isaac.model

import java.time.LocalDate

case class UserProfile(
                        username: String,
                        email: Option[String] = None,
                        dob: Option[LocalDate] = None,
                        gender: Option[String] = None,
                        heightCm: Option[Double] = None,
                        weightKg: Option[Double] = None,
                        activityLevel: Option[String] = None,
                        targetWeightKg: Option[Double] = None,
                        dailyCalorieGoal: Option[Int] = None,
                        dietaryPreferences: Seq[String] = Seq.empty,
                        allergies: Seq[String] = Seq.empty,
                        healthFlags: Seq[String] = Seq.empty
                      ) {
  // Body mass index = Weight (kilogrammes) / (height (metres))^2
  def bmi: Option[Double] = for {
    h <- heightCm
    w <- weightKg
    if h > 0
  } yield {
    val meters = h / 100.0
    val raw = w / (meters * meters)
    BigDecimal(raw).setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
}
