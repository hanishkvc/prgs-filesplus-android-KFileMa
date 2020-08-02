/**
 * HPopupMenu - Hierarchical popup menu for android
 * @author C Hanish Menon <hanishkvc@gmail.com>
 * @version v20200802IST1604
 */
package india.hanishkvc.filesharelocal

import android.content.Context
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu

/**
 * HPopupMenu - Hierarchical popup menu for android
 * As Android and ANdroidX PopupMenu seems to have issue with Hierarchical menus
 * i.e menu with submenus in it, This helper class to provide same functionality
 * has been created.
 *
 * Android(X) generates
 *     java.lang.ClassCastException: android.widget.HeaderViewListAdapter cannot be cast to androidx.appcompat.view.menu.MenuAdapter
 * This works around it by, handling the menu hierarchy has independent menus
 *
 * Define the Menu Hierarchy
 * Load the root Menu
 * Show the menu
 */

class HPopupMenu(val context: Context, val view: View ) {

    val TAGME = "HPopupMenu"
    lateinit var popupMenu: PopupMenu
    val hm = HashMap<String, Int>()
    var curLvl = 0
    var curPath = "$curLvl:-1"
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
            Log.v(TAGME, "onMenuItemClick: newPath=$newPath")
            if (newPath in hm) {
                Log.v(TAGME, "onMenuItemClick:SubMenu: $newPath")
                popupMenu.dismiss()
                popupMenu = PopupMenu(context, view)
                popupMenu.inflate(hm[newPath]!!)
                curLvl += 1
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