package com.naeem.musicplayer.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.naeem.musicplayer.R
import com.naeem.musicplayer.activity.PlayerActivity
import com.naeem.musicplayer.adapter.setSongPosition
import com.naeem.musicplayer.databinding.FragmentNowPlayingBinding

class NowPlayingFragment : Fragment() {
    companion object {
        @SuppressLint("StaticFieldLeak")
       private lateinit var _binding: FragmentNowPlayingBinding
        // This property is only valid between onCreateView and onDestroyView.
         val binding get() = _binding
    }

override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {
    _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
    binding.root.visibility = View.INVISIBLE
    binding.fbtnPlayPauseNP.setOnClickListener {
            if (PlayerActivity.isPlaying) pauseMusic()
            else playMusic()
        }
    binding.fbtnNextNP.setOnClickListener {
        setSongPosition(increment = true)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(this)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
            .into(binding.imgSongNP)
        binding.tvSongNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        PlayerActivity.musicService!!.showNotification(R.drawable.pause,1F)
        playMusic()
    }
    binding.root.setOnClickListener {
        val intent = Intent(requireContext(),PlayerActivity::class.java)
        intent.putExtra("index",PlayerActivity.songPosition)
        intent.putExtra("class","NowPlaying")
        ContextCompat.startActivity(requireContext(),intent,null)
    }
    return binding.root
}
    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null){
            if(binding.root.visibility == View.INVISIBLE) {
                binding.root.visibility = View.VISIBLE
            }
            binding.tvSongNameNP.isSelected = true
            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
                .into(binding.imgSongNP)
            binding.tvSongNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            if (PlayerActivity.isPlaying){
                binding.fbtnPlayPauseNP.setIconResource(R.drawable.pause)
            }else{
                binding.fbtnPlayPauseNP.setIconResource(R.drawable.play)
            }
        }
    }
    private fun playMusic() {
        try {
            PlayerActivity.isPlaying = true
            PlayerActivity.musicService!!.mediaPlayer!!.start()
            binding.fbtnPlayPauseNP.setIconResource(R.drawable.pause)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause,1F)
            PlayerActivity.binding.fbtnNext.setIconResource(R.drawable.pause)
        } catch (e: Exception) {
            return
        }
    }

    private fun pauseMusic() {
        try {
            PlayerActivity.isPlaying = false
            PlayerActivity.musicService!!.mediaPlayer!!.pause()
            binding.fbtnPlayPauseNP.setIconResource(R.drawable.play)
            PlayerActivity.musicService!!.showNotification(R.drawable.play,0F)
            PlayerActivity.binding.fbtnNext.setIconResource(R.drawable.play)
        } catch (e: Exception) {
            return
        }
    }
}