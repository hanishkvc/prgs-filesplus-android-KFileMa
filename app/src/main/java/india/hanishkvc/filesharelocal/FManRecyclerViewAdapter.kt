package india.hanishkvc.filesharelocal

import android.util.Log
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
    private val values: List<FManItem>
) : RecyclerView.Adapter<FManRecyclerViewAdapter.ViewHolder>() {

    private val TAGME = "FManRVAdap"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_fman, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.id = item.id
        holder.typeView.text = item.type.shortDesc
        holder.pathView.text = item.path.substringAfterLast(File.separator)
        //Log.v(TAGME, "onBindVH:[${File.pathSeparator},${File.separator}]: in[${item.path}], out[${holder.pathView.text}]")
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var id: Int = -1
        val typeView: TextView = view.findViewById(R.id.item_type)
        val pathView: TextView = view.findViewById(R.id.item_path)

        init {
            view.setOnClickListener {
                Log.v(TAGME, "VHOnClick:${id}, ${pathView.text}, ${FMan.ITEMS[id]}")
                FMan.fManItemInteractionIF?.doNavigate(id)
            }

            view.setOnLongClickListener {
                Log.v(TAGME, "VHOnClick:${id}, ${pathView.text}, ${FMan.ITEMS[id]}")
                FMan.fManItemInteractionIF?.doSelect(id)!!
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + pathView.text + "'"
        }
    }
}