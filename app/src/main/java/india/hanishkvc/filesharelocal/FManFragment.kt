package india.hanishkvc.filesharelocal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import india.hanishkvc.filesharelocal.fman.FMan
import kotlin.time.ExperimentalTime

/**
 * A fragment representing a list of Items.
 */
@ExperimentalTime
@Suppress("MoveLambdaOutsideParentheses")
class FManFragment : Fragment() {

    private val TAGME = "FManFrag"
    private var columnCount = defaultColCnt
    private var recyclerView: SimpRecycView<FMan.FManItem>? = null
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
        fmd!!.loadPath(defaultPathStr)
        recyclerView?.assignDataList(fmd!!.ITEMS as ArrayList<FMan.FManItem>)
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

