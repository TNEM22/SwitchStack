package com.tnem.switchsquad

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.Executors

class WebsocketListener(viewModel: MainViewModel) : WebSocketListener() {
    private val viewModel: MainViewModel = viewModel
    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        // Execute on the main thread if necessary (e.g., in Android)
        Executors.newSingleThreadExecutor().execute {
            viewModel.setStatus(true)
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        // Execute on the main thread if necessary
        Executors.newSingleThreadExecutor().execute {
            viewModel.setMessage(text)
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Executors.newSingleThreadExecutor().execute {
            viewModel.setMessage(reason)
            viewModel.setStatus(false)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        // Execute on the main thread if necessary
        Executors.newSingleThreadExecutor().execute {
            viewModel.setMessage(reason)
            viewModel.setStatus(false)
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        // Execute on the main thread if necessary
        Executors.newSingleThreadExecutor().execute {
            viewModel.setStatus(false)
        }
    }
}