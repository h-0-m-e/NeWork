package ru.netology.nework.listener

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.dto.Event
import ru.netology.nework.ui.CreateFragment.Companion.textArg
import ru.netology.nework.viewmodel.EventViewModel

open class EventOnInteractionListener(
    private val context: Context,
    private val view: View,
    private val viewModel: EventViewModel,
){

    open fun onLike(event: Event) {
        viewModel.likeById(event)
    }

    open fun onFollowLink(event: Event){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.link))
        val linkIntent =
            Intent.createChooser(intent, context.getString(R.string.link_intent))
        context.startActivity(linkIntent)
    }

    open fun onOpenPhoto(event: Event){
        findNavController(view).navigate(R.id.photoFragment,
            Bundle().apply {
                textArg = event.attachment!!.url
            })
    }

    open fun onOpenVideo(event: Event){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.attachment?.url))
        val videoIntent =
            Intent.createChooser(intent, context.getString(R.string.link_intent))
        context.startActivity(videoIntent)
    }

    open fun onOpenEvent(event: Event){
        findNavController(view).navigate(R.id.eventFragment,
            Bundle().apply {
                textArg = event.id.toString()
            })
    }

    open fun onOpenUserWall(id: String){
        findNavController(view).navigate(R.id.wallFragment,
            Bundle().apply {
                textArg = id
            })
    }

    open fun onPlayAudio(event: Event){

    }

    open fun onRemove(event: Event) {
        viewModel.removeById(event.id.toLong())
        findNavController(view).navigate(R.id.feedFragment)
    }

    open fun onEdit(event: Event) {
        viewModel.edit(event)
        findNavController(view).navigate(R.id.createFragment,
                Bundle().apply {
            textArg = event.id.toString()
        })
    }
}
