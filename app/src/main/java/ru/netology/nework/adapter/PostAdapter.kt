package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.extentions.load
import ru.netology.nework.extentions.loadCircle
import ru.netology.nework.listener.PostOnInteractionListener
import ru.netology.nework.types.AttachmentType
import ru.netology.nework.utils.CounterUtil

class PostAdapter(
    private val postOnInteractionListener: PostOnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, postOnInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val postOnInteractionListener: PostOnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            nickname.text = post.author
            postText.text = post.content
            published.text = CounterUtil.countPublishDate(post.published)
            link.text = post.link
            link.isVisible = !post.link.isNullOrBlank()
            like.text = CounterUtil.shortingByLetters(post.likes)
            like.isChecked = post.likedByMe
            if (post.authorAvatar.isNullOrBlank()){
                publisherAvatar.setImageResource(R.drawable.profile_48)
            }else{
                publisherAvatar.loadCircle("${post.authorAvatar}")
            }
            photo.load("${post.attachment?.url}")

            photo.isVisible = (post.attachment?.type == AttachmentType.IMAGE) &&
                    (post.attachment.url.isNotBlank())
            videoGroup.isVisible = (post.attachment?.type == AttachmentType.VIDEO) &&
                    (post.attachment.url.isNotBlank())
            audioGroup.isVisible = (post.attachment?.type == AttachmentType.AUDIO) &&
                    (post.attachment.url.isNotBlank())

            publisherAvatar.setOnClickListener {
                postOnInteractionListener.onOpenUserWall(post.authorId.toString())
            }

            nickname.setOnClickListener{
                postOnInteractionListener.onOpenUserWall(post.authorId.toString())
            }

            link.setOnClickListener {
                postOnInteractionListener.onFollowLink(post)
            }

            videoPreview.setOnClickListener {
                postOnInteractionListener.onOpenVideo(post)
            }

            playVideo.setOnClickListener {
                postOnInteractionListener.onOpenVideo(post)
            }

            photo.setOnClickListener {
                postOnInteractionListener.onOpenPhoto(post)
            }

            playAudioButton.setOnClickListener {
                postOnInteractionListener.onPlayAudio(post)
            }

            like.setOnClickListener {
                postOnInteractionListener.onLike(post)
            }

            menu.isVisible = post.ownedByMe

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_publication)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                postOnInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                postOnInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }


            root.setOnClickListener {
                postOnInteractionListener.onOpenPost(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
