/**
 * ArchiveMa - A simple class to work with Archive files, which may be compressed also.
 * This uses the apache commons compress library to work with the archive files.
 * @author: C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License: LGPL
 */

package india.hanishkvc.filesharelocal

import android.util.Log
import org.apache.commons.compress.archivers.ArchiveException
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import java.io.FileInputStream
import java.io.InputStream

class ArchiveMa {

    private val TAGME = "ArchiveMa"

    init {
    }

    fun listArchive(sInFile: String): ArrayList<String> {
        val fileList = ArrayList<String>()
        val inFile: InputStream = FileInputStream(sInFile)
        var sType = "UNKNOWN"
        try {
            sType = ArchiveStreamFactory.detect(inFile)
        } catch (e: ArchiveException) {
            Log.v(TAGME, "UnknownArchive:$sInFile: ${e.localizedMessage}" )
            return fileList
        }
    }

}