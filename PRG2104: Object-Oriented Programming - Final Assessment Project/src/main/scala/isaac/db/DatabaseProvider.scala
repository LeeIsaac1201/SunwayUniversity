package isaac.db

// Provides a single shared NutritionDatabase instance for the application.
object DatabaseProvider {
  private val configured = Option(System.getProperty("isaac.db.impl"))
    .orElse(Option(System.getenv("ISAAC_DB")))
  private val implName = configured.map(_.trim.toLowerCase).getOrElse("inmemory")
  val db: NutritionDatabase = implName match {
    case "inmemory" | "memory" | "default" =>
      println(s"[INFO] Using InMemoryNutritionDB as NutritionDatabase implementation.")
      new InMemoryNutritionDB()

    // Placeholder for potential future implementations.
    case other =>
      println(s"[WARN] Unknown isaac.db.impl='$other' — falling back to InMemoryNutritionDB.")
      new InMemoryNutritionDB()
  }
}
