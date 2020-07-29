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
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SimpRecycView(context: Context, val dataList: ArrayList<String>) : RecyclerView(context) {

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = SimpViewAdapter()
    }

    inner class SimpViewAdapter : RecyclerView.Adapter<SimpViewAdapter.SimpViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpViewHolder {
            val view = TextView(context)
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

}