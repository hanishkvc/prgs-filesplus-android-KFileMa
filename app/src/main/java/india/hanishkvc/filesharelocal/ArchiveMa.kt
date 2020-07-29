/**
 * ArchiveMa - A simple class to work with Archive files, which may be compressed also.
 * This uses the apache commons compress library to work with the archive files.
 * @author: C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License: LGPL
 */

package india.hanishkvc.filesharelocal

import android.util.Log
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import java.io.FileInputStream

class ArchiveMa {

    private val TAGME = "ArchiveMa"

    init {
    }

    fun listArchive(sInFile: String): ArrayList<String> {
        Log.v(TAGME, "listArchive: $sInFile")
        val fileList = ArrayList<String>()
        val inFile = FileInputStream(sInFile)
        val inFileA = ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, inFile)
        while(true) {
            val entryA = inFileA.nextEntry
            if (entryA == null) break
            fileList.add(entryA.name)
        }
        inFileA.close()
        inFile.close()
        return fileList
    }

}