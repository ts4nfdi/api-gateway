package org.semantics.apigateway.artefacts.search;

import lombok.NoArgsConstructor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.spans.SpanFirstQuery;
import org.apache.lucene.queries.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import org.apache.commons.text.similarity.CosineSimilarity;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class SearchLocalIndexerService {

    public static final String INDEXED_FIELD = "label";

    public List<Map<String, Object>> sortByCosineSimilarity(String query, List<Map<String, Object>> results) {
        CosineSimilarity cosineSimilarity = new CosineSimilarity();
        Map<CharSequence, Integer> queryVector = toCharacterVector(query.toLowerCase());

        return results.stream()
                .sorted((a, b) -> {
                    String labelA = a.get("label") != null ? a.get("label").toString().toLowerCase() : "";
                    String labelB = b.get("label") != null ? b.get("label").toString().toLowerCase() : "";

                    Map<CharSequence, Integer> vectorA = toCharacterVector(labelA);
                    Map<CharSequence, Integer> vectorB = toCharacterVector(labelB);

                    double scoreA = cosineSimilarity.cosineSimilarity(queryVector, vectorA);
                    double scoreB = cosineSimilarity.cosineSimilarity(queryVector, vectorB);

                    return Double.compare(scoreB, scoreA);
                })
                .collect(Collectors.toList());
    }

    private Map<CharSequence, Integer> toCharacterVector(String text) {
        Map<CharSequence, Integer> vector = new HashMap<>();
        for (char c : text.toCharArray()) {
            String key = String.valueOf(c);
            vector.put(key, vector.getOrDefault(key, 0) + 1);
        }
        return vector;
    }


    public List<Map<String, Object>> reIndexResults(String query, List<Map<String, Object>> combinedResults, Logger logger) throws IOException, ParseException {
        Directory index = indexResults(combinedResults);

        List<Map<String, Object>> localIndexedResult = localIndexSearch(query, logger, index, INDEXED_FIELD);

        return localIndexedResult.stream().map(x ->
                combinedResults.stream().filter(y -> y.get("iri").equals(x.get("iri")) && y.get("backend_type").equals(x.get("backend_type")))
                .findFirst().orElse(null)).collect(Collectors.toList());
    }

    private static List<Map<String, Object>> localIndexSearch(String query, Logger logger, Directory index, String field) throws IOException {
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery.Builder mainQuery = new BooleanQuery.Builder();

        String[] terms = query.toLowerCase().split("\\s+");

        Query q = queryBuilder(field, terms, mainQuery);


        TopDocs resultsTopDocs = searcher.search(q, Math.max(reader.numDocs(), 1));


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
        if (terms.length == 0) {
            return mainQuery.build();
        }

        // Store original query terms exactly as entered
        String[] queryTerms = terms.clone();
        // Get lowercase versions for case-insensitive matching
        String[] lowerTerms = Arrays.stream(terms).map(String::toLowerCase).toArray(String[]::new);

        // 1. Exact match of query term at start (highest priority)
        Term exactQueryTerm = new Term(field, queryTerms[0]);
        PrefixQuery exactQueryPrefix = new PrefixQuery(exactQueryTerm);
        SpanQuery exactQuerySpan = new SpanMultiTermQueryWrapper<>(exactQueryPrefix);
        SpanFirstQuery exactQueryFirst = new SpanFirstQuery(exactQuerySpan, 1);
        mainQuery.add(new BoostQuery(exactQueryFirst, 200), BooleanClause.Occur.SHOULD);

        // 2. Exact match of query term anywhere
        TermQuery exactTermQuery = new TermQuery(exactQueryTerm);
        mainQuery.add(new BoostQuery(exactTermQuery, 150), BooleanClause.Occur.SHOULD);

        // 3. Case-insensitive prefix match at start
        Term lowerTerm = new Term(field + ".lowercase", lowerTerms[0]);
        PrefixQuery lowerPrefix = new PrefixQuery(lowerTerm);
        SpanQuery lowerSpan = new SpanMultiTermQueryWrapper<>(lowerPrefix);
        SpanFirstQuery lowerFirst = new SpanFirstQuery(lowerSpan, 1);
        mainQuery.add(new BoostQuery(lowerFirst, 50), BooleanClause.Occur.SHOULD);

        if (terms.length > 1) {
            // 4. Exact phrase matches with query case
            PhraseQuery.Builder phraseQuery = new PhraseQuery.Builder();
            for (int i = 0; i < terms.length; i++) {
                phraseQuery.add(new Term(field, queryTerms[i]), i);
            }
            mainQuery.add(new BoostQuery(phraseQuery.build(), 100), BooleanClause.Occur.SHOULD);

            // 5. Case-insensitive phrase matches
            PhraseQuery.Builder phraseLowerQuery = new PhraseQuery.Builder();
            for (int i = 0; i < terms.length; i++) {
                phraseLowerQuery.add(new Term(field + ".lowercase", lowerTerms[i]), i);
            }
            mainQuery.add(new BoostQuery(phraseLowerQuery.build(), 40), BooleanClause.Occur.SHOULD);
        }

        // 6. Individual term matches
        for (int i = 0; i < terms.length; i++) {
            // Exact case match of query terms
            TermQuery termQuery = new TermQuery(new Term(field, queryTerms[i]));
            mainQuery.add(new BoostQuery(termQuery, Math.max(30 - (i * 5), 10)), BooleanClause.Occur.SHOULD);

            // Case-insensitive matches
            TermQuery termLowerQuery = new TermQuery(new Term(field + ".lowercase", lowerTerms[i]));
            mainQuery.add(new BoostQuery(termLowerQuery, Math.max(20 - (i * 5), 5)), BooleanClause.Occur.SHOULD);
        }

        return mainQuery.build();
    }

    private static Directory indexResults(List<Map<String, Object>> combinedResults) throws IOException {
        Directory index = new ByteBuffersDirectory();

        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

        IndexWriter w = new IndexWriter(index, config);


        for (Map<String, Object> result : combinedResults) {
            Document doc = new Document();
            String iri = (String) result.get("iri");
            String ontology = (String) result.getOrDefault("ontology", "");
            doc.add(new StringField("id", iri + "_" + ontology, Field.Store.YES));
            result.forEach((key, value) -> {
                doc.add(new TextField(key, String.valueOf(value), Field.Store.YES));
                doc.add(new TextField(key + ".lowercase", String.valueOf(value).toLowerCase(), Field.Store.YES));
            });
            w.addDocument(doc);
        }

        w.close();
        return index;
    }
}
