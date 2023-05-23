package com.naeem.merasangeet.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naeem.merasangeet.R
import com.naeem.merasangeet.adapter.*
import com.naeem.merasangeet.databinding.ActivityPlayerBinding
import com.naeem.merasangeet.fragment.FavouritesFragment
import com.naeem.merasangeet.fragment.HomeFragment
import com.naeem.merasangeet.fragment.HomeFragment.Companion.musicListSearch
import com.naeem.merasangeet.fragment.PlaylistsFragment
import com.naeem.merasangeet.services.MusicService

class PlayerActivity : AppCompatActivity(),ServiceConnection,MediaPlayer.OnCompletionListener{
    companion object{
        lateinit var musicListPA:ArrayList<Music>
        var songPosition:Int = 0
        var isPlaying:Boolean=false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat:Boolean = false
        var min15:Boolean = false
        var min30:Boolean = false
        var min60:Boolean = false
        var nowPlayingId:String = ""
        var isFavourite:Boolean = false
        var fIndex:Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.data?.scheme.contentEquals("content")){
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            Glide.with(this)
                .load(getImgArt(musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
                .into(binding.imgSong)
            binding.tvSongName.text = musicListPA[songPosition].title

        }else {
            //for
            songPosition = intent.getIntExtra("index", 0)
            when (intent.getStringExtra("class")) {
                "PlaylistDetailsShuffle" -> {
                    startingService()
                    musicListPA = ArrayList()
                    musicListPA.addAll(PlaylistsFragment.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                    musicListPA.shuffle()
                    setLayout()
                }
                "PlaylistDetailsAdapter" -> {
                    startingService()
                    musicListPA = ArrayList()
                    musicListPA.addAll(PlaylistsFragment.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                    setLayout()
                }
                "FavouriteShuffle" -> {
                    startingService()
                    musicListPA = ArrayList()
                    musicListPA.addAll(FavouritesFragment.favouriteSongs)
                    musicListPA.shuffle()
                    setLayout()
                }
                "FavouritesAdapter" -> {
                    startingService()
                    musicListPA = ArrayList()
                    musicListPA.addAll(FavouritesFragment.favouriteSongs)
                    setLayout()
                }
                "MusicAdapterSearch" -> {
                    startingService()
                    musicListPA = ArrayList()
                    musicListPA.addAll(musicListSearch)
                    setLayout()
                }
                "NowPlaying" -> {
                    binding.tvStartTime.text =
                        formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                    binding.tvEndTime.text =
                        formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                    binding.seekbar.progress = musicService!!.mediaPlayer!!.currentPosition
                    binding.seekbar.max = musicService!!.mediaPlayer!!.duration
                    if (isPlaying) {
                        binding.fbtnPausePlay.setIconResource(R.drawable.pause)
                    } else
                        binding.fbtnPausePlay.setIconResource(R.drawable.play)
                    setLayout()
                }
                "MusicAdapter" -> {
                    startingService()
                    musicListPA = ArrayList()
                    musicListPA.addAll(HomeFragment.musicListMA)
                    setLayout()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.fbtnPausePlay.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }
        binding.fbtnPre.setOnClickListener {
            preNextSong(
                increment = false
            )
        }
        binding.fbtnNext.setOnClickListener {
            preNextSong(increment = true)
        }
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit

        })
        binding.btnRepeat.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            } else {
                repeat = false
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            }
        }
        binding.btnEqualizer.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId
                )
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            } catch (e: Exception) {
                Toast.makeText(this, "Equalizer Feature not supported!!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnTimer.setOnClickListener {
            val timer = min15 || min30 || min60
            if (!timer) {
                showBottomSheetDialog()
            } else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop timer?")
                    .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.btnTimer.setColorFilter(ContextCompat.getColor(
                            this,
                            R.color.cool_pink
                        ))
                    }
                    .setNegativeButton("No") { dialog: DialogInterface, i: Int -> dialog.dismiss() }
                val customDialog = builder.create()
                customDialog.show()
            }
        }
        binding.btnShare.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent,"Sharing Music File!"))
        }
        binding.btnFav.setOnClickListener {
            if (isFavourite){
                isFavourite = false
                binding.btnFav.setImageResource(R.drawable.favorite_empty)
                FavouritesFragment.favouriteSongs.removeAt(fIndex)
            }else{
                isFavourite = true
                binding.btnFav.setImageResource(R.drawable.favorite_filled)
                FavouritesFragment.favouriteSongs.add(musicListPA[songPosition])
            }
        }
    }

    private fun getMusicDetails(contentUri: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri,projection,null,null,null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }!!
            return Music(id = "Unknown", title = path.toString(), album = "Unknown", artist = "Unknown",
                duration, path = path.toString(), artUri = "Unknown")
        }finally {
            cursor?.close()
        }
    }

    private fun setLayout() {
            fIndex = favouriteChecker(musicListPA[songPosition].id)
            Glide.with(this)
                .load(musicListPA[songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
                .into(binding.imgSong)
            binding.tvSongName.text = musicListPA[songPosition].title
            if (repeat) binding.btnRepeat.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.purple_500
                )
            )
            if (min15 || min30 || min60) binding.btnTimer.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.purple_500
                )
            )
            if (isFavourite) binding.btnFav.setImageResource(R.drawable.favorite_filled)
            else binding.btnFav.setImageResource(R.drawable.favorite_empty)
        }

        private fun createMediaPlayer() {
            try {
                if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
                musicService!!.mediaPlayer!!.reset()
                musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
                musicService!!.mediaPlayer!!.prepare()
                musicService!!.mediaPlayer!!.start()
                isPlaying = true
                binding.fbtnPausePlay.setIconResource(R.drawable.pause)
                musicService!!.showNotification(R.drawable.pause,0F)
                binding.tvStartTime.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvEndTime.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekbar.progress = 0
                binding.seekbar.max = musicService!!.mediaPlayer!!.duration
                musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                nowPlayingId = musicListPA[songPosition].id
            } catch (e: Exception) {
                return
            }
        }

        private fun playMusic() {
            try {
                isPlaying = true
                musicService!!.mediaPlayer!!.start()
                binding.fbtnPausePlay.setIconResource(R.drawable.pause)
                musicService!!.showNotification(R.drawable.pause,1F)
            } catch (e: Exception) {
                return
            }
        }

        private fun pauseMusic() {
            try {
                isPlaying = false
                musicService!!.mediaPlayer!!.pause()
                binding.fbtnPausePlay.setIconResource(R.drawable.play)
                musicService!!.showNotification(R.drawable.play,0F)
            } catch (e: Exception) {
                return
            }
        }

        private fun preNextSong(increment: Boolean) {
            if (increment) {
                setSongPosition(increment = true)
                setLayout()
                createMediaPlayer()
            } else {
                setSongPosition(increment = false)
                setLayout()
                createMediaPlayer()
            }
        }


        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as MusicService.MyBinder
                musicService = binder.currentService()
                createMediaPlayer()
                musicService!!.seekBarSetup()
                musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                musicService!!.audioManager.requestAudioFocus(musicService,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)

            } catch (e: Exception) {
                return
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            musicService = null
        }

        override fun onCompletion(mp: MediaPlayer?) {
            setSongPosition(increment = true)
            createMediaPlayer()
            try {
                setLayout()
            } catch (e: Exception) {
                return
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 13 || resultCode == RESULT_OK)
                return
        }

        private fun showBottomSheetDialog() {
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(R.layout.bottom_sheet_dialog)
            dialog.show()
            dialog.findViewById<LinearLayout>(R.id.llMin_15)?.setOnClickListener {
                Toast.makeText(baseContext, "Music will stop after 15 minutes", Toast.LENGTH_SHORT)
                    .show()
                binding.btnTimer.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                min15 = true
                Thread {
                    Thread.sleep(15 * 60000)
                    if (min15) exitApplication()
                }.start()
                dialog.dismiss()
            }
            dialog.findViewById<LinearLayout>(R.id.llMin_30)?.setOnClickListener {
                Toast.makeText(baseContext, "Music will stop after 30 minutes", Toast.LENGTH_SHORT)
                    .show()
                binding.btnTimer.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                min30 = true
                Thread {
                    Thread.sleep(30 * 60000)
                    if (min30) exitApplication()
                }.start()
                dialog.dismiss()
            }
            dialog.findViewById<LinearLayout>(R.id.llMin_60)?.setOnClickListener {
                Toast.makeText(baseContext, "Music will stop after 60 minutes", Toast.LENGTH_SHORT)
                    .show()
                binding.btnTimer.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                min60 = true
                Thread {
                    Thread.sleep(60 * 60000)
                    if (min60) exitApplication()
                }.start()
                dialog.dismiss()
            }
        }
    private fun startingService(){
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPA[songPosition].id == "Unknown" && !isPlaying) exitApplication()
    }
}



