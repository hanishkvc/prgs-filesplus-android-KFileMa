/**
 * Bunch of utility functions
 * @author: C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @license: LGPL
 */
package india.hanishkvc.filesharelocal

import android.content.res.Resources
import android.util.Log

object UtilsMa {

    val TAGME = "UtilsMa"

    /**
     * Chop a given string to fit within display width
     */
    fun strChopToDispWidth(resources: Resources, inStr: String?, charPixelWidth: Int=12): String? {
        Log.d(TAGME, "xdpi: ${resources.displayMetrics.xdpi}")
        Log.d(TAGME, "ydpi: ${resources.displayMetrics.ydpi}")
        Log.d(TAGME, "widthPixels: ${resources.displayMetrics.widthPixels}")
        Log.d(TAGME, "density: ${resources.displayMetrics.density}")
        Log.d(TAGME, "scaledDensity: ${resources.displayMetrics.scaledDensity}")
        Log.d(TAGME, "densityDpi: ${resources.displayMetrics.densityDpi}")
        val limit = resources.displayMetrics.widthPixels/charPixelWidth
        Log.d(TAGME, "strChopDW: w=${resources.displayMetrics.widthPixels}, lim=$limit")
        if (inStr == null) {
            Log.d(TAGME, "strChopDW: $inStr")
            return inStr
        }
        var theStr = inStr
        if (inStr.length > limit) {
            theStr = inStr.substring(inStr.length-limit)
            theStr = "...$theStr"
        }
        return theStr
    }

    fun longTo7Str(inLong: Long): String {
        var theStr: String = ""
        if (inLong > 999999) {
            theStr = "%.2G".format(inLong.toDouble())
        } else {
            theStr = "% 7d".format(inLong)
        }
        return theStr
    }

    fun longToKMG8Str(inLong: Long): String {
        var theDbl = inLong.toDouble()
        var theStr: String = ""
        when {
            inLong <= 999 -> theStr = "% 7d ".format(inLong)
            inLong <= 999999 -> {
                theDbl = theDbl/1024
                theStr = "% 7.2fK".format(theDbl)
            }
            inLong <= 999999999 -> {
                theDbl = theDbl/(1024*1024)
                theStr = "% 7.2fM".format(theDbl)
            }
            inLong > 999999999 -> {
                theDbl = theDbl/(1024*1024*1024)
                theStr = "% 7.2fG".format(theDbl)
            }
        }
        return theStr
    }

}