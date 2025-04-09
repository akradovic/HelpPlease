// service/FileParserService.kt
package com.example.helpplease.service

import android.content.Context
import android.net.Uri
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Service responsible for parsing different document formats and extracting text content.
 * Supports PDF, DOCX, and TXT files with format preservation.
 */
class FileParserService(private val context: Context) {

    /**
     * Represents both raw text and formatted text for a document
     */
    data class ParsedDocument(
        val rawText: String,         // Plain text for processing
        val formattedText: String,   // Text with formatting preserved
        val originalFormat: String   // Original MIME type
    )

    /**
     * Parse file from URI and extract text content based on file type
     * @param uri The URI of the file to parse
     * @param mimeType The MIME type of the file (application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document, text/plain)
     * @return Extracted text content as ParsedDocument containing both raw and formatted text
     */
    suspend fun parseFile(uri: Uri, mimeType: String): Result<ParsedDocument> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Failed to open file"))

            val parsedDocument = when (mimeType) {
                "application/pdf" -> extractPdfTextWithFormatting(inputStream, mimeType)
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> extractDocxTextWithFormatting(inputStream, mimeType)
                "text/plain" -> extractTxtTextWithFormatting(inputStream, mimeType)
                else -> return@withContext Result.failure(Exception("Unsupported file type: $mimeType"))
            }

            inputStream.close()
            Result.success(parsedDocument)
        } catch (e: Exception) {
            Result.failure(Exception("Error parsing file: ${e.message}", e))
        }
    }

    /**
     * Extract text from PDF file with basic formatting preservation
     */
    private fun extractPdfTextWithFormatting(inputStream: InputStream, mimeType: String): ParsedDocument {
        val reader = PdfReader(inputStream)
        val pdfDocument = PdfDocument(reader)
        val rawTextBuilder = StringBuilder()
        val formattedTextBuilder = StringBuilder()

        val numberOfPages = pdfDocument.numberOfPages
        for (i in 1..numberOfPages) {
            val page = pdfDocument.getPage(i)

            // Extract with location strategy for better paragraph detection
            val locationStrategy = LocationTextExtractionStrategy()
            val text = PdfTextExtractor.getTextFromPage(page, locationStrategy)

            // Add to raw text
            rawTextBuilder.append(text).append("\n")

            // Add to formatted text with paragraph preservation
            val paragraphs = text.split("\n\n")
            for (paragraph in paragraphs) {
                if (paragraph.trim().isNotEmpty()) {
                    formattedTextBuilder.append("<p>").append(paragraph.trim()).append("</p>\n")
                }
            }

            // Add page break marker for multi-page PDFs
            if (i < numberOfPages) {
                formattedTextBuilder.append("<div class=\"page-break\"></div>\n")
            }
        }

        reader.close()
        pdfDocument.close()

        return ParsedDocument(
            rawText = rawTextBuilder.toString().trim(),
            formattedText = formattedTextBuilder.toString().trim(),
            originalFormat = mimeType
        )
    }

    /**
     * Extract text from DOCX file with formatting preservation
     */
    private fun extractDocxTextWithFormatting(inputStream: InputStream, mimeType: String): ParsedDocument {
        val document = XWPFDocument(inputStream)
        val wordExtractor = XWPFWordExtractor(document)
        val rawText = wordExtractor.text

        // Process document to preserve formatting
        val formattedTextBuilder = StringBuilder()

        // Process paragraphs with spacing
        for (paragraph in document.paragraphs) {
            if (paragraph.text.trim().isNotEmpty()) {
                formattedTextBuilder.append("<p")

                // Handle alignment using ParagraphAlignment enum
                when (paragraph.alignment) {
                    org.apache.poi.xwpf.usermodel.ParagraphAlignment.RIGHT ->
                        formattedTextBuilder.append(" class=\"text-right\"")
                    org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER ->
                        formattedTextBuilder.append(" class=\"text-center\"")
                    else -> { /* Default left alignment, no special class needed */ }
                }

                formattedTextBuilder.append(">")
                formattedTextBuilder.append(paragraph.text.trim())
                formattedTextBuilder.append("</p>\n")
            } else {
                // Empty paragraph (spacing)
                formattedTextBuilder.append("<p>&nbsp;</p>\n")
            }
        }

        wordExtractor.close()
        document.close()

        return ParsedDocument(
            rawText = rawText.trim(),
            formattedText = formattedTextBuilder.toString().trim(),
            originalFormat = mimeType
        )
    }

    /**
     * Extract text from TXT file with basic formatting preservation
     */
    private fun extractTxtTextWithFormatting(inputStream: InputStream, mimeType: String): ParsedDocument {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val rawTextBuilder = StringBuilder()
        val formattedTextBuilder = StringBuilder()

        var line: String?
        var inParagraph = false

        while (reader.readLine().also { line = it } != null) {
            // Add to raw text
            rawTextBuilder.append(line).append("\n")

            // Add to formatted text with paragraph preservation
            if (line?.trim()?.isEmpty() == true) {
                if (inParagraph) {
                    formattedTextBuilder.append("</p>\n")
                    inParagraph = false
                }
                // Add spacing
                formattedTextBuilder.append("<p>&nbsp;</p>\n")
            } else {
                if (!inParagraph) {
                    formattedTextBuilder.append("<p>")
                    inParagraph = true
                } else {
                    formattedTextBuilder.append(" ")
                }
                formattedTextBuilder.append(line?.trim())
            }
        }

        // Close last paragraph if needed
        if (inParagraph) {
            formattedTextBuilder.append("</p>\n")
        }

        reader.close()

        return ParsedDocument(
            rawText = rawTextBuilder.toString().trim(),
            formattedText = formattedTextBuilder.toString().trim(),
            originalFormat = mimeType
        )
    }
}