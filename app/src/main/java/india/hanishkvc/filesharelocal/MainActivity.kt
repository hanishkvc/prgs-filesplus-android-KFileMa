/**
 * FileShareLocal: Allow files to be shared locally on the same network across multiple devices
 * @author C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License GPL
 */
package india.hanishkvc.filesharelocal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import india.hanishkvc.filesharelocal.fman.FMan

class MainActivity : AppCompatActivity() {

    private var btnUp: Button? = null
    private var tvPath: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FMan.loadPath(filesDir.absolutePath)
        btnUp = findViewById(R.id.btnUp)
        tvPath = findViewById(R.id.tvPath) as TextView
        tvPath?.text = filesDir.absolutePath
    }
}