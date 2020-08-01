package india.hanishkvc.filesharelocal.fman

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
@Suppress("MoveLambdaOutsideParentheses")
object FMan {

    private const val TAGME = "FSLFMan"

    /**
     * Maintain a list of storage base paths (as strings)
     */
    var volIndex: Int = -1
    var volBasePathStrs = ArrayList<String>()
    private var volDefaultStr: String? = null

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

    fun getDefaultVolume(context: Context): String {
        if (volDefaultStr == null) {
            volDefaultStr = context.filesDir.canonicalPath.toString()
        }
        return volDefaultStr as String
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

    fun copyRecursive(src: File, dst: File): Pair<Boolean, String> {
        var bDone = true
        if ((dst.isDirectory) && (src.isFile)) {
            //actualDst = dst.resolve()
        }
        Log.v(TAGME, "copy: ${src.absolutePath} to ${dst.absolutePath}")
        return Pair(src.copyRecursively(dst, false, { file: File, ioException: IOException ->
            if (ioException is FileAlreadyExistsException) {
                bDone = false
            }
            Log.e(TAGME, "copy: ${file.absolutePath} : ${ioException.localizedMessage}")
            OnErrorAction.SKIP
        }), "TODO")
    }

    fun copyFiles(srcPathsStr: ArrayList<String>, dstPathStr: String) {
        val fDst = File(dstPathStr)
        for (srcStr in srcPathsStr) {
            val fSrc = File(srcStr)
            copyRecursive(fSrc, fDst)
        }
    }

    fun deleteRecursive(file: File): Boolean {
        Log.v(TAGME, "delete: ${file.name}")
        return file.deleteRecursively()
    }

    fun deleteFiles(filePathsStr: ArrayList<String>) {
        for (fileStr in filePathsStr) {
            val fFile = File(fileStr)
            deleteRecursive(fFile)
        }
    }

    class FManData {
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

        fun clearItems() {
            ITEMS.clear()
        }

        fun addItem(item: FManItem) {
            ITEMS.add(item)
        }

        fun dummyItems(start: Int, end: Int) {
            for (i in start..end) {
                addItem(createFManItem(i, "path$i", FManItemType.NONE_TEST))
            }
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
                val dEntriesTmp = curPath?.listFiles()
                val dEntries = dEntriesTmp?.sorted()
                if (dEntries != null) {
                    val dEntriesGrouped = dEntries.groupBy {
                        if (it.isDirectory) FManItemType.DIR else FManItemType.FILE
                    }
                    if (dEntriesGrouped[FManItemType.DIR] != null) {
                        for (de in dEntriesGrouped.get(FManItemType.DIR)!!) {
                            addItem(createFManItem(iCur, de.normalize().toString(), FManItemType.DIR))
                            iCur += 1
                        }
                    }
                    val lFiles = dEntriesGrouped.get(FManItemType.FILE)
                    if (lFiles != null) {
                        for (de in lFiles) {
                            addItem(createFManItem(iCur, de.normalize().toString(), FManItemType.FILE))
                            iCur += 1
                        }
                    }
                    bDone = true
                } else {
                    Log.w(TAGME, "loadPath:Failed:Null: $curPath")
                }
            } catch (e: Exception) {
                Log.e(TAGME, "loadPath:Failed: $curPath")
                Log.e(TAGME, "$e")
                for (s in e.stackTrace) {
                    Log.e(TAGME, "${s.toString()}")
                }
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

        fun indexOf(path: String): Int {
            for (i in ITEMS.indices) {
                if (ITEMS[i].path == path) {
                    return i
                }
            }
            return -1
        }

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
        fun doSelect(itemId: Int): Boolean
    }

}