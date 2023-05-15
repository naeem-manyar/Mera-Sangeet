package com.naeem.musicplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.naeem.musicplayer.R
import com.naeem.musicplayer.activity.PlayerActivity
import com.naeem.musicplayer.activity.PlaylistDetails
import com.naeem.musicplayer.activity.SelectionActivity
import com.naeem.musicplayer.databinding.MusicViewBinding
import com.naeem.musicplayer.fragment.HomeFragment.Companion.search
import com.naeem.musicplayer.fragment.PlaylistsFragment

class MusicAdapter(private val context:Context, var musicList:ArrayList<Music>,private val playlistDetails:Boolean = false,private val selectionActivity:Boolean = false): RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {
    inner class MusicViewHolder(binding: MusicViewBinding) : ViewHolder(binding.root) {
        val title = binding.tvSongNameMV
        val album = binding.tvSongAlbumMV
        val duration = binding.tvSongDuration
        val image = binding.imgMV
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(
            MusicViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val currentItem = musicList[position]
        holder.title.text = currentItem.title
        holder.album.text = currentItem.album
        holder.duration.text = formatDuration(currentItem.duration)
        Glide.with(context)
            .load(currentItem.artUri)
            .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
            .into(holder.image)
        when {
            playlistDetails -> {
                holder.root.setOnClickListener {
                    sendIntent(ref = "PlaylistDetailsAdapter", pos = position)
                }
            }
            selectionActivity -> {
                holder.root.setOnClickListener {
                    if (addSong(musicList[position])) {
                        holder.root.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.cool_pink
                            )
                        )
                        SelectionActivity.addBtn.visibility = View.VISIBLE
                    } else
                        holder.root.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.white
                            )
                        )
                }
            }
            else -> {
                holder.root.setOnClickListener {
                    when {
                        search -> sendIntent(ref = "MusicAdapterSearch", pos = position)
                        musicList[position].id == PlayerActivity.nowPlayingId -> {
                            sendIntent(ref = "NowPlaying", pos = PlayerActivity.songPosition)
                        }
                        else -> sendIntent(ref = "MusicAdapter", pos = position)
                    }
                }
            }
        }

    }

    private fun addSong(song: Music): Boolean {
        PlaylistsFragment.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            if (song.id == music.id) {
                PlaylistsFragment.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.removeAt(
                    index
                )
                return false
            }
        }
        PlaylistsFragment.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.add(song)
        return true
    }
    fun deleteItem(i:Int){
        musicList.removeAt(i)
        notifyDataSetChanged()
    }

    fun refreshPlaylist(){
        musicList = ArrayList()
        musicList.addAll(PlaylistsFragment.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList:ArrayList<Music>){
        this.musicList = ArrayList()
        this.musicList.addAll(searchList)
        notifyDataSetChanged()
    }
    private fun sendIntent(ref:String,pos:Int){
        val intent = Intent(context,PlayerActivity::class.java)
        intent.putExtra("index",pos)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }
}