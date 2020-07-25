/**
 * FileShareLocal: Allow files to be shared locally on the same network across multiple devices
 * @author C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License GPL
 */
package india.hanishkvc.filesharelocal

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
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
import india.hanishkvc.filesharelocal.fman.FMan.FManItemInteractionIF
import java.io.File
import kotlin.time.ExperimentalTime

@ExperimentalTime
class MainActivity : AppCompatActivity() {

    private val TAGME = "FSLMain"
    private val BID_SAVEPATH = "BID_SAVEPATH"

    private var btnUp: Button? = null
    private var tvPath: TextView? = null
    private var fragMain: FManFragment? = null

    private val CHECKPERMISSIONS_MAXCNT = 3
    private var checkPermissionsCnt = 0
    private val REQUEST_WRITE_EXTERNAL_STORAGE = 0x1001
    private var bPermWriteExternalStorage = false
    private var bPermissionsOk = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvPath = findViewById<TextView>(R.id.tvPath)
        fragMain = supportFragmentManager.findFragmentById(R.id.fragMain) as FManFragment
        var sPath = savedInstanceState?.getCharSequence(BID_SAVEPATH)
        if (sPath == null) {
            sPath = filesDir.absolutePath
            Log.v(TAGME,"FreshState: $sPath")
        } else {
            Log.v(TAGME,"LoadState: $sPath")
        }
        loadPath(sPath.toString())
        btnUp = findViewById<Button>(R.id.btnUp)
        btnUp?.setOnClickListener {
            backPath()
            Log.v(TAGME, "btnUp: items ${fragMain!!.fmd?.ITEMS?.size}")
            Toast.makeText(this,"Items ${fragMain?.fmd?.ITEMS?.size}", Toast.LENGTH_SHORT).show()
        }
        checkPermissions()
        FMan.fManItemInteractionIF = object : FManItemInteractionIF {
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

    private fun viewFile(path: String) {
        val file = File(path)
        val uri = Uri.fromFile(file)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(File(path).extension)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setDataAndType(uri,mime)
        try {
            startActivity(intent)
            Log.v(TAGME, "$intent")
        } catch (e: Exception) {
            Log.e(TAGME, "$intent, $e")
            var msg = "Exception occured"
            when(e) {
                is ActivityNotFoundException -> msg = "Didnt find any Viewer"
                is FileUriExposedException -> msg = "Cant share with other app"
            }
            Toast.makeText(this, "Android:$msg",Toast.LENGTH_SHORT).show()
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
                volumeSelector(vols.toTypedArray())
            }
        }
    }

    private fun volumeSelector(sPaths: Array<String>) {
        val sPathsPlus = sPaths + "Exit"
        val builder = AlertDialog.Builder(this).also {
            it.setTitle("Select Volume")
            it.setItems(sPathsPlus,
                { dialogInterface: DialogInterface, i: Int ->
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

    private fun backPath() {
        val back = fragMain?.fmd?.curPath.toString()
        val path = fragMain?.fmd?.backPath()
        tvPath?.text = path
        Log.v(TAGME,"backPath: $path")
        loadPath(defEntry = back)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequence(BID_SAVEPATH,fragMain?.fmd?.curPath.toString())
        Log.v(TAGME,"SaveState: ${outState.getCharSequence(BID_SAVEPATH)}")
        super.onSaveInstanceState(outState)
    }

}
