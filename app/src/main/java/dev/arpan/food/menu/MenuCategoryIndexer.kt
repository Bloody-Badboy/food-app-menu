package dev.arpan.food.menu

import dev.arpan.food.menu.data.MenuItem

class MenuCategoryIndexer(private val categories: List<String>, items: List<MenuItem>) {

    private val startPositions: List<Int>

    init {
        val mapping = categories.associateWith { category ->
            items.indexOfFirst {
                it.categoryName == category
            }
        }.filterValues { it >= 0 }

        startPositions = mapping.map { it.value }
    }

    fun categoryForPosition(position: Int): String? {
        startPositions.asReversed().forEachIndexed { index, intVal ->
            if (intVal <= position) {
                return categories[categories.size - index - 1]
            }
        }
        return null
    }

    fun positionForCategory(category: String): Int {
        val index = categories.indexOf(category)
        if (index == -1) {
            throw IllegalArgumentException("Unknown category $category")
        }
        return startPositions[index]
    }
}
