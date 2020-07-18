package india.hanishkvc.filesharelocal.fman

import android.util.Log
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object FMan {

    val TAGME = "FSLFMan"

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
        dummyItems(0,5)
    }

    public fun dummyItems(start: Int, end: Int) {
        for (i in start..end) {
            addItem(createFManItem(i, "path$i", "test"))
        }
    }

    public fun clearItems() {
        ITEMS.clear()
    }

    private fun addItem(item: FManItem) {
        ITEMS.add(item)
    }

    /**
     * First time when called, it should be non null
     */
    public fun loadPath(path: String? = null, clear: Boolean = true): Boolean {
        var bDone: Boolean = false
        if (clear) clearItems()
        if (path != null) {
            curPath = Paths.get(path)
        }
        Log.v(TAGME, "loadPath: $curPath")
        try {
            var iCur = 0
            for (de in Files.list(curPath)) {
                var sType = if (Files.isDirectory(de)) "Dir" else "File"
                addItem(createFManItem(iCur, de.normalize().toString(), sType))
                iCur += 1
            }
            bDone = true
        } catch (e: Exception) {
            Log.e(TAGME, "loadPath: failed for $curPath")
        }
        return bDone
    }

    public fun backPath(): String {
        Log.v(TAGME, "backPath:I: $curPath")
        curPath = curPath?.subpath(0, curPath?.nameCount?.minus(1)!!)
        Log.v(TAGME, "backPath:O: $curPath")
        return curPath?.toAbsolutePath().toString()
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