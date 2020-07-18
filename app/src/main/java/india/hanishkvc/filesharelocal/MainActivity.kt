/**
 * FileShareLocal: Allow files to be shared locally on the same network across multiple devices
 * @author C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License GPL
 */
package india.hanishkvc.filesharelocal

import android.os.Bundle
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
                thePath = getExternalFilesDir(null)?.absolutePath
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