package com.example.orderly

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rajat.pdfviewer.PdfRendererView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() , TextToSpeech.OnInitListener{
    private lateinit var vm: MainViewModel
    private var tts: TextToSpeech? = null

    private lateinit var pdfView: PdfRendererView
    private lateinit var openPdf: Button
    private lateinit var savePdf: Button
    private lateinit var speak: Button

    private val launcher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { fileUri ->
            pdfView.initWithUri(fileUri)
            vm.updatePDFFile(MainViewModel.PDFFile(fileUri))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        openPdf = findViewById(R.id.openPDF)
        savePdf = findViewById(R.id.savePDF)
        pdfView = findViewById(R.id.pdfview)
        speak = findViewById(R.id.speechText)

        vm = ViewModelProvider(this)[MainViewModel::class.java]
        setupObservers()
        setupListeners()

        tts = TextToSpeech(this, this)
    }


    private fun setupObservers() {
        vm.getPDFFile().observe(this) { pdfFile ->
            if (pdfFile != null) {
                pdfView.initWithUri(pdfFile.fileuri)
                val page = vm.getPDFCurPage()
                if (page != -1) {
                    pdfView.jumpToPage(vm.getPDFCurPage())
                }
            }
        }
    }

    private fun setupListeners() {
        pdfView.statusListener = object : PdfRendererView.StatusCallBack {
            override fun onPdfLoadStart() {
                Log.i("statusCallBack","onPdfLoadStart")
            }
            override fun onPdfLoadProgress(
                progress: Int,
                downloadedBytes: Long,
                totalBytes: Long?
            ) {}

            override fun onPdfLoadSuccess(absolutePath: String) {
                Log.i("statusCallBack","onPdfLoadSuccess")
            }

            override fun onError(error: Throwable) {
                Log.i("statusCallBack","onError")
            }

            override fun onPageChanged(currentPage: Int, totalPage: Int) {
                vm.updatePDFCurPage(currentPage)
            }
        }

        openPdf.setOnClickListener {
            startLauncher()
        }

        speak.setOnClickListener {
            speakOut()
        }

    }

    private fun startLauncher(){
        launcher.launch(arrayOf("application/pdf"))
    }

    private fun speakOutText(text: String, pageIndex: Int) {
        tts!!.speak(text, TextToSpeech.QUEUE_ADD, null, "MyUniqueUtteranceId")
        if (pageIndex != 0) {
            vm.viewModelScope.launch {
                delay(60000)
                pdfView.jumpToPage(pageIndex)
            }
        }
    }

    private fun speakOut() {
        val uriFile = vm.getPDFFile().value?.fileuri
        if (uriFile != null) {
            val pdfMg = PDFManager(uriFile, this.application.applicationContext)
            val texts = pdfMg.getTextFromEachPage()
            pdfView.jumpToPage(0)
            for ((index, value) in texts.withIndex())
            {
                speakOutText(value, index)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            speak.isEnabled = false
            val result = tts!!.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {
                speak.isEnabled = true
            }
        }
    }

    public override fun onDestroy() {
        // Shutdown TTS when
        // activity is destroyed
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}
