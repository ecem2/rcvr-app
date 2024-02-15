package com.adentech.rcvr.audio

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import com.adentech.rcvr.R
import com.adentech.rcvr.common.Constants
import com.adentech.rcvr.common.Constants.IS_DELETED
import com.adentech.rcvr.databinding.FragmentDeletedAudioBinding
import com.adentech.rcvr.decoration.SpacesItemDecoration
import com.adentech.rcvr.extensions.observe
import com.adentech.rcvr.fragments.BaseFragment
import com.adentech.rcvr.fullscreen.DeletedAudioActivity
import com.adentech.rcvr.home.ImagesActivity
import com.adentech.rcvr.model.FileLocation
import com.adentech.rcvr.model.FileModel
import com.adentech.rcvr.model.MainFileModel
import com.adentech.rcvr.scan.ScanViewModel
import com.adentech.rcvr.utils.FilesFetcher
import com.adentech.rcvr.utils.ImagesFilesCollector
import com.adentech.rcvr.utils.SearchAllFile
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DeletedAudioFragment : BaseFragment<ScanViewModel, FragmentDeletedAudioBinding>() {

    private val deletedAudioAdapter by lazy {
        DeletedAudioAdapter(
            context = requireContext().applicationContext,
            onItemClicked = ::onItemClicked
        )
    }
    private var myMotor: SearchAllFile? = null
    private var volums: ArrayList<String> = ArrayList()
    var audioUri: Uri? = null

    override fun getResourceLayoutId() = R.layout.fragment_deleted_audio

    override fun viewModelClass() = ScanViewModel::class.java

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onInitDataBinding() {
        initRecyclerView()
        handleBackPressed()
        setupBackButton()
        getDeletedAudioFilesFromMediaStore()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            viewModel.getAllTrashedFiles()
            observe(viewModel.allTrashedFilesList, ::getTrashedList)
        } else {
            getFileList()
        }
    }

    private fun initRecyclerView() {
        viewBinding.rvDeletedAudio.apply {
            adapter = deletedAudioAdapter
            layoutManager = GridLayoutManager(requireContext(), 1)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_2)
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
            setHasFixedSize(true)

        }
    }

    //    private fun getDeletedAudioFilesFromStorage(): List<FileModel> {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            getDeletedAudioFilesFromMediaStore()
//        } else {
//            getDeletedAudioFilesFromFileSystem()
//        }
//    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getDeletedAudioFilesFromMediaStore(): List<FileModel> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA
        )

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        var selection: String? = null
        var selectionArgs: Array<String>? = null

        // Sadece Android 10 ve üzeri sürümlerde is_pending sütununu kullan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.Audio.Media.IS_PENDING} = ?"
            selectionArgs = arrayOf("0")
        }

        val query = requireContext().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        val deletedAudioList = mutableListOf<FileModel>()

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val data = cursor.getString(dataColumn)

                val fileUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                Log.d("DeletedAudioFragment", "Filename: $displayName")

                val fileModel = FileModel(
                    fileName = displayName,
                    fileExtension = getFileExtension(displayName),
                    location = FileLocation.GALLERY,
                    isSelected = false,
                    creationDate = dateAdded,
                    fileSize = "",
                    isDeleted = false,
                    imageUri = fileUri,
                    videoUri= fileUri,
                    audioUri = fileUri,
                    isPreviewMode = false,
                    isRewarded = false
                )

                deletedAudioList.add(fileModel)
            }
        }

        return deletedAudioList
    }
    private fun getFileExtension(fileName: String): String? {
        val lastDotIndex = fileName.lastIndexOf(".")
        return if (lastDotIndex != -1) {
            fileName.substring(lastDotIndex + 1)
        } else {
            null
        }
    }
    private fun modifyFileNameWithMp3Extension(audioList: List<FileModel>): List<FileModel> {
        return audioList.map { audio ->
            val modifiedFileName = "${audio.fileName}.mp3"
            audio.copy(fileName = modifiedFileName)
        }
    }


    private fun getFileList() {
        volums = FilesFetcher(requireContext()).getStorageVolumes()
        if (volums.isNotEmpty()) {
            myMotor = SearchAllFile(volums, requireActivity(), requireContext())
            myMotor?.execute(*arrayOfNulls(0))
            deletedAudioAdapter.submitList(ImagesFilesCollector.foundAudioList)
        }
    }
    private fun setupBackButton() {
        viewBinding.backButton.setOnClickListener {
            val intent = Intent(requireContext(), ImagesActivity::class.java)
            startActivity(intent)
        }
    }
    private fun getTrashedList(resource: com.adentech.rcvr.common.Resource<MainFileModel>) {
        when (resource.status) {
            com.adentech.rcvr.common.Status.SUCCESS -> {
                if (resource.data?.audios?.size == 0 || resource.data?.audios.isNullOrEmpty()) {
                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
                } else {
                    viewBinding.llEmptyFolder.visibility = View.GONE
                    resource.data?.audios?.let { checkProgressBar(it) }
                    val modifiedList = resource.data?.audios?.map { audio ->
                        val modifiedFileName = "${audio.fileName}.mp3"
                        audio.copy(fileName = modifiedFileName)
                    }

                    deletedAudioAdapter.submitList(modifiedList)
                }
                viewBinding.progressBar.visibility = View.GONE
            }
            com.adentech.rcvr.common.Status.ERROR -> {
                viewBinding.progressBar.visibility = View.GONE
                viewBinding.llEmptyFolder.visibility = View.VISIBLE
            }

            com.adentech.rcvr.common.Status.LOADING -> {
                viewBinding.progressBar.visibility = View.VISIBLE
            }
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

    private fun onItemClicked(audio: FileModel) {
        navigateToDeletedAudioActivity(audio)
    }

    private fun navigateToDeletedAudioActivity(audio: FileModel) {
        val intent = Intent(requireContext().applicationContext, DeletedAudioActivity::class.java)
        intent.putExtra("audioUri", audio)
        startActivity(intent)
        intent.putExtra(Constants.AUDIO_PATH, audio)
        intent.putExtra(IS_DELETED, true)
        intent.putExtra(Constants.PREVIEW_MODE, true)
        startActivity(intent)
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