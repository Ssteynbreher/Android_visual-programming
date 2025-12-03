package com.example.mycalculator

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class SocketsActivity : AppCompatActivity() {
    private val TAG = "AndroidZMQ"
    private lateinit var tvStatus: TextView
    private lateinit var tvMessages: TextView
    private lateinit var btnStartClient: Button
    private lateinit var btnStartServer: Button
    private lateinit var btnConnectToPC: Button

    private var isClientRunning = false
    private var isServerRunning = false
    private var isConnectedToPC = false

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sockets)

        handler = Handler(Looper.getMainLooper())

        tvStatus = findViewById(R.id.tvStatus)
        tvMessages = findViewById(R.id.tvMessages)
        btnStartClient = findViewById(R.id.btnStartClient)
        btnStartServer = findViewById(R.id.btnStartServer)
        btnConnectToPC = findViewById(R.id.btnConnectToPC)

        btnStartClient.setOnClickListener {
            if (!isClientRunning) {
                startAndroidClient()
            } else {
                stopAndroidClient()
            }
        }

        btnStartServer.setOnClickListener {
            if (!isServerRunning) {
                startAndroidServer()
            } else {
                stopAndroidServer()
            }
        }

        btnConnectToPC.setOnClickListener {
            if (!isConnectedToPC) {
                connectToPCServer()
            } else {
                disconnectFromPC()
            }
        }

        updateUI()
    }

    private fun startAndroidServer() {
        Thread {
            isServerRunning = true
            updateUI()

            val context = ZMQ.context(1)
            val socket = ZContext().createSocket(SocketType.REP)
            socket.bind("tcp://*:2222")

            handler.post {
                appendMessage("Android сервер запущен на порту 2222")
            }

            var counter = 0
            while (isServerRunning) {
                try {
                    val request = socket.recvStr(0)
                    counter++

                    handler.post {
                        appendMessage("Получено от клиента: $request")
                        tvStatus.text = "Сервер активен. Пакетов: $counter"
                    }

                    val response = "Привет от Android сервера! (#$counter)"
                    socket.send(response, 0)

                    handler.post {
                        appendMessage("Отправлено клиенту: $response")
                    }

                    Thread.sleep(500)

                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка сервера: ${e.message}")
                }
            }

            socket.close()
            context.close()

            handler.post {
                appendMessage("Android сервер остановлен")
            }
        }.start()
    }

    private fun stopAndroidServer() {
        isServerRunning = false
        updateUI()
    }

    private fun startAndroidClient() {
        Thread {
            isClientRunning = true
            updateUI()

            val context = ZMQ.context(1)
            val socket = ZContext().createSocket(SocketType.REQ)
            socket.connect("tcp://localhost:2222")

            handler.post {
                appendMessage("Android клиент запущен")
            }

            var counter = 0
            while (isClientRunning && counter < 10) {
                try {
                    val request = "Привет от Android клиента! (#${counter + 1})"
                    socket.send(request, 0)

                    handler.post {
                        appendMessage("Отправлено серверу: $request")
                    }

                    val reply = socket.recvStr(0)
                    counter++

                    handler.post {
                        appendMessage("Получено от сервера: $reply")
                        tvStatus.text = "Клиент активен. Отправлено: $counter"
                    }

                    Thread.sleep(1000)

                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка клиента: ${e.message}")
                }
            }

            socket.close()
            context.close()
            isClientRunning = false

            handler.post {
                updateUI()
                appendMessage("Клиент завершил работу (отправлено $counter пакетов)")
            }
        }.start()
    }

    private fun stopAndroidClient() {
        isClientRunning = false
        updateUI()
    }

    private fun connectToPCServer() {
        Thread {
            isConnectedToPC = true
            updateUI()

            val serverIP = "172.29.160.1"
            val port = "5555"

            val context = ZMQ.context(1)
            val socket = ZContext().createSocket(SocketType.REQ)
            socket.connect("tcp://$serverIP:$port")

            handler.post {
                appendMessage("Подключение к серверу ПК по адресу $serverIP:$port...")
            }

            var counter = 0
            while (isConnectedToPC && counter < 20) {
                try {
                    val request = "Привет от Android! (#${counter + 1})"
                    socket.send(request, 0)

                    handler.post {
                        appendMessage("Отправлено на ПК: $request")
                        tvStatus.text = "Подключен к ПК. Пакетов: ${counter + 1}"
                    }

                    val reply = socket.recvStr(0)
                    counter++

                    handler.post {
                        appendMessage("Получено от ПК: $reply")
                    }

                    Thread.sleep(2000)

                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка подключения к ПК: ${e.message}")
                    handler.post {
                        appendMessage("Ошибка подключения: ${e.message}")
                        appendMessage("Проверьте: 1) Сервер запущен 2) Правильный IP 3) Одна сеть")
                    }
                    break
                }
            }

            socket.close()
            context.close()
            isConnectedToPC = false

            handler.post {
                updateUI()
                appendMessage("Завершена отправка $counter пакетов на ПК")
            }
        }.start()
    }

    private fun disconnectFromPC() {
        isConnectedToPC = false
        updateUI()
    }

    private fun appendMessage(message: String) {
        handler.post {
            val currentText = tvMessages.text.toString()
            tvMessages.text = "$currentText\n$message"
        }
    }

    private fun updateUI() {
        handler.post {
            btnStartClient.text = if (isClientRunning) "Остановить Android клиент" else "Запустить Android клиент"
            btnStartServer.text = if (isServerRunning) "Остановить Android сервер" else "Запустить Android сервер"
            btnConnectToPC.text = if (isConnectedToPC) "Отключиться от ПК" else "Подключиться к серверу ПК"

            val status = buildString {
                append("Статус: ")
                when {
                    isConnectedToPC -> append("Подключен к ПК")
                    isClientRunning -> append("Android клиент работает")
                    isServerRunning -> append("Android сервер работает")
                    else -> append("Ожидание")
                }
            }
            tvStatus.text = status
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isClientRunning = false
        isServerRunning = false
        isConnectedToPC = false
    }
}