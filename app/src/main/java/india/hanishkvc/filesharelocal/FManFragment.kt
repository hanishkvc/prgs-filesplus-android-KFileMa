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
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

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

        recyclerView?.setOnKeyListener({ view: View, i: Int, keyEvent: KeyEvent ->
            val prevIndex = listIndex
            var bAct = false

            if (keyEvent.action == KeyEvent.ACTION_UP) {
                if (keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    listIndex += 1
                    if (listIndex >= FMan.ITEMS.size) listIndex = 0
                    bAct = true
                    recyclerView?.scrollToPosition(listIndex)
                }
                if ( (keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER) ||
                    (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) ) {
                    Log.v(TAGME, "TimeElapsed: ${timeMark?.elapsedNow()?.inMilliseconds}")
                    if (timeMark != null) timeMark = null
                    FMan.fManItemInteractionIF?.doNavigate(listIndex)
                    return@setOnKeyListener true
                }
            }
            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                if ( (keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER) ||
                    (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) ) {
                    if (timeMark == null) {
                        timeMark = TimeSource.Monotonic.markNow()
                    }
                }
            }
            if (bAct) {
                Log.v(TAGME, "OnKey:${listIndex}: $i, ${keyEvent.action}")
                highlightRecyclerItem(prevIndex, false)
                recyclerView?.post {
                    highlightRecyclerItem(listIndex, true)
                }
                return@setOnKeyListener true
            } else {
                false // The last line / expression is the return value of the lambda fun
            }
        })

        return recyclerView
    }

    private fun colorRecyclerItem(position: Int, color: Int) {
        /*
         * get(index), here index seems to represent index of list of viewholders
         * getChildAt(index), index seems to belong to some internal list,
         *      which we dont have info about
         * findViewByPosition(index), index seems to represent actual list order,
         *      but there seems to be some racing issue. Maybe I have to let scroll
         *      finish before doing this? Maybe?? Update: Yes that was right.
         */
        recyclerView?.layoutManager?.findViewByPosition(position)?.let {
            val vh = recyclerView?.getChildViewHolder(it) as FManRecyclerViewAdapter.ViewHolder
            vh.pathView.setBackgroundColor(color)
        }
    }

    private fun highlightRecyclerItem(position: Int, highlight: Boolean = true) {
        recyclerView?.layoutManager?.findViewByPosition(position)?.let {
            val vh = recyclerView?.getChildViewHolder(it) as FManRecyclerViewAdapter.ViewHolder
            /*
            var color = vh.pathView.highlightColor
            if (highlight) {
                colorBackground = vh.pathView.backgroundTintList?.defaultColor ?: Color.WHITE
            } else {
                color = colorBackground
            }
            vh.pathView.setBackgroundColor(color)
             */
            if (highlight) {
                //vh.itemView.setActivated(true)
                vh.itemView.requestFocus()
            } else {
                vh.itemView.setActivated(false)
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
        var colorBackground: Int = Color.WHITE
        var timeMark: TimeMark? = null

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

