package com.naeem.merasangeet.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naeem.merasangeet.R
import com.naeem.merasangeet.activity.PlaylistDetails
import com.naeem.merasangeet.databinding.PlaylistViewBinding
import com.naeem.merasangeet.fragment.PlaylistsFragment

class PlaylistViewAdapter (private val context: Context, private var playlistList:ArrayList<Playlist>): RecyclerView.Adapter<PlaylistViewAdapter.PlaylistViewHolder>() {
    inner class PlaylistViewHolder(binding: PlaylistViewBinding): RecyclerView.ViewHolder(binding.root){
        val image = binding.imgPlaylist
        val name = binding.tvPlaylistName
        val root = binding.root
        val delete = binding.btnPlaylistDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(
            PlaylistViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return playlistList.size
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.name.text = playlistList[position].name
        holder.name.isSelected = true
        holder.delete.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playlistList[position].name)
                .setMessage("Do you want to delete playlist?")
                .setPositiveButton("Yes") { dialog: DialogInterface, i: Int ->
                    PlaylistsFragment.musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog: DialogInterface, i: Int -> dialog.dismiss() }
            val customDialog = builder.create()
            customDialog.show()
        }
        holder.root.setOnClickListener {
            val intent = Intent(context, PlaylistDetails::class.java)
            intent.putExtra("index",position)
            ContextCompat.startActivity(context,intent,null)
        }
        if (PlaylistsFragment.musicPlaylist.ref[position].playlist.size > 0)
        {
            Glide.with(context)
                .load(PlaylistsFragment.musicPlaylist.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
                .into(holder.image)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist(){
        playlistList = ArrayList()
        playlistList.addAll(PlaylistsFragment.musicPlaylist.ref)
        notifyDataSetChanged()
    }
}