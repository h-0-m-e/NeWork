package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectIndexed
import ru.netology.nework.adapter.PostPagingViewHolder
import ru.netology.nework.adapter.PostViewHolder
import ru.netology.nework.databinding.PostFragmentBinding
import ru.netology.nework.listener.PostOnInteractionListener
import ru.netology.nework.utils.StringArg
import ru.netology.nework.viewmodel.PostViewModel

class PostFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PostFragmentBinding.inflate(
            inflater,
            container,
            false
        )

        val postViewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val currentPostId = requireArguments().textArg?.toLong()

        val postInteractionListener = PostOnInteractionListener(
            this@PostFragment.requireActivity(),
            binding.root,
            postViewModel
        )

        binding.publication.apply {
            postViewModel.getById(currentPostId ?: 0)
            postViewModel.lastPost.observe(viewLifecycleOwner){ it ->
                val viewHolder = PostViewHolder(binding.publication, postInteractionListener)
                it?.let { viewHolder.bind(it) }
            }
        }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}
