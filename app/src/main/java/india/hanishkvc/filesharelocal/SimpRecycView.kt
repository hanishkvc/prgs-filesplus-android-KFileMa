/**
 * SimpRecycView - A simple recycler view based list/grid view which provides the
 * view adaptor and holder classes within itself.
 * This is useful if one is working with simple list of strings or so. And just wants
 * to show the same on the screen without breaking head about too many things.
 * @author: C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License: LGPL
 */

package india.hanishkvc.filesharelocal

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Callback which will be called when user clicks or presses an item
 * position: position of the item within the dataList
 * view: the item specific view which got clicked/pressed
 */
typealias SRCVItemClickListener = (position: Int, view: View) -> kotlin.Unit
/**
 * Callback which will be called when user long clicks/presses an item
 * position: position of the item within the dataList
 * view: the item specific view which got clicked/pressed
 */
typealias SRCVItemLongClickListener = (position: Int, view: View) -> kotlin.Boolean

/**
 * SimpRecycView: A RecyclerView, which takes care of the boilerplate required wrt
 * associated view adapter and view holder, for simple recyclerview use cases.
 * It allows one to show a list of items either in a linear or grid layout.
 * Each item in turn could be either a single data value or a set of data values.
 * If single and textual then SRcV can handle it on its own fully.
 *
 * Handling Display of Individual Item, Developer could either
 * * let SRcV handle the display of items to users
 *   If there is only a single data per item to be shown, then SRcV will handle this
 *   using textview, automatically.
 * * or they could handle the item view on their own similar to in RecyclerView
 *   IN this case one is required to provide the call backs for onSRcVCreateViewHolder
 *   and onSRcVBindViewHolder.
 *
 * Handling Interaction with Individual Items: Developer needs to assign callbacks
 * to onSRCVItemClickListener and onSRCVItemLongClickListener. These are called for
 * any one of the following user interaction with the individual item view.
 * * clicking on the item and or
 * * pressing dpad center when focus is on the item and or
 * * pressing keyboard enter or space button when focus is on the item.
 *   * enter keypress triggers the ClickListener
 *   * space keypress triggers the LongClickListener
 *
 *
 */
class SimpRecycView<E> : RecyclerView {

    private val TAGME = "SimpRecycView"
    var dataList = ArrayList<E>()
    var selected = ArrayList<Int>()
    var bHandleMultiSelection = true
    var onSRCVItemClickListener: SRCVItemClickListener? = null
    var onSRCVItemLongClickListener: SRCVItemLongClickListener? = null

    fun initHelper(context: Context) {
        Log.v(TAGME, "init helper")
        when {
            defaultColumnCount <= 1 -> layoutManager = LinearLayoutManager(context)
            else -> layoutManager = GridLayoutManager(context, defaultColumnCount)
        }
        adapter = SimpViewAdapter()
    }

    constructor(context: Context) : super(context) {
        Log.v(TAGME, "Constructor with 1arg")
        initHelper(context)
    }

    constructor(context: Context, intf: AttributeSet) : super(context, intf) {
        Log.v(TAGME, "Constructor with 2args")
        initHelper(context)
    }

    fun assignDataList(inDataList: ArrayList<E>) {
        dataList.clear()
        for (item in inDataList) {
            dataList.add(item)
        }
        Log.v(TAGME, "assignDataList:${dataList.size} items in")
        if (bHandleMultiSelection) selected.clear()
        adapter?.notifyDataSetChanged()
    }

    inner class SimpViewAdapter : RecyclerView.Adapter<SimpViewAdapter.SimpViewHolder>() {

        private fun createInternalSimpleView(): View {
            val view = TextView(context)
            view.isEnabled = true
            view.visibility = View.VISIBLE
            view.isFocusable = true
            view.typeface = viewTypeface
            view.gravity = Gravity.CENTER_VERTICAL
            view.setTextColor(viewTextColor)
            view.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.setBackgroundResource(viewBackgroundResource)
            return view
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpViewHolder {
            val view = createInternalSimpleView()
            return SimpViewHolder(view)
        }

        private fun bindInternalSimpleView(holder: SimpViewHolder, position: Int) {
            holder.id = position
            (holder.itemView as TextView).text = dataList[position].toString()
            if (bHandleMultiSelection) holder.itemView.isActivated = selected.contains(position)
        }

        override fun onBindViewHolder(holder: SimpViewHolder, position: Int) {
            bindInternalSimpleView(holder, position)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class SimpViewHolder(itemView: View) : ViewHolder(itemView) {
            var id = -1

            private fun handleSelection() {
                if (selected.contains(id)) {
                    selected.remove(id)
                    itemView.isActivated = false
                } else  {
                    selected.add(id)
                    itemView.isActivated = true
                }
            }

            init {
                itemView.setOnClickListener {
                    onSRCVItemClickListener?.invoke(id, it)
                }
                itemView.setOnLongClickListener {
                    if (bHandleMultiSelection) {
                        handleSelection()
                        Log.v(TAGME, "onLongClick: $id, ${selected.contains(id)}")
                    }
                    var res = onSRCVItemLongClickListener?.invoke(id, it)
                    if (res == null) res = false
                    return@setOnLongClickListener res
                }
            }
        }

    }

    companion object {
        var defaultColumnCount: Int = 1
        var viewTextColor: Int = Color.BLACK
        var viewTypeface: Typeface = Typeface.MONOSPACE
        var viewBackgroundResource: Int = android.R.drawable.list_selector_background
    }

}