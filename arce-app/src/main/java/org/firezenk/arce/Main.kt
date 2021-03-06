package org.firezenk.arce

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers.io
import org.firezenk.arce_client.WebSocket

class Main : AppCompatActivity(R.layout.activity_main) {

    companion object {
        private const val NICKNAME = "Android"
    }

    private val webSocket = WebSocket("http://192.168.0.12:8080/ws")
    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        disposable = webSocket.listen()
            .doOnSubscribe { webSocket.send("who $NICKNAME") }
            .subscribeOn(io())
            .observeOn(mainThread())
            .subscribe(
                { processCommand(it); print(it) },
                { it.printStackTrace() }
            )

        findViewById<Button>(R.id.sendButton).setOnClickListener {
            val text = findViewById<TextView>(R.id.textToSend).text
            webSocket.send(text.toString())
        }
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    private fun processCommand(string: String) {
        if (string.contains("toast")) {
            val stringStart = string.indexOf("toast ") + 6
            Toast.makeText(this, string.substring(stringStart, string.length),
                Toast.LENGTH_SHORT).show()
        } else if (!string.startsWith("onOpen")) {
            val output = findViewById<TextView>(R.id.output)
            output.text = "${output.text}\n$string"
        }
    }
}