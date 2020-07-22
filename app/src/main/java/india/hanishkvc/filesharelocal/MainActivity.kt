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
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import india.hanishkvc.filesharelocal.fman.FMan
import india.hanishkvc.filesharelocal.fman.FMan.FManItemSelectIF
import java.io.File

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
            Log.v(TAGME, "btnUp: items ${FMan.ITEMS.size}")
            Toast.makeText(this,"Items ${FMan.ITEMS.size}", Toast.LENGTH_SHORT).show()
        }
        checkPermissions()
        FMan.fManItemSelectIF = object : FManItemSelectIF {
            override fun onSelectListener(itemId: Int) {
                if ((itemId < 0) || (itemId >= FMan.ITEMS.size)) {
                    Log.v(TAGME, "FManISIF:Ignoring invalid $itemId/${FMan.ITEMS.size}")
                    return
                }
                Log.v(TAGME, "FManISIF: $itemId, ${FMan.ITEMS[itemId]}")
                if (FMan.ITEMS[itemId].type == FMan.FManItemType.DIR) {
                    loadPath(FMan.ITEMS[itemId].path)
                } else {
                    viewFile(FMan.ITEMS[itemId].path)
                }
            }
        }

    }

    private fun viewFile(path: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.fromFile(File(path))
        intent.type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(File(path).extension)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {

            Toast.makeText(this, "Android didnt find any Viewer",Toast.LENGTH_SHORT).show()
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

    private fun loadPath(path: String? = null) {
        var thePath = path
        var bLoaded = false
        fragMain?.clearHighlights()
        while (!bLoaded) {
            Log.v(TAGME,"loadPath: $thePath")
            if (thePath != null) {
                tvPath?.text = thePath
            }
            bLoaded = FMan.loadPath(thePath, true)
            if (!bLoaded) {
                val vols = FMan.getVolumes(this)
                thePath = vols[0]
                volumeSelector(vols.toTypedArray())
            } else {
                fragMain?.clearHighlights()
                FManFragment.listIndex = -1
            }
        }
        fragMain?.updateFrag()
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
        val path = FMan.backPath()
        tvPath?.text = path
        Log.v(TAGME,"backPath: $path")
        loadPath()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequence(BID_SAVEPATH,FMan.curPath.toString())
        Log.v(TAGME,"SaveState: ${outState.getCharSequence(BID_SAVEPATH)}")
        super.onSaveInstanceState(outState)
    }

}