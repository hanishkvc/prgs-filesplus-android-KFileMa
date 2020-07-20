package india.hanishkvc.filesharelocal.fman

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object FMan {

    val TAGME = "FSLFMan"

    var volId: Int = -1

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

    private fun getBasePath(inPath: Path, marker: String): Path? {
        var nP = inPath.root
        for (cP in inPath) {
            if (cP.startsWith("Android"))
                break
            nP = nP.resolve(cP)
        }
        return nP
    }

    /**
     * Return a array of root storage paths
     */

    fun getVolumes(context: Context): ArrayList<String> {
        val vols = ArrayList<String>()

        val appInt = context.filesDir.toPath().toRealPath()
        Log.v(TAGME, "getVol:appInt: $appInt")
        vols.add(appInt.toString())

        //val appExt = context.getExternalFilesDir(null)?.absolutePath
        val appExts = context.getExternalFilesDirs(null)
        for (appExt in appExts) {
            Log.v(TAGME, "getVol:appExt: ${appExt.absolutePath}")
            vols.add(getBasePath(appExt.toPath().toRealPath(),"Android").toString())
        }

        val sysRoot = Environment.getRootDirectory().absolutePath
        Log.v(TAGME, "getVol:sysRoot: $sysRoot")
        vols.add(sysRoot)

        /* API Lvl 30
        val sysMnt = Environment.getStorageDirectory().absolutePath
        */
        val sysExt = Environment.getExternalStorageDirectory().absolutePath // Using deprecated
        Log.v(TAGME, "getVol:sysExt: $sysExt")
        vols.add(sysExt)

        val storageManager: StorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        for (storageVolume in storageManager.storageVolumes) {
            val svGetPath = storageVolume.javaClass.getMethod("getPath") // Using hidden func
            val sDesc = storageVolume.getDescription(context) // applicationContext will also do
            val sPath = svGetPath.invoke(storageVolume)
            Log.v(TAGME, "strMgr:$sDesc:$sPath")
        }
        return vols
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
            Log.e(TAGME, "loadPath:Failed: $curPath")
            Log.e(TAGME, "${e.toString()}")
        }
        return bDone
    }

    public fun backPath(): String {
        Log.v(TAGME, "backPath:I: $curPath")
        curPath = curPath?.subpath(0, curPath?.nameCount?.minus(1)!!)?.toAbsolutePath()
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