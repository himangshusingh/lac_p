package org.eggzampl;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
//import org.eggzampl.Constants;

public class write_index {

    static void Main(String docsPath) {
        final Path docDir = Paths.get(docsPath);

        File parentDir = new File(docsPath);
        File childDir = new File(parentDir, Constants.INDEX_FOLDER);

        String path = folderExistsInDirectory(docsPath, Constants.INDEX_FOLDER);
        if (path != null) {
            System.out.println("The folder exists within the parent directory.");
            System.out.println("Path: " + path);
        } else {
            String indexdir = createFolderInDirectory(docsPath, Constants.INDEX_FOLDER);
            if (indexdir != null) {
                System.out.println("The folder was successfully created in the parent directory.");
                System.out.println("Path: " + indexdir);
            } else {
                System.out.println("Failed to create the folder in the parent directory.");
            }
        }

        String indexPath = childDir.getPath();  // Getting the path of the indexed folder

        try {
            // org.apache.lucene.store.Directory instance
            Directory dir = FSDirectory.open(Paths.get(indexPath));

            // Analyzer with the default stop words
            Analyzer analyzer = new StandardAnalyzer();

            // IndexWriter Configuration
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);  // Set the file mode to append or create if not created

            // IndexWriter writes new index files to the directory
            IndexWriter writer = new IndexWriter(dir, iwc);  // Writing new index files

            indexDocs(writer, docDir);  // It's a recursive method to iterate all files and directories
            writer.close();
        } catch (IOException e) {
            System.out.println("Error Occurred \n\n");
            e.printStackTrace();
        }
    }

    static void indexDocs(final IndexWriter writer, Path path) throws IOException {
        // If directory exists then continue
        if (Files.isDirectory(path) && !path.endsWith(Constants.INDEX_FOLDER)) {
            // Iterate directory
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Index this file
                    indexDoc(writer, file, attrs.lastModifiedTime().toMillis());  // Converting the last modified time to milliseconds
                    return FileVisitResult.CONTINUE;
                }
            });
        } else if (!Files.isDirectory(path)) {
            // Index this file if it's not a directory
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        if (!Files.isRegularFile(file) || !Files.isReadable(file) || !file.toString().endsWith(".txt")) {
            // Skip non-text files or files that cannot be read or do not end with ".txt"
            return;
        }

        try (InputStream stream = Files.newInputStream(file)) {
            // Create Lucene Document
            Document doc = new Document();

            doc.add(new TextField("Author Name", new String(Files.readAllBytes(file)), Field.Store.YES));
            doc.add(new StringField(Constants.PATH_FIELD, file.toString(), Field.Store.YES));
            doc.add(new LongPoint("modified", lastModified));
            doc.add(new TextField(Constants.CONTENTS_FIELD, new String(Files.readAllBytes(file)), Field.Store.YES));

            // Updates a document by first deleting the document(s)
            // containing <code>term</code> and then adding the new
            // document. The delete and then add are atomic as seen
            // by a reader on the same index
            writer.updateDocument(new Term(Constants.PATH_FIELD, file.toString()), doc);
        }
    }

    public static String folderExistsInDirectory(String docsPath, String folderName) {
        File folder = new File(docsPath, folderName);
        if (folder.exists() && folder.isDirectory())
            return folder.getAbsolutePath();
        return null;
    }

    public static String createFolderInDirectory(String docsPath, String folderName) {
        File folder = new File(docsPath, folderName);
        if (folder.mkdir())
            return folder.getAbsolutePath();
        return null;
    }
}
