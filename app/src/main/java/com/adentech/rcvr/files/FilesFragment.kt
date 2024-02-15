package com.adentech.rcvr.files


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import com.adentech.rcvr.R
import com.adentech.rcvr.common.Status
import com.adentech.rcvr.databinding.FragmentFilesBinding
import com.adentech.rcvr.decoration.SpacesItemDecoration
import com.adentech.rcvr.extensions.observe
import com.adentech.rcvr.fragments.BaseFragment
import com.adentech.rcvr.model.MainFileModel
import com.adentech.rcvr.scan.ScanViewModel
import com.adentech.rcvr.common.Resource
import com.adentech.rcvr.home.ImagesActivity
import com.adentech.rcvr.model.FileModel
import com.adentech.rcvr.utils.FileUtils.getPath
import com.adentech.rcvr.utils.FilesFetcher
import com.adentech.rcvr.utils.ImagesFilesCollector
import com.adentech.rcvr.utils.SearchAllFile
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class FilesFragment : BaseFragment<ScanViewModel, FragmentFilesBinding>() {

    private val deletedFilesAdapter by lazy {
        DeletedFilesAdapter(
            context = requireContext().applicationContext,
            onItemClicked = ::onItemClicked
        )
    }
    private var myMotor: SearchAllFile? = null
    private var volums: ArrayList<String> = ArrayList()

    override fun viewModelClass() = ScanViewModel::class.java

    override fun getResourceLayoutId() = R.layout.fragment_files

    override fun onInitDataBinding() {
        initRecyclerView()
        setupBackButton()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            viewModel.getAllTrashedFiles()
            observe(viewModel.allTrashedFilesList, ::getTrashedList)
        } else {
            getFilesFromOldVersions()
        }

        handleBackPressed()
    }

    private fun getFilesFromOldVersions() {
        volums = FilesFetcher(requireContext()).getStorageVolumes()
        if (volums.isNotEmpty()) {
            myMotor = SearchAllFile(volums, requireActivity(), requireContext())
            myMotor?.execute(*arrayOfNulls(0))
            deletedFilesAdapter.submitList(ImagesFilesCollector.foundFilesList)
        }
    }

    private fun initRecyclerView() {
        viewBinding.rvDeletedImages.apply {
            adapter = deletedFilesAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_6)
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
            setHasFixedSize(true)
        }
    }

    private fun getTrashedList(resource: Resource<MainFileModel>) {
        when (resource.status) {
            Status.SUCCESS -> {
                if (resource.data?.files?.size == 0 || resource.data?.files.isNullOrEmpty()) {
                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
                } else {
                    viewBinding.llEmptyFolder.visibility = View.GONE
                    resource.data?.files?.let { checkProgressBar(it) }
                    deletedFilesAdapter.submitList(resource.data?.files)
                }
                viewBinding.progressBar.visibility = View.GONE
            }

            Status.ERROR -> {
                viewBinding.progressBar.visibility = View.GONE
                viewBinding.llEmptyFolder.visibility = View.VISIBLE
            }

            Status.LOADING -> {
                viewBinding.progressBar.visibility = View.VISIBLE
            }
        }
    }
    private fun setupBackButton() {
        viewBinding.backButton.setOnClickListener {
            val intent = Intent(requireContext(), ImagesActivity::class.java)
            startActivity(intent)
        }
    }
    private fun checkProgressBar(list: ArrayList<FileModel>) {
        if (list.size.compareTo(0) != 0) {
            viewBinding.progressBar.visibility = View.GONE
            if (activity != null && context != null) {
                askRatings()
            } else {
                return
            }
        }
    }

    private fun onItemClicked(file: FileModel) {
        val filePath = getPath(requireContext(), file.imageUri)
        val mimeType = getMimeType(filePath.toUri())
        Log.d("FileMimeType", "Selected file MIME type: $mimeType")
        openFolder(filePath)
    }

    private fun openFolder(filePath: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(requireContext(), requireActivity().packageName + ".provider", file)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val mimeType = getMimeType(uri)
        intent.setDataAndType(uri, mimeType)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "No app found to handle this file type", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getMimeType(uri: Uri): String? {
        val contentResolver = requireContext().contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            contentResolver.getType(uri)
        } else {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))
        }
    }
    private fun askRatings() {
        val manager: ReviewManager = ReviewManagerFactory.create(requireContext())
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo: ReviewInfo = task.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener {

                }.addOnFailureListener {

                }
            }
        }
    }

    private fun handleBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
}