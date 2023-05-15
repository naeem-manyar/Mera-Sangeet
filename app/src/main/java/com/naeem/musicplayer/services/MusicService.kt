package com.naeem.musicplayer.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.naeem.musicplayer.R
import com.naeem.musicplayer.activity.MainActivity
import com.naeem.musicplayer.activity.PlayerActivity
import com.naeem.musicplayer.adapter.formatDuration
import com.naeem.musicplayer.adapter.getImgArt
import com.naeem.musicplayer.fragment.NowPlayingFragment

class MusicService: Service(),AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    var mediaPlayer:MediaPlayer?=null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }
    inner class MyBinder: Binder(){
        fun currentService(): MusicService {
            return this@MusicService
        }
    }
 @SuppressLint("UnspecifiedImmutableFlag")
 fun showNotification(playPauseBtn:Int,playbackSpeed:Float){
        val intent = Intent(baseContext,MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this,0,intent,0)

        val prevIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if (imgArt != null){
            BitmapFactory.decodeByteArray(imgArt,0,imgArt.size)
        }else{
            BitmapFactory.decodeResource(resources,R.drawable.splash_screen)
        }
        val notification = NotificationCompat.Builder(baseContext,ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.ic_music)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_previous,"Previous",prevPendingIntent)
            .addAction(playPauseBtn,"Play",playPendingIntent)
            .addAction(R.drawable.ic_next,"Next",nextPendingIntent)
            .build()

     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
         mediaSession.setMetadata(MediaMetadataCompat.Builder()
             .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,mediaPlayer!!.duration.toLong())
             .build())
         mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
             .setState(PlaybackStateCompat.STATE_PLAYING,mediaPlayer!!.currentPosition.toLong(),playbackSpeed)
             .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
             .build())
     }
        startForeground(13,notification)
    }
    fun createMediaPlayer(){
        try {
            if (mediaPlayer == null) mediaPlayer = MediaPlayer()
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
           mediaPlayer!!.prepare()
            PlayerActivity.binding.fbtnPausePlay.setIconResource(R.drawable.pause)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause,0F)
            PlayerActivity.binding.tvStartTime.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvEndTime.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekbar.progress = 0
            PlayerActivity.binding.seekbar.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
        }catch (e:Exception){return}
    }
    fun seekBarSetup(){
        runnable = Runnable {
            PlayerActivity.binding.tvStartTime.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekbar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange >= 0){
            //pause music
            try {
                PlayerActivity.isPlaying = false
                mediaPlayer!!.pause()
                PlayerActivity.binding.fbtnPausePlay.setIconResource(R.drawable.play)
                NowPlayingFragment.binding.fbtnPlayPauseNP.setIconResource(R.drawable.play)
                showNotification(R.drawable.play,0F)
            } catch (e: Exception) {
                return
            }
        }else{
            //play music
            try {
                PlayerActivity.isPlaying = true
                mediaPlayer!!.start()
                PlayerActivity.binding.fbtnPausePlay.setIconResource(R.drawable.pause)
                NowPlayingFragment.binding.fbtnPlayPauseNP.setIconResource(R.drawable.pause)
                showNotification(R.drawable.pause,1F)
            } catch (e: Exception) {
                return
            }
        }
    }
}