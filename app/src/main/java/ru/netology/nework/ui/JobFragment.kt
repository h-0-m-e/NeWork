package ru.netology.nework.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.databinding.JobFragmentBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.viewmodel.MyWallViewModel
import java.util.Calendar
import javax.xml.datatype.DatatypeConstants.MONTHS

class JobFragment : Fragment() {

    private val myWallViewModel: MyWallViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = JobFragmentBinding.inflate(
            inflater,
            container,
            false
        )

        val defaultColor =
            ContextCompat.getColor(this.requireContext(), R.color.purple_small_button)
        val redColor = ContextCompat.getColor(this.requireContext(), R.color.red)

        var jobIsReady: Boolean

        myWallViewModel.job.observe(viewLifecycleOwner) { job ->
            if (job != null) {
                binding.positionField.setText(job.position)
                binding.companyField.setText(job.name)
                binding.linkField.setText(job.link)
                binding.employmentDateField.setText(job.start)
                binding.dismissalDateField.setText(job.finish)
                binding.stillWorkCheckbox.isChecked = job.finish.isNullOrBlank()
                binding.deleteButton.visibility = View.VISIBLE
            } else {
                binding.stillWorkCheckbox.isChecked = false
                binding.deleteButton.visibility = View.GONE
            }
        }

        binding.stillWorkCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                binding.dismissalDateField.isVisible = false
                binding.dismissalDate.isVisible = false
            }else{
                binding.dismissalDateField.isVisible = true
                binding.dismissalDate.isVisible = true
            }
        }


        binding.saveButton.setOnClickListener {
            jobIsReady = true
            binding.position.setTextColor(
                if (binding.positionField.text.isNullOrBlank())
                    redColor.also { jobIsReady = false }
                else
                    defaultColor
            )

            binding.company.setTextColor(
                if (binding.companyField.text.isNullOrBlank())
                    redColor.also { jobIsReady = false }
                else
                    defaultColor
            )

            binding.employmentDate.setTextColor(
                if (binding.employmentDateField.text.isNullOrBlank()
                    || binding.employmentDateField.text.toString().toInt() > Calendar.getInstance()
                        .get(Calendar.YEAR)
                    || binding.employmentDateField.text.toString().toInt() < 1900
                )
                    redColor.also { jobIsReady = false }
                else
                    defaultColor
            )

            binding.link.setTextColor(
                if (binding.linkField.text.isNullOrBlank())
                    redColor.also { jobIsReady = false }
                else
                    defaultColor
            )

            binding.dismissalDate.setTextColor(
                if ((binding.dismissalDateField.text.isNullOrBlank()
                            || binding.dismissalDateField.text.toString()
                        .toInt() > Calendar.getInstance()
                        .get(Calendar.YEAR)
                            || binding.dismissalDateField.text.toString()
                        .toInt() < binding.employmentDateField.text.toString().toInt())
                    && !binding.stillWorkCheckbox.isChecked
                )
                    redColor.also { jobIsReady = false }
                else
                    defaultColor
            )

            if (jobIsReady) {
                myWallViewModel.saveJob(
                    Job(
                        id = myWallViewModel.job.value?.id ?: 1,
                        name = binding.companyField.text.toString().trim(),
                        position = binding.positionField.text.toString().trim(),
                        start = binding.employmentDateField.text.toString() + "-01-01T00:00:00.003000Z",
                        finish = if (binding.stillWorkCheckbox.isChecked) null
                        else binding.dismissalDateField.text.toString() + "-01-01T00:00:00.003000Z",
                        link = binding.linkField.text.toString().trim()
                    )
                )
                myWallViewModel.loadJob()
                findNavController().navigate(R.id.action_jobFragment_to_myWallFragment)
            }
        }

        binding.deleteButton.setOnClickListener {
            myWallViewModel.deleteJob()
            findNavController().navigate(R.id.action_jobFragment_to_myWallFragment)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_jobFragment_to_myWallFragment)
        }




        return binding.root
    }
}
