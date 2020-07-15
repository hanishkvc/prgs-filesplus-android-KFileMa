package india.hanishkvc.filesharelocal.fman

import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object FMan {

    /**
     * An array of sample (fman) items.
     */
    val ITEMS: MutableList<FManItem> = ArrayList()

    /**
     * A map of sample (fman) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, FManItem> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }

    private fun addItem(item: FManItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun createDummyItem(position: Int): FManItem {
        return FManItem(position.toString(), "Item " + position, makeDetails(position))
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A fman item representing a piece of content.
     */
    data class FManItem(val id: String, val content: String, val details: String) {
        override fun toString(): String = content
    }
}