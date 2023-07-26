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
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
 
public class write_index
{
   static void Main(String docsPath)
    {
        //Input Path Variable
        final Path docDir = Paths.get(docsPath);
        boolean flag = false;

        String childFolder = "indexedFiles";                 //name of the folder where the indexed files will be stored

        File parentDir = new File(docsPath);
        File childDir = new File(parentDir, childFolder);

        if (!parentDir.exists()) {
            System.out.println("Parent folder does not exist.");
        }

        if (childDir.mkdir()) {
            System.out.println("Index Folder created successfully folder created successfully.");
            flag = true;
        } else {
            System.out.println("Failed to create child folder.");
        }
        String indexPath = childDir.getPath();                      //getting the path of the indexed Folder

/* ---------------------------------------------------------------------------------------------------------------------*/
        try
        {
            //org.apache.lucene.store.Directory instance
            Directory dir = FSDirectory.open( Paths.get(indexPath) );
             
            //analyzer with the default stop words
            Analyzer analyzer = new StandardAnalyzer();
             
            //IndexWriter Configuration
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);                 //set the file mode to append or create if not created
             
            //IndexWriter writes new index files to the directory
            IndexWriter writer = new IndexWriter(dir, iwc);             //writing new index files

            indexDocs(writer, docDir);                                  //Its recursive method to iterate all files and directories
            writer.close();
        }
        catch (IOException e)
        {
            System.out.println("Error Occurred \n\n");
            e.printStackTrace();
        }
        if(flag)                                                            //checking if the flag value is true
            System.out.println("Index Folder Created In: "+indexPath);      //printing the path of the INDEX FOLDER created
    }
     
    static void indexDocs(final IndexWriter writer, Path path) throws IOException
    {
        //if directory exists then continue
        if (Files.isDirectory(path))
        {
            //Iterate directory
            Files.walkFileTree(path, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    try
                    {
                        //Index this file
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis());    //converting the lastmodified time to millisecond
                    }
                    catch (IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        else
        {
            //Index this file
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }
 
    static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException
    {
        try (InputStream stream = Files.newInputStream(file))
        {
            //Create lucene Document
            Document doc = new Document();

            doc.add(new TextField("Author Name", new String(Files.readAllBytes(file)), Store.YES));
            doc.add(new StringField("path", file.toString(), Field.Store.YES));
            doc.add(new LongPoint("modified", lastModified));
            doc.add(new TextField("contents", new String(Files.readAllBytes(file)), Store.YES));
             
            //Updates a document by first deleting the document(s)
            //containing <code>term</code> and then adding the new
            //document.  The delete and then add are atomic as seen
            //by a reader on the same index
            writer.updateDocument(new Term("path", file.toString()), doc);
        }
    }
}