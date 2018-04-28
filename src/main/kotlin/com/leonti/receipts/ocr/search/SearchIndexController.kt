package com.leonti.receipts.ocr.search

import com.google.api.client.util.IOUtils
import com.google.api.services.storage.Storage
import com.google.appengine.api.search.*
import com.google.appengine.repackaged.org.antlr.runtime.CharStream
import com.google.appengine.tools.cloudstorage.*
import org.springframework.web.bind.annotation.*
import java.util.*
import com.google.common.io.CharStreams
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.channels.Channels


@RestController
@RequestMapping("api")
class SearchIndexController {

    //https://stackoverflow.com/questions/38960703/how-to-escape-special-operators-symbols-in-google-app-engine-search-without-requ
    @GetMapping("search/{userId}")
    fun search(@PathVariable userId: String, @RequestParam("q") query: String): SearchResult {
        val sanitized = query.toLowerCase()
        val result = getIndex().search("userId = $userId AND content = ($sanitized)")
        return SearchResult(result.results.map { doc -> doc.id })
    }

    @PostMapping("search/{userId}/{receiptId}")
    fun search(@PathVariable userId: String, @PathVariable receiptId: String, @RequestBody receipt: Receipt) {

        val document = Document.newBuilder()
                .setId(receiptId)
                .addField(Field.newBuilder().setName("userId").setAtom(userId))
                .addField(Field.newBuilder().setName("content").setText(receipt.content))
                .addField(Field.newBuilder().setName("published").setDate(Date()))
                .build()
        getIndex().put(document)
    }

    private fun getIndex(): Index {
        val indexSpec = IndexSpec.newBuilder().setName("receipts").build()
        return SearchServiceFactory.getSearchService().getIndex(indexSpec)
    }

}
