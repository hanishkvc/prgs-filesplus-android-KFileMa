/**
 * ArchiveMa - A simple class to work with Archive files, which may be compressed also.
 * This uses the apache commons compress library to work with the archive files.
 * @author: C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License: LGPL
 */

package india.hanishkvc.filesharelocal

import android.util.Log
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class ArchiveMa {

    private val TAGME = "ArchiveMa"

    init {
    }

    fun mapExtToArchiveType(sInFile: String): String {
        val file = File(sInFile)
        val sExt = file.extension.toLowerCase()
        val sType = when(sExt) {
            "zip" -> ArchiveStreamFactory.ZIP
            "tar" -> ArchiveStreamFactory.TAR
            "7z" -> ArchiveStreamFactory.SEVEN_Z
            "ar" -> ArchiveStreamFactory.AR
            "jar" -> ArchiveStreamFactory.JAR
            "arj" -> ArchiveStreamFactory.ARJ
            else -> "UNKNOWN"
        }
        return sType
    }

    fun listArchive(sInFile: String): ArrayList<String> {
        Log.v(TAGME, "listArchive: $sInFile")
        val fileList = ArrayList<String>()
        val inFile = FileInputStream(sInFile)
        val inFileB = BufferedInputStream(inFile)
        //val sType = mapExtToArchiveType(sInFile)
        var inFileX = inFileB
        try {
            val inFileC = CompressorStreamFactory().createCompressorInputStream(inFileB)
            inFileX = BufferedInputStream(inFileC)
            Log.v(TAGME, "listArchive: Is compressed")
        } catch (e: Exception) {
            Log.v(TAGME, "listArchive: Is not compressed or ..., ${e.localizedMessage}")
        }
        val inFileA = ArchiveStreamFactory().createArchiveInputStream(inFileX)
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