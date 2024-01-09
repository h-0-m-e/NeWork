package ru.netology.nework.listener

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation.findNavController
import ru.netology.nework.R
import ru.netology.nework.dto.Post
import ru.netology.nework.ui.CreateFragment.Companion.textArg
import ru.netology.nework.viewmodel.PostViewModel

open class PostOnInteractionListener(
    private val context: Context,
    private val view: View,
    private val viewModel: PostViewModel,
){

    open fun onLike(post: Post) {
        viewModel.likeById(post)
    }

    open fun onFollowLink(post: Post){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.link))
        val linkIntent =
            Intent.createChooser(intent, context.getString(R.string.link_intent))
        context.startActivity(linkIntent)
    }

    open fun onOpenPhoto(post: Post){
        findNavController(view).navigate(R.id.photoFragment,
            Bundle().apply {
                textArg = post.attachment!!.url
            })
    }

    open fun onOpenVideo(post: Post){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
        val videoIntent =
            Intent.createChooser(intent, context.getString(R.string.link_intent))
        context.startActivity(videoIntent)
    }

    open fun onOpenPost(post: Post){
        findNavController(view).navigate(R.id.postFragment,
            Bundle().apply {
                textArg = post.id.toString()
            })
    }

    open fun onOpenUserWall(id: String){
        findNavController(view).navigate(R.id.wallFragment,
            Bundle().apply {
                textArg = id
            })
    }

    open fun onPlayAudio(post: Post){

    }

    open fun onRemove(post: Post) {
        viewModel.removeById(post.id.toLong())
        findNavController(view).navigate(R.id.feedFragment)
    }

    open fun onEdit(post: Post) {
        viewModel.edit(post)
        findNavController(view).navigate(R.id.createFragment,
            Bundle().apply {
                textArg = post.id.toString()
            })
    }
}
