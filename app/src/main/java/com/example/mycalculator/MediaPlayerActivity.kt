package com.example.mycalculator
import android.Manifest
import android.content.Context
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
    private var musicFiles: MutableList<File> = mutableListOf()
    private var currentTrackIndex = 0
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadMusicFiles()
        } else {
            Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_LONG).show()
        }
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
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacksAndMessages(null)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (mediaPlayer != null && seekBar != null) {
                    mediaPlayer!!.seekTo(seekBar.progress)
                    if (isPlaying) {
                        mediaPlayer!!.start()
                        updateSeekBar()
                    }
                }
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
        musicFiles.clear()
        val myMusicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "MyMusic")

        if (!myMusicDir.exists()) {
            myMusicDir.mkdirs()
        }

        if (myMusicDir.exists() && myMusicDir.isDirectory) {
            val files = myMusicDir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile && file.name.endsWith(".mp3", ignoreCase = true)) {
                        musicFiles.add(file)
                    }
                }
            }
        }

        if (musicFiles.isEmpty()) {
            Toast.makeText(this, "MP3 не найдены в Music/MyMusic", Toast.LENGTH_LONG).show()
            tvCurrentTrack.text = "Нет треков"
        } else {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, musicFiles.map { it.name })
            lvMusicList.adapter = adapter
            playTrack(0)
        }
    }

    private fun playTrack(position: Int) {
        if (position < 0 || position >= musicFiles.size) return

        currentTrackIndex = position
        tvCurrentTrack.text = musicFiles[position].name
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(musicFiles[position].absolutePath)
        mediaPlayer!!.prepare()
        mediaPlayer!!.start()

        isPlaying = true
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)

        seekBarProgress.max = mediaPlayer!!.duration
        seekBarProgress.progress = 0

        updateSeekBar()

        mediaPlayer!!.setOnCompletionListener {
            playNext()
        }
    }

    private fun togglePlayPause() {
        if (mediaPlayer == null) return

        if (isPlaying) {
            mediaPlayer!!.pause()
            isPlaying = false
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
        } else {
            mediaPlayer!!.start()
            isPlaying = true
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            updateSeekBar()
        }
    }

    private fun stopTrack() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.prepareAsync()
            seekBarProgress.progress = 0
            isPlaying = false
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            tvCurrentTrack.text = "Остановлено"
        }
    }

    private fun playNext() {
        if (musicFiles.isEmpty()) return
        currentTrackIndex = (currentTrackIndex + 1) % musicFiles.size
        playTrack(currentTrackIndex)
    }

    private fun playPrevious() {
        if (musicFiles.isEmpty()) return
        currentTrackIndex = if (currentTrackIndex - 1 < 0) musicFiles.size - 1 else currentTrackIndex - 1
        playTrack(currentTrackIndex)
    }

    private fun updateSeekBar() {
        handler.removeCallbacksAndMessages(null)
        if (mediaPlayer != null && isPlaying) {
            val current = mediaPlayer!!.currentPosition
            seekBarProgress.progress = current
            handler.postDelayed({ updateSeekBar() }, 1000)
        }
    }

    private fun setupVolumeControl() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        seekBarVolume.max = maxVolume
        seekBarVolume.progress = currentVolume

        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        handler.removeCallbacksAndMessages(null)
    }
}
