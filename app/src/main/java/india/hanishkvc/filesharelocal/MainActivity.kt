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
            val fragMain = supportFragmentManager.findFragmentById(R.id.fragMain) as FManFragment
            fragMain.updateFrag()
            Log.v(TAGME, "caught you button up, ${FMan.ITEMS.size}")
        }
    }

    private fun loadPath(path: String? = null) {
        if (path != null) {
            tvPath?.text = path
        }
        FMan.loadPath(path, true)
    }

    private fun backPath() {
        tvPath?.text = FMan.backPath()
        loadPath()
    }

}