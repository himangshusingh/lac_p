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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;

public class read_index {

    static void Main(String searchTerm, String INDEX_DIR) throws IOException, ParseException {

        File pfile = new File(INDEX_DIR);
//        System.out.println("File: " + pfile);
//        System.out.println("Parent: " + pfile.getParent());


        // Field to search on
        String fieldName = "contents";

        List<DocumentData> matchingDocuments = performExactMatchQuery(INDEX_DIR, fieldName, searchTerm);

        System.out.println("Search Term: " + searchTerm);
        System.out.println("Number of Hits: " + matchingDocuments.size());
        System.out.println("Results: \n");


        // Specify the file path where you want to save the JSON output
        String filePath = pfile.getParent();                                    //this gets the parent directory of current specified directory
        deleteFile(getOutputFilePath(filePath));                                //this deletes the existing data from the output JSON file

        List<UserData> existingData = readExistingData(getOutputFilePath(filePath));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Process and display the matching documents
        for (DocumentData docData : matchingDocuments) {
            Document doc = docData.document;
            String snippet = docData.snippet;
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

                UserData userData = new UserData(doc.get("path"),time,user.getName(), snippet);
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
            System.out.println(e);
        }
//-----------------------------------------------
        //String searchWord = searchTerm; // Replace with the actual word you want to search
    }

    private static String getOutputFilePath(String inputFilePath) {
        File inputFile = new File(inputFilePath);
        String parentDir = inputFile.getParent();
        return parentDir + File.separator + "Search_Result.json";           //naming the output JSON file
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



    private static List<DocumentData> performExactMatchQuery(String INDEX_DIR, String fieldName, String searchTerm) throws IOException, ParseException {
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
        int maxHits = 10; // Maximum Number of search results to retrieve
        TopDocs topDocs = searcher.search(query, maxHits);

        Path indexDirPath = Paths.get(INDEX_DIR).toRealPath(); // change
        ScoreDoc[] hits = topDocs.scoreDocs;
        for (ScoreDoc hit : hits) {
            int docId = hit.doc;
            Document document = searcher.doc(docId);
            String documentPath = document.get("path");
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

    private static class UserData {
        String Path;
        String Creation_Time;
        String Owner;
        String snippet;
        public UserData(String Path, String Creation_Time, String Owner, String snippet) {
            this.Path = Path;
            this.Creation_Time = Creation_Time;
            this.Owner = Owner;
            this.snippet = snippet;
        }
    }

    private static class DocumentData {
        Document document;
        String snippet;

        public DocumentData(Document document, String snippet) {
            this.document = document;
            this.snippet = snippet;
        }
    }
}

