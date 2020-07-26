/**
 * FileShareLocal: Allow files to be shared locally on the same network across multiple devices
 * ViewerActivity: Allows file to be viewed if possible
 * @author C Hanish Menon <hanishkvc@gmail.com>, 2020
 * @License GPL3
 */

package india.hanishkvc.filesharelocal

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class ViewerActivity : AppCompatActivity() {

    val TAGME = "FSLViewer"

    private var webv: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAGME, "onCreate: Entered, ${intent.data}")
        setContentView(R.layout.activity_viewer)

        title = "Viewer:"+intent.dataString
        webv = findViewById<WebView>(R.id.webv)
        webv?.settings?.loadWithOverviewMode = true
        webv?.settings?.useWideViewPort = true
        //webv.settings.setSupportZoom(true)
        webv?.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Log.w(TAGME, "onCreate: Cant handle $mimetype, $url")
        }
        webv?.loadUrl(intent.data.toString())
    }
}