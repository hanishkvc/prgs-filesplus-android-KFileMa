package india.hanishkvc.filesharelocal.fman

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.ArrayList

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
    var curPath: Path? = null

    init {
        // Do nothing for now
    }

    private fun clearItems() {
        ITEMS.clear()
    }

    private fun addItem(item: FManItem) {
        ITEMS.add(item)
    }

    /**
     * First time when called, it should be non null
     */
    public fun loadPath(path: String? = null) {
        if (path != null) {
            clearItems()
            curPath = Paths.get(path)
        }
        var iCur = 0
        for (de in Files.list(curPath)) {
            var sType = if (Files.isDirectory(de)) "Dir" else "File"
            addItem(createFManItem(iCur, de.normalize().toString(), sType))
            iCur += 1
        }
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