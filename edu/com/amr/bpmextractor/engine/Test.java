package com.amr.bpmextractor.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Test {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        Analyzer analyzer = new Analyzer() {
            @Override
             protected TokenStreamComponents createComponents(String fieldName) {
               Tokenizer source = new StandardTokenizer();
               TokenStream filter = new StandardTokenizer();
//               filter.
               return new TokenStreamComponents(source, filter);
             }

           };
           
           

        Directory directory;
        try {

            //analyzer.tokenStream("test", "This is just a trial");
            
            File file = new File("/Users/amr/Documents/Downloads/WordNet/3.0/prolog/wn_s.pl");
            InputStream stream = new FileInputStream(file);
            Reader rulesReader = new InputStreamReader(stream); 
            SynonymMap.Builder parser = null;
            parser = new WordnetSynonymParser(true, true, new StandardAnalyzer());
//            ((WordnetSynonymParser) parser).add(rulesReader);         
            SynonymMap synonymMap = parser.build();
            
//            filter = new SynonymFilter(filter, synonymMap, false);        
//            return new TokenStreamComponents(source, filter);

            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
//        } catch (ParseException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
        }
    }

}
