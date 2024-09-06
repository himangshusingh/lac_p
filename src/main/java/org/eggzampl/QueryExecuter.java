package org.eggzampl;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eggzampl.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QueryExecuter {

    public static List<DocumentData> performExactMatchQuery(String INDEX_DIR, String fieldName, String searchTerm) throws IOException, ParseException {
        List<DocumentData> matchingDocuments = new ArrayList<>();

        // Open the index directory
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader indexReader = DirectoryReader.open(directory);

        // Initialize the search
        IndexSearcher searcher = new IndexSearcher(indexReader);

        StandardAnalyzer analyzer = new StandardAnalyzer();
        QueryParser queryParser = new QueryParser(fieldName, analyzer);

        // Create an exact match query for the search term
        Query query = queryParser.parse('"' + searchTerm + '"');

        // Perform the search and retrieve matching documents
        TopDocs topDocs = searcher.search(query, Constants.MAX_SEARCH_RESULTS);

        ScoreDoc[] hits = topDocs.scoreDocs;
        for (ScoreDoc hit : hits) {
            int docId = hit.doc;
            Document document = searcher.doc(docId);
            String documentPath = document.get(Constants.PATH_FIELD);
            String snippet = getSnippetFromDocument(documentPath, searchTerm); // Get the snippet of the matching content
            DocumentData documentData = new DocumentData(document, snippet);
            matchingDocuments.add(documentData);
        }

        // Close the index reader and directory
        indexReader.close();
        directory.close();

        return matchingDocuments;
    }

    private static String getSnippetFromDocument(String documentPath, String searchTerm) {
        try (BufferedReader br = new BufferedReader(new FileReader(documentPath))) {
            String line;
            StringBuilder snippetBuilder = new StringBuilder();
            boolean isPreviousLine = false;

            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains(searchTerm.toLowerCase())) {
                    // Append the previous line (if available)
                    if (isPreviousLine) {
                        snippetBuilder.append(line).append('\n');
                    }

                    // Append the line containing the search term
                    snippetBuilder.append(line).append('\n');

                    // Append the next line (if available)
                    if ((line = br.readLine()) != null) {
                        snippetBuilder.append(line).append('\n');
                    }

                    // Mark that the previous line was added
                    isPreviousLine = true;
                } else {
                    // Reset the marker for previous line when search term is not found
                    isPreviousLine = false;
                }
            }

            return snippetBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error while extracting snippet from the document.";
        }
    }
}
