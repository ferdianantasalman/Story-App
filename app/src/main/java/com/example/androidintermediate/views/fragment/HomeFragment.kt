package com.example.androidintermediate.views.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.androidintermediate.R
import com.example.androidintermediate.databinding.FragmentHomeBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.views.activity.MainActivity
import com.example.androidintermediate.views.adapter.StoryAdapter
import com.example.androidintermediate.views.adapter.LoadingStateAdapter
import java.util.Timer
import kotlin.concurrent.schedule

class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: FragmentHomeBinding
    private val storyAdapter = StoryAdapter()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val storyViewModel = (activity as MainActivity).getStoryViewModel()

        storyViewModel.story.observe(viewLifecycleOwner) {
            storyAdapter.submitData(
                lifecycle,
                it
            )
            Helper.updateWidgetData(requireContext())
        }

        binding.toolbar.inflateMenu(R.menu.toolbar_menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.infoDialog -> {
                    Helper.showDialogInfo(
                        (activity as MainActivity),
                        (activity as MainActivity).getString(R.string.UI_info_homescreen),
                        Gravity.START
                    )
                    true
                }

                R.id.swipeRefresh -> {
                    onRefresh()
                    true
                }

                else -> false
            }
        }


        binding.swipeRefresh.setOnRefreshListener {
            onRefresh()
        }

        binding.rvStory.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            adapter = this@HomeFragment.storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    this@HomeFragment.storyAdapter.retry()
                }
            )
        }

        setHasOptionsMenu(true)
    }

    override fun onRefresh() {
        binding.swipeRefresh.isRefreshing = true
        storyAdapter.refresh()
        Timer().schedule(2000) {
            binding.swipeRefresh.isRefreshing = false
            binding.rvStory.smoothScrollToPosition(0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}