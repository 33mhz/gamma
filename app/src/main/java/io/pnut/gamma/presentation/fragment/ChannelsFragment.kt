package io.pnut.gamma.presentation.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import io.pnut.gamma.presentation.adapter.pager.ChannelsPagerAdapter
import io.pnut.gamma.databinding.FragmentChannelsBinding

class ChannelsFragment : BaseFragment() {
    private val adapter by lazy {
        ChannelsPagerAdapter(requireContext(), this)
    }

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        binding.toolbar.setNavigationOnClickListener { backToPrevFragment() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ChannelsPagerAdapter(requireContext(), this)
        binding.channelsViewPager.adapter = adapter
        TabLayoutMediator(binding.channelsTabLayout, binding.channelsViewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }

    companion object {
        fun newInstance() = ChannelsFragment()
    }
}
