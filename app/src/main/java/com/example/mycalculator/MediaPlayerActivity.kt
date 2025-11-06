package com.example.mycalculator

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class MediaPlayerActivity : AppCompatActivity() {

    private val logTag = "MEDIA_PLAYER"
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
    private var isUserSeeking = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) loadMusicFiles() else Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)
        initViews()
        setupVolumeControl()
        requestStoragePermission()
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

        lvMusicList.setOnItemClickListener { _, _, position, _ ->
            playTrack(musicFiles[position], position)
        }

        btnPlayPause.setOnClickListener { togglePlayPause() }
        btnNext.setOnClickListener { playNext() }
        btnPrev.setOnClickListener { playPrevious() }
        btnStop.setOnClickListener { stopTrack() }

        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { isUserSeeking = true }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                mediaPlayer?.seekTo(seekBar!!.progress)
            }
        })
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadMusicFiles()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun loadMusicFiles() {
        val musicList = mutableListOf<File>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        try {
            contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathColumn)
                    val file = File(path)
                    if (file.exists() && file.name.endsWith(".mp3", ignoreCase = true)) {
                        musicList.add(file)
                    }
                }
            }
            musicFiles = musicList
            if (musicFiles.isEmpty()) {
                Toast.makeText(this, "MP3 не найдены", Toast.LENGTH_SHORT).show()
            } else {
                setupListView()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListView() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, musicFiles.map { it.name })
        lvMusicList.adapter = adapter
    }

    private fun playTrack(file: File, position: Int) {
        currentTrackIndex = position
        tvCurrentTrack.text = file.name

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()
                // Переключаем на паузу (системная иконка)
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                updateSeekBar()
            } catch (e: IOException) {
                Toast.makeText(this@MediaPlayerActivity, "Ошибка воспроизведения", Toast.LENGTH_SHORT).show()
            }
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
                updateSeekBar()
            }
        }
    }

    private fun stopTrack() {
        mediaPlayer?.let {
            it.pause()
            it.seekTo(0)
            seekBarProgress.progress = 0
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            tvCurrentTrack.text = "Выберите трек"
            handler.removeCallbacksAndMessages(null)
        }
    }

    private fun playNext() {
        if (musicFiles.isEmpty()) return
        val next = (currentTrackIndex + 1) % musicFiles.size
        playTrack(musicFiles[next], next)
    }

    private fun playPrevious() {
        if (musicFiles.isEmpty()) return
        val prev = if (currentTrackIndex - 1 < 0) musicFiles.size - 1 else currentTrackIndex - 1
        playTrack(musicFiles[prev], prev)
    }

    private fun updateSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (!isUserSeeking) {
                        val current = it.currentPosition
                        seekBarProgress.max = it.duration
                        seekBarProgress.progress = current
                    }
                    if (it.isPlaying) {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        })
    }

    private fun setupVolumeControl() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        seekBarVolume.max = max
        seekBarVolume.progress = current
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}