package ru.netology.nework.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nework.R
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.databinding.MyWallFragmentBinding
import ru.netology.nework.extentions.loadWallAvatar
import ru.netology.nework.listener.PostOnInteractionListener
import ru.netology.nework.viewmodel.MyWallViewModel
import ru.netology.nework.viewmodel.PostViewModel

class MyWallFragment: Fragment() {

    private val myWallViewModel: MyWallViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val postViewModel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MyWallFragmentBinding.inflate(
            inflater,
            container,
            false
        )

        val swipeRefresh = binding.swipeRefresh

        myWallViewModel.refreshGeneral()

        myWallViewModel.dataState.observe(viewLifecycleOwner){ state ->
            binding.progressBar.isVisible = state.refreshing || state.loading
        }

        swipeRefresh.setOnRefreshListener{
            binding.swipeRefresh.isRefreshing = false
            myWallViewModel.refreshGeneral()
        }


        myWallViewModel.user.observe(viewLifecycleOwner){user ->
            if (user.avatar.isNullOrBlank()) {
                binding.avatar.setImageResource(R.drawable.sign_up_avatar_128)
            } else {
                binding.avatar.loadWallAvatar(user.avatar)
            }

            binding.userName.text = user.name
        }

        myWallViewModel.job.observe(viewLifecycleOwner){ job ->
            if (job == null) {
                binding.jobGroup.visibility = View.GONE
                binding.addJobGroup.visibility = View.VISIBLE
            } else {
                val jobStart = job.start.take(4) + " - "
                val jobFinish = job.finish?.take(4)
                binding.addJobGroup.visibility = View.GONE
                binding.jobGroup.visibility = View.VISIBLE
                binding.startJob.text = jobStart
                binding.endJob.text =
                    if (!job.finish.isNullOrBlank()) jobFinish else getString(R.string.now)
                binding.company.text = job.name
                binding.position.text = job.position
                binding.jobLinkText.isVisible = !job.link.isNullOrBlank()
                binding.jobLinkButton.isVisible = !job.link.isNullOrBlank()
            }
        }

        binding.jobLinkButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse(myWallViewModel.job.value?.link))
            val linkIntent =
                Intent.createChooser(intent, this.getString(R.string.link_intent))
            this.startActivity(linkIntent)
        }

        val postInteractionListener = PostOnInteractionListener(
            this@MyWallFragment.requireActivity(),
            binding.root,
            postViewModel
        )

        val postAdapter = PostAdapter(postInteractionListener)


        binding.list.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )

        binding.jobEditButton.setOnClickListener {
            findNavController().navigate(R.id.action_myWallFragment_to_jobFragment)
        }

        binding.addJobButton.setOnClickListener {
            findNavController().navigate(R.id.action_myWallFragment_to_jobFragment)
        }

        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_myWallFragment_to_settingsFragment)
        }


        return binding.root
    }

}
