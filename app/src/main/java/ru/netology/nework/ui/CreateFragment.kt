package ru.netology.nework.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
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
import ru.netology.nework.dto.Post
import ru.netology.nework.extentions.loadFromLocalStorage
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.types.AttachmentType
import ru.netology.nework.types.PublicationType
import ru.netology.nework.utils.StringArg
import ru.netology.nework.viewmodel.CreateViewModel
import java.io.File
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
    private var publicationType = PublicationType.POST

    private var pickedAttachment: AttachmentModel? = null
    private var pickedAttachmentType: AttachmentType? = null

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
                    if (savedMinute != 0) {
                        "$savedDay.$savedMonth.$savedYear  $savedHour:$savedMinute"
                    } else {
                        getString(R.string.not_chosen)
                    }
                dateChanged.value = !it
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
                pickedAttachment = AttachmentModel(it.contentUri, File(it.contentUri.path!!))
                pickedAttachmentType = AttachmentType.IMAGE
                if (it.contentUri.toString().isNotBlank()) {
                    binding.photoPreview.loadFromLocalStorage(it.contentUri)
                    binding.photoPreviewContainer.visibility = View.VISIBLE
                    binding.videoPreview.visibility = View.GONE
                    binding.audioPreviewContainer.visibility = View.GONE
                }
            }
        }
        binding.clearPhoto.setOnClickListener {
            binding.photoPreviewContainer.visibility = View.GONE
            pickedAttachment = null
            pickedAttachmentType = null
        }

        binding.addVideo.setOnClickListener {
            SingleVideoPicker.showPicker(context = this.requireContext()) {
                pickedAttachment = AttachmentModel(it.contentUri, File(it.contentUri.path!!))
                pickedAttachmentType = AttachmentType.VIDEO
//                if (it.contentUri.toString().isNotBlank()) {
                binding.videoPreviewContainer.visibility = View.VISIBLE
                binding.photoPreview.visibility = View.GONE
                binding.audioPreviewContainer.visibility = View.GONE
//                }
            }
        }
        binding.clearVideo.setOnClickListener {
            binding.videoPreviewContainer.visibility = View.GONE
            pickedAttachment = null
            pickedAttachmentType = null
        }

        binding.addAudio.setOnClickListener {
            SingleAudioPicker.showPicker(context = this.requireContext()) {
                pickedAttachment = AttachmentModel(it.contentUri, File(it.contentUri.path!!))
                pickedAttachmentType = AttachmentType.VIDEO
                if (it.contentUri.toString().isNotBlank()) {
                    binding.audioTitle.text = it.artist + " - " + it.displayName
                    binding.photoPreview.visibility = View.GONE
                    binding.videoPreview.visibility = View.GONE
                    binding.audioPreviewContainer.visibility = View.VISIBLE
                }
            }
        }
        binding.clearAudio.setOnClickListener {
            binding.audioPreviewContainer.visibility = View.GONE
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
            if (pickedAttachment != null){



            }
            if (publicationType == PublicationType.POST) {


            } else {

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
        savedMonth = month
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
}
