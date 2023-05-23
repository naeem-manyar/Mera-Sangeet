package com.naeem.merasangeet.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.naeem.merasangeet.R
import com.naeem.merasangeet.activity.PlayerActivity
import com.naeem.merasangeet.adapter.favouriteChecker
import com.naeem.merasangeet.adapter.setSongPosition
import com.naeem.merasangeet.fragment.NowPlayingFragment

class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PREVIOUS -> preNextSong(increment = false, context = context!!)
            ApplicationClass.PLAY -> if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> preNextSong(increment = true, context = context!!)
        }
    }
    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause,1F)
        PlayerActivity.binding.fbtnPausePlay.setIconResource(R.drawable.pause)
        NowPlayingFragment.binding.fbtnPlayPauseNP.setIconResource(R.drawable.pause)
    }

    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play,0F)
        PlayerActivity.binding.fbtnPausePlay.setIconResource(R.drawable.play)
        NowPlayingFragment.binding.fbtnPlayPauseNP.setIconResource(R.drawable.play)
    }
    private fun preNextSong(increment:Boolean,context: Context){
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
            .into(PlayerActivity.binding.imgSong)
        PlayerActivity.binding.tvSongName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
            .into(NowPlayingFragment.binding.imgSongNP)
        NowPlayingFragment.binding.tvSongNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()
        PlayerActivity.fIndex = favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if (PlayerActivity.isFavourite){
            PlayerActivity.binding.btnFav.setImageResource(R.drawable.favorite_filled)
        }else{
            PlayerActivity.binding.btnFav.setImageResource(R.drawable.favorite_empty)
        }
    }

}