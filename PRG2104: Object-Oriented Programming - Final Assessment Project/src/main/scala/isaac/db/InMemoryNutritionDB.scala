package isaac.db

import isaac.model.{Category, FoodItem}
import scala.collection.mutable

// In-memory implementation of NutritionDatabase using a mutable ListBuffer.
class InMemoryNutritionDB extends NutritionDatabase {
  private val store: mutable.ListBuffer[FoodItem] = mutable.ListBuffer.empty

  override def add(item: FoodItem): Unit =
    store += item

  // Deprecated single-argument update kept for compatibility.
  @deprecated("Use update(oldItem, updatedItem) instead — safer when fields used for identity change.", "2025-08-11")
  override def update(item: FoodItem): Unit = {
    // Prefer matching by name+category (common uniqueness heuristic)
    val byNameCat = store.indexWhere(i => i.name == item.name && i.category == item.category)
    if (byNameCat >= 0) {
      store.update(byNameCat, item)
      return
    }

    // Fallback: try equality (case-class equality / exact match)
    val byEquality = store.indexWhere(_ == item)
    if (byEquality >= 0) {
      store.update(byEquality, item)
      return
    }

    // Give up and append (with a warning) — this is why the two-arg update is preferred
    println(s"[WARN] InMemoryNutritionDB.update(item): could not find existing item for '${item.name}' / ${item.category}. Appending as new.")
    store += item
  }

  // Reliable update that replaces the specific stored `oldItem` with `updatedItem`.
  override def update(oldItem: FoodItem, updatedItem: FoodItem): Unit = {
    val idxByEquality = store.indexWhere(_ == oldItem)
    if (idxByEquality >= 0) {
      store.update(idxByEquality, updatedItem)
      return
    }

    val idxByNameCat = store.indexWhere(i => i.name == oldItem.name && i.category == oldItem.category)
    if (idxByNameCat >= 0) {
      store.update(idxByNameCat, updatedItem)
      return
    }

    println(s"[WARN] InMemoryNutritionDB.update(old, updated): original not found for '${oldItem.name}' / ${oldItem.category}. Appending updated item.")
    store += updatedItem
  }

  // Delete attempts equality-based removal first, then falls back to name+category and logs a warning when nothing is found.
  override def delete(item: FoodItem): Unit = {
    val removedByEquality = store.indexWhere(_ == item)
    if (removedByEquality >= 0) {
      store.remove(removedByEquality)
      return
    }

    val idxByNameCat = store.indexWhere(i => i.name == item.name && i.category == item.category)
    if (idxByNameCat >= 0) {
      store.remove(idxByNameCat)
      return
    }

    println(s"[WARN] InMemoryNutritionDB.delete: item not found for deletion: '${item.name}' / ${item.category}")
  }

  override def all(): Seq[FoodItem] =
    store.toList

  override def findByName(name: String): Option[FoodItem] =
    store.find(_.name.equalsIgnoreCase(name))

  override def findByCategory(category: Category): Seq[FoodItem] =
    store.filter(_.category == category).toList
}
