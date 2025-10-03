package isaac.db

import isaac.model.{Category, FoodItem}

// Defines basic Create, Read, Update, and Delete (CRUD) operations for managing FoodItem data.
trait NutritionDatabase {
  // Add a new food item to the database.
  def add(item: FoodItem): Unit
  // Update an existing food item (single-argument, deprecated).
  @deprecated("Use update(oldItem, updatedItem) instead — safer when fields used for identity change.", "2025-08-11")
  def update(item: FoodItem): Unit
  // Replace a specific existing item with an updated item.
  def update(oldItem: FoodItem, updatedItem: FoodItem): Unit
  // Remove a food item from the database.
  def delete(item: FoodItem): Unit
  // Retrieve all food items.
  def all(): Seq[FoodItem]
  // Find a food item by exact name match (case-insensitive recommended).
  def findByName(name: String): Option[FoodItem]
  // List all food items in the given category.
  def findByCategory(category: Category): Seq[FoodItem]
}
