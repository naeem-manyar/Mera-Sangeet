package com.naeem.merasangeet.activity

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.naeem.merasangeet.R
import com.naeem.merasangeet.adapter.Music
import com.naeem.merasangeet.adapter.MusicPlaylist
import com.naeem.merasangeet.adapter.exitApplication
import com.naeem.merasangeet.databinding.ActivityMainBinding
import com.naeem.merasangeet.fragment.FavouritesFragment
import com.naeem.merasangeet.fragment.HomeFragment
import com.naeem.merasangeet.fragment.PlaylistsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragments(HomeFragment())
        requestRuntimePermission()
        //for retrieve favourites data using shared preferences
        FavouritesFragment.favouriteSongs = ArrayList()
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
        val jsonString =editor.getString("FavouriteSongs",null)
        val typeToken = object : TypeToken<ArrayList<Music>>(){}.type
        if (jsonString != null){
            val data:ArrayList<Music> = GsonBuilder().create().fromJson(jsonString,typeToken)
            FavouritesFragment.favouriteSongs.addAll(data)
        }
        //for retrieve playlist data using shared preferences
        PlaylistsFragment.musicPlaylist = MusicPlaylist()
        val jsonStringPlaylist =editor.getString("MusicPlaylist",null)
        if (jsonStringPlaylist != null){
            val dataPlaylist:MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist,MusicPlaylist::class.java)
            PlaylistsFragment.musicPlaylist = dataPlaylist
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragments(HomeFragment())
                R.id.favourites -> replaceFragments(FavouritesFragment())
                R.id.playlists -> replaceFragments(PlaylistsFragment())
                else -> { }
            }
            true
        }
    }
    private fun replaceFragments(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
    //for requesting permission
    private fun requestRuntimePermission(){
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null)
        {
            exitApplication()
        }
    }

    override fun onResume() {
        super.onResume()
        //for storing favourites data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouritesFragment.favouriteSongs)
        editor.putString("FavouriteSongs",jsonString)
        //for storing playlist data using shared preferences
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistsFragment.musicPlaylist)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()
    }
}