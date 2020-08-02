/**
 * HPopupMenu - Hierarchical popup menu for android
 * As Android and ANdroidX PopupMenu seems to have issue with Hierarchical menus
 * i.e menu with submenus in it, This helper class to provide same functionality
 * has been created.
 *
 * @author C Hanish Menon <hanishkvc@gmail.com>
 * @version v20200802IST1604
 */
package india.hanishkvc.filesharelocal

import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu


class HPopupMenu(context: Context, view: View ) {

    lateinit var popupMenu: PopupMenu
    val hm = HashMap<String, Int>()
    var curLvl = 0
    var curPath = "$curLvl:0"
    var onMenuItemClickListener: ((MenuItem)->Boolean)? = null

    init {
        popupMenu = PopupMenu(context, view)
    }

    fun buildMenuMap(menuLvl: Int, itemId: Int, menuRes: Int) {
        hm.put("$menuLvl:$itemId", menuRes)
    }

    fun prepare() {
        popupMenu.inflate(hm[curPath]!!)
        popupMenu.setOnMenuItemClickListener {
            val newPath = "$curLvl:${it.itemId}"
            if (newPath in hm) {
                popupMenu.dismiss()
                popupMenu.inflate(hm[newPath]!!)
                popupMenu.show()
                return@setOnMenuItemClickListener true
            }
            if (onMenuItemClickListener != null) return@setOnMenuItemClickListener onMenuItemClickListener!!.invoke(it)
            false
        }
    }

    fun show() {
        popupMenu.show()
    }

}