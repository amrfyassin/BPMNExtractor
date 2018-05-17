package com.amr.bpmextractor.learn.opennlp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;


/**
 * Hello world OpenNLP!
 * 
 */

public class BasicNameFinder {
    public static void main(String[] args) throws InvalidFormatException,
            IOException {

       String[] sentences = {
                "If President John F. Kennedy, after visiting France in 1961 with his immensely popular wife,"
                        + " famously described himself as 'the man who had accompanied Jacqueline Kennedy to Paris,'"
                        + " Mr. Hollande has been most conspicuous on this state visit for traveling alone.",
                "Mr. Draghi spoke on the first day of an economic policy conference here organized by"
                        + " the E.C.B. as a sort of counterpart to the annual symposium held in Jackson"
                        + " Hole, Wyo., by the Federal Reserve Bank of Kansas City. " 
                        + " Donald Trump is now the new president after Barak Obama."};

        // Load the model file downloaded from OpenNLP
        // http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin
        TokenNameFinderModel model = new TokenNameFinderModel(new File(
                "/Users/amr/Documents/Downloads/Apache/OpenNLP/models/en-ner-person.bin"));

        // Create a NameFinder using the model
        NameFinderME finder = new NameFinderME(model);

        Tokenizer tokenizer = SimpleTokenizer.INSTANCE;

        for (String sentence : sentences) {

            // Split the sentence into tokens
            String[] tokens = tokenizer.tokenize(sentence);

            // Find the names in the tokens and return Span objects
            Span[] nameSpans = finder.find(tokens);

            // Print the names extracted from the tokens using the Span data
            System.out.println(Arrays.toString(Span.spansToStrings(nameSpans, tokens)));
        }
    }
}