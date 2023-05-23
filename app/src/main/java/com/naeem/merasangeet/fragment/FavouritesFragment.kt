package com.naeem.merasangeet.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.naeem.merasangeet.activity.PlayerActivity
import com.naeem.merasangeet.adapter.FavouritesAdapter
import com.naeem.merasangeet.adapter.Music
import com.naeem.merasangeet.adapter.checkPlaylist
import com.naeem.merasangeet.databinding.FragmentFavouritesBinding

class FavouritesFragment : Fragment() {

    private var _binding: FragmentFavouritesBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var favouritesAdapter: FavouritesAdapter
    companion object{
        var favouriteSongs:ArrayList<Music> = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        favouriteSongs = checkPlaylist(favouriteSongs)
        binding.rvFavourites.setHasFixedSize(true)
        binding.rvFavourites.setItemViewCacheSize(13)
        binding.rvFavourites.layoutManager = GridLayoutManager(context,4)
        if (activity != null)
             favouritesAdapter = FavouritesAdapter(requireContext(), favouriteSongs)
        binding.rvFavourites.adapter = favouritesAdapter
        if (favouriteSongs.size < 1){
            binding.btnShuffles.visibility = View.INVISIBLE
        }
        binding.btnShuffles.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","FavouriteShuffle")
            ContextCompat.startActivity(requireContext(),intent,null)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
