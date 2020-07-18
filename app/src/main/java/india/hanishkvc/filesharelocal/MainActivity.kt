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
import androidx.recyclerview.widget.RecyclerView
import india.hanishkvc.filesharelocal.fman.FMan

class MainActivity : AppCompatActivity() {

    private val TAGME = "FSLMain"
    private var btnUp: Button? = null
    private var tvPath: TextView? = null
    private var rv: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FMan.loadPath(filesDir.absolutePath)
        tvPath = findViewById<TextView>(R.id.tvPath)
        tvPath?.text = filesDir.absolutePath
        rv = findViewById<RecyclerView>(R.id.list)
        btnUp = findViewById<Button>(R.id.btnUp)
        btnUp?.setOnClickListener {
            FMan.clearItems()
            FMan.dummyItems(1,20)
            rv?.adapter?.notifyDataSetChanged()
            if (rv != null) {
                Log.v(TAGME, "rv not null ${rv}")
                if (rv?.adapter != null) {
                    Log.v(TAGME, "rv.adaptor not null ${rv?.adapter}")
                }
            } else {
                Log.v(TAGME, "rv is null ${rv}")
            }
            rv?.adapter?.let {
                it.notifyDataSetChanged()
            }
            Log.v(TAGME, "caught you button up, ${FMan.ITEMS.size}")
        }
    }
}