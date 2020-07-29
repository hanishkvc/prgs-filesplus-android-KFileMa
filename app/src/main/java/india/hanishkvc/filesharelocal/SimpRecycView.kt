/**
 * SimpRecycView - A simple recycler view based list/grid view which provides the
 * view adaptor and holder classes within itself.
 * This is useful if one is working with simple list of strings or so. And just wants
 * to show the same on the screen without breaking head about too many things.
 * HanishKVC, 2020
 * GPL
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

class SimpRecycView : RecyclerView {

    private val TAGME = "SimpRecycView"
    var dataList = ArrayList<String>()

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

    fun assignDataList(inDataList: ArrayList<String>) {
        dataList.clear()
        for (item in inDataList) {
            dataList.add(item)
        }
        Log.v(TAGME, "assignDataList:${dataList.size} items in")
        adapter?.notifyDataSetChanged()
    }

    inner class SimpViewAdapter : RecyclerView.Adapter<SimpViewAdapter.SimpViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpViewHolder {
            val view = TextView(context)
            view.isEnabled = true
            view.visibility = View.VISIBLE
            view.isFocusable = true
            view.typeface = viewTypeface
            view.gravity = Gravity.CENTER_VERTICAL
            view.setTextColor(viewTextColor)
            view.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.setBackgroundResource(viewBackgroundResource)
            return SimpViewHolder(view)
        }

        override fun onBindViewHolder(holder: SimpViewHolder, position: Int) {
            holder.id = position
            (holder.itemView as TextView).text = dataList[position]
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class SimpViewHolder(itemView: View) : ViewHolder(itemView) {
            var id = -1
        }

    }

    companion object {
        var defaultColumnCount: Int = 1
        var viewTextColor: Int = Color.BLACK
        var viewTypeface: Typeface = Typeface.MONOSPACE
        var viewBackgroundResource: Int = android.R.drawable.list_selector_background
    }

}