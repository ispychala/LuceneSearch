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

        // TERM QUERY
        // A Query that matches documents containing a term.
        // This may be combined with other terms with a BooleanQuery.
       /* String queryMammal = "MaMMal";
        TermQuery tq1;
        {
            System.out.println("1) term query: mammal (CONTENT)");
            queryMammal = analyzer.normalize(queryMammal, queryMammal).utf8ToString();
            Term t1 = new Term(Constants.content, queryMammal);
            tq1 = new TermQuery(t1);

            results = printResultsForQuery(indexSearcher, tq1);
        }
/*
        String queryBird = "bird";
        TermQuery tq2;
        {
            System.out.println("2) term query bird (CONTENT)");
            queryBird = analyzer.normalize(queryBird, queryBird).utf8ToString();
            Term t2 = new Term(Constants.content, queryBird);
            tq2 = new TermQuery(t2);

            printResultsForQuery(indexSearcher, tq2);
        }

        // Boolean query
        {
            System.out.println("3) boolean query (CONTENT): mammal or bird");
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(tq1, BooleanClause.Occur.SHOULD);
            builder.add(tq2, BooleanClause.Occur.SHOULD);
            builder.setMinimumNumberShouldMatch(1);
            BooleanQuery boolquery = builder.build();

            printResultsForQuery(indexSearcher, boolquery);
        }

        // Range query.
        {
            System.out.println("4) range query: file size in [0b, 1000b]");
            Query rangequery = IntPoint.newRangeQuery(Constants.filesize_int, 0, 1000);
            printResultsForQuery(indexSearcher, rangequery);
        }

        // PrefixQuery.
        {
            System.out.println("5) Prefix query (FILENAME): ant");
            Term prefix = new Term(Constants.filename, "ant");
            PrefixQuery prefixquery = new PrefixQuery(prefix);
            printResultsForQuery(indexSearcher, prefixquery);
        }

        // WildcardQuery
        {
            System.out.println("6) Wildcard query (CONTENT): eat?");
            Term wildcard = new Term(Constants.content, "eat?");
            WildcardQuery wildcardquery = new WildcardQuery(wildcard);
            printResultsForQuery(indexSearcher, wildcardquery);
        }

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
            results = printResultsForQuery(indexSearcher, q);
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    private static ArrayList<Result> printResultsForQuery(IndexSearcher indexSearcher, Query q) throws IOException {

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
