
package com.abaddon.client
import android.app.AlertDialog
import android.app.Activity
import android.os.Bundle
import android.os.BatteryManager
import android.content.Context
import android.widget.*
import android.graphics.Color
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private val serverUrl = "http://127.0.0.1:8080"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(35, 60, 35, 30)
            setBackgroundColor(Color.rgb(16,16,16))
        }

        fun label(text: String, size: Float, color: Int): TextView {
            return TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(color)
                setPadding(0, 15, 0, 15)
            }
        }

        layout.addView(
            label("ABADDON CLIENT", 27f, Color.RED)
        )

        layout.addView(
            label(
                "Android Device Administration",
                16f,
                Color.WHITE
            )
        )

        layout.addView(
            label(
                "This application sends basic device status " +
                "to your configured server after your approval.",
                14f,
                Color.LTGRAY
            )
        )

        val button = Button(this).apply {
            text = "CONNECT TO SERVER"
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Device connection")
                    .setMessage(
                        "Do you approve sending basic device information?"
                    )
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Approve") { _, _ ->
                        sendDeviceInfo()
                    }
                    .show()
            }
        }

        layout.addView(button)
        setContentView(layout)
    }

    private fun sendDeviceInfo() {
        thread {
            try {
                val batteryManager =
                    getSystemService(Context.BATTERY_SERVICE)
                        as BatteryManager

                val battery = batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )

                val data = JSONObject().apply {
                    put("device_id", "my-android-device")
                    put("model", android.os.Build.MODEL)
                    put("android", android.os.Build.VERSION.RELEASE)
                    put("battery", battery.coerceIn(0, 100))
                    put("network", "Connected")
                }

                val connection = URL(serverUrl)
                    .openConnection() as HttpsURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.outputStream.use {
                    it.write(data.toString().toByteArray())
                }

                val result = connection.responseCode

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Server response: $result",
                        Toast.LENGTH_LONG
                    ).show()
                }

                connection.disconnect()

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Connection failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
