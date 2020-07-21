package india.hanishkvc.filesharelocal

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
        recyclerView = inflater.inflate(R.layout.fragment_fman_list, container, false) as RecyclerView?

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
            false // The last line / expression is the return value of the lambda fun
        })

        recyclerView?.setOnClickListener({
            Log.v(TAGME, "RVOnClick:DBG: ${it.javaClass}")
            if (it is RecyclerView) {
                Log.v(TAGME, "RVOnClick:DBG: ${it.javaClass}")
                if (it.focusedChild != null) {
                    Log.v(TAGME, "RVOnClick:DBG: ${it.focusedChild?.javaClass}")
                    val vh = it.getChildViewHolder(it.focusedChild)
                    if (vh is FManRecyclerViewAdapter.ViewHolder) {
                        Log.v(TAGME, "RVOnClick: ${vh.id}, ${vh.pathView}")
                    }
                }
            } else {
                Log.v(TAGME, "RVOnClick:DBG: ${it.javaClass}")
            }
        })

        return recyclerView
    }

    fun updateFrag() {
        recyclerView?.adapter?.notifyDataSetChanged()
    }

    companion object {

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