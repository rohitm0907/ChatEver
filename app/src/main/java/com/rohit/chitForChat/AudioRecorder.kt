package com.rohit.chitForChat

import android.media.MediaRecorder
import java.io.IOException

class AudioRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private fun initMediaRecorder() {
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
    }

    @Throws(IOException::class)
    fun start(filePath: String?) {
        if (mediaRecorder == null) {
            initMediaRecorder()
        }
        mediaRecorder!!.setOutputFile(filePath)
        mediaRecorder!!.prepare()
        mediaRecorder!!.start()
    }

    fun stop() {
        try {
            mediaRecorder!!.stop()
            mediaRecorder!!.release()
            destroyMediaRecorder()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun destroyMediaRecorder() {
        mediaRecorder!!.release()
        mediaRecorder = null
    }
}