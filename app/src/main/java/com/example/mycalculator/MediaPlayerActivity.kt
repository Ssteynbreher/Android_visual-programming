package com.example.mycalculator

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
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
            playTrack(position)
        }

        btnPlayPause.setOnClickListener { togglePlayPause() }
        btnNext.setOnClickListener { playNext() }
        btnPrev.setOnClickListener { playPrevious() }
        btnStop.setOnClickListener { stopTrack() }

        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer?.seekTo(seekBar!!.progress)
            }
        })
    }

    private fun requestStoragePermission() {
        val permission = Manifest.permission.READ_MEDIA_AUDIO
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadMusicFiles()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun loadMusicFiles() {
        val musicList = mutableListOf<File>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        contentResolver.query(collection, projection, selection, null, null)?.use { cursor ->
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
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, musicFiles.map { it.name })
            lvMusicList.adapter = adapter
        }
    }

    private fun playTrack(position: Int) {
        currentTrackIndex = position
        tvCurrentTrack.text = musicFiles[position].name

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(musicFiles[position].absolutePath)
            prepare()
            start()
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            updateSeekBar()
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
            it.stop()
            seekBarProgress.progress = 0
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            tvCurrentTrack.text = "Выберите трек"
        }
    }

    private fun playNext() {
        if (musicFiles.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % musicFiles.size
            playTrack(currentTrackIndex)
        }
    }

    private fun playPrevious() {
        if (musicFiles.isNotEmpty()) {
            currentTrackIndex = if (currentTrackIndex - 1 < 0) musicFiles.size - 1 else currentTrackIndex - 1
            playTrack(currentTrackIndex)
        }
    }

    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    seekBarProgress.max = it.duration
                    seekBarProgress.progress = it.currentPosition
                    if (it.isPlaying) handler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    private fun setupVolumeControl() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        seekBarVolume.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        seekBarVolume.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        handler.removeCallbacksAndMessages(null)
    }
}