package com.naeem.musicplayer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.naeem.musicplayer.adapter.MusicAdapter
import com.naeem.musicplayer.databinding.ActivitySelectionBinding
import com.naeem.musicplayer.fragment.HomeFragment
import java.util.*
import kotlin.collections.ArrayList

class SelectionActivity : AppCompatActivity() {
    lateinit var binding:ActivitySelectionBinding
    lateinit var adapter: MusicAdapter
    companion object{
        lateinit var addBtn:ExtendedFloatingActionButton
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvSelection.setItemViewCacheSize(13)
        binding.rvSelection.setHasFixedSize(true)
        binding.rvSelection.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, HomeFragment.musicListMA, selectionActivity = true)
        binding.rvSelection.adapter = adapter
        binding.searchViewSE.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }

        })
        addBtn = binding.fbtnAddSE
        addBtn.setOnClickListener {
            finish()
        }
        binding.fbtnAddSE.setOnClickListener {
            finish()
        }
    }

    private fun filterList(query: String?) {
        if (query != null){
            HomeFragment.musicListSearch = ArrayList()
            for (song in HomeFragment.musicListMA) {
                if (song.title.lowercase(Locale.ROOT).contains(query))
                    HomeFragment.musicListSearch.add(song)
            }
            if (HomeFragment.musicListSearch.isEmpty())
                Toast.makeText(this,"No Data Found", Toast.LENGTH_SHORT).show()
            else
                HomeFragment.search = true
            adapter.updateMusicList(HomeFragment.musicListSearch)
        }
    }
}