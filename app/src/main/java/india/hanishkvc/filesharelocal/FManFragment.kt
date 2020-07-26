package india.hanishkvc.filesharelocal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import india.hanishkvc.filesharelocal.fman.FMan
import kotlin.time.ExperimentalTime

/**
 * A fragment representing a list of Items.
 */
@ExperimentalTime
@Suppress("MoveLambdaOutsideParentheses")
class FManFragment : Fragment() {

    private val TAGME = "FManFrag"
    private var columnCount = 2
    private var recyclerView: RecyclerView? = null
    var fmd: FMan.FManData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAGME, "onCreate: Entered")

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAGME, "onCreateView: Entered")
        recyclerView = inflater.inflate(R.layout.fragment_fman_list, container, false) as RecyclerView

        // Set the adapter
        if (recyclerView is RecyclerView) {
            with(recyclerView) {
                this?.layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                fmd = FMan.FManData()
                fmd!!.loadPath(defaultPathStr)
                this?.adapter = FManRecyclerViewAdapter(fmd!!)
            }
        }
        recyclerView?.preserveFocusAfterLayout = true
        return recyclerView
    }

    private fun highlightRecyclerItem(position: Int, highlight: Boolean = true) {
        recyclerView?.layoutManager?.findViewByPosition(position)?.let {
            /*
            Log.v(TAGME, "highlightRecycItem: pos[{$position}], view[${it.javaClass}], vh[${recyclerView?.getChildViewHolder(it)}]")
            val vh = recyclerView?.getChildViewHolder(it) as FManRecyclerViewAdapter.ViewHolder
            if (highlight) {
                vh.itemView.requestFocus()
            } else {
                vh.itemView.clearFocus()
            }
             */
            if (highlight) {
                it.requestFocus()
            } else {
                it.clearFocus()
            }
        }
    }

    fun clearHighlights() {
    }

    fun updateFrag(initialPosition: Int = -1) {
        recyclerView?.adapter?.notifyDataSetChanged()
        (recyclerView?.adapter as FManRecyclerViewAdapter).selected.clear()
        if (initialPosition >= 0) {
            recyclerView?.scrollToPosition(initialPosition)
            recyclerView?.post {
                highlightRecyclerItem(initialPosition, true)
            }
        }
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
            updateFrag(initialPosition=initialPosition)
        }
        return bLoaded
    }

    companion object {

        // App specific
        var defaultPathStr: String? = null

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            FManFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}

