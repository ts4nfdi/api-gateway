package org.semantics.apigateway.service.search;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.NoArgsConstructor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class SearchLocalIndexerService {

    public static final String INDEXED_FIELD = "label";


    public List<Map<String, Object>> reIndexResults(String query, List<Map<String, Object>> combinedResults , Logger logger) throws IOException, ParseException {
        Directory index = indexResults(combinedResults);

        List<Map<String, Object>> localIndexedResult =  localIndexSearch(query, logger, index, INDEXED_FIELD);

        return localIndexedResult.stream().map(x -> combinedResults.stream().filter(y -> y.get("iri").equals(x.get("iri")) && y.get("backend_type").equals(x.get("backend_type")))
                .findFirst().orElse(null)).collect(Collectors.toList());
    }

    private static List<Map<String, Object>> localIndexSearch(String query, Logger logger, Directory index, String field) throws IOException {
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery.Builder mainQuery = new BooleanQuery.Builder();

        String[] terms = query.toLowerCase().split("\\s+");

        Query q = queryBuilder(field, terms, mainQuery);


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

    /*
        Define the local search result order/rank
     */
    private static Query queryBuilder(String field, String[] terms, BooleanQuery.Builder mainQuery) {
        // Match the full query as a phrase prefix
        PhraseQuery.Builder phraseQuery = new PhraseQuery.Builder();
        for (int i = 0; i < terms.length; i++) {
            phraseQuery.add(new Term(field, terms[i]), i);
        }
        PhraseQuery fullPhraseQuery = phraseQuery.build();
        mainQuery.add(new BoostQuery(fullPhraseQuery, 10), BooleanClause.Occur.SHOULD);

        // Match individual terms as prefixes
        if (terms.length > 0) {
            for (int i = 0; i < terms.length; i++) {
                Term term = new Term(field, terms[i]);
                PrefixQuery prefixQuery = new PrefixQuery(term);
                mainQuery.add(new BoostQuery(prefixQuery, Math.max(100-(i*10), 0)), BooleanClause.Occur.SHOULD);
            }
        }

        // Match individual terms as prefixes
        for (String term : terms) {
            PrefixQuery prefixQuery = new PrefixQuery(new Term(field, term));
            mainQuery.add(prefixQuery, BooleanClause.Occur.SHOULD);
        }
        return mainQuery.build();
    }

    private static Directory indexResults(List<Map<String, Object>> combinedResults) throws IOException {
        Directory index = new ByteBuffersDirectory();

        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

        IndexWriter w = new IndexWriter(index, config);


        for (Map<String, Object> result : combinedResults) {
            Document doc = new Document();
            doc.add(new StringField("id", result.get("iri").toString() + "_" + result.get("ontology").toString(), Field.Store.YES));
            result.forEach((key, value) -> doc.add(new TextField(key, String.valueOf(value), Field.Store.YES)));
            w.addDocument(doc);
        }

        w.close();
        return index;
    }
}