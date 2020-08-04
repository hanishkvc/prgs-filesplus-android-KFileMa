package india.hanishkvc.filesharelocal

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import india.hanishkvc.filesharelocal.fman.FMan
import java.io.File

/**
 * A fragment representing a list of Items.
 */
@Suppress("MoveLambdaOutsideParentheses")
class FManFragment : Fragment() {

    private val TAGME = "FManFrag"
    private var columnCount = defaultColCnt
    var recyclerView: SimpRecycView<FMan.FManItem>? = null
    var fmd: FMan.FManData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAGME, "onCreate: Entered, colCnt[$columnCount]")

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
        Log.v(TAGME, "onCreate: argProcessed, colCnt[$columnCount]")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAGME, "onCreateView: Entered")
        recyclerView = inflater.inflate(R.layout.fragment_fman_list, container, false) as SimpRecycView<FMan.FManItem>

        fmd = FMan.FManData()
        //fmd!!.loadPath(defaultPathStr)

        recyclerView?.onSRCVCreateView = { parent ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_fman_item, parent, false)
            val sizeView: TextView = view.findViewById(R.id.item_size)
            sizeView.typeface = Typeface.MONOSPACE
            view
        }

        recyclerView?.onSRCVBindView = { view: View, position: Int ->
            val item = fmd!!.ITEMS[position]
            val typeView: TextView = view.findViewById(R.id.item_type)
            val pathView: TextView = view.findViewById(R.id.item_path)
            val sizeView: TextView = view.findViewById(R.id.item_size)
            typeView.text = item.type.shortDesc
            pathView.text = item.path.substringAfterLast(File.separator)
            if (item.size > 999999) {
                sizeView.text = "%.2G".format(item.size.toDouble())
            } else {
                sizeView.text = "% 7d".format(item.size)
            }
        }

        recyclerView?.onSRCVItemClickListener = { position: Int, view: View ->
            Log.d(TAGME, "onSRcVItemClick: ${position}, ${fmd?.ITEMS?.get(position)}")
            FMan.fManItemInteractionIF?.doNavigate(position)
        }

        recyclerView?.onSRCVItemLongClickListener = { position: Int, view: View ->
            Log.d(TAGME, "onSRcVItemLongClick: ${position}, ${fmd?.ITEMS?.get(position)}")
            FMan.fManItemInteractionIF?.doSelect(position)!!
        }

        //recyclerView?.assignDataList(fmd!!.ITEMS as ArrayList<FMan.FManItem>)
        recyclerView?.preserveFocusAfterLayout = true
        return recyclerView
    }


    fun clearHighlights() {
    }

    fun updateFrag(initialPosition: Int = -1) {
        recyclerView?.assignDataList(fmd!!.ITEMS as ArrayList<FMan.FManItem>, initialPosition)
    }

    fun loadPath(path: String? = null, defEntry: String? = null): Boolean {
        val curFMD = fmd ?: return false
        clearHighlights()
        Log.v(TAGME,"loadPath: $path")
        val bLoaded = curFMD.loadPath(path, true)
        if (bLoaded) {
            var initialPosition = 0
            if (defEntry != null) {
                initialPosition = curFMD.indexOf(defEntry)
            }
            updateFrag(initialPosition)
        }
        return bLoaded
    }

    companion object {

        // App specific
        var defaultPathStr: String? = null
        var defaultColCnt: Int = 1

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            FManFragment().apply {
                Log.v(TAGME, "newInstance: colCnt[$columnCount]")
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}

