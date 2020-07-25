package india.hanishkvc.filesharelocal

import android.os.Bundle
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
    private var columnCount = 1
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recyclerView = inflater.inflate(R.layout.fragment_fman_list, container, false) as RecyclerView

        // Set the adapter
        if (recyclerView is RecyclerView) {
            with(recyclerView) {
                this?.layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                this?.adapter = FManRecyclerViewAdapter(FMan.ITEMS)
            }
        }
        recyclerView?.preserveFocusAfterLayout = true
        return recyclerView
    }

    private fun highlightRecyclerItem(position: Int, highlight: Boolean = true) {
        recyclerView?.layoutManager?.findViewByPosition(position)?.let {
            val vh = recyclerView?.getChildViewHolder(it) as FManRecyclerViewAdapter.ViewHolder
            if (highlight) {
                vh.itemView.requestFocus()
            } else {
                vh.itemView.clearFocus()
            }
        }
    }

    fun clearHighlights() {
        highlightRecyclerItem(listIndex, false)
    }

    fun updateFrag() {
        recyclerView?.adapter?.notifyDataSetChanged()
        recyclerView?.scrollToPosition(0)
    }

    companion object {

        var listIndex: Int = -1

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

