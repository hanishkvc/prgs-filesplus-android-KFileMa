/**
 * FileShareLocal: Allow files to be shared locally on the same network across multiple devices
 * ViewerActivity: Allows file to be viewed if possible
 * @author C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License GPL3
 */

package india.hanishkvc.filesharelocal

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import java.util.zip.ZipFile

class ViewerActivity : AppCompatActivity() {

    val TAGME = "FSLViewer"

    private var webv: WebView? = null
    private var videov: VideoView? = null
    private var mtextv: EditText? = null
    private var srcv: SimpRecycView? = null

    var bVideoFault = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAGME, "onCreate: Entered, ${intent.type}, ${intent.data}")
        setContentView(R.layout.activity_viewer)

        title = "Viewer:"+intent.dataString
        webv = findViewById<WebView>(R.id.webv)
        webv?.isEnabled = false
        webv?.visibility = View.GONE
        videov = findViewById<VideoView>(R.id.videov)
        videov?.isEnabled = false
        videov?.visibility = View.GONE
        mtextv = findViewById<EditText>(R.id.mtextv)
        mtextv?.isEnabled = false
        mtextv?.visibility = View.GONE
        mtextv?.typeface = Typeface.MONOSPACE
        srcv = findViewById<SimpRecycView>(R.id.srcv)
        srcv?.isEnabled = false
        srcv?.visibility = View.GONE

        setResult(Activity.RESULT_OK)
        handleContent()
    }

    fun handleContent() {
        val itype = intent.type?.toLowerCase()
        val ifileStr = intent.data?.toFile().toString()
        if (itype != null) {
            if (itype.startsWith("video")) {
                return showVideo()
            } else {
                var sArcType = ArchiveMa.UNKNOWN
                if (itype.endsWith("/zip"))
                    sArcType = ArchiveMa.mapExtToArchiveType(ifileStr)
                if (itype.endsWith("/x-tar"))
                    sArcType = ArchiveMa.mapExtToArchiveType(ifileStr)
                if (itype.endsWith("/gzip"))
                    sArcType = ArchiveMa.mapExtToCompressType(ifileStr)
                if (itype.endsWith("/bzip2"))
                    sArcType = ArchiveMa.mapExtToCompressType(ifileStr)
                if (itype.endsWith("/x-xz"))
                    sArcType = ArchiveMa.mapExtToCompressType(ifileStr)
                if (itype.endsWith("/x-7z-compressed"))
                    sArcType = ArchiveMa.mapExtToArchiveType(ifileStr)
                if (sArcType != ArchiveMa.UNKNOWN) {
                    return showArchive(sArcType)
                }
            }
        } else {
            if (ifileStr != null) {
                var sCType = ArchiveMa.mapExtToCompressType(ifileStr)
                var sAType = ArchiveMa.mapExtToArchiveType(ifileStr)
                if ((sCType != ArchiveMa.UNKNOWN) || (sAType != ArchiveMa.UNKNOWN) ) {
                    var sArcType = if (sCType == ArchiveMa.UNKNOWN) sAType else sCType
                    return showArchive(sArcType)
                }
            }
        }
        showGeneralUseWebV()
    }

    fun showGeneralUseWebV() {
        Log.v(TAGME, "useWebV: Entered")
        webv?.visibility = View.VISIBLE
        webv?.isEnabled = true
        webv?.settings?.loadWithOverviewMode = true
        webv?.settings?.useWideViewPort = true
        /*
        webv?.settings?.setSupportZoom(true)
        webv?.settings?.builtInZoomControls = true
         */

        webv?.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Log.e(TAGME, "onCreate: Cant handle $mimetype, $url")
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        webv?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Log.v(TAGME, "webv:onPgFin: H[${webv?.contentHeight}, ${webv?.height}], W[${webv?.width}]")
            }

            override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
                super.onScaleChanged(view, oldScale, newScale)
                Log.v(TAGME, "webv:onScaleChg: oldScale[$oldScale] newScale[$newScale]")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Log.e(TAGME, "webv:onRcvdErr: req[${request?.url}], err[${error?.errorCode}:${error?.description}]")
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

        }
        webv?.loadUrl(intent.data.toString())

    }

    fun showVideo() {
        Log.v(TAGME, "useVideoV: Entered")
        videov?.visibility = View.VISIBLE
        videov?.isEnabled = true
        videov?.setMediaController(MediaController(this).apply {
            setAnchorView(videov)
            keepScreenOn = true
        })

        videov?.setOnErrorListener { mp, what, extra ->
            Log.e(TAGME, "useVideoV: Error playing")
            setResult(Activity.RESULT_CANCELED)
            bVideoFault = true
            //videov?.stopPlayback()
            finish()
            true
        }

        videov?.setOnCompletionListener {
            Log.v(TAGME, "useVideoV: OnCompletion")
            finish()
        }

        bVideoFault = false
        videov?.setVideoURI(intent.data)
        videov?.start()
    }

    fun showZipJavaUtil() {
        Log.v(TAGME, "showZip: Entered")
        srcv?.visibility = View.VISIBLE
        srcv?.isEnabled = true
        val sFiles = ArrayList<String>()
        val zipFile = ZipFile(intent.data?.toFile())
        for (entry in zipFile.entries()) {
            val type = if (entry.isDirectory) "[D]" else "[f]"
            val sEntry = "$type ${entry.name}\n"
            sFiles.add(sEntry)
        }
        srcv?.assignDataList(sFiles)
    }

    fun showArchive(sArcType: String) {
        Log.v(TAGME, "showArchive: Entered")
        srcv?.visibility = View.VISIBLE
        srcv?.isEnabled = true
        var sFiles: ArrayList<String>? = null
        srcv?.onSRCVItemClickListener = {
            Log.v(TAGME, "onSRCVItemClick: ${sFiles?.get(it)}")
        }
        srcv?.onSRCVItemLongClickListener = {
            Log.v(TAGME, "onSRCVItemLongClick: $sFiles[it]")
            false
        }
        try {
            val sArchFile = intent.data?.toFile().toString()
            if (sArcType == ArchiveMa.ARCHIVE_7Z) {
                sFiles = ArchiveMa().listArchive7z(sArchFile)
            } else {
                sFiles = ArchiveMa().listArchive(sArchFile)
            }
            srcv?.assignDataList(sFiles)
        } catch (e: Exception) {
            Log.v(TAGME, "showArchive:Failed: ${e.localizedMessage}")
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

}