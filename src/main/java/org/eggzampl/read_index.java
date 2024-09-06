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

        List<DocumentData> matchingDocuments = QueryExecuter.performExactMatchQuery(INDEX_DIR, Constants.CONTENTS_FIELD, searchTerm);

        System.out.println("Search Term: " + searchTerm);
        System.out.println("Number of Hits: " + matchingDocuments.size());
        System.out.println("Results: \n");

        String filePath = pfile.getParent();
        deleteFile(getOutputFilePath(filePath));

        List<UserData> existingData = readExistingData(getOutputFilePath(filePath));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Process and display the matching documents
        for (DocumentData docData : matchingDocuments) {
            Document doc = docData.getDocument();
            String snippet = docData.getSnippet();
            System.out.println("\n\nDocument Path:      " + doc.get(Constants.PATH_FIELD));
            try {
                Path file = Paths.get(doc.get(Constants.PATH_FIELD));
                BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                System.out.println("creationTime:       " + attr.creationTime());
                String time = attr.creationTime().toString();

                FileOwnerAttributeView path = Files.getFileAttributeView(file, FileOwnerAttributeView.class);
                UserPrincipal user = path.getOwner();
                System.out.println("Owner:              " + user.getName());

                UserData userData = new UserData(doc.get(Constants.PATH_FIELD), time, user.getName(), snippet);
                existingData.add(userData);

            } catch (IOException e) {
                System.out.println("Error while Displaying Time !");
            }
        }

        try {
            String jsonString = gson.toJson(existingData);

            FileWriter fileWriter = new FileWriter(getOutputFilePath(filePath));
            fileWriter.write(jsonString);
            fileWriter.flush();
            fileWriter.close();

            System.out.println("JSON output has been written to " + getOutputFilePath(filePath));
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static String getOutputFilePath(String inputFilePath) {
        File inputFile = new File(inputFilePath);
        String parentDir = inputFile.getParent();
        return parentDir + File.separator + Constants.SEARCH_RESULT_FILE;
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
}
