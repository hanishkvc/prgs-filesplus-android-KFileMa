/**
 * ArchiveMa - A simple class to work with Archive files, which may be compressed also.
 * This uses the apache commons compress library to work with the archive files.
 * @author: C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License: LGPL
 */

package india.hanishkvc.filesharelocal

import android.util.Log
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.StreamingNotSupportedException
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class ArchiveMa {

    private val TAGME = "ArchiveMa"

    init {
    }

    fun listArchive(sInFile: String): ArrayList<String> {
        Log.v(TAGME, "listArchive: $sInFile")
        val fileList = ArrayList<String>()
        val inFile = FileInputStream(sInFile)
        val inFileB = BufferedInputStream(inFile)
        var inFileX = inFileB
        try {
            val inFileC = CompressorStreamFactory().createCompressorInputStream(inFileB)
            inFileX = BufferedInputStream(inFileC)
            Log.v(TAGME, "listArchive: Is compressed")
        } catch (e: Exception) {
            Log.v(TAGME, "listArchive: Is not compressed or ..., ${e.localizedMessage}")
        }
        var inFileA: ArchiveInputStream? = null
        try {
            inFileA = ArchiveStreamFactory().createArchiveInputStream(inFileX)
        } catch (e: StreamingNotSupportedException) {
            Log.w(TAGME, "listArchive:${e.localizedMessage}, chaining in 7z")
            if (e.format == ARCHIVE_7Z) {
                inFileX.close()
                inFileB.close()
                inFile.close()
                return listArchive7z(sInFile)
            }
        }
        while(true) {
            val entryA = inFileA?.nextEntry
            if (entryA == null) break
            fileList.add(entryA.name)
        }
        if (inFileX != inFileB) inFileB.close()
        inFileX.close()
        inFileA?.close()
        inFile.close()
        return fileList
    }

    fun listArchive7z(sInFile: String): ArrayList<String> {
        val f7 = SevenZFile(File(sInFile))
        val fileList = ArrayList<String>()
        while (true) {
            val entry7 = f7.nextEntry
            if (entry7 == null) break
            fileList.add(entry7.name)
        }
        f7.close()
        return fileList
    }

    companion object {
        const val UNKNOWN = "UNKNOWN"
        const val ARCHIVE_7Z = ArchiveStreamFactory.SEVEN_Z

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
                else -> UNKNOWN
            }
            return sType
        }

        fun mapExtToCompressType(sInFile: String): String {
            val sInFile2 = sInFile.toLowerCase()
            val sExt = sInFile2.substringAfterLast(".", UNKNOWN)
            val sType = when(sExt) {
                "bz2" -> CompressorStreamFactory.BZIP2
                "gz" -> CompressorStreamFactory.GZIP
                "xz" -> CompressorStreamFactory.XZ
                else -> UNKNOWN
            }
            return sType
        }

    }

}