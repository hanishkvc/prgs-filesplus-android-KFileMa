package india.hanishkvc.filesharelocal.fman

import java.nio.file.Files
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
     * the current path
     */
    var curPath = "/"

    /**
     * A map of sample (fman) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, FManItem> = HashMap()

    init {
        // Start at the root dir
        var iCur = 0
        for (de in Files.list(curPath)) {
            createDummyItem(iCur, de.normalize(),"dirORfile")
            iCur += 1
        }
    }

    private fun addItem(item: FManItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    public fun loadPath(path: String) {

    }

    private fun createDummyItem(position: Int, path: String, type: String): FManItem {
        return FManItem(position.toString(), path, type)
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
    data class FManItem(val id: String, val path: String, val type: String) {
        override fun toString(): String = path
    }
}