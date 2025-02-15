import android.content.BroadcastReceiver
import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.content.Intent
import android.net.wifi.p2p.WifiP2pConfig
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val activity: WifiDirectActivity
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Respond to new connection or disconnections
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
            }
        }
    }
}


class WifiDirectActivity : AppCompatActivity() {
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver
    private val peers = mutableListOf<WifiP2pDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a simple layout programmatically
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val discoverButton = Button(this).apply {
            text = "Discover Peers"
            setOnClickListener { discoverPeers() }
        }
        layout.addView(discoverButton)

        setContentView(layout)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
        receiver = WiFiDirectBroadcastReceiver(manager, channel, this)
    }

    private fun discoverPeers() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Discovery initiated successfully
            }

            override fun onFailure(reasonCode: Int) {
                discoverPeers()
            }
        })
    }

    private fun connectToPeer(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Connection successful
            }

            override fun onFailure(reason: Int) {
                // Connection failed
            }
        })
    }
    private fun sendIdCode(idCode: String, targetAddress: InetAddress) {
        Thread {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(targetAddress, 8888), 5000)
                    socket.getOutputStream().use { outputStream ->
                        outputStream.write(idCode.toByteArray())
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }
    private fun receiveIdCode() {
        Thread {
            try {
                ServerSocket(8888).use { serverSocket ->
                    val client = serverSocket.accept()
                    client.getInputStream().use { inputStream ->
                        val buffer = ByteArray(1024)
                        val bytes = inputStream.read(buffer)
                        val receivedIdCode = String(buffer, 0, bytes)
                        // Process received ID code
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

}

