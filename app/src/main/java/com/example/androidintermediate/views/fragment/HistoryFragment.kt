package com.example.androidintermediate.views.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.androidintermediate.R
import com.example.androidintermediate.databinding.FragmentHistoryBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.views.adapter.HistoryAdapter
import com.example.androidintermediate.views.viewmodel.HistoryViewModel

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    val historyViewModel: HistoryViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyViewModel.loadingStory.observe(viewLifecycleOwner) {
                binding.loadingStory.isVisible = it
            }

            /* show recyclerView data when data successfully fetched */
        historyViewModel.assetImageStory.observe(viewLifecycleOwner) { data ->
                binding.rvStory.let {
                    binding.nullStory.isVisible = data.isEmpty()
                    it.setHasFixedSize(true)
                    it.layoutManager = GridLayoutManager(context, 2)
                    it.isNestedScrollingEnabled = false
                    it.adapter = HistoryAdapter(data)
                }
            }

        binding.btnInfoStory.setOnClickListener {
            Helper.showDialogInfo(
                requireContext(),
                getString(R.string.UI_info_uploaded_story)
            )
        }

    }
}