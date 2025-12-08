package com.example.mycalculator

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class SocketsActivity : AppCompatActivity() {
    private var log_tag: String = "MY_LOG_TAG"

    private lateinit var tvSockets: TextView
    private lateinit var btnSendData: Button
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sockets)

        tvSockets = findViewById(R.id.tvSockets)
        btnSendData = findViewById(R.id.btnSendData)
        handler = Handler(Looper.getMainLooper())

        btnSendData.setOnClickListener {
            logToUI("Запуск клиента: отправка сообщений на сервер...")
            startClient()
        }

        logToUI("Сервер и клиент готовы. Нажмите кнопку для отправки данных.")
    }

    private fun logToUI(message: String) {
        handler.post {
            tvSockets.append("$message\n")
            val scrollAmount = tvSockets.layout?.getLineTop(tvSockets.lineCount) ?: 0 - tvSockets.height
            if (scrollAmount > 0) tvSockets.scrollTo(0, scrollAmount)
        }
    }

    // Сервер
//    fun startServer() {
//        val context = ZMQ.context(1)
//        val socket = ZContext().createSocket(SocketType.REP)
//        socket.bind("tcp://172.20.10.10:5555")
//        var counter: Int = 0
//
//        logToUI("Сервер запущен на tcp://*:2222 (в фоне)")
//
//        while (true) {
//            try {
//                counter++
//                val requestBytes = socket.recv(0)
//                val request = String(requestBytes, ZMQ.CHARSET)
//                println("[SERVER] Received request: [$request]")
//
//                Thread.sleep(1000)
//
//                logToUI("Получено от клиента #$counter: $request")
//
//                val response = "Hello from Android ZMQ Server!"
//                socket.send(response.toByteArray(ZMQ.CHARSET), 0)
//                println("[SERVER] Sent reply: [$response]")
//            } catch (e: Exception) {
//                logToUI("Ошибка сервера: ${e.message}")
//                break
//            }
//        }
//        socket.close()
//        context.close()
//        logToUI("Сервер остановлен")
//    }

    // Клиент
    fun startClient() {
        Thread {
            val context = ZMQ.context(1)
            val socket = ZContext().createSocket(SocketType.REQ)
            socket.connect("tcp://172.20.10.10:5555")

            val request = "Hello from Android!"
            try {
                for (i in 1..10) {
                    socket.send(request.toByteArray(ZMQ.CHARSET), 0)
                    logToUI("Отправлено #$i: $request")

                    val reply = socket.recv(0)
                    val replyStr = String(reply, ZMQ.CHARSET)
                    logToUI("Ответ от сервера: $replyStr")

                    Thread.sleep(800)
                }
                logToUI("Передача завершена")
            } catch (e: Exception) {
                logToUI("Ошибка: ${e.message}")
            } finally {
                socket.close()
                context.close()
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
//        val runnableServer = Runnable { startServer() }
//        val threadServer = Thread(runnableServer)
//        threadServer.start()

        handler.postDelayed({
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}