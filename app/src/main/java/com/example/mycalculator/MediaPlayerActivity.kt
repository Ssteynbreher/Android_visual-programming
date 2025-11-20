package com.example.mycalculator

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var lvMusicList: ListView
    private lateinit var seekBarProgress: SeekBar
    private lateinit var seekBarVolume: SeekBar
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrev: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var tvCurrentTrack: TextView

    private var mediaPlayer: MediaPlayer? = null
    private var musicFiles: List<File> = emptyList()
    private var currentTrackIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateSeekBar: Runnable

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadMusicFromFolder()
        } else {
            Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_LONG).show()
            tvCurrentTrack.text = "Нет доступа к музыке"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        mediaPlayer = MediaPlayer()

        initViews()
        setupVolumeControl()
        requestStoragePermission() 
    }

    private fun requestStoragePermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadMusicFromFolder()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun initViews() {
        lvMusicList = findViewById(R.id.lvMusicList)
        seekBarProgress = findViewById(R.id.seekBarProgress)
        seekBarVolume = findViewById(R.id.seekBarVolume)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrev = findViewById(R.id.btnPrev)
        btnStop = findViewById(R.id.btnStop)
        tvCurrentTrack = findViewById(R.id.tvCurrentTrack)

        lvMusicList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            playMusic(position)
        }

        btnPlayPause.setOnClickListener { togglePlayPause() }
        btnNext.setOnClickListener { playNext() }
        btnPrev.setOnClickListener { playPrevious() }
        btnStop.setOnClickListener { stopMusic() }

        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateSeekBar)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer?.seekTo(seekBar!!.progress)
                if (mediaPlayer?.isPlaying == true) {
                    handler.postDelayed(updateSeekBar, 0)
                }
            }
        })

        updateSeekBar = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        seekBarProgress.progress = it.currentPosition
                        handler.postDelayed(this, 100)
                    }
                }
            }
        }
    }

    private fun loadMusicFromFolder() {
        val musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path
        val directory = File(musicPath)

        if (!directory.exists()) {
            directory.mkdirs()
            Toast.makeText(this, "Папка Music создана. Положите MP3 туда", Toast.LENGTH_LONG).show()
            tvCurrentTrack.text = "Папка пуста"
            return
        }

        musicFiles = directory.listFiles()
            ?.filter { it.isFile && it.extension in listOf("mp3", "wav", "aac") }
            ?: emptyList()

        if (musicFiles.isEmpty()) {
            Toast.makeText(this, "MP3 не найдены в /Music", Toast.LENGTH_LONG).show()
            tvCurrentTrack.text = "Нет треков"
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, musicFiles.map { it.name })
        lvMusicList.adapter = adapter
    }

    private fun playMusic(position: Int) {
        if (position !in musicFiles.indices) return

        currentTrackIndex = position
        tvCurrentTrack.text = musicFiles[position].name

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer!!.setDataSource(musicFiles[position].absolutePath)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()

            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            seekBarProgress.max = mediaPlayer!!.duration
            seekBarProgress.progress = 0

            handler.postDelayed(updateSeekBar, 0)
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            } else {
                it.start()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                handler.postDelayed(updateSeekBar, 0)
            }
        } ?: run {
            playMusic(currentTrackIndex)
        }
    }

    private fun stopMusic() {
        mediaPlayer?.let {
            it.stop()
            it.reset()
            seekBarProgress.progress = 0
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            tvCurrentTrack.text = "Остановлено"
            handler.removeCallbacks(updateSeekBar)
        }
    }

    private fun playNext() {
        if (musicFiles.isEmpty()) return
        currentTrackIndex = (currentTrackIndex + 1) % musicFiles.size
        playMusic(currentTrackIndex)
    }

    private fun playPrevious() {
        if (musicFiles.isEmpty()) return
        currentTrackIndex = if (currentTrackIndex - 1 < 0) musicFiles.size - 1 else currentTrackIndex - 1
        playMusic(currentTrackIndex)
    }

    private fun setupVolumeControl() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val progress = (currentVolume * 100) / maxVolume

        seekBarVolume.max = 100
        seekBarVolume.progress = progress

        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: 0
                val systemVolume = (progress * maxVolume) / 100
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, systemVolume, 0)
                val volume = progress / 100.0f
                mediaPlayer?.setVolume(volume, volume)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBar)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}