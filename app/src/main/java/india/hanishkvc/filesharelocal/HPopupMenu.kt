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
import androidx.core.view.iterator
import java.util.*
import kotlin.collections.HashMap

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
    var curLvl = ROOTMENU_LVL
    var curId = ROOTMENU_ID
    private val curPath: String
            get() {
                return "$curLvl:$curId"
            }
    var onMenuItemClickListener: ((MenuItem)->Boolean)? = null
    val callStack = Stack<Pair<Int, Int>>()
    var hotFromClick = false

    init {
        popupMenu = PopupMenu(context, view)
    }

    fun buildMenuMap(menuLvl: Int, itemId: Int, menuRes: Int) {
        hm.put("$menuLvl:$itemId", menuRes)
    }

    fun markSubs(lvl: Int) {
        for (item in popupMenu.menu) {
            val newPath = "$lvl:${item.itemId}"
            if (newPath in hm) {
                item.title = item.title as String + "    >"
            }
        }
    }

    fun setupOnMenuItemClickListener(curPopup: PopupMenu) {
        curPopup.setOnMenuItemClickListener {
            hotFromClick = true
            val newPath = "$curLvl:${it.itemId}"
            Log.v(TAGME, "onMenuItemClick:newPath=$newPath")
            if (newPath in hm) {
                Log.v(TAGME, "onMenuItemClick:$newPath:is a submenu")
                curPopup.dismiss()
                callStack.push(Pair(curLvl, curId))
                popupMenu = PopupMenu(context, view)
                popupMenu.inflate(hm[newPath]!!)
                curLvl += 1
                curId = it.itemId
                markSubs(curLvl)
                setupOnMenuItemClickListener(popupMenu)
                show()
                return@setOnMenuItemClickListener true
            }
            if (onMenuItemClickListener != null) return@setOnMenuItemClickListener onMenuItemClickListener!!.invoke(it)
            false
        }
        curPopup.setOnDismissListener {
            if (hotFromClick) {
                hotFromClick = false
                return@setOnDismissListener
            }
            Log.v(TAGME, "onDismiss:In:$curPath")
            val (backLvl, backId) = callStack.pop()
            curLvl = backLvl
            curId = backId
            Log.v(TAGME, "onDismiss:BackTo:$curPath")
            popupMenu = PopupMenu(context, view)
            popupMenu.inflate(hm[curPath]!!)
            markSubs(curLvl)
            setupOnMenuItemClickListener(popupMenu)
            show()
        }
    }

    fun prepare() {
        popupMenu.inflate(hm[curPath]!!)
        markSubs(curLvl)
        setupOnMenuItemClickListener(popupMenu)
    }

    fun show() {
        popupMenu.show()
    }

    companion object {
        const val ROOTMENU_LVL=0
        const val ROOTMENU_ID=-1
    }
}