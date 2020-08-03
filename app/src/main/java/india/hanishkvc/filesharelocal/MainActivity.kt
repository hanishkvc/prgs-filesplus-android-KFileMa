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
import android.text.InputType
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import india.hanishkvc.filesharelocal.fman.FMan
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private val TAGME = "FSLMain"
    private val BID_SAVEPATH = "BID_SAVEPATH"
    private val BID_SELECTEDLIST = "BID_SELECTEDLIST"
    private val BID_VIEWFILEINTERNALFIRST = "BID_VIEWFILEINTERNALFIRST"

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
    private var bViewFileInternalFirst: Boolean = true
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

    val BTNMA_ERRORMILLIS = 5000

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
        // Handle selectedFileList
        val savedSelectedFileList = savedInstanceState?.getStringArrayList(BID_SELECTEDLIST)
        savedSelectedFileList?.let { selectedFileList.addAll(it) }
        // Handle bViewFileInternalFirst
        savedInstanceState?.apply {
            bViewFileInternalFirst = getBoolean(BID_VIEWFILEINTERNALFIRST)
        }
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
        loadDefaultPath()
    }

    fun loadDefaultPath() {
        fileioJob = scope.launch {
            withContext(Dispatchers.IO){
                fragMain?.fmd?.loadPath(FManFragment.defaultPathStr)
            }
            withContext(Dispatchers.Main) {
                fragMain?.updateFrag()
            }
        }
    }

    /**
     * NOTE THAT if the internal viewer returns with a failure, we chain into external viewer.
     * However if external viewer returns with a failure, we dont chain into internal viewer.
     * This is because, most external viewers dont consistently return with success or failure
     * to view file related status.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v(TAGME, "onActRes: rc[$requestCode], r[$resultCode], d[$data]")
        if ((requestCode == REQUESTCODE_VIEWFILEINT) && (resultCode == Activity.RESULT_CANCELED)) {
            if (bViewFileInternalFirst) {
                if (vfUri != null) {
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
        if (bViewFileInternalFirst) {
            bActivityStarted = viewFileInt(vfUri!!, vfMime)
            if (!bActivityStarted) {
                viewFileExt(vfUri!!, vfMime)
            }
        } else {
            bActivityStarted = viewFileExt(vfUri!!, vfMime)
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
                    }, BTNMA_ERRORMILLIS.toLong())
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

    fun handleNewFolder() {
        AlertDialog.Builder(this).apply {
            setTitle("Folder Name")
            val editText = EditText(context)
            editText.inputType = InputType.TYPE_CLASS_TEXT
            setView(editText)
            setPositiveButton("Create", { dialogInterface: DialogInterface, i: Int ->
                val folderName = editText.text.toString()
                Log.v(TAGME, "handleNewFolder: $folderName")
                var bDone = true
                try {
                    val dir = File(fragMain?.fmd?.curPath, folderName)
                    bDone = dir.mkdir()
                } catch (e: Exception) {
                    bDone = false
                    Log.e(TAGME, "handleNewFolder: ${e.localizedMessage}")
                }
                if (!bDone) {
                    btnMa?.text = resources.getString(R.string.Error)
                    btnMa?.postDelayed({
                        btnMa?.text = resources.getString(R.string.Ma)
                    }, BTNMA_ERRORMILLIS.toLong())
                }
            })
            setNegativeButton("Cancel", { dialogInterface: DialogInterface, i: Int ->
                Log.w(TAGME, "handleNewFolder: canceled")
                dialogInterface.cancel()
            })
            show()
        }
    }

    fun handleSettings() {
        AlertDialog.Builder(this).apply {
            setTitle("Settings")
            val sSettings = arrayOf("Viewer: Internal First")
            val bSettings = ArrayList<Boolean>()
            bSettings.add(bViewFileInternalFirst)
            setMultiChoiceItems(sSettings, bSettings.toBooleanArray(), { dlgIF: DialogInterface, index: Int, bChecked: Boolean ->
                bSettings[index] = bChecked
            })
            setPositiveButton("Ok", { dialogInterface: DialogInterface, id: Int ->
                bViewFileInternalFirst = bSettings[0]
            })
            setNegativeButton("Cancel", { dialogInterface: DialogInterface, id: Int ->
                dialogInterface.cancel()
            })
            show()
        }
    }

    fun contextMenu() {
        // Check we are ok to do things
        if (fileioJob != null) {
            if (!fileioJob!!.isCompleted) {
                Toast.makeText(this, "A FileIO Job is active, waiting for it to finish", Toast.LENGTH_LONG).show()
                return
            }
        }
        // Create the popup menu
        val popupMenu = HPopupMenu(this, btnMa!!)
        popupMenu.buildMenuMap(0, HPopupMenu.ROOTMENU_ID, R.menu.main_ma_menu)
        popupMenu.buildMenuMap(0, R.id.file, R.menu.main_ma_file)
        popupMenu.buildMenuMap(0, R.id.nwshare, R.menu.main_ma_nwshare)
        popupMenu.prepare()
        // Show or hide items
        val disabledMenuItems = ArrayList<Int>()
        val selectedList = fragMain?.recyclerView?.getSelectedList()
        if ((selectedList != null) && (selectedList.size > 0)){
            Log.v(TAGME, "contextMenu: selectedList : $selectedList")
        } else {
            disabledMenuItems.add(R.id.copy)
            disabledMenuItems.add(R.id.send)
            disabledMenuItems.add(R.id.delete)
        }
        if (selectedFileList.size <= 0) disabledMenuItems.add(R.id.paste)
        popupMenu.disabledMenuItems = disabledMenuItems
        // Setup interaction callback
        popupMenu.onMenuItemClickListener = {
            Log.v(TAGME, "contextMenu:Clicked: ${it}")
            when (it.itemId) {
                R.id.back -> backPath()
                R.id.copy -> handleCopy(selectedList!!)
                R.id.paste -> handleFileIO(FILEIOTYPE.COPY, selectedFileList, fragMain!!.fmd!!.curPath!!.absolutePath)
                R.id.storagevolume -> storageVolumeSelector()
                R.id.delete -> handleDelete(selectedList!!)
                R.id.newfolder -> handleNewFolder()
                R.id.settings -> handleSettings()
                R.id.exit -> finish()
            }
            true
        }
        // Show it
        popupMenu.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequence(BID_SAVEPATH,fragMain?.fmd?.curPath.toString())
        Log.v(TAGME,"SaveState: ${outState.getCharSequence(BID_SAVEPATH)}")
        outState.putStringArrayList(BID_SELECTEDLIST, selectedFileList)
        outState.putBoolean(BID_VIEWFILEINTERNALFIRST, bViewFileInternalFirst)
        super.onSaveInstanceState(outState)
    }

}
