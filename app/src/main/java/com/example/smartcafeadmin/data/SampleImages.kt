package com.example.smartcafeadmin.data

import android.content.Context

// Gives menu items a bundled photo (res/drawable-nodpi/menu_<name>.jpg) if they don't have one yet
object SampleImages {

    // "White Chocolate Matcha" -> "menu_white_chocolate_matcha"
    fun resourceNameFor(itemName: String): String =
        "menu_" + itemName.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')

    fun attach(context: Context, db: DatabaseHelper) {
        for (item in db.getAllMenuItems()) {
            if (item.imagePath.isNotEmpty()) continue

            val resId = context.resources.getIdentifier(
                resourceNameFor(item.name), "drawable", context.packageName
            )
            if (resId == 0) continue   // no bundled photo for this item

            val path = ImageStorage.saveFromResource(context, resId) ?: continue
            db.updateMenuItem(item.copy(imagePath = path))
        }
    }
}