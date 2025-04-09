// service/FileExportService.kt
package com.example.helpplease.service

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Service responsible for exporting processed documents to the device
 * Supports PDF, DOCX, and TXT formats
 */
class FileExportService(private val context: Context) {

    companion object {
        private const val FILE_PREFIX = "rehumanized_"
    }

    /**
     * Export document to device storage
     * @param processedText The text to save in the document
     * @param originalFormat The MIME type to save the document as
     * @param fileName Optional base filename (without extension)
     * @return Uri of the saved file
     */
    suspend fun exportDocument(
        processedText: String,
        originalFormat: String,
        fileName: String? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            // Generate base filename if not provided
            val baseFileName = fileName ?: generateFileName()

            // Export based on format
            val uri = when (originalFormat) {
                "application/pdf" -> exportAsPdf(processedText, baseFileName)
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    exportAsDocx(processedText, baseFileName)
                "text/plain" -> exportAsTxt(processedText, baseFileName)
                else -> exportAsTxt(processedText, baseFileName) // Default to TXT
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(Exception("Error exporting document: ${e.message}", e))
        }
    }

    /**
     * Export document as PDF
     */
    private fun exportAsPdf(processedText: String, baseFileName: String): Uri {
        val fileName = "$baseFileName.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        ) ?: throw Exception("Failed to create new MediaStore record")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            // Create PDF document
            val pdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Split by paragraphs and add to document
            val paragraphs = processedText.split("<p>")
            for (para in paragraphs) {
                if (para.trim().isNotEmpty()) {
                    // Remove any HTML tags in the paragraph
                    val cleanText = para.replace(Regex("<.*?>"), "").trim()
                    if (cleanText.isNotEmpty()) {
                        document.add(Paragraph(cleanText))
                    }
                }
            }

            document.close()
        } ?: throw Exception("Failed to open output stream")

        return uri
    }

    /**
     * Export document as DOCX
     */
    private fun exportAsDocx(processedText: String, baseFileName: String): Uri {
        val fileName = "$baseFileName.docx"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        ) ?: throw Exception("Failed to create new MediaStore record")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            // Create DOCX document
            val document = XWPFDocument()

            // Split by paragraphs and add to document
            val paragraphs = processedText.split("<p>")
            for (para in paragraphs) {
                if (para.trim().isNotEmpty()) {
                    // Remove any HTML tags in the paragraph
                    val cleanText = para.replace(Regex("<.*?>"), "").trim()
                    if (cleanText.isNotEmpty()) {
                        val paragraph: XWPFParagraph = document.createParagraph()
                        val run: XWPFRun = paragraph.createRun()
                        run.setText(cleanText)
                    }
                }
            }

            document.write(outputStream)
            document.close()
        } ?: throw Exception("Failed to open output stream")

        return uri
    }

    /**
     * Export document as TXT
     */
    private fun exportAsTxt(processedText: String, baseFileName: String): Uri {
        val fileName = "$baseFileName.txt"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        ) ?: throw Exception("Failed to create new MediaStore record")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                // Remove HTML tags and write text
                val cleanText = processedText.replace(Regex("<.*?>"), "")
                writer.write(cleanText)
            }
        } ?: throw Exception("Failed to open output stream")

        return uri
    }

    /**
     * Generate a unique filename based on current timestamp
     */
    private fun generateFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${FILE_PREFIX}${timestamp}"
    }
}