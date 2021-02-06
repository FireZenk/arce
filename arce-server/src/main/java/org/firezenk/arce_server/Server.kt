package org.firezenk.arce_server

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.sessions.CurrentSession
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessionId
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.util.generateNonce
import io.ktor.websocket.*
import io.ktor.websocket.webSocket
import kotlin.random.Random

data class ChatSession(val id: String, var nick: String? = "Guest${Random.nextInt()}")

fun main(args: Array<String>) {
    val sessionList = mutableMapOf<ChatSession, WebSocketServerSession>()

    val server = embeddedServer(CIO, port = 8080) {
        install(WebSockets)

        install(Sessions) {
            cookie<ChatSession>("SESSION")
        }

        intercept(ApplicationCallPipeline.Features) {
            if (call.sessions.get<ChatSession>() == null) {
                call.sessions.set(ChatSession(generateNonce()))
            }
        }

        routing {
            static("/static") {
                resources("assets")
                default("404.html")
            }

            webSocket("/ws") {

                val session = call.sessions.get<ChatSession>()!!
                if (!sessionList.containsValue(session)) {
                    sessionList[session] = this
                }

                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val command = frame.readText()
                            when {
                                command == "help" -> {
                                    outgoing.send(Frame.Text(
                                        """
                                            Available commands are:<br/>
                                            <br/>
                                            <div class="tab">who [text]     &#9;Defines your nickname</div><br/>
                                            <div class="tab">toast [text]   &#9;Send a special message</div><br/>
                                            <div class="tab">help           &#9;You already know this one</div>
                                            <div class="tab">[text]         &#9;Send the text as chat message</div>
                                        """
                                    ))
                                }
                                command.startsWith("who ") -> {
                                    val newNick = command.substring(command.indexOf("who ") + 4, command.length)
                                    session.nick = newNick
                                    outgoing.send(Frame.Text("WELCOME to ARCE! You're connected as: $newNick"))
                                }
                                command.equals("bye", ignoreCase = true) -> {
                                    close(CloseReason(CloseReason.Codes.NORMAL, "${session.nick} said BYE"))
                                }
                                else -> sessionList.forEach {
                                    if (command.startsWith("toast ")) { // Example command
                                        it.value.outgoing.send(Frame.Text( "${session.nick}: $command"))
                                    } else {
                                        it.value.outgoing.send(Frame.Text("${session.nick}: $command"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    server.start(wait = true)
}