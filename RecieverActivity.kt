package com.example.nearbyshare

import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.cardemulation.HostApduService
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReaderActivity : AppCompatActivity() {
    private lateinit var nfcAdapter: NfcAdapter
    private val AID = byteArrayOf(0xF0.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06)

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        } catch (e: Exception) {
            Log.e("ReaderActivity", "Error in onCreate", e)
            Toast.makeText(this, "An error occurred while starting the activity", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::nfcAdapter.isInitialized) {
            enableReaderMode()
        } else {
            Toast.makeText(this, "NFC adapter not initialized", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onPause() {
        super.onPause()
        disableReaderMode()
    }

    private fun enableReaderMode() {
        val readerCallback = object : NfcAdapter.ReaderCallback {
            override fun onTagDiscovered(tag: Tag) {
                val isoDep = IsoDep.get(tag)
                isoDep.connect()

                val selectApdu = buildSelectApdu(AID)
                val response = isoDep.transceive(selectApdu)

                if (response.isNotEmpty() && response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))) {
                    val dataResponse = isoDep.transceive(byteArrayOf(0x00.toByte()))
                    val receivedString = String(dataResponse.dropLast(2).toByteArray())

                    runOnUiThread {
                        Toast.makeText(this@ReaderActivity, "Received: $receivedString", Toast.LENGTH_LONG).show()
                    }
                }

                isoDep.close()
            }
        }

        nfcAdapter.enableReaderMode(this, readerCallback,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null)
    }

    private fun disableReaderMode() {
        nfcAdapter.disableReaderMode(this)
    }

    private fun buildSelectApdu(aid: ByteArray): ByteArray {
        return byteArrayOf(0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte()) + byteArrayOf(aid.size.toByte()) + aid
    }
}
class MyHostApduService : HostApduService() {

    private val TAG = "MyHostApduService"

    // AID for this service
    private val AID = byteArrayOf(0xF0.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06)

    // Success response status
    private val STATUS_SUCCESS = byteArrayOf(0x90.toByte(), 0x00)

    // Error response status
    private val STATUS_FAILED = byteArrayOf(0x6F.toByte(), 0x00)

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            Log.e(TAG, "Received null APDU")
            return STATUS_FAILED
        }

        Log.d(TAG, "Received APDU: ${commandApdu.toHex()}")

        if (commandApdu.size >= 5 && commandApdu[0] == 0x00.toByte() && commandApdu[1] == 0xA4.toByte()) {
            // This is a SELECT AID command
            if (commandApdu.size >= (5 + AID.size) && commandApdu.sliceArray(5 until 5 + AID.size).contentEquals(AID)) {
                Log.d(TAG, "AID selected successfully")
                return STATUS_SUCCESS
            }
        }

        // Process other APDU commands here
        // For demonstration, we'll just echo back the received APDU
        return commandApdu + STATUS_SUCCESS
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: " + when(reason) {
            DEACTIVATION_LINK_LOSS -> "Link Loss"
            DEACTIVATION_DESELECTED -> "Deselected"
            else -> "Unknown reason"
        })
    }

    private fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02X".format(eachByte) }
}
class NfcDetector : AppCompatActivity() {
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var nfcStatusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcStatusTextView = findViewById(R.id.nfcStatusTextView)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        updateNfcStatus()
    }

    private fun updateNfcStatus() {
        if (nfcAdapter == null) {
            nfcStatusTextView.text = getString(R.string.nfc_status_not_supported)
        } else {
            if (nfcAdapter.isEnabled) {
                nfcStatusTextView.text = getString(R.string.nfc_status_active)
                nfcStatusTextView.setTextColor(Color.GREEN)
            } else {
                nfcStatusTextView.text = getString(R.string.nfc_status_inactive)
                nfcStatusTextView.setTextColor(Color.RED)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        updateNfcStatus()
    }
}

