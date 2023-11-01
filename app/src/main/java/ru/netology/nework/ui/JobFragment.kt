package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.databinding.JobFragmentBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.viewmodel.WallViewModel

class JobFragment : Fragment() {

    private val wallViewModel: WallViewModel by viewModels()

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

        if (wallViewModel.job.value?.id != 0) {
            binding.deleteButton.visibility = View.VISIBLE
            binding.positionField.setText(wallViewModel.job.value?.position)
            binding.companyField.setText(wallViewModel.job.value?.name)
            binding.linkField.setText(wallViewModel.job.value?.link ?: "")
            binding.employmentDateField.setText(wallViewModel.job.value?.start)
            binding.dismissalDateField.setText(wallViewModel.job.value?.finish)
            binding.stillWorkCheckbox.isChecked = !wallViewModel.job.value?.finish.isNullOrBlank()
        } else {
            binding.deleteButton.visibility = View.GONE
            binding.stillWorkCheckbox.isChecked = false
        }

        binding.dismissalDate.isVisible = !binding.stillWorkCheckbox.isChecked
        binding.dismissalDateField.isVisible = !binding.stillWorkCheckbox.isChecked

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
                if (binding.employmentDateField.text.isNullOrBlank())
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
                if (binding.dismissalDateField.text.isNullOrBlank()
                    && !binding.stillWorkCheckbox.isChecked
                )
                    redColor.also { jobIsReady = false }
                else
                    defaultColor
            )

            if (jobIsReady) {
                wallViewModel.saveJob(
                    Job(
                        id = 1,
                        name = binding.companyField.text.toString().trim(),
                        position = binding.positionField.text.toString().trim(),
                        start = binding.employmentDateField.text.toString().trim(),
                        finish = if (binding.stillWorkCheckbox.isChecked) null
                        else binding.dismissalDateField.text.toString().trim(),
                        link = binding.linkField.text.toString().trim()
                    )
                )
                wallViewModel.loadJob()
                findNavController().navigate(R.id.action_jobFragment_to_myWallFragment)
            }
        }

        binding.deleteButton.setOnClickListener {
            wallViewModel.deleteJob()
            findNavController().navigate(R.id.action_jobFragment_to_myWallFragment)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_jobFragment_to_myWallFragment)
        }




        return binding.root
    }
}
