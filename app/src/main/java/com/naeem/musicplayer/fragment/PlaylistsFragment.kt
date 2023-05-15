package com.naeem.musicplayer.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naeem.musicplayer.R
import com.naeem.musicplayer.adapter.MusicPlaylist
import com.naeem.musicplayer.adapter.Playlist
import com.naeem.musicplayer.adapter.PlaylistViewAdapter
import com.naeem.musicplayer.databinding.AddPlaylistDialogBinding
import com.naeem.musicplayer.databinding.FragmentPlaylistsBinding
import java.text.SimpleDateFormat
import java.util.*

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    lateinit var playlistAdapter: PlaylistViewAdapter
    companion object{
        var musicPlaylist:MusicPlaylist = MusicPlaylist()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        binding.rvPlaylist.setHasFixedSize(true)
        binding.rvPlaylist.setItemViewCacheSize(13)
        binding.rvPlaylist.layoutManager = GridLayoutManager(context,2)
        if (activity != null)
            playlistAdapter = PlaylistViewAdapter(requireContext(), playlistList = musicPlaylist.ref)
        binding.rvPlaylist.adapter = playlistAdapter
        binding.fbtnAddPlaylist.setOnClickListener {
            customAlertDialog()
        }
        return binding.root
    }
    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(context).inflate(R.layout.add_playlist_dialog,binding.root,false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setView(customDialog)
            .setTitle("Playlist Details")
            .setPositiveButton("ADD") { dialog: DialogInterface, i: Int ->
                val playlistName = binder.edtPlaylistName.text
                val createdBy = binder.edtUserName.text
                if (playlistName != null && createdBy != null){
                    if (playlistName.isNotEmpty() && createdBy.isNotEmpty()){
                        addPlaylist(playlistName.toString(),createdBy.toString())
                    }
                }
                dialog.dismiss()
            }.show()
    }

    private fun addPlaylist(name: String, createdBy: String) {
        var playlistExists = false
        for (i in musicPlaylist.ref){
            if (name.equals(i.name)){
                playlistExists = true
                break
            }
        }
        if (playlistExists) Toast.makeText(context,"Playlist Exist!!",Toast.LENGTH_SHORT).show()
        else {
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createdBy = createdBy
            val calendar = Calendar.getInstance().time
            val  sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calendar)
            musicPlaylist.ref.add(tempPlaylist)
            playlistAdapter.refreshPlaylist()
        }
    }

    override fun onResume() {
        super.onResume()
        playlistAdapter.notifyDataSetChanged()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}