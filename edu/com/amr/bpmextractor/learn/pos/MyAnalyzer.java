package com.amr.bpmextractor.learn.pos;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class MyAnalyzer extends Analyzer {

//	private Version matchVersion;

	public MyAnalyzer(Version matchVersion) {
//		this.matchVersion = matchVersion;
	}

//	@Override
//	protected TokenStreamComponents createComponents(String fieldName) {
//		return new TokenStreamComponents(new WhitespaceTokenizer());
//	}
	
//	@Override
//	protected TokenStreamComponents createComponents(String fieldName) {
//		final Tokenizer source = new WhitespaceTokenizer();
////		TokenStream result = new LengthFilter(true, source, 3, Integer.MAX_VALUE);
//		TokenStream result = new LengthFilter(source, 3, Integer.MAX_VALUE);
//		return new TokenStreamComponents(source, result);
//	}
	
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		final Tokenizer source = new WhitespaceTokenizer();
		// TokenStream result = new LengthFilter(true, source, 3, Integer.MAX_VALUE);
		TokenStream result = new LengthFilter(source, 0, Integer.MAX_VALUE);
		result = new PartOfSpeechTaggingFilter(result);
		return new TokenStreamComponents(source, result);
	}
	
//	   @Override
//	   protected Reader initReader(String fieldName, Reader reader) {
//	     // wrap the Reader in a CharFilter chain.
//	     return new SecondCharFilter(new FirstCharFilter(reader));
//	   }
	
	
	public static void main(String[] args) throws IOException {
		// text to tokenize
		final String text = "This is a demo of the TokenStream API";
		//final String text = "Start a new Application";
		//final String text = "My son went shopping with my wife at the mall";
		
		Version matchVersion = Version.LUCENE_7_3_0;
		MyAnalyzer analyzer = new MyAnalyzer(matchVersion);
		TokenStream stream = analyzer.tokenStream("field", new StringReader(text));

		// get the CharTermAttribute from the TokenStream
		CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
		
		// get the PartOfSpeechAttribute from the TokenStream
	     PartOfSpeechAttribute posAtt = stream.addAttribute(PartOfSpeechAttribute.class);

		try {
			stream.reset();

			// print all tokens until stream is exhausted
			while (stream.incrementToken()) {
//				System.out.println(termAtt.toString());
				System.out.println(termAtt.toString() + ": " + posAtt.getPartOfSpeech());
			}

			stream.end();
		} finally {
			stream.close();
			analyzer.close();
		}
	}
}