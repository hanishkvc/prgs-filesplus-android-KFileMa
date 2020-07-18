/**
 * FileShareLocal: Allow files to be shared locally on the same network across multiple devices
 * @author C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License GPL
 */
package india.hanishkvc.filesharelocal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import india.hanishkvc.filesharelocal.fman.FMan

class MainActivity : AppCompatActivity() {

    private val TAGME = "FSLMain"
    private var btnUp: Button? = null
    private var tvPath: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FMan.loadPath(filesDir.absolutePath)
        tvPath = findViewById<TextView>(R.id.tvPath)
        tvPath?.text = filesDir.absolutePath
        btnUp = findViewById(R.id.btnUp)
        btnUp?.setOnClickListener {
            FMan.dummyItems(100,110)
            Log.v(TAGME, "caught you button up, ${FMan.ITEMS.size}")
        }
    }
}