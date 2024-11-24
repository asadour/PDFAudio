package com.example.orderly

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    data class PDFFile(val fileuri: Uri)

    private val pdfFile_ = MutableLiveData<PDFFile>(null)
    val pdfFile = pdfFile_

    private val pdfFileCurPage_ = MutableLiveData(-1)
    val pdfFileCurPage = pdfFileCurPage_

    fun updatePDFFile(pdf: PDFFile) {
        pdfFile_.postValue(pdf)
    }

    fun getPDFFile(): LiveData<PDFFile> {
        return pdfFile
    }

    fun updatePDFCurPage(pdfPage: Int) {
        pdfFileCurPage_.postValue(pdfPage)
    }

    fun getPDFCurPage(): Int {
        return pdfFileCurPage.value ?: -1
    }


}