package com.prashant.socket

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prashant.socket.databinding.ActivityMainBinding
import com.prashant.socket.sockethelper.SocketHelper
import com.prashant.socket.sockethelper.SocketHelper.Companion.getSocketInstance


const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SocketHelper.GlobalListeners {

    private val _binding: ActivityMainBinding? = null
    private var binding = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val listener=getSocketInstance()
        listener.emit("","")
    }

    override fun onSyncData(syncData: Any) {
        TODO("Not yet implemented")
    }

    override fun onNewBooking(booking: Any) {
        TODO("Not yet implemented")
    }

    override fun onBookingStatusChange(booking: Any) {
        TODO("Not yet implemented")
    }

    override fun onBookingCancelAlert(booking: Any) {
        TODO("Not yet implemented")
    }

    override fun onSessionEnd() {
        TODO("Not yet implemented")
    }
}