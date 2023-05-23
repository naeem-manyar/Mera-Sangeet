package com.naeem.merasangeet.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.naeem.merasangeet.R
import com.naeem.merasangeet.activity.PlayerActivity
import com.naeem.merasangeet.databinding.FavouritesViewBinding

class FavouritesAdapter (private val context: Context, private var musicList:ArrayList<Music>): RecyclerView.Adapter<FavouritesAdapter.FavouritesViewHolder>() {
    inner class FavouritesViewHolder(binding: FavouritesViewBinding): RecyclerView.ViewHolder(binding.root){
        val image = binding.imgSongFV
        val name = binding.tvSongNameFV
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesViewHolder {
        return FavouritesViewHolder(
            FavouritesViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: FavouritesViewHolder, position: Int) {
        holder.name.text = musicList[position].title
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
            .into(holder.image)
        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index",position)
            intent.putExtra("class","FavouritesAdapter")
            ContextCompat.startActivity(context,intent,null)
        }
    }
}