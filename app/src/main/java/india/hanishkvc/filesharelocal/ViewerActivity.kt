/**
 * FileShareLocal: Allow files to be shared locally on the same network across multiple devices
 * ViewerActivity: Allows file to be viewed if possible
 * @author C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License GPL3
 */

package india.hanishkvc.filesharelocal

import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class ViewerActivity : AppCompatActivity() {

    val TAGME = "FSLViewer"

    private var webv: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAGME, "onCreate: Entered, ${intent.type}, ${intent.data}")
        setContentView(R.layout.activity_viewer)

        title = "Viewer:"+intent.dataString
        webv = findViewById<WebView>(R.id.webv)
        webv?.settings?.loadWithOverviewMode = true
        webv?.settings?.useWideViewPort = true
        /*
        webv?.settings?.setSupportZoom(true)
        webv?.settings?.builtInZoomControls = true
         */
        webv?.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Log.w(TAGME, "onCreate: Cant handle $mimetype, $url")
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
                Log.v(TAGME, "webv:onRcvdErr: req[$request], err[$error]")
            }

        }
        webv?.loadUrl(intent.data.toString())
    }
}