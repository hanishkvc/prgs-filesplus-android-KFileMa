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
import india.hanishkvc.filesharelocal.fman.FMan
import java.io.File
import kotlin.time.ExperimentalTime

@ExperimentalTime
class MainActivity : AppCompatActivity() {

    private val TAGME = "FSLMain"
    private val BID_SAVEPATH = "BID_SAVEPATH"

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

    fun contextMenu() {
        val menuList = ArrayList<String>()
        menuList.add(MenuEntries.BACK.text)
        val selectedList = fragMain?.recyclerView?.getSelectedList()
        if ((selectedList != null) && (selectedList.size > 0)){
            Log.v(TAGME, "contextMenu: $selectedList")
            menuList.add(MenuEntries.COPY.text)
        }
        if (selectedFileList.size > 0) menuList.add(MenuEntries.PASTE.text)
        menuList.add(MenuEntries.NEWFOLDER.text)
        menuList.add(MenuEntries.SEND.text)
        menuList.add(MenuEntries.RECEIVE.text)
        menuList.add(MenuEntries.STORAGEVOLUME.text)
        menuList.add(MenuEntries.SETTINGS.text)
        menuList.add(MenuEntries.EXIT.text)
        // Show context menu dialog
        val builder = AlertDialog.Builder(this).also {
            it.setTitle("KFileMa")
            it.setItems(menuList.toTypedArray(),
                { _: DialogInterface, i: Int ->
                    Log.v(TAGME, "contextMenu: $menuList[i]")
                    if (menuList[i] == MenuEntries.BACK.text) {
                        backPath()
                    } else if (menuList[i] == MenuEntries.COPY.text) {
                        for (e in selectedList!!) {
                            selectedFileList.add(e.path)
                        }
                    } else if (menuList[i] == MenuEntries.PASTE.text) {
                        Log.v(TAGME, "contextMenu: $selectedFileList")
                        for (e in selectedFileList.toSet()) {
                            Log.v(TAGME, "$e")
                        }
                        selectedFileList.clear()
                    } else if (menuList[i] == MenuEntries.STORAGEVOLUME.text) {
                        storageVolumeSelector()
                    }
                    if (menuList[i] == MenuEntries.EXIT.text) {
                        finish()
                    }
                })
        }
        val dlg = builder.create()
        dlg.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequence(BID_SAVEPATH,fragMain?.fmd?.curPath.toString())
        Log.v(TAGME,"SaveState: ${outState.getCharSequence(BID_SAVEPATH)}")
        super.onSaveInstanceState(outState)
    }

}
