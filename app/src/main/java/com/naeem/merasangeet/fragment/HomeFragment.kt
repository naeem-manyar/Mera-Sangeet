package com.naeem.merasangeet.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.naeem.merasangeet.R
import com.naeem.merasangeet.adapter.Music
import com.naeem.merasangeet.adapter.MusicAdapter
import com.naeem.merasangeet.databinding.FragmentHomeBinding
import java.io.File
import java.util.*

class HomeFragment : Fragment() {
    companion object{
        lateinit var musicListMA:ArrayList<Music>
        @SuppressLint("StaticFieldLeak")
        lateinit var musicAdapter: MusicAdapter
        lateinit var musicListSearch:ArrayList<Music>
        var search:Boolean = false
    }

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        search = false
        musicListMA = getAllAudio()
        binding.rv.setHasFixedSize(true)
        binding.rv.setItemViewCacheSize(13)
        binding.rv.layoutManager = LinearLayoutManager(context)
        if (activity != null)
            musicAdapter = MusicAdapter(requireContext(),musicListMA)
        binding.rv.adapter = musicAdapter
        val divider = DividerItemDecoration(context,RecyclerView.VERTICAL)
        ResourcesCompat.getDrawable(resources,R.drawable.divider,null)?.let {
            divider.setDrawable(it)
        }
        binding.rv.addItemDecoration(divider)
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
               return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
               filterList(newText)
                return true
            }

        })
    }

    private fun filterList(query: String?) {
        if (query != null){
            musicListSearch = ArrayList()
            for (song in musicListMA) {
                if (song.title.lowercase(Locale.ROOT).contains(query))
                    musicListSearch.add(song)
            }
            if (musicListSearch.isEmpty()) {
                Toast.makeText(context, "No Data Found", Toast.LENGTH_SHORT).show()
            }else {
                search = true
                musicAdapter.updateMusicList(musicListSearch)
            }
        }
    }

    @SuppressLint("Range")
    private fun getAllAudio():ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val projection = arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED,MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ALBUM_ID)
        val cursor = context?.contentResolver?.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,selection,null,
            MediaStore.Audio.Media.DATE_ADDED, null)
        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(
                        id = idC, title = titleC, album = albumC, artist = artistC,
                        duration = durationC, path = pathC, artUri = artUriC
                    )
                    val file = File(music.path)
                    if (file.exists())
                        tempList.add(music)
                } while (cursor.moveToNext())
            cursor.close()
        }
        return tempList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}