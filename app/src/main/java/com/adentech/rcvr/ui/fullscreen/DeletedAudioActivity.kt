package com.adentech.rcvr.ui.fullscreen


import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.adentech.rcvr.R
import com.adentech.rcvr.core.common.Constants
import com.adentech.rcvr.core.common.Constants.DATE_FORMAT
import com.adentech.rcvr.core.common.Constants.DCIM_PATH
import com.adentech.rcvr.core.common.Constants.MP3_EXTENSION
import com.adentech.rcvr.core.activities.BaseActivity
import com.adentech.rcvr.data.model.FileModel
import com.adentech.rcvr.databinding.ActivityDeletedAudioBinding
import com.adentech.rcvr.databinding.DialogRecoverAudioBinding
import com.adentech.rcvr.extensions.parcelable
import com.adentech.rcvr.ui.audio.DeletedAudioFragment
import com.adentech.rcvr.scan.ScanViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

@AndroidEntryPoint
class DeletedAudioActivity() : BaseActivity<ScanViewModel, ActivityDeletedAudioBinding>() {

    private var audioClicked: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    private var recoverDialog: Dialog? = null
    private lateinit var audioFile: FileModel
    private var audioUri: Uri? = null
    private var isPlaying: Boolean = false
    private var currentPosition: Int = 0
    var currentSong: Uri? = null
    private var mp: MediaPlayer? = null


    override fun viewModelClass() = ScanViewModel::class.java

    override fun viewDataBindingClass() = ActivityDeletedAudioBinding::class.java

    override fun onInitDataBinding() {
        viewBinding.buttonRestore.isEnabled = true
        mediaPlayer = MediaPlayer()
        viewBinding.icPlay.setOnClickListener {
            playPauseAudio()
        }

        viewBinding.icPause.setOnClickListener {
            playPauseAudio()
        }
        viewBinding.ivBackButton.setOnClickListener {
            navigateToFragment()
            viewBinding.clToolbar.visibility = View.GONE
            viewBinding.cardAudio.visibility = View.GONE
            viewBinding.buttonRestore.visibility = View.GONE
        }

        // Add a null check for audioFile
        if (intent.hasExtra(Constants.AUDIO_PATH)) {
            audioFile = intent.parcelable(Constants.AUDIO_PATH)!!
            getAudioData()

            if (audioFile.audioUri != null && isFileUri(audioFile.audioUri!!)) {
                currentSong = audioFile.audioUri

                // playContentUri(audioFile.audioUri!!)
            }
        }
        initSeekBar()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        viewBinding.clDeletedContainer.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        clickListeners()

    }
    private fun playPauseAudio() {
        if (isPlaying) {
            pauseAudio()
        } else {
            playAudio()
        }
    }

    private fun playAudio() {
        mediaPlayer?.start()
        isPlaying = true
        setupUI()
        updateSeekBar()
    }
    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        currentPosition = mediaPlayer?.currentPosition ?: 0
    }


    private fun handleBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this@DeletedAudioActivity, callback)
    }

    private fun updateSeekBar() {
        viewBinding.seekBar.max = mediaPlayer?.duration ?: 0
        val updateHandler = Handler()
        updateHandler.postDelayed(object : Runnable {
            override fun run() {
                viewBinding.seekBar.progress = mediaPlayer?.currentPosition ?: 0
                updateHandler.postDelayed(this, 1000) // Update every second
            }
        }, 1000)
    }
    private fun isFileUri(uri: Uri): Boolean {
        val scheme = uri.scheme
        return if (ContentResolver.SCHEME_CONTENT == scheme) {
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
            val cursor = contentResolver.query(uri, projection, null, null, null)
            val isFile = cursor != null
            cursor?.close()
            isFile
        } else ContentResolver.SCHEME_FILE == scheme
    }
    private fun getAudioData() {
        audioFile = intent.parcelable(Constants.AUDIO_PATH)!!
        val creationDate = audioFile.creationDate?.let { Date(it) }
        val format = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val formattedDate = creationDate?.let { format.format(it) }
        viewBinding.tvAudioInfo.text = formattedDate.toString()
    }
    private fun clickListeners() {
        viewBinding.apply {
            buttonRestore.setOnClickListener {
                launchRecoverProcess()
            }


            icStopMusic.setOnClickListener {
                stopAndResetAudio()            }
        }
    }

    private fun navigateToFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = DeletedAudioFragment()
        fragmentTransaction.replace(R.id.cl_deleted_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun setupUI() {
        mediaPlayer?.setOnPreparedListener { player ->
            player.start()
        }

        try {
            audioFile.audioUri?.let {
                mediaPlayer?.setDataSource(this, it)
                mediaPlayer?.prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun playContentUri(uri: Uri) {
        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(application, uri)
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    private fun launchRecoverProcess() {
        val singleThreadedExecutor = Executors.newSingleThreadExecutor()
        singleThreadedExecutor.execute {
            runOnUiThread {
                onBtnSaveAudio()
                showRecoverPopup()
            }
        }
    }

    private fun showRecoverPopup() {
        val dialogBuilder = Dialog(this@DeletedAudioActivity, R.style.CustomDialog)
        val dialogBinding = DialogRecoverAudioBinding.inflate(layoutInflater)
        dialogBuilder.setContentView(dialogBinding.root)
        dialogBuilder.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.apply {
            checkButton.setOnClickListener {
                openFolder()
                dialogBuilder.cancel()
                finish()
            }

            cancelButton.setOnClickListener {
                recoverDialog?.dismiss()
            }
        }

        recoverDialog?.show()
    }


    private fun openSavedAudioFile(uri: Uri) {
        val openIntent = Intent(Intent.ACTION_VIEW)
        openIntent.setDataAndType(uri, "audio/*")
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(openIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this@DeletedAudioActivity,
                "No app found to open the audio file",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onBtnSaveAudio() {
        try {
            val fileName: String = System.currentTimeMillis().toString() + MP3_EXTENSION
            val values = ContentValues()

            values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, DCIM_PATH)
            } else {
                val directory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                values.put(MediaStore.MediaColumns.DATA, File(directory, fileName).absolutePath)
            }

            val uri =
                contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            contentResolver.openOutputStream(uri!!).use { output ->
                val inputStream =
                    audioFile.audioUri?.let { contentResolver.openInputStream(it) }

                if (inputStream != null) {
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        output?.write(buffer, 0, read)
                    }
                    inputStream.close()
                } else {
                    viewBinding.buttonRestore.isClickable = false
                    viewBinding.buttonRestore.isEnabled = false
                }
            }

            openSavedAudioFile(uri)

        } catch (e: IOException) {

        }
    }
    private fun stopAndResetAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            reset()
            setDataSource(this@DeletedAudioActivity, currentSong!!)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            prepareAsync()
            setOnPreparedListener {
                start()
            }
        }
    }

    private fun initSeekBar() {
        viewBinding.seekBar.max = mediaPlayer?.duration ?: 0
        viewBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Kullanıcı seekbar'a dokunduğunda yapılacak işlemler
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Kullanıcı seekbar'dan elini çektiğinde yapılacak işlemler
            }
        })
        if (mp != null) {
            viewBinding.seekBar.max = mp!!.duration

            val handler = Handler()
            handler.postDelayed(object : Runnable {
                override fun run() {
                    try {
                        viewBinding.seekBar.progress = mp!!.currentPosition
                        handler.postDelayed(this, 1000)
                    } catch (e: Exception) {
                        viewBinding.seekBar.progress = 0
                    }
                }

            }, 0)
        } else {
            // mp null ise, gerekli işlemleri yapabilir veya hata mesajı verebilirsiniz.
        }
    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val uri = Uri.parse(
            StringBuilder(Environment.getExternalStorageDirectory().path)
                .append(Constants.DCIM_PATH)
                .toString()
        )
        intent.setDataAndType(uri, "*/*")
        startActivity(Intent.createChooser(intent, "Open folder"))
    }

    override fun onStart() {
        super.onStart()
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, audioFile.audioUri)
            mediaPlayer!!.isLooping = true
            mediaPlayer!!.prepareAsync()
            mediaPlayer!!.setOnPreparedListener {
                mediaPlayer!!.start()
            }
        } else mediaPlayer!!.start()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.apply {
            if (!isPlaying) {
                start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopAudio()
    }

    private fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
    }

}