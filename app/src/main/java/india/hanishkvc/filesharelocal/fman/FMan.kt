package india.hanishkvc.filesharelocal.fman

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import java.io.File

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object FMan {

    private const val TAGME = "FSLFMan"

    /**
     * Maintain a list of storage base paths (as strings)
     */
    var volIndex: Int = -1
    var volBasePathStrs = ArrayList<String>()

    /**
     * An array of fman items.
     */
    val ITEMS: MutableList<FManItem> = ArrayList()

    /**
     * the current path
     */
    var curPath: File? = null
        set(value) {
            field = value?.canonicalFile
        }

    /**
     * Hold a reference to a class/object implementing the FManItemInteractionIF
     */
    var fManItemInteractionIF: FManItemInteractionIF? = null

    /**
     * Enum for directory entry types
     */
    enum class FManItemType(val shortDesc: String) {
        DIR("D"),
        FILE("F"),
        NONE_TEST("T")
    }

    init {
        // Do nothing for now
    }

    fun dummyItems(start: Int, end: Int) {
        for (i in start..end) {
            addItem(createFManItem(i, "path$i", FManItemType.NONE_TEST))
        }
    }

    private fun clearItems() {
        ITEMS.clear()
    }

    private fun addItem(item: FManItem) {
        ITEMS.add(item)
    }

    private fun getBasePath(inPath: File?, marker: String): File? {
        var basePath = inPath
        var exitNext = false
        while(basePath != null) {
            basePath = basePath.parentFile
            if (exitNext) break
            val name = basePath.name
            if (name.startsWith(marker)) exitNext = true
        }
        return basePath
    }

    /**
     * Get Storage Volumes using Storage Manager
     */
    private fun getVolumesSM(context: Context): ArrayList<String> {
        val vols = ArrayList<String>()
        val storageManager: StorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        for (storageVolume in storageManager.storageVolumes) {
            val svGetPath = storageVolume.javaClass.getMethod("getPath") // Using hidden func
            val sDesc = storageVolume.getDescription(context) // applicationContext will also do
            val sPath = svGetPath.invoke(storageVolume) as String
            Log.v(TAGME, "strMgr:$sDesc:$sPath")
            vols.add(sPath)
        }
        return vols
    }

    /**
     * Return a array of root storage paths
     */

    fun getVolumes(context: Context): ArrayList<String> {
        val vols = ArrayList<String>()
        // Get External storage paths usable by the App
        val appExts = context.getExternalFilesDirs(null)
        for (appExt in appExts) {
            Log.v(TAGME, "getVol:appExt: ${appExt.absolutePath}")
            vols.add(getBasePath(appExt,"Android").toString())
        }
        // Get Android System's root directory
        val sysRoot = Environment.getRootDirectory().absolutePath
        Log.v(TAGME, "getVol:sysRoot: $sysRoot")
        vols.add(sysRoot)
        // Get Apps internal storage directory
        val appInt = context.filesDir.canonicalPath
        Log.v(TAGME, "getVol:appInt: $appInt")
        vols.add(appInt.toString())

        /* API Lvl 30
        val sysMnt = Environment.getStorageDirectory().absolutePath
        */
        /* getBasePath on 0th getExternalFilesDirs should give same info
        val sysExt = Environment.getExternalStorageDirectory().absolutePath // Using deprecated
        Log.v(TAGME, "getVol:sysExt: $sysExt")
        vols.add(sysExt)
         */
        volBasePathStrs = vols
        return vols
    }

    /**
     * Fetch the directory entries for the specified path
     * NOTE: First time when called, it should be non null
     */
    fun loadPath(path: String? = null, clear: Boolean = true): Boolean {
        var bDone = false
        if (clear) clearItems()
        if (path != null) {
            curPath = File(path)
        }
        Log.v(TAGME, "loadPath: $curPath")
        try {
            var iCur = 0
            for (de in curPath!!.listFiles()) {
                val sType = if (de.isDirectory) FManItemType.DIR else FManItemType.FILE
                addItem(createFManItem(iCur, de.normalize().toString(), sType))
                iCur += 1
            }
            bDone = true
        } catch (e: Exception) {
            Log.e(TAGME, "loadPath:Failed: $curPath")
            Log.e(TAGME, "$e")
        }
        return bDone
    }

    /**
     * Go back one step in the current Path
     */
    fun backPath(): String {
        Log.v(TAGME, "backPath:I: $curPath")
        curPath = curPath?.parentFile
        if (curPath == null) {
            curPath = File("/")
        }
        Log.v(TAGME, "backPath:O: $curPath")
        return curPath.toString()
    }

    private fun createFManItem(position: Int, path: String, type: FManItemType): FManItem {
        return FManItem(position, path, type)
    }

    /**
     * A fman item representing a piece of content.
     */
    data class FManItem(val id: Int, val path: String, val type: FManItemType) {
        override fun toString(): String = path
    }

    interface FManItemInteractionIF {
        fun doNavigate(itemId: Int)
    }
}