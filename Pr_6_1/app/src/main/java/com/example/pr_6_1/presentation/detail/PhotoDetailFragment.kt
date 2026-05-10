package com.example.pr_6_1.presentation.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.example.pr_6_1.R
import com.example.pr_6_1.databinding.FragmentPhotoDetailBinding

class PhotoDetailFragment : Fragment() {

    private var _binding: FragmentPhotoDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PhotoDetailViewModel by viewModels()

    private var downloadUrl: String = ""
    private var photoId: String = ""

    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.downloadPhoto(downloadUrl, uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoId = arguments?.getString("photoId") ?: ""
        val author = arguments?.getString("author") ?: ""
        val width = arguments?.getInt("width") ?: 0
        val height = arguments?.getInt("height") ?: 0
        downloadUrl = arguments?.getString("downloadUrl") ?: ""

        binding.imageViewPhoto.load(downloadUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
        }
        binding.textViewAuthor.text = author
        binding.textViewDimensions.text = "Размер: $width × $height px"
        binding.textViewLink.text = downloadUrl

        binding.buttonDownload.setOnClickListener { launchSafPicker() }

        observeDownloadState()
    }

    private fun launchSafPicker() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/jpeg"
            putExtra(Intent.EXTRA_TITLE, "photo_$photoId.jpg")
        }
        createFileLauncher.launch(intent)
    }

    private fun observeDownloadState() {
        viewModel.downloadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DownloadState.Idle -> Unit
                is DownloadState.Downloading -> {
                    binding.buttonDownload.isEnabled = false
                    binding.progressBarDownload.visibility = View.VISIBLE
                }
                is DownloadState.Success -> {
                    binding.buttonDownload.isEnabled = true
                    binding.progressBarDownload.visibility = View.GONE
                    Toast.makeText(requireContext(), "Фото сохранено!", Toast.LENGTH_SHORT).show()
                }
                is DownloadState.Error -> {
                    binding.buttonDownload.isEnabled = true
                    binding.progressBarDownload.visibility = View.GONE
                    Toast.makeText(requireContext(), "Ошибка: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
