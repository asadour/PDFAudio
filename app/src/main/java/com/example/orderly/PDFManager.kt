package com.example.orderly

import android.content.Context
import android.net.Uri
import android.util.Log
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.codec.Base64
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.io.File

class PDFManager(pdfFile: Uri, ctx: Context) {
    private var pdfReader: PdfReader = PdfReader(ctx.contentResolver.openInputStream(pdfFile))

    fun getTextFromEachPage(): List<String> {
        val pagesTexts = mutableListOf<String>()
        val n = pdfReader.numberOfPages
        for (i in 0 until n) {
            val textPage = PdfTextExtractor.getTextFromPage(pdfReader, i + 1).trim { it <= ' ' }
            Log.d("TEXT", textPage)
            pagesTexts.add(textPage.trim())
        }
        return pagesTexts.toList()
    }

}