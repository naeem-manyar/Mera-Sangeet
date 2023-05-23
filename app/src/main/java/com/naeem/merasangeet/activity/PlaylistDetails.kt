package com.naeem.merasangeet.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.naeem.merasangeet.R
import com.naeem.merasangeet.services.SwipeGesture
import com.naeem.merasangeet.adapter.MusicAdapter
import com.naeem.merasangeet.adapter.checkPlaylist
import com.naeem.merasangeet.databinding.ActivityPlaylistDetailsBinding
import com.naeem.merasangeet.fragment.PlaylistsFragment

class PlaylistDetails : AppCompatActivity() {
    lateinit var binding: ActivityPlaylistDetailsBinding
    lateinit var adapter: MusicAdapter

    companion object {
        var currentPlaylistPos: Int = -1
        @SuppressLint("StaticFieldLeak")
        lateinit var removeLayout: LinearLayout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlaylistPos = intent.extras?.get("index") as Int
        PlaylistsFragment.musicPlaylist.ref[currentPlaylistPos].playlist =
            checkPlaylist(playlist = PlaylistsFragment.musicPlaylist.ref[currentPlaylistPos].playlist)
        binding.rvPlaylistDetails.setItemViewCacheSize(13)
        binding.rvPlaylistDetails.setHasFixedSize(true)
        binding.rvPlaylistDetails.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(
            this,
            PlaylistsFragment.musicPlaylist.ref[currentPlaylistPos].playlist,
            playlistDetails = true
        )
        binding.rvPlaylistDetails.adapter = adapter
        removeLayout = binding.llRemovePD
        binding.btnBackPD.setOnClickListener {
            finish()
        }
        binding.fbtnShufflePD.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")
            startActivity(intent)
        }
        binding.btnAddSongPD.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }
        //for removing item from playlist
        val swipeGesture = object : SwipeGesture(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                        adapter.deleteItem(viewHolder.adapterPosition)
                }
            }
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(binding.rvPlaylistDetails)

        binding.btnRemoveAllPD.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove")
                .setMessage("Do you want to remove all songs from playlist?")
                .setPositiveButton("Yes") { dialog: DialogInterface, i: Int ->
                    PlaylistsFragment.musicPlaylist.ref[currentPlaylistPos].playlist.clear()
                    adapter.refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog: DialogInterface, i: Int -> dialog.dismiss() }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }

    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.tvPlaylistNamePD.text =
            PlaylistsFragment.musicPlaylist.ref[currentPlaylistPos].name
        binding.tvMoreInfoPD.text = "Total ${adapter.itemCount} Songs.\n\n" +
                "Created On:\n${PlaylistsFragment.musicPlaylist.ref[currentPlaylistPos].createdOn}\n\n" +
                " ${PlaylistsFragment.musicPlaylist.ref[currentPlaylistPos].createdBy}"
        if (adapter.itemCount > 0) {
            Glide.with(this)
                .load(PlaylistsFragment.musicPlaylist.ref[currentPlaylistPos].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
                .into(binding.imgPlaylistPD)
            binding.fbtnShufflePD.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()

        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        //for storing playlist data using shared preferences
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistsFragment.musicPlaylist)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()
    }
}
