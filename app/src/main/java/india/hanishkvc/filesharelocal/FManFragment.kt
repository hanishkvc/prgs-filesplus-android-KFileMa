package india.hanishkvc.filesharelocal

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import india.hanishkvc.filesharelocal.fman.FMan

/**
 * A fragment representing a list of Items.
 */
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

        recyclerView?.setOnKeyListener({ view: View, i: Int, keyEvent: KeyEvent ->
            Log.v(TAGME, "OnKey: ${view.javaClass}, $i, ${keyEvent.action}")
            val prevIndex = listIndex
            var bAct = false
            if ( (keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                    && (keyEvent.action == KeyEvent.ACTION_UP) ) {
                listIndex += 1
                if (listIndex > FMan.ITEMS.size) listIndex = 0
                bAct = true
                recyclerView?.smoothScrollToPosition(listIndex)
            }
            if (bAct) {
                recyclerView?.getChildAt(listIndex)?.let {
                    val vh = recyclerView?.getChildViewHolder(it) as FManRecyclerViewAdapter.ViewHolder
                    vh.pathView.setBackgroundColor(Color.LTGRAY)
                }
                recyclerView?.getChildAt(prevIndex)?.let {
                    val vh = recyclerView?.getChildViewHolder(it) as FManRecyclerViewAdapter.ViewHolder
                    vh.pathView.setBackgroundColor(Color.WHITE)
                }
                return@setOnKeyListener true
            } else {
                false // The last line / expression is the return value of the lambda fun
            }
        })

        return recyclerView
    }

    fun updateFrag() {
        recyclerView?.adapter?.notifyDataSetChanged()
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