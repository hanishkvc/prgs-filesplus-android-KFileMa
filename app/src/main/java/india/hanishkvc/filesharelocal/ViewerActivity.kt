/**
 * FileShareLocal: Allow files to be shared locally on the same network across multiple devices
 * ViewerActivity: Allows file to be viewed if possible
 * @author C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License GPL3
 */

package india.hanishkvc.filesharelocal

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class ViewerActivity : AppCompatActivity() {

    val TAGME = "FSLViewer"

    private var webv: WebView? = null
    private var videov: VideoView? = null

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

        setResult(Activity.RESULT_OK)
        handleContent()
    }

    fun handleContent() {
        if ( (intent.type != null) && intent.type!!.startsWith("video") ) {
            useVideoV()
        } else {
            useWebV()
        }
    }

    fun useWebV() {
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

    fun useVideoV() {
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

}