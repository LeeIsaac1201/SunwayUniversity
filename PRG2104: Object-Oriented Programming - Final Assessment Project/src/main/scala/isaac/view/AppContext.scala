// Main application entry point located in the 'isaac' package
package isaac.view

// Tiny global event hub/holder for cross-controller notifications.
object AppContext {
  @volatile var dashboardController: Option[DashboardController] = None
  // Food list change listeners
  private val foodListListeners = scala.collection.mutable.ListBuffer.empty[() => Unit]
  // Register a listener that will be called when FoodList changes.
  def registerFoodListListener(listener: () => Unit): Unit = synchronized {
    foodListListeners += listener
  }
  // Unregister a listener if needed.
  def unregisterFoodListListener(listener: () => Unit): Unit = synchronized {
    foodListListeners -= listener
  }
  // Notify all listeners that the food list has changed (e.g. new item saved).
  def notifyFoodListChanged(): Unit = synchronized {
    // Copy to avoid modification during iteration
    val copy = foodListListeners.toList
    copy.foreach { l =>
      try l() catch { case e: Throwable => e.printStackTrace() }
    }
  }
}
