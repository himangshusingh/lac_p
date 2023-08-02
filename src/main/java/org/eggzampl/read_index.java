package org.eggzampl;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.attribute.UserPrincipal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

public class read_index {
    static void Main(String searchTerm, String INDEX_DIR) throws IOException, ParseException {

        // Field to search on
        String fieldName = "contents";

        List<Document> matchingDocuments = performExactMatchQuery(INDEX_DIR, fieldName, searchTerm);

        System.out.println("Search Term: " + searchTerm);
        System.out.println("Field Name: " + fieldName);
        System.out.println("Number of Hits: " + matchingDocuments.size());

        // Process and display the matching documents
        for (Document doc : matchingDocuments) {
            System.out.println("\n\nDocument Path:      " + doc.get("path"));// + ", Content: "+ doc.get(fieldName));

            Path file = Paths.get(doc.get("path"));

            {
                try {
//  Access time snippet
//                  Path file = Paths.get(fileName);
                    BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                    System.out.println("creationTime:       " + attr.creationTime());
                    System.out.println("lastAccessTime:     " + attr.lastAccessTime());
                    System.out.println("lastModifiedTime:   " + attr.lastModifiedTime());

//  Owner Name Snippet
                    FileOwnerAttributeView path = Files.getFileAttributeView(file, FileOwnerAttributeView.class);

                    UserPrincipal user = path.getOwner();                           // Taking owner name from the file
                    System.out.println("Owner:              " + user.getName());    // Printing the owner's name

                } catch (IOException e) {
                    System.out.println("Error while Displaying Time !");
                }
            }
        }
    }

    private static List<Document> performExactMatchQuery(String INDEX_DIR, String fieldName, String searchTerm) throws IOException, ParseException {
        List<Document> matchingDocuments = new ArrayList<>();

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
        int maxHits = 10; // Maximum Number of search results to retrieve
        TopDocs topDocs = searcher.search(query, maxHits);
        ScoreDoc[] hits = topDocs.scoreDocs;
        for (ScoreDoc hit : hits) {
            int docId = hit.doc;
            Document document = searcher.doc(docId);
            matchingDocuments.add(document);
        }

        // matchingdocument contains all the content of the matched file

//        System.out.println("Printing MATCHINGDOCUMENTS \n\n"+matchingDocuments+"\n\nPRINTED MATCHING DOCUMENTS");

        // Close the index reader and directory
        indexReader.close();
        directory.close();

        return matchingDocuments;
    }
}

