package org.firezenk.arce_client

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocket(private val url: String) : WebSocketListener() {

    private lateinit var broadcaster: FlowableEmitter<String>
    private lateinit var disposable: Disposable
    private lateinit var webSocket: WebSocket
    private val flowable = Flowable.create(FlowableOnSubscribe<String> {
            emitter -> broadcaster = emitter }, BackpressureStrategy.BUFFER)

    fun listen(): Flowable<String> {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, this)
        client.dispatcher.executorService.shutdown()

        disposable = flowable.subscribe()

        return flowable
    }

    fun send(message: String) = webSocket.send(message)

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        broadcaster.onNext("onOpen")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        broadcaster.onNext("Connection closed")
        disposable.dispose()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        broadcaster.onNext(text)
    }

    override fun onFailure(webSocket: WebSocket, throwable: Throwable, response: Response?) {
        super.onFailure(webSocket, throwable, response)
        broadcaster.onError(throwable)
    }
}
