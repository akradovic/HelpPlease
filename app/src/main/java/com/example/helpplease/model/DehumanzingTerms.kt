// model/DehumanizingTerms.kt
package com.example.helpplease.model

// This class contains the base list of dehumanizing terms to be replaced
object DehumanizingTerms {
    // The map stores the term and a brief explanation of why it's dehumanizing
    val commonTerms = mapOf(
        "the accused" to "Legal term that reduces a person to their accusation",
        "the respondent" to "Removes personal identity in legal proceedings",
        "the defendant" to "Defines person solely by their role in legal proceedings",
        "the perpetrator" to "Assumes guilt and reduces person to alleged actions",
        "the aggressor" to "Labels person negatively without context",
        "the abuser" to "Defines person by alleged negative actions",
        "alleged offender" to "Combines accusation with negative label",
        "the appellant" to "Legal jargon that depersonalizes",
        "criminal defendant" to "Combines 'criminal' label with legal role",
        "suspect" to "Focuses only on suspicion status",
        "offender" to "Assumes guilt without legal determination",
        "culprit" to "Assumes responsibility before legal determination",
        "the prisoner" to "Reduces person to their incarceration status"
    )
}

// Document processing model classes
data class DocumentProcessingRequest(
    val documentText: String,
    val personName: String,
    val selectedTermsToReplace: List<String> = DehumanizingTerms.commonTerms.keys.toList()
)

data class DocumentProcessingResult(
    val originalText: String,
    val processedText: String,
    val replacementsMade: Map<String, Int> // Term -> Count of replacements
)