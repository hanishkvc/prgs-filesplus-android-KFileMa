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

    init {
        // Start at the root dir
        var iCur = 0
        for (de in Files.list(curPath)) {
            createFManItem(iCur, de.normalize(),"dirORfile")
            iCur += 1
        }
    }

    private fun addItem(item: FManItem) {
        ITEMS.add(item)
    }

    public fun loadPath(path: String) {

    }

    private fun createFManItem(position: Int, path: String, type: String): FManItem {
        return FManItem(position.toString(), path, type)
    }

    /**
     * A fman item representing a piece of content.
     */
    data class FManItem(val id: String, val path: String, val type: String) {
        override fun toString(): String = path
    }
}