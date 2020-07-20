package india.hanishkvc.filesharelocal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import india.hanishkvc.filesharelocal.fman.FMan.FManItem

/**
 * [RecyclerView.Adapter] that can display a [FManItem].
 * TODO: Replace the implementation with code for your data type.
 */
class FManRecyclerViewAdapter(
    private val values: List<FManItem>
) : RecyclerView.Adapter<FManRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_fman, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.typeView.text = item.type
        holder.pathView.text = item.path
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val typeView: TextView = view.findViewById(R.id.item_number)
        val pathView: TextView = view.findViewById(R.id.content)

        override fun toString(): String {
            return super.toString() + " '" + pathView.text + "'"
        }
    }
}