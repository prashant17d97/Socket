package com.prashant.socket.sockethelper

import android.util.Log
import com.prashant.socket.sockethelper.SocketKeys.LOGOUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.prashant.socket.sockethelper.SocketKeys.NEW_BOOKING_REQUEST
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class SocketHelper {
    // For Singleton instantiation
    companion object {
        @Volatile
        private var instance: SocketHelper? = null
        fun getSocketInstance(): SocketHelper {
            return instance ?: synchronized(this) {
                instance ?: buildSocket().also { instance = it }
            }
        }

        private fun buildSocket(): SocketHelper {
            return SocketHelper()
        }

    }

    @Volatile
    var socket: Socket? = null

    @Volatile
    var globalListeners: GlobalListeners? = null

    @Synchronized
    fun initSocket() {
        if (socket == null) {
            val options = IO.Options()
            options.reconnection = true
            options.reconnectionAttempts = Int.MAX_VALUE
            options.reconnectionDelay = 1000
            options.forceNew = true
//            options.query = "token=${AppController.auth}"
//            if(!BuildConfig.DEBUG)
//            options.path= "/v2/socket.io"
            socket = IO.socket("http://chat.socket.io", options)
            socketOn()
        }
        socket?.let {
            if (!it.connected())
                it.connect()
        }
    }

    @Synchronized
    private fun socketOn() {
        listener(Socket.EVENT_CONNECT, onConnect)
        listener(Socket.EVENT_DISCONNECT, onDisconnect)
        listener(Socket.EVENT_CONNECT_ERROR, onConnectError)
    }

    @Synchronized
    fun disconnectSocket() {
        socket?.disconnect()
        socket = null
    }

    fun listener(key: String, emitter: Emitter.Listener) {
        initSocket()
        if (socket?.hasListeners(key) != true) {
            socket?.on(key, emitter)
        }
    }

    fun <T> emit(key: String, data: T) {
        if (!isConnected) {
            initSocket()
            return
        }
        Log.e("SocketHelper", "emit: $key = $data")
        socket?.emit(key, data)
    }

    private val isConnected: Boolean = socket?.connected() == true

    fun addListeners() {
        try {
            listener(NEW_BOOKING_REQUEST, newBooking)
            listener(LOGOUT, logout)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface GlobalListeners {
        fun onSyncData(syncData: Any)
        fun onNewBooking(booking: Any)
        fun onBookingStatusChange(booking: Any)
        fun onBookingCancelAlert(booking: Any)
        fun onSessionEnd()
    }

    private val onConnect = Emitter.Listener { it ->
        Log.e("SocketConnection", "======>>>>>>>>  Connected $it")
        initSocket()
    }
    private val onDisconnect = Emitter.Listener { it ->
        Log.e("SocketConnection", "======>>>>>>>>  Disconnected ${it[0]}")
        initSocket()
    }
    private val onConnectError = Emitter.Listener { it ->
        Log.e("onConnectError", "======>>>>>>>> onConnectError ${it[0]}")
        initSocket()
    }

    private var newBooking = Emitter.Listener { it ->
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.e("SocketHelper", "addListeners: $NEW_BOOKING_REQUEST = ${it[0]}")
                globalListeners?.onNewBooking(JSONObject(it[0].toString()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var logout = Emitter.Listener {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                globalListeners?.onSessionEnd()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}