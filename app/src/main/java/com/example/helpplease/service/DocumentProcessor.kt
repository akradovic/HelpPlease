// service/DocumentProcessor.kt
package com.example.helpplease.service

import com.example.helpplease.model.DocumentProcessingRequest
import com.example.helpplease.model.DocumentProcessingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentProcessor {

    // This function handles the text processing in a background thread
    suspend fun processDocument(request: DocumentProcessingRequest): DocumentProcessingResult =
        withContext(Dispatchers.Default) {
            // The original text for comparison
            val originalText = request.documentText
            var processedText = originalText

            // Track replacements for reporting back to user
            val replacementCount = mutableMapOf<String, Int>()

            // Process each term selected for replacement
            for (term in request.selectedTermsToReplace) {
                // Create regex for case-insensitive replacement
                // This will match the term regardless of capitalization
                val regex = Regex(term, RegexOption.IGNORE_CASE)

                // Count matches before replacement
                val matches = regex.findAll(processedText).count()
                if (matches > 0) {
                    replacementCount[term] = matches

                    // Replace the term with the person's name
                    processedText = regex.replace(processedText, request.personName)
                }
            }

            // Return the result object with original, processed text and stats
            DocumentProcessingResult(
                originalText = originalText,
                processedText = processedText,
                replacementsMade = replacementCount
            )
        }
}