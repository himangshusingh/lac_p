package org.eggzampl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.lang.reflect.Type;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
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

        File pfile = new File(INDEX_DIR);
//        System.out.println("File: " + pfile);
//        System.out.println("Parent: " + pfile.getParent());


        // Field to search on
        String fieldName = "contents";

        List<Document> matchingDocuments = performExactMatchQuery(INDEX_DIR, fieldName, searchTerm);

        System.out.println("Search Term: " + searchTerm);
        System.out.println("Number of Hits: " + matchingDocuments.size());
        System.out.println("Results: \n");


        // Specify the file path where you want to save the JSON output
        String filePath = pfile.getParent();
        deleteFile(getOutputFilePath(filePath));                                //this gets the parent directory of current specified directory

        List<UserData> existingData = readExistingData(getOutputFilePath(filePath));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Process and display the matching documents
        for (Document doc : matchingDocuments) {
            System.out.println("\n\nDocument Path:      " + doc.get("path"));
            try {
                //  Access time snippet
                Path file = Paths.get(doc.get("path"));
                BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                System.out.println("creationTime:       " + attr.creationTime());
                String time = attr.creationTime().toString();
//                    System.out.println("lastAccessTime:     " + attr.lastAccessTime());
//                    System.out.println("lastModifiedTime:   " + attr.lastModifiedTime());

                FileOwnerAttributeView path = Files.getFileAttributeView(file, FileOwnerAttributeView.class);
                UserPrincipal user = path.getOwner();                           // Taking owner name from the file
                System.out.println("Owner:              " + user.getName());    // Printing the owner's name

                UserData userData = new UserData(doc.get("path"),time,user.getName());
                existingData.add(userData);

            } catch (IOException e) {
                System.out.println("Error while Displaying Time !");
            }
        }
//-----------------------------------------------
        try {
            // Convert the updated data list to a JSON string
            String jsonString = gson.toJson(existingData);

            // Write the JSON string to the file
            FileWriter fileWriter = new FileWriter(getOutputFilePath(filePath));
            fileWriter.write(jsonString);
            fileWriter.flush();
            fileWriter.close();

            System.out.println("JSON output has been written to " + getOutputFilePath(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
    }

    private static String getOutputFilePath(String inputFilePath) {
        File inputFile = new File(inputFilePath);
        String parentDir = inputFile.getParent();
        return parentDir + File.separator + "Search_Result.json";
    }
    private static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
    private static List<UserData> readExistingData(String filePath) {
        List<UserData> existingData = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(filePath);
            Type dataType = new TypeToken<List<UserData>>() {}.getType();
            Gson gson = new Gson();
            existingData = gson.fromJson(fileReader, dataType);
            fileReader.close();
        } catch (IOException e) {
            // If the file doesn't exist or cannot be read, ignore the exception and return an empty list
        }

        return existingData;
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
        Query query = queryParser.parse('"'+searchTerm+'"');

        // Perform the search and retrieve matching documents
        int maxHits = 10; // Maximum Number of search results to retrieve
        TopDocs topDocs = searcher.search(query, maxHits);

        Path indexDirPath = Paths.get(INDEX_DIR).toRealPath(); // change
        ScoreDoc[] hits = topDocs.scoreDocs;
        for (ScoreDoc hit : hits) {
            int docId = hit.doc;
            Document document = searcher.doc(docId);
            String documentPath = document.get("path");
            Path documentRealPath;
            try {
                documentRealPath = Paths.get(documentPath).toRealPath();
            } catch (IOException e) {
                // If there's an error with the file path, continue to the next document
                continue;
            }
            if (!documentRealPath.startsWith(indexDirPath)) {
                matchingDocuments.add(document);
            }
        }

        // Close the index reader and directory
        indexReader.close();
        directory.close();

        return matchingDocuments;
    }

    private static class UserData {
        String Path;
        String Creation_Time;
        String Owner;
        public UserData(String Path, String Creation_Time, String Owner) {
            this.Path = Path;
            this.Creation_Time = Creation_Time;
            this.Owner = Owner;
        }
    }
}

