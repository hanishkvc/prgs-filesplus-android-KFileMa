package india.hanishkvc.filesharelocal

import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import india.hanishkvc.filesharelocal.fman.FMan
import india.hanishkvc.filesharelocal.fman.FMan.FManItem
import java.io.File

/**
 * [RecyclerView.Adapter] that can display a [FManItem].
 * TODO: Replace the implementation with code for your data type.
 */
class FManRecyclerViewAdapter(
    private val fmd: FMan.FManData
) : RecyclerView.Adapter<FManRecyclerViewAdapter.ViewHolder>() {

    private val TAGME = "FManRVAdap"
    val selected: ArrayList<Int> = ArrayList<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_fman_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = fmd.ITEMS[position]
        holder.id = item.id
        holder.typeView.text = item.type.shortDesc
        holder.pathView.text = item.path.substringAfterLast(File.separator)
        if (selected.contains(position)) holder.itemView.setActivated(true) else holder.itemView.isActivated = false
        //Log.v(TAGME, "onBindVH:[${File.pathSeparator},${File.separator}]: in[${item.path}], out[${holder.pathView.text}]")
    }

    fun handleSelect(position: Int, view: View): Boolean {
        if (selected.contains(position)) {
            selected.remove(position)
            view.isActivated = false
        } else {
            selected.add(position)
            view.isActivated = true
        }
        return FMan.fManItemInteractionIF?.doSelect(position)!!
    }

    override fun getItemCount(): Int = fmd.ITEMS.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var id: Int = -1
        val typeView: TextView = view.findViewById(R.id.item_type)
        val pathView: TextView = view.findViewById(R.id.item_path)

        init {
            view.setOnClickListener {
                Log.v(TAGME, "VHOnClick:${id}, ${pathView.text}, ${fmd.ITEMS[id]}")
                FMan.fManItemInteractionIF?.doNavigate(id)
            }

            view.setOnLongClickListener {
                Log.v(TAGME, "VHOnLongClick:${it.javaClass}:${id}, ${pathView.text}, ${fmd.ITEMS[id]}")
                handleSelect(id, it)
            }

            view.setOnKeyListener { v, keyCode, event ->
                if (event != null) {
                    if ( (keyCode == KeyEvent.KEYCODE_SPACE) &&
                        (event.action == KeyEvent.ACTION_DOWN) ) {
                        Log.v(TAGME, "VHOnKey:${v.javaClass}:SPACE:${id}, ${pathView.text}, ${fmd.ITEMS[id]}")
                        return@setOnKeyListener handleSelect(id, v)
                    }
                }
                false
            }

        }

        override fun toString(): String {
            return super.toString() + " '" + pathView.text + "'"
        }
    }
}