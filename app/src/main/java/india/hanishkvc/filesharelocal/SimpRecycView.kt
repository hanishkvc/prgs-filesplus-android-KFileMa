/**
 * SimpRecycView - A simple recycler view based list/grid view which provides and handles
 * the view adaptor and view holder classes within itself.
 * This simplifies the use of recycler view for many simple use cases like when wanting
 * to show a simple list of items (with textual info), by cutting out the boiler plate
 * needed around its usage. At the same time it still allows one to provide more complex
 * views of the item, if they so desire, without worrying about all the underlying boilerplate,
 * that would be required.
 * i.e this is useful if one wants to show a simple list/grid of items, on the screen,
 * without breaking head about too many things.
 * @author: C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License: LGPL
 */

package india.hanishkvc.filesharelocal

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

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
 * Callback which will be called when SRcV wants a view to show one of the items
 * given to it. This could occur when SRcV becomes visible and or its contents change.
 * This helps create a set of itemviews which will be used as required to show the
 * contents of the dataList (and or as the developer chooses).
 */
typealias SRCVCreateView = (parent: ViewGroup) -> View
/**
 * Callback which will be called when SRcV wants to show a item using an existing
 * view, from its cache of views (view holders with their views).
 * Developers using SRcV are expected to update the view's content based on the data
 * corresponding to the given position in the datalist.
 */
typealias SRCVBindView = (view: View, position: Int) -> Unit


/**
 * SimpRecycView: A RecyclerView, which takes care of the boilerplate required wrt
 * associated view adapter and view holder, for simple recyclerview use cases.
 * It allows one to show a list of items either in a linear or grid layout.
 * Each item in turn could be either a single data value or a set of data values.
 * If single and textual then SRcV can handle it on its own fully.
 *
 * List or Grid Layout: Based on the number of columns to show, it decides between
 * list and grid layout. If more than 1 column is required then grid layout is used
 * else it will appear as a list. If explicitly creating SRcV in code, then one
 * can pass the columnCount to its constructor. Else there is companion var called
 * defaultColumnCount, whose value will be used to decide, developer using SRcV can
 * set this static/companion var to their liking. By default it will be 1.
 *
 * Handling Display of Individual Item: Developer could either
 * * let SRcV handle the display of items to users
 *   If there is only a single data per item to be shown, then SRcV will handle this
 *   using textview, automatically.
 * * or they could handle the item view on their own similar to in RecyclerView
 *   IN this case one is required to provide the call backs for onSRcVCreateView
 *   and onSRcVBindView.
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
 * MultiSelection of Displayed Items: By setting bHandleMultiSelection, the developer
 * can let SRcV maintain a list of selected items. User can select or deselect items
 * from this list by long clicking the corresponding item in the SRcV view. SRcV will
 * indicate selection of the item by setting its itemview state to activated.
 *     It expects ItemView background drawable selector to support activated state,
 *     with a distinguishable appearance, so that user can notice the selections.
 *
 */
class SimpRecycView<E> : RecyclerView {

    private val TAGME = "SimpRecycView"

    /**
     * dataList: The list of data items associated with this SRcV.
     */
    var dataList = ArrayList<E>()
    /**
     * selected: The list of item positions which have been selected by the user.
     */
    var selected = ArrayList<Int>()
    /**
     * bHandleMultiSelection: Set to allow SRcV handle the multi selection logic.
     *     It expects ItemView background drawable selector to support activated state.
     *     Selected items will have their itemview's state set to activated.
     */
    var bHandleMultiSelection = true

    /**
     * CallBacks: Interaction
     * onSRCVItemClickListener: Called when the user clicks/presses an item
     */
    var onSRCVItemClickListener: SRCVItemClickListener? = null
    /**
     * CallBacks: Interaction
     * onSRCVItemLongClickListener: Called when user long clicks/presses the item.
     *     If using keyboard, then pressing spacebar triggers this.
     */
    var onSRCVItemLongClickListener: SRCVItemLongClickListener? = null

    /**
     * CallBacks: Display
     * onSRCVCreateView: Called when a new item view is required.
     */
    var onSRCVCreateView: SRCVCreateView? = null
    /**
     * CallBacks: Display
     * onSRCVBindView: Called when a item view is being assinged to new or different item.
     *     Thus needing its content to change.
     * NOTE: By using view and not any view holder, it potentially sacrifices perf a bit ;)
     * as one is required to use findViewById on the view to get the child views. But then
     * it keeps the flow simple for the SimpRecycView, without too much overhead at one level.
     */
    var onSRCVBindView: SRCVBindView? = null

    private fun initHelper(context: Context, columnCount: Int = -1) {
        Log.v(TAGME, "init helper")
        val theColumnCount = if (columnCount == -1) defaultColumnCount else columnCount
        when {
            theColumnCount <= 1 -> layoutManager = LinearLayoutManager(context)
            else -> layoutManager = GridLayoutManager(context, defaultColumnCount)
        }
        adapter = SimpViewAdapter()
    }

    constructor(context: Context) : super(context) {
        Log.v(TAGME, "Constructor with 1arg")
        initHelper(context)
    }

    /**
     * The constructor that is normally called during layout xml inflating
     */
    constructor(context: Context, intf: AttributeSet) : super(context, intf) {
        Log.v(TAGME, "Constructor with 2args: attribute set")
        initHelper(context)
    }

    /**
     * Constructor to call, if you want to explicitly control the number of columns
     * to be shown and inturn whether a list or grid layout will be provided.
     */
    constructor(context: Context, columnCount: Int) : super(context) {
        Log.v(TAGME, "Constructor with 2args: columnCount")
        initHelper(context, columnCount)
    }

    /**
     * Use this to load a new dataList to this SRcV instance.
     * It takes care of clearing the old data and selected list,
     * then copies over the newly passed dataList,
     * notifies internal adapter and layout managers about new dataset,
     * and in turn scrolls and sets focus to a predefined position in
     * the dataList if requested for now.
     */
    fun assignDataList(inDataList: ArrayList<E>, initialPosition: Int = -1) {
        dataList.clear()
        for (item in inDataList) {
            dataList.add(item)
        }
        Log.v(TAGME, "assignDataList:${dataList.size} items in")
        if (bHandleMultiSelection) selected.clear()
        adapter?.notifyDataSetChanged()
        if (initialPosition >= 0) {
            focus(initialPosition, true)
        }
    }

    fun getSelectedList(): ArrayList<E> {
        val selectedList = ArrayList<E>()
        for (i in selected) {
            selectedList.add(dataList[i])
        }
        return selectedList
    }

    /**
     * Set focus to the view corresponding to the specified position in the dataList.
     * If requested it even scrolls the item into view, if not already in view.
     */
    fun focus(position: Int, bFocus: Boolean = true, bScrollIfRequired: Boolean = true) {
        if (bScrollIfRequired) scrollToPosition(position)
        post {
            layoutManager?.findViewByPosition(position)?.let {
                if (bFocus) {
                    it.requestFocus()
                } else {
                    it.clearFocus()
                }
            }
        }
    }


    inner class SimpViewAdapter : RecyclerView.Adapter<SimpViewAdapter.SimpViewHolder>() {

        private fun createInternalSimpleView(): View {
            val view = TextView(context)
            view.isEnabled = true
            view.visibility = View.VISIBLE
            view.isFocusable = true
            view.typeface = viewTypeface
            view.textSize = viewTextSizeSP //viewTextSizeBasePixels*resources.displayMetrics.scaledDensity
            view.gravity = Gravity.CENTER_VERTICAL
            view.setTextColor(viewTextColor)
            /*
            val mlayout = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val wpx = (16*resources.displayMetrics.density).roundToInt()
            val wpy = (8*resources.displayMetrics.density).roundToInt()
            mlayout.setMargins(wpx, wpy, wpx, wpy)
            view.layoutParams = mlayout
             */
            view.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val wpx = (viewPadHorz*resources.displayMetrics.density).roundToInt()
            val wpy = (viewPadVert*resources.displayMetrics.density).roundToInt()
            view.setPadding(wpx, wpy, wpx, wpy)
            view.setBackgroundResource(viewBackgroundResource)
            return view
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpViewHolder {
            val view = onSRCVCreateView?.invoke(parent) ?: createInternalSimpleView()
            return SimpViewHolder(view)
        }

        private fun bindInternalSimpleView(view: View, position: Int) {
            (view as TextView).text = dataList[position].toString()
        }

        override fun onBindViewHolder(holder: SimpViewHolder, position: Int) {
            holder.id = position
            onSRCVBindView?.invoke(holder.itemView, position) ?: bindInternalSimpleView(holder.itemView, position)
            if (bHandleMultiSelection) holder.itemView.isActivated = selected.contains(position)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class SimpViewHolder(itemView: View) : ViewHolder(itemView) {
            var id = -1

            private fun handleSelectedPlus() {
                if (selected.contains(id)) {
                    selected.remove(id)
                    itemView.isActivated = false
                } else  {
                    selected.add(id)
                    itemView.isActivated = true
                }
            }

            private fun handleSelection(view: View): Boolean {
                if (bHandleMultiSelection) {
                    handleSelectedPlus()
                    Log.v(TAGME, "onLongClick: $id, ${selected.contains(id)}")
                }
                var res = onSRCVItemLongClickListener?.invoke(id, view)
                if (res == null) res = false
                return res
            }

            init {
                itemView.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        onSRCVItemClickListener?.invoke(id, v)
                        return@setOnTouchListener true
                    }
                    false
                }
                itemView.setOnClickListener {
                    onSRCVItemClickListener?.invoke(id, it)
                }
                itemView.setOnLongClickListener {
                    return@setOnLongClickListener handleSelection(it)
                }
                itemView.setOnKeyListener { v, keyCode, event ->
                    if (event != null) {
                        if ( (keyCode == KeyEvent.KEYCODE_SPACE) &&
                            (event.action == KeyEvent.ACTION_DOWN) ) {
                            return@setOnKeyListener handleSelection(v)
                        }
                    }
                    false
                }
            }
        }

    }

    companion object {
        var defaultColumnCount: Int = 1
        var viewTextColor: Int = Color.BLACK
        var viewTypeface: Typeface = Typeface.MONOSPACE
        var viewTextSizeSP = 16.0f
        var viewPadHorz = 16
        var viewPadVert = 8
        var viewBackgroundResource: Int = android.R.drawable.list_selector_background
    }

}