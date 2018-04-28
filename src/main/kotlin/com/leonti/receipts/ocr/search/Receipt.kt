package com.leonti.receipts.ocr.search

data class Receipt(val content: String)

data class SearchResult(val ids: List<String>)