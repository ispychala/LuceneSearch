package com.ztpd.projekt;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

class FileData {
    public String content;
    public String createdDate;
    public String modifiedDate;
    public String author;

    public FileData(String cont, String cD, String mD, String auth) {
        this.content = cont;
        this.createdDate = cD;
        this.modifiedDate = mD;
        this.author = auth;
    }
}

public class Indexer {

    private static SimpleDateFormat dateFormat;

    public static void main(String args[]) {
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Indexer indexer = new Indexer();
        indexer.indexDocuments();
    }

    private void indexDocuments() {
        try {
            FileUtils.deleteDirectory(new File(Constants.index_dir));
        } catch (IOException ignored) {
        }

        ArrayList<Document> documents = getDocuments();

        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexconfig = new IndexWriterConfig(analyzer);
        try {
            FSDirectory dir = FSDirectory.open(Paths.get(Constants.index_dir));
            IndexWriter writer = new IndexWriter(dir, indexconfig);

            writer.addDocuments(documents);
            writer.commit();
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ArrayList<Document> getDocuments() {
        File dir = new File("database");
        File[] files = dir.listFiles();
        if (files != null) {
            ArrayList<Document> docs = new ArrayList<>(files.length);
            for (int id = 0; id < files.length; id++) {
                System.out.println("Loading " + files[id].getName());
                docs.add(getDocument("database/" + files[id].getName(), id));
            }
            return docs;
        }
        return null;
    }

    private Document getDocument(String path, int id) {
        File file = new File(path);
        Document document = new Document();

        StoredField idx = new StoredField(Constants.id, id);

        FileData fd = getTextFromFile(file);

        Field content = new TextField(Constants.content, fd.content, Field.Store.NO);
        Field filename = new TextField(Constants.filename, file.getName(), Field.Store.YES);
        Field intfield = new IntPoint(Constants.filesize_int, (int) file.length());
        Field size = new StoredField(Constants.filesize, (int) file.length());

        Field createdDate  = new TextField(Constants.created, fd.createdDate, Field.Store.YES);
        Field modDate  = new TextField(Constants.modified, fd.modifiedDate, Field.Store.YES);
        Field author  = new TextField(Constants.author, fd.author, Field.Store.YES);

        document.add(idx);
        document.add(content);
        document.add(filename);
        document.add(intfield);
        document.add(size);
        document.add(createdDate);
        document.add(modDate);
        document.add(author);

        return document;
    }

    private FileData getTextFromFile(File file) {
        BodyContentHandler handler = new BodyContentHandler(500000);
        Metadata metadata = new Metadata();
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        ParseContext pContext = new ParseContext();
        AutoDetectParser parser = new AutoDetectParser();

        try {
            parser.parse(inputStream, handler, metadata, pContext);
        } catch (IOException | SAXException | TikaException e) {
            e.printStackTrace();
        }

        String content = handler.toString();

        Date creat = metadata.getDate(TikaCoreProperties.CREATED);
        String creatStr = creat == null ? "" : dateFormat.format(creat);

        Date mod = metadata.getDate(TikaCoreProperties.MODIFIED);
        String modStr = mod == null ? "" : dateFormat.format(mod);

        String auth = metadata.get(TikaCoreProperties.CREATOR);
        if(auth == null) {
            auth = "";
        }

        return new FileData(content, creatStr, modStr, auth);
    }

}
