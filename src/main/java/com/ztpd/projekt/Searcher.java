package com.ztpd.projekt;

import com.sun.xml.internal.ws.api.addressing.WSEndpointReference;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Searcher {

    public static ArrayList<Result> search(String query) throws IOException, ParseException {
        System.out.println(query);

        IndexReader reader = getIndexReader();
        assert reader != null : "reader is null";

        IndexSearcher indexSearcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        ArrayList<Result> results;
/*
        // TERM QUERY
        // Boolean query
        // PrefixQuery.
        // WildcardQuery
        // FuzzyQuerry
        {
            System.out.println("7) Fuzzy querry (CONTENT): mamml?");
            Term fuzz = new Term(Constants.content, "mamml");
            FuzzyQuery fuzzyquery = new FuzzyQuery(fuzz);
            printResultsForQuery(indexSearcher, fuzzyquery);
        }

        // QueryParser
        String queryP1 = "MaMMal AND bat";
        String queryP2 = "ante*";
        String queryP3 = "brd~ ";
        String queryP4 = "(\"nocturnal life\"~10) OR bat";
        String queryP5 = "(\"nocturnal life\"~10) OR (\"are nocturnal\"~10)";
        String selectedQuery = queryP5;*/
        {
            System.out.println("8) query parser = " + query);
            Query q = new QueryParser(Constants.content, analyzer).parse(query);
            results = getResultsForQuery(indexSearcher, q);
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    private static ArrayList<Result> getResultsForQuery(IndexSearcher indexSearcher, Query q) throws IOException {

        TopDocs topdocs = indexSearcher.search(q, Constants.top_docs);
        ArrayList<Result> results = new ArrayList<>();

        for (ScoreDoc top : topdocs.scoreDocs) {
            Document document = indexSearcher.doc(top.doc);
            Result tmp = new Result();
            tmp.setScore(Float.toString(top.score));
            tmp.setFilename(document.get("filename"));
            tmp.setId(document.get("id"));
            tmp.setFilesize(document.get("filesize"));
            tmp.setContent(document.get("content"));
            tmp.setURI(Constants.URI + document.get("filename"));
            tmp.setCreatedDate(document.get(Constants.created));
            tmp.setModifiedDate(document.get(Constants.modified));
            tmp.setAuthor(document.get(Constants.author));

            results.add(tmp);
        }

        return results;
    }

    private static IndexReader getIndexReader() {
        try {
            Directory dir = FSDirectory.open(Paths.get(Constants.index_dir));
            return DirectoryReader.open(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
