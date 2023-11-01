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
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.WallViewModel

class MyWallFragment: Fragment() {


    private val wallViewModel: WallViewModel by viewModels()

    private val postViewModel: PostViewModel by viewModels()

    private val authViewModel: AuthViewModel by viewModels()

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

        val userId = authViewModel.data.value!!.id
        wallViewModel.setUser(userId)
        wallViewModel.loadUser()
        wallViewModel.loadJob()
        wallViewModel.loadPosts()

        wallViewModel.dataState.observe(viewLifecycleOwner){ state ->
            binding.progressBar.isVisible = state.refreshing || state.loading
        }

        swipeRefresh.setOnRefreshListener{
            binding.swipeRefresh.isRefreshing = false
            postViewModel.refresh()
            wallViewModel.refreshGeneral(userId)
        }


        wallViewModel.user.observe(viewLifecycleOwner){user ->
            if (user.avatar.isNullOrBlank()) {
                binding.avatar.setImageResource(R.drawable.sign_up_avatar_128)
            } else {
                binding.avatar.loadWallAvatar(user.avatar)
            }

            binding.userName.text = user.name
        }

        wallViewModel.job.observe(viewLifecycleOwner){ job ->
            if (job == null) {
                binding.jobGroup.visibility = View.GONE
                binding.addJobGroup.visibility = View.VISIBLE
            } else {
                binding.addJobGroup.visibility = View.GONE
                binding.jobGroup.visibility = View.VISIBLE
                binding.startJob.text = job.start
                binding.endJob.text =
                    if (!job.finish.isNullOrBlank()) job.finish else getString(R.string.now)
                binding.company.text = job.name
                binding.position.text = job.position
                binding.jobLinkText.isVisible = !job.link.isNullOrBlank()
                binding.jobLinkButton.isVisible = !job.link.isNullOrBlank()
            }

            binding.jobLinkButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job?.link))
                val linkIntent =
                    Intent.createChooser(intent, this.getString(R.string.link_intent))
                this.startActivity(linkIntent)
            }

        }

        val postInteractionListener = PostOnInteractionListener(
            this@MyWallFragment.requireActivity(),
            binding.root,
            postViewModel
        )

        val postAdapter = PostAdapter(postInteractionListener)

        binding.list.adapter = postAdapter
        wallViewModel.data.observe(viewLifecycleOwner) { data ->
            postAdapter.submitList(data.posts)
            binding.empty.isVisible = data.empty
        }
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
