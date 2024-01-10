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
import ru.netology.nework.databinding.CardSpeakerBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.extentions.load
import ru.netology.nework.extentions.loadCircle
import ru.netology.nework.listener.EventOnInteractionListener
import ru.netology.nework.listener.PostOnInteractionListener
import ru.netology.nework.types.AttachmentType
import ru.netology.nework.utils.CounterUtil

class SpeakersAdapter(
    private val listener: EventOnInteractionListener
) : ListAdapter<User, SpeakerViewHolder>(SpeakerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeakerViewHolder {
        val binding = CardSpeakerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SpeakerViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: SpeakerViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
}

class SpeakerViewHolder(
    private val binding: CardSpeakerBinding,
    private val listener: EventOnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.apply {

            if(user.avatar.isNullOrBlank()){
                avatar.setImageResource(R.drawable.publication_avatar_48)
            } else{
                avatar.loadCircle("${user.avatar}")
            }

            nickname.text = user.name

            avatar.setOnClickListener {
                listener.onOpenUserWall(user.id)
            }
            nickname.setOnClickListener {
                listener.onOpenUserWall(user.id)
            }
            root.setOnClickListener {
                listener.onOpenUserWall(user.id)
            }
        }
    }
}

class SpeakerDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
