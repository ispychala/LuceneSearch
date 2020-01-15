package com.ztpd.projekt;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Indexer {
    public static void main(String args[]) {
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

        StoredField stored = new StoredField(Constants.id, id);

        String content = getTextFromFile(file);
        Field indexed = new TextField(Constants.content, content, Field.Store.NO);

        Field storedindexed = new TextField(Constants.filename, file.getName(), Field.Store.YES);

        Field intfield = new IntPoint(Constants.filesize_int, (int) file.length());

        Field storedint = new StoredField(Constants.filesize, (int) file.length());

        document.add(stored);
        document.add(indexed);
        document.add(storedindexed);
        document.add(intfield);
        document.add(storedint);

        return document;
    }

    private String getTextFromFile(File file) {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        ParseContext pContext = new ParseContext();

        Parser parser = null;
        String[] fileName = file.getName().split("\\.");
        String fileType = fileName[fileName.length - 1];

        switch (fileType) {
            case "html":
                parser = new HtmlParser();
                break;
            case "pdf":
                parser = new PDFParser();
                break;
            case "txt":
                parser = new TXTParser();
                break;
            case "xml":
                parser = new XMLParser();
                break;
            default:
                System.out.println("Can't parse this filetype");
                break;
        }

        if (parser != null) {
            try {
                parser.parse(inputStream, handler, metadata, pContext);
            } catch (IOException | SAXException | TikaException e) {
                e.printStackTrace();
            }
        }

        return handler.toString();
    }

}
