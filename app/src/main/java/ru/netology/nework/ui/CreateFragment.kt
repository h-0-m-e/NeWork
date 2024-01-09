package ru.netology.nework.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.crazylegend.audiopicker.pickers.SingleAudioPicker
import com.crazylegend.imagepicker.pickers.SingleImagePicker
import com.crazylegend.videopicker.pickers.SingleVideoPicker
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.CreateFragmentBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.extentions.loadFromLocalStorage
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.types.AttachmentType
import ru.netology.nework.types.EventType
import ru.netology.nework.types.PublicationType
import ru.netology.nework.utils.StringArg
import ru.netology.nework.viewmodel.CreateViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.Calendar

class CreateFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private val createViewModel: CreateViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0

    private var fullDate = ""

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedHour = 0
    private var savedMinute = 0

    private val dateChanged = MutableLiveData(false)
    private var dateChosen = false
    private var publicationType = PublicationType.POST


    private var pickedAttachment: AttachmentModel? = null
    private var pickedAttachmentType: AttachmentType? = null

    private var myId = 0
    private var myAvatar: String? = null
    private var myName = ""

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = CreateFragmentBinding.inflate(
            inflater,
            container,
            false
        )

        val defaultColor =
            ContextCompat.getColor(this.requireContext(), R.color.purple_small_button)
        val redColor = ContextCompat.getColor(this.requireContext(), R.color.red)
        val grayColor = ContextCompat.getColor(this.requireContext(), R.color.gray)

        createViewModel.loadUser()
        createViewModel.user.observe(viewLifecycleOwner) {
            myId = it.id
            myAvatar = it.avatar
            myName = it.name
        }

        binding.editDateTime.setOnClickListener {
            getDateTimeCalendar()

            DatePickerDialog(
                this@CreateFragment.requireContext(),
                this,
                year,
                month,
                day
            ).show()

        }

        dateChanged.observe(viewLifecycleOwner) {
            fullDate = "$savedYear-$savedMonth-$savedDay" + "T$savedHour:$savedMinute:00Z"
            if (it) {
                binding.eventDateText.text =
                    if (savedYear != 0) {
                        "$savedDay.$savedMonth.$savedYear  $savedHour:$savedMinute"
                    } else {
                        getString(R.string.not_chosen)
                    }
                dateChanged.value = !it
                dateChosen = true
            }
        }

        binding.radioType.setOnCheckedChangeListener { _, i ->
            if (i == binding.radioPost.id) {
                //Post Radio checked
                publicationType = PublicationType.POST
                binding.eventSettingsGroup.visibility = View.GONE
            } else {
                //Event Radio checked
                publicationType = PublicationType.EVENT
                binding.eventSettingsGroup.visibility = View.VISIBLE
            }

        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.attachmentButton.setOnClickListener {
            binding.attachmentButtonsGroup.isVisible =
                !binding.attachmentButtonsGroup.isVisible
        }

        binding.addPhoto.setOnClickListener {
            SingleImagePicker.showPicker(context = this.requireContext()) {
                pickedAttachment = AttachmentModel(
                    it.contentUri,
                    getFileFromUri(it.contentUri, this.requireContext())
                )
                pickedAttachmentType = AttachmentType.IMAGE
                binding.photoPreview.loadFromLocalStorage(it.contentUri)
                binding.photoPreview.visibility = View.VISIBLE
                binding.clearPhoto.visibility = View.VISIBLE
                binding.videoPreview.visibility = View.GONE
                binding.clearVideo.visibility = View.GONE
                binding.audioPreview.visibility = View.GONE
                binding.audioTitle.visibility = View.GONE
                binding.clearAudio.visibility = View.GONE

            }
        }
        binding.clearPhoto.setOnClickListener {
            binding.photoPreview.visibility = View.GONE
            binding.clearPhoto.visibility = View.GONE
            pickedAttachment = null
            pickedAttachmentType = null
        }

        binding.addVideo.setOnClickListener {
            SingleVideoPicker.showPicker(context = this.requireContext()) {
                pickedAttachment = AttachmentModel(
                    it.contentUri,
                    getFileFromUri(it.contentUri, this.requireContext())
                )
                pickedAttachmentType = AttachmentType.VIDEO
                binding.videoPreview.visibility = View.VISIBLE
                binding.clearVideo.visibility = View.VISIBLE
                binding.photoPreview.visibility = View.GONE
                binding.clearPhoto.visibility = View.GONE
                binding.audioPreview.visibility = View.GONE
                binding.audioTitle.visibility = View.GONE
                binding.clearAudio.visibility = View.GONE
            }
        }
        binding.clearVideo.setOnClickListener {
            binding.videoPreview.visibility = View.GONE
            binding.clearVideo.visibility = View.GONE
            pickedAttachment = null
            pickedAttachmentType = null
        }

        binding.addAudio.setOnClickListener {
            SingleAudioPicker.showPicker(context = this.requireContext()) {
                pickedAttachment = AttachmentModel(
                    it.contentUri,
                    getFileFromUri(it.contentUri, this.requireContext())
                )
                pickedAttachmentType = AttachmentType.AUDIO
                if (it.contentUri.toString().isNotBlank()) {
                    binding.audioTitle.text = it.artist + " - " + it.displayName
                    binding.audioPreview.visibility = View.VISIBLE
                    binding.audioTitle.visibility = View.VISIBLE
                    binding.clearAudio.visibility = View.VISIBLE
                    binding.videoPreview.visibility = View.GONE
                    binding.clearVideo.visibility = View.GONE
                    binding.photoPreview.visibility = View.GONE
                    binding.clearPhoto.visibility = View.GONE
                }
            }
        }
        binding.clearAudio.setOnClickListener {
            binding.audioPreview.visibility = View.GONE
            binding.audioTitle.visibility = View.GONE
            binding.clearAudio.visibility = View.GONE
            pickedAttachment = null
            pickedAttachmentType = null
        }

        binding.videoPreview.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pickedAttachment?.uri.toString()))
            val videoIntent =
                Intent.createChooser(intent, this.getString(R.string.link_intent))
            this.startActivity(videoIntent)
        }

        binding.publishButton.setOnClickListener {
            val currentDateTime = LocalDateTime.now()
            binding.link.setTextColor(defaultColor)
            binding.content.setHintTextColor(grayColor)
            binding.eventDate.setTextColor(defaultColor)
//POST SAVING
            if (publicationType == PublicationType.POST) {
                var postIsReady = true
                val newPost = Post(
                    id = (0..1_000_000).random(),
                    authorId = myId,
                    author = myName,
                    authorAvatar = myAvatar,
                    authorJob = null,
                    content = binding.content.text.toString(),
                    published = currentDateTime.toString(),
                    coords = null,
                    link = binding.linkText.text.toString(),
                    likeOwnerIds = emptyList(),
                    mentionIds = emptyList(),
                    mentionedMe = false,
                    likedByMe = false,
                    likes = 0,
                    ownedByMe = true,
                    attachment = null
                )
//Link checking
                if (binding.linkText.text.isNullOrBlank()){
                    binding.link.setTextColor(redColor)
                    postIsReady = false
//Content checking
                } else if(binding.content.text.isNullOrBlank()) {
                    binding.content.setHintTextColor(redColor)
                    postIsReady = false
                } else if(postIsReady){
//Without Attachment
                    if (pickedAttachment == null) {
                        createViewModel.publishPost(newPost)
                        findNavController().popBackStack()
//With Attachment
                    } else {
                        createViewModel.publishPostWithAttachment(
                            newPost,
                            pickedAttachment!!,
                            pickedAttachmentType!!
                        )
                        findNavController().popBackStack()
                    }
                }

//EVENT SAVING
            } else {
                var eventIsReady = true
                val newEvent = Event(
                    id = (0..1_000_000).random(),
                    authorId = myId,
                    author = myName,
                    authorAvatar = myAvatar,
                    content = binding.content.text.toString(),
                    datetime = calculateDateTimeForEvent(),
                    published = currentDateTime.toString(),
                    type = if (binding.offlineCheckbox.isChecked) EventType.OFFLINE
                    else EventType.ONLINE,
                    likeOwnerIds = emptyList(),
                    likedByMe = false,
                    speakerIds = emptyList(),
                    participantIds = emptyList(),
                    participatedByMe = false,
                    attachment = null,
                    link = binding.linkText.text.toString(),
                    ownedByMe = true,
                    likes = 0,
                    speakersAvatarUrls = emptyList(),
                    isPlayingAudio = false,
                )
//Link checking
                if (binding.linkText.text.isNullOrBlank()){
                    binding.link.setTextColor(redColor)
                    eventIsReady = false
//Content checking
                } else if(binding.content.text.isNullOrBlank()) {
                    binding.content.setHintTextColor(redColor)
                    eventIsReady = false
//Date Checking
                } else if(!dateChosen) {
                    binding.eventDate.setTextColor(redColor)
                    eventIsReady = false
                } else if(eventIsReady) {
//Without Attachment
                    if (pickedAttachment == null) {
                        createViewModel.publishEvent(newEvent)
                        findNavController().popBackStack()
//With Attachment
                    } else {
                        createViewModel.publishEventWithAttachment(
                            newEvent,
                            pickedAttachment!!,
                            pickedAttachmentType!!
                        )
                        findNavController().popBackStack()
                    }
                }
            }
        }

        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private fun getDateTimeCalendar() {
        val cal = Calendar.getInstance()

        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
        minute = cal.get(Calendar.MINUTE)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month + 1
        savedYear = year

        getDateTimeCalendar()
        TimePickerDialog(
            this@CreateFragment.requireContext(),
            this,
            hour,
            minute,
            true
        ).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute
        dateChanged.value = true
    }

    private fun getFileFromUri(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)

        inputStream.use { input ->
            val file =
                File(context.cacheDir, "temp_file")
            FileOutputStream(file).use { output ->
                input?.copyTo(output)
            }
            return file
        }
    }

    private fun calculateDateTimeForEvent(): String{
//        2023-01-27T17:00:00Z
        var dateTime = "$savedYear-"
        if (savedMonth>=10){
            dateTime = "$dateTime$savedMonth-"
        } else{
            dateTime += "0$savedMonth-"
        }

        if (savedDay>=10){
            dateTime = "$dateTime$savedDay" + "T"
        } else{
            dateTime += "0$savedDay" + "T"
        }

        if (savedHour>=10){
            dateTime = "$dateTime$savedHour:"
        }else{
            dateTime += "0$savedHour:"
        }

        if (savedMinute>=10){
            dateTime = "$dateTime$savedMinute:00Z"
        }else{
            dateTime += "0$savedMinute:00Z"
        }

        return dateTime
    }
}

