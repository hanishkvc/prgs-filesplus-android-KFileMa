/**
 * FileShareLocal: Allow files to be shared locally on the same network across multiple devices
 * @author C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License GPL
 */
package india.hanishkvc.filesharelocal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import india.hanishkvc.filesharelocal.fman.FMan

class MainActivity : AppCompatActivity() {

    private val TAGME = "FSLMain"
    private var btnUp: Button? = null
    private var tvPath: TextView? = null

    private val CHECKPERMISSIONS_MAXCNT = 3
    private var checkPermissionsCnt = 0
    private val REQUEST_WRITE_EXTERNAL_STORAGE = 0x1001
    private var bPermWriteExternalStorage = false
    private var bPermissionsOk = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvPath = findViewById<TextView>(R.id.tvPath)
        loadPath(filesDir.absolutePath)
        btnUp = findViewById<Button>(R.id.btnUp)
        btnUp?.setOnClickListener {
            backPath()
            Log.v(TAGME, "btnUp: items ${FMan.ITEMS.size}")
            Toast.makeText(applicationContext,"Items ${FMan.ITEMS.size}", Toast.LENGTH_LONG)
        }
        checkPermissions()
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
        var bLoaded: Boolean = false
        while (!bLoaded) {
            Log.v(TAGME,"loadPath: $thePath")
            if (thePath != null) {
                tvPath?.text = thePath
            }
            bLoaded = FMan.loadPath(thePath, true)
            if (!bLoaded) {
                val appExt = getExternalFilesDir(null)?.absolutePath
                val sysRoot = Environment.getRootDirectory().absolutePath
                val sysExt = Environment.getExternalStorageDirectory().absolutePath
                Log.v(TAGME, "appExt:$appExt")
                Log.v(TAGME, "sysRoot:$sysRoot")
                Log.v(TAGME, "sysExt:$sysExt")
                thePath = sysExt
            }
        }
        val fragMain = supportFragmentManager.findFragmentById(R.id.fragMain) as FManFragment
        fragMain.updateFrag()
    }

    private fun backPath() {
        val path = FMan.backPath()
        tvPath?.text = path
        Log.v(TAGME,"backPath: $path")
        loadPath()
    }

}