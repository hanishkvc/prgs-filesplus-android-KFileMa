/**
 * ArchiveMa - A simple class to work with Archive files, which may be compressed also.
 * This uses the apache commons compress library to work with the archive files.
 * @author: C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License: LGPL
 */

package india.hanishkvc.filesharelocal

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

class ArchiveMa {

    private val TAGME = "ArchiveMa"

    init {
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun listArchive(sInFile: String): ArrayList<String> {
        Log.v(TAGME, "listArchive: $sInFile")
        val fileList = ArrayList<String>()
        val inFile: InputStream = Files.newInputStream(Paths.get(sInFile))
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