/**
 * FileShareLocal: Allow files to be shared locally on the same network across multiple devices
 * @author C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License GPL3
 */
@file:Suppress("MoveLambdaOutsideParentheses")

package india.hanishkvc.filesharelocal

import android.Manifest
import android.app.Activity
import android.app.UiModeManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.FileUriExposedException
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import india.hanishkvc.filesharelocal.fman.FMan
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private val TAGME = "FSLMain"
    private val BID_SAVEPATH = "BID_SAVEPATH"
    val scope = MainScope()
    var fileioJob: Job? = null

    private var btnMa: Button? = null
    private var tvPath: TextView? = null
    private var fragMain: FManFragment? = null

    private val CHECKPERMISSIONS_MAXCNT = 3
    private var checkPermissionsCnt = 0
    private val REQUEST_WRITE_EXTERNAL_STORAGE = 0x1001
    private var bPermWriteExternalStorage = false
    private var bPermissionsOk = false

    private val REQUESTCODE_VIEWFILEINT = 0x5a52
    private val REQUESTCODE_VIEWFILEEXT = 0x5a53
    private val bViewFileInt2Ext: Boolean = true
    private var vfUri: Uri? = null
    private var vfMime: String? = null

    enum class MainState {
        NORMAL,
        TRANSFERING,
    }
    var mainState = MainState.NORMAL

    enum class MenuEntries(val text: String) {
        BACK("Back"),
        COPY("Copy"),
        PASTE("Paste"),
        SEND("Send"),
        RECEIVE("Receive"),
        STORAGEVOLUME("StorageVolume"),
        NEWFOLDER("NewFolder"),
        SETTINGS("Settings"),
        EXIT("Exit")
    }

    val bCopyAddToList = false
    val selectedFileList = ArrayList<String>()

    private fun setupStartState(savedInstanceState: Bundle?) {
        // Handle initial path
        var sPath = savedInstanceState?.getCharSequence(BID_SAVEPATH)
        if (sPath == null) {
            sPath = FMan.getDefaultVolume(applicationContext)
            Log.v(TAGME,"Start:FreshState: $sPath")
        } else {
            Log.v(TAGME,"Start:LoadState: $sPath")
        }
        FManFragment.defaultPathStr = sPath as String
        // Handle num of cols in View
        var bModeGrid = false
        if (windowManager.defaultDisplay.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bModeGrid = true
        }
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
            bModeGrid = true
        }
        if (bModeGrid) {
            FManFragment.defaultColCnt = 2
            SimpRecycView.defaultColumnCount = 2
        } else {
            FManFragment.defaultColCnt = 1
            SimpRecycView.defaultColumnCount = 1
        }
        Log.v(TAGME,"Start:ModeGrid: $bModeGrid")
        // Misc setup
        SimpRecycView.viewBackgroundResource = R.drawable.list
        // packageManager.hasSystemFeature
    }

    private fun setupFManInteractions() {
        FMan.fManItemInteractionIF = object : FMan.FManItemInteractionIF {

            override fun doNavigate(itemId: Int) {
                if ((itemId < 0) || (itemId >= fragMain?.fmd?.ITEMS?.size!!)) {
                    Log.v(TAGME, "FManISIF:Ignoring invalid $itemId/${fragMain?.fmd?.ITEMS?.size}")
                    return
                }
                Log.v(TAGME, "FManISIF: $itemId, ${fragMain?.fmd?.ITEMS!![itemId]}")
                if (fragMain?.fmd?.ITEMS!![itemId].type == FMan.FManItemType.DIR) {
                    loadPath(fragMain?.fmd?.ITEMS!![itemId].path)
                } else {
                    viewFile(fragMain?.fmd?.ITEMS!![itemId].path)
                }
            }

            override fun doSelect(itemId: Int): Boolean {
                return true
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAGME, "onCreate: Entered")
        setupStartState(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.v(TAGME, "onCreate: View inflated")
        tvPath = findViewById<TextView>(R.id.tvPath)
        tvPath?.text = FManFragment.defaultPathStr
        fragMain = supportFragmentManager.findFragmentById(R.id.fragMain) as FManFragment
        btnMa = findViewById<Button>(R.id.btnMa)
        btnMa?.setOnClickListener {
            contextMenu()
            /*
            backPath()
            Log.v(TAGME, "btnMa: items ${fragMain!!.fmd?.ITEMS?.size}")
            Toast.makeText(this,"Items ${fragMain?.fmd?.ITEMS?.size}", Toast.LENGTH_SHORT).show()
             */
        }
        checkPermissions()
        setupFManInteractions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v(TAGME, "onActRes: rc[$requestCode], r[$resultCode], d[$data]")
        if ((requestCode == REQUESTCODE_VIEWFILEINT) && (resultCode == Activity.RESULT_CANCELED)) {
            if (bViewFileInt2Ext) {
                if ((vfUri != null) && (vfMime != null)) {
                        viewFileExt(vfUri!!, vfMime)
                }
            }
        }
    }

    private fun viewFileExt(uri: Uri, mime: String?): Boolean {
        var bActivityStarted = false
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (mime != null) {
            intent.setDataAndType(uri,mime)
        }
        try {
            Log.v(TAGME, "viewFile:External: $intent")
            startActivityForResult(intent, REQUESTCODE_VIEWFILEEXT)
            bActivityStarted = true
        } catch (e: Exception) {
            Log.e(TAGME, "viewFile:External: $intent, $e")
            var msg = "Exception occured"
            when(e) {
                is ActivityNotFoundException -> msg = "Didnt find any Viewer"
                is FileUriExposedException -> msg = "Cant share with other app"
            }
            Toast.makeText(this, "Android:$msg",Toast.LENGTH_SHORT).show()
        }
        return bActivityStarted
    }

    private fun viewFileInt(uri: Uri, mime: String?): Boolean {
        var bActivityStarted = false
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (mime != null) {
            intent.setDataAndType(uri,mime)
        }
        //intent.setComponent(ComponentName(this, "india.hanishkvc.filesharelocal.ViewerActivity"))
        intent.component = ComponentName(this, ViewerActivity::class.java)
        try {
            Log.v(TAGME, "viewFile:Internal: $intent")
            startActivityForResult(intent, REQUESTCODE_VIEWFILEINT)
            bActivityStarted = true
        } catch (e: java.lang.Exception) {
            Log.e(TAGME, "viewFile:Internal: $intent, $e")
            Toast.makeText(this, "Android:${e.localizedMessage}",Toast.LENGTH_SHORT).show()
        }
        return bActivityStarted
    }

    private fun viewFile(path: String) {
        var bActivityStarted = false
        val file = File(path)
        vfUri = Uri.fromFile(file)
        //if (vfUri == null) return
        vfMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(File(path).extension)
        if (bViewFileInt2Ext) {
            bActivityStarted = viewFileInt(vfUri!!, vfMime)
            if (!bActivityStarted) {
                viewFileExt(vfUri!!, vfMime)
            }
        } else {
            if (vfMime != null) {
                bActivityStarted = viewFileExt(vfUri!!, vfMime)
            }
            if (!bActivityStarted) {
                viewFileInt(vfUri!!, vfMime)
            }
        }
    }

    override fun onBackPressed() {
        backPath()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAGME, "ReqPerm:OK: ${Manifest.permission.WRITE_EXTERNAL_STORAGE}")
                bPermWriteExternalStorage = true
            } else {
                Log.e(TAGME, "ReqPerm:NO: ${Manifest.permission.WRITE_EXTERNAL_STORAGE}")
                bPermWriteExternalStorage = false
            }
        }
        checkPermissions()
    }

    private fun checkPermissions() {
        if (bPermissionsOk) return
        checkPermissionsCnt += 1
        if (checkPermissionsCnt > CHECKPERMISSIONS_MAXCNT) {
            Log.w(TAGME, "ChkPerm:$checkPermissionsCnt: Not enough permissions, quiting...")
            finish()
            return
        }
        Log.w(TAGME, "ChkPerm:$checkPermissionsCnt: Not enough permissions, asking user...")
        if (checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE)
            Log.w(TAGME, "ChkPerm:NO: ${Manifest.permission.WRITE_EXTERNAL_STORAGE}")
        } else {
            bPermWriteExternalStorage = true
            Log.v(TAGME, "ChkPerm:OK: ${Manifest.permission.WRITE_EXTERNAL_STORAGE}")
        }
        if (bPermWriteExternalStorage) {
            bPermissionsOk = true
        }
    }

    private fun loadPath(path: String? = null, defEntry: String? = null) {
        var thePath = path
        var bLoaded = false
        while (!bLoaded) {
            Log.v(TAGME,"loadPath: $thePath")
            if (thePath != null) {
                tvPath?.text = thePath
            }
            bLoaded = fragMain?.loadPath(thePath, defEntry)!!
            if (!bLoaded) {
                val vols = FMan.getVolumes(this)
                thePath = vols[0]
                storageVolumeSelector()
            }
        }
    }

    private fun _storageVolumeSelector(sPaths: Array<String>) {
        val sPathsPlus = sPaths + "Exit"
        val builder = AlertDialog.Builder(this).also {
            it.setTitle("Storage Volume")
            it.setItems(sPathsPlus,
                { _: DialogInterface, i: Int ->
                    if (i >= FMan.volBasePathStrs.size) {
                        finish()
                    } else {
                        FMan.volIndex = i
                        Log.v(TAGME, "SelVolDlg:$i")
                        loadPath(FMan.volBasePathStrs[i])
                    }
                })
        }
        val dlg = builder.create()
        dlg.show()
    }

    fun storageVolumeSelector() {
        val vols = FMan.getVolumes(this)
        _storageVolumeSelector(vols.toTypedArray())
    }

    private fun backPath() {
        val back = fragMain?.fmd?.curPath.toString()
        val path = fragMain?.fmd?.backPath()
        tvPath?.text = path
        Log.v(TAGME,"backPath: $path")
        loadPath(defEntry = back)
    }

    fun handleCopy(selectedList: ArrayList<FMan.FManItem>) {
        if (!bCopyAddToList) selectedFileList.clear()
        for (e in selectedList) {
            selectedFileList.add(e.path)
        }
    }

    enum class FILEIOTYPE {
        COPY,
        DELETE
    }

    /**
     * Handle FileIO operation in the background on the IOThread
     * and update the progress and end status using btnMa text.
     *
     * If it is a copy operation, then the global selectedFileList will be cleared.
     * It also refreshes the MainActivity view through a loadPath, this can lead to
     *     any selections made by user in the current path to get cleared.
     *     Users should ideally wait for fileio operations to complete, before
     *     doing any other operations.
     */
    fun handleFileIO(fileiotype: FILEIOTYPE, srcFileList: ArrayList<String>, dstPath: String) {
        Log.v(TAGME, "handleFileIO:Started:$srcFileList")
        btnMa?.text = resources.getString(R.string.Wait)
        fileioJob = scope.launch {
            var errCnt = 0
            withContext(Dispatchers.IO) {
                for ((i,curFile) in srcFileList.withIndex()) {
                    val curStat = "${i+1}/${srcFileList.size}"
                    Log.v(TAGME, "handleFileIO:Start:$curStat: $curFile")
                    val bDone = when(fileiotype) {
                        FILEIOTYPE.COPY -> FMan.copyRecursive(File(curFile), File(dstPath))
                        FILEIOTYPE.DELETE -> FMan.deleteRecursive(File(curFile))
                    }
                    if (!bDone) errCnt += 1
                    Log.v(TAGME, "handleFileIO:End:$curStat:$errCnt: $curFile")
                    withContext(Dispatchers.Main) {
                        btnMa?.text = resources.getString(R.string.Wait) + ":$curStat; E:$errCnt"
                    }
                }
            }
            withContext(Dispatchers.Main) {
                if (errCnt == 0) {
                    btnMa?.text = resources.getString(R.string.Ma)
                } else {
                    btnMa?.text = resources.getString(R.string.Ma) + " [E:$errCnt/${srcFileList.size}]"
                    btnMa?.postDelayed({
                        if (btnMa?.text != resources.getString(R.string.Ma)) {
                            btnMa?.text = resources.getString(R.string.Ma)
                        }
                    }, 10000)
                }
                if (fileiotype == FILEIOTYPE.COPY) {
                    selectedFileList.clear()
                }
                btnMa?.post {
                    loadPath()
                }
            }
            Log.v(TAGME, "handleFileIO:Done")
        }
    }

    fun handleDelete(selectedList: ArrayList<FMan.FManItem>) {
        val deleteList = ArrayList<String>()
        for (e in selectedList) {
            deleteList.add(e.path)
        }
        handleFileIO(FILEIOTYPE.DELETE, deleteList, "")
    }

    fun contextMenu() {
        val popupMenu = HPopupMenu(this, btnMa!!)
        popupMenu.buildMenuMap(0, HPopupMenu.ROOTMENU_ID, R.menu.main_ma_menu)
        popupMenu.buildMenuMap(0, R.id.file, R.menu.main_ma_file)
        popupMenu.buildMenuMap(1, R.id.newfolder, R.menu.main_ma_menu)
        popupMenu.prepare()
        popupMenu.show()
    }

    fun contextMenuAndroidPopup() {
        if (fileioJob != null) {
            if (!fileioJob!!.isCompleted) {
                Toast.makeText(this, "A FileIO Job is active, waiting for it to finish", Toast.LENGTH_LONG).show()
                return
            }
        }
        val popupMenu = PopupMenu(this, btnMa!!)
        popupMenu.inflate(R.menu.main_ma_menu)
        // Show or hide items
        val selectedList = fragMain?.recyclerView?.getSelectedList()
        if ((selectedList != null) && (selectedList.size > 0)){
            Log.v(TAGME, "contextMenu: copy ok $selectedList")
        } else {
            //popupMenu.menu.findItem(R.id.copy).isVisible = false
            //popupMenu.menu.findItem(R.id.send).isVisible = false
            popupMenu.menu.findItem(R.id.copy).isEnabled = false
            popupMenu.menu.findItem(R.id.send).isEnabled = false
            popupMenu.menu.findItem(R.id.delete).isEnabled = false
        }
        if (selectedFileList.size <= 0) popupMenu.menu.findItem(R.id.paste).isVisible = false

        popupMenu.setOnMenuItemClickListener {
            Log.v(TAGME, "${popupMenu.menu.findItem(it.itemId)}")
            when (it.itemId) {
                R.id.back -> backPath()
                R.id.copy -> handleCopy(selectedList!!)
                R.id.paste -> handleFileIO(FILEIOTYPE.COPY, selectedFileList, fragMain!!.fmd!!.curPath!!.absolutePath)
                R.id.storagevolume -> storageVolumeSelector()
                R.id.delete -> handleDelete(selectedList!!)
                R.id.exit -> finish()
            }
            true
        }
        popupMenu.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequence(BID_SAVEPATH,fragMain?.fmd?.curPath.toString())
        Log.v(TAGME,"SaveState: ${outState.getCharSequence(BID_SAVEPATH)}")
        super.onSaveInstanceState(outState)
    }

}
