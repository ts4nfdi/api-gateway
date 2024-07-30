package org.semantics.apigateway.service;

import lombok.NoArgsConstructor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.queries.spans.SpanNearQuery;
import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.queries.spans.SpanTermQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@NoArgsConstructor
public class SearchLocalIndexerService {


    public List<Map<String, Object>> reIndexResults(String query, List<Map<String, Object>> combinedResults , Logger logger) throws IOException, ParseException {
        Directory index = new ByteBuffersDirectory();

        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

        IndexWriter w = new IndexWriter(index, config);


        for (Map<String, Object> result : combinedResults) {
            Document doc = new Document();
            doc.add(new StringField("id", result.get("iri").toString() + result.get("ontology").toString(), Field.Store.YES));
            result.forEach((key, value) -> doc.add(new TextField(key, String.valueOf(value), Field.Store.YES)));
            w.addDocument(doc);
        }

        w.close();

        String field = "label";

        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery.Builder mainQuery = new BooleanQuery.Builder();

        String[] terms = query.toLowerCase().split("\\s+");

        // Match the full query as a phrase prefix
        PhraseQuery.Builder phraseQuery = new PhraseQuery.Builder();
        for (int i = 0; i < terms.length; i++) {
            phraseQuery.add(new Term(field, terms[i]), i);
        }
        PhraseQuery fullPhraseQuery = phraseQuery.build();
        mainQuery.add(new BoostQuery(fullPhraseQuery, 10), BooleanClause.Occur.SHOULD);

        // Match the last term as a prefix at the end of the phrase
        if (terms.length > 0) {
            Term lastTerm = new Term(field, terms[terms.length - 1]);
            PrefixQuery prefixQuery = new PrefixQuery(lastTerm);
            SpanMultiTermQueryWrapper<PrefixQuery> spanPrefix = new SpanMultiTermQueryWrapper<>(prefixQuery);
            SpanNearQuery spanNearQuery = new SpanNearQuery(
                    new SpanQuery[]{new SpanTermQuery(new Term(field, terms[0])), spanPrefix},
                    terms.length - 1,
                    true
            );
            mainQuery.add(new BoostQuery(spanNearQuery, 5), BooleanClause.Occur.SHOULD);
        }

        // Match individual terms as prefixes
        for (String term : terms) {
            PrefixQuery prefixQuery = new PrefixQuery(new Term(field, term));
            mainQuery.add(prefixQuery, BooleanClause.Occur.SHOULD);
        }
        Query q = mainQuery.build();


        TopDocs resultsTopDocs = searcher.search(q, 100);

        List<Map<String, Object>> newResults = new ArrayList<>();
        for (ScoreDoc scoreDoc : resultsTopDocs.scoreDocs) {
            Document foundDoc = searcher.doc(scoreDoc.doc);
            Map<String, Object> newMap = new HashMap<>();
            foundDoc.forEach(r -> newMap.put(r.name(), r.stringValue()));
            newResults.add(newMap);
            logger.info("Score of: {} is {}", newMap.get("label"), scoreDoc.score);
        }
        reader.close();

        return newResults;
    }
}