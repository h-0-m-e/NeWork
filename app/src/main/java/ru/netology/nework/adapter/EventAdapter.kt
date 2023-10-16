package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.extentions.load
import ru.netology.nework.extentions.loadCircle
import ru.netology.nework.listener.EventOnInteractionListener
import ru.netology.nework.types.AttachmentType
import ru.netology.nework.types.EventType
import ru.netology.nework.utils.CounterUtil

class EventAdapter(
    private val eventOnInteractionListener: EventOnInteractionListener
) : ListAdapter<Event, EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, eventOnInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val eventOnInteractionListener: EventOnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        binding.apply {
            nickname.text = event.author
            published.text = CounterUtil.countPublishDate(event.published)
            eventDateText.text = CounterUtil.countPublishDate(event.datetime)
            eventText.text = event.content
            link.text = event.link
            link.isVisible = !event.link.isNullOrBlank()
            like.text = CounterUtil.shortingByLetters(event.likes)
            like.isChecked = event.likedByMe
            if (event.authorAvatar.isNullOrBlank()) {
                publisherAvatar.setImageResource(R.drawable.profile_48)
            } else {
                publisherAvatar.loadCircle("${event.authorAvatar}")
            }
            photo.load("${event.attachment?.url}")
            if (event.type == EventType.ONLINE) {
                eventType.isChecked = true
                eventType.setText(R.string.online_event)
            } else {
                eventType.isChecked = false
                eventType.setText(R.string.offline_event)
            }
//I decided to use "if" operator instead "when" operator cause of DRY.
//"when" is faster, but implementation requires to repeat "GONE\VISIBLE" code many times.
            if (event.speakerIds.isEmpty()) {
                eventSpeakers.visibility = View.GONE
                firstSpeakerAvatar.visibility = View.GONE
                firstSpeakerBackground.visibility = View.GONE
                secondSpeakerAvatar.visibility = View.GONE
                secondSpeakerBackground.visibility = View.GONE
                thirdSpeakerAvatar.visibility = View.GONE
                speakersMore.visibility = View.GONE
                speakersMoreText.visibility = View.GONE
            } else {
                eventSpeakers.visibility = View.VISIBLE
//Code below makes first speaker avatar visible
                firstSpeakerAvatar.isVisible = true
                if (event.speakersAvatarUrls[0] == "") {
                    firstSpeakerAvatar.setImageResource(R.drawable.publication_avatar_48)
                } else {
                    firstSpeakerAvatar.loadCircle(event.speakersAvatarUrls[0])
                }
                firstSpeakerBackground.isVisible = true
//Code below makes second speaker avatar visible
                if (event.speakerIds.size >= 2) {

                    secondSpeakerAvatar.isVisible = true
                    if (event.speakersAvatarUrls[1] == "") {
                        secondSpeakerAvatar.setImageResource(R.drawable.publication_avatar_48)
                    } else {
                        secondSpeakerAvatar.loadCircle(event.speakersAvatarUrls[1])
                    }
                    secondSpeakerBackground.isVisible = true
//Code below makes third speaker avatar visible
                    if (event.speakerIds.size >= 3) {

                        thirdSpeakerAvatar.isVisible = true
                        if (event.speakersAvatarUrls[2] == "") {
                            thirdSpeakerAvatar.setImageResource(R.drawable.publication_avatar_48)
                        } else {
                            thirdSpeakerAvatar.loadCircle(event.speakersAvatarUrls[2])
                        }
//Code below makes other speaker count visible
                        if (event.speakerIds.size > 3) {
                            speakersMore.visibility = View.VISIBLE
                            speakersMoreText.visibility = View.VISIBLE
                            speakersMore.text = "${event.speakerIds.size - 3}"
                        } else {
                            speakersMore.visibility = View.GONE
                            speakersMoreText.visibility = View.GONE
                        }
                    } else {
                        thirdSpeakerAvatar.isVisible = false
                        speakersMore.visibility = View.GONE
                        speakersMoreText.visibility = View.GONE
                    }

                } else {
                    secondSpeakerAvatar.isVisible = false
                    secondSpeakerBackground.isVisible = false
                    thirdSpeakerAvatar.isVisible = false
                    speakersMore.visibility = View.GONE
                    speakersMoreText.visibility = View.GONE
                }
            }



            photo.isVisible = (event.attachment?.type == AttachmentType.IMAGE) &&
                    (event.attachment.url.isNotBlank())
            videoGroup.isVisible = (event.attachment?.type == AttachmentType.VIDEO) &&
                    (event.attachment.url.isNotBlank())
            audioGroup.isVisible = (event.attachment?.type == AttachmentType.AUDIO) &&
                    (event.attachment.url.isNotBlank())

            publisherAvatar.setOnClickListener {
                eventOnInteractionListener.onOpenUserWall(event.id.toString())
            }

            nickname.setOnClickListener {
                eventOnInteractionListener.onOpenUserWall(event.id.toString())
            }

            link.setOnClickListener {
                eventOnInteractionListener.onFollowLink(event)
            }

            videoPreview.setOnClickListener {
                eventOnInteractionListener.onOpenVideo(event)
            }

            playVideo.setOnClickListener {
                eventOnInteractionListener.onOpenVideo(event)
            }

            photo.setOnClickListener {
                eventOnInteractionListener.onOpenPhoto(event)
            }

            playAudioButton.setOnClickListener {
                eventOnInteractionListener.onPlayAudio(event)
            }

            like.setOnClickListener {
                eventOnInteractionListener.onLike(event)
            }

            menu.isVisible = event.ownedByMe

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_publication)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                eventOnInteractionListener.onRemove(event)
                                true
                            }

                            R.id.edit -> {
                                eventOnInteractionListener.onEdit(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            root.setOnClickListener {
                eventOnInteractionListener.onOpenEvent(event)
            }
        }
    }
}


class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}
