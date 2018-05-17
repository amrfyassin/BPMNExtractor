package com.amr.bpmextractor.learn.opennlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * www.tutorialkart.com
 * POS Tagger Example in Apache OpenNLP using Java
 */
public class ProcessTagger1 {

	public static void main(String[] args) {

		InputStream tokenModelIn = null;
		InputStream posModelIn = null;
		
		try {
			String sentence = "Complete your Form DS-11 Application for U.S. Passport on the State Department website.\n" + 
					"Print your completed application. DO NOT SIGN YOUR APPLICATION. ...\n" + 
					"Have a passport photo taken. \n" + 
					"Photocopy your proof of identity and U.S. Citizenship documents.\n" + 
					"Calculate your fees.";
			// tokenize the sentence
			//tokenModelIn = new FileInputStream("en-token.bin");
			tokenModelIn = new FileInputStream("/Users/amr/Documents/Downloads/Apache/OpenNLP/models/en-token.bin");
			TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
			Tokenizer tokenizer = new TokenizerME(tokenModel);
			String tokens[] = tokenizer.tokenize(sentence);

			// Parts-Of-Speech Tagging
			// reading parts-of-speech model to a stream 
			posModelIn = new FileInputStream("/Users/amr/Documents/Downloads/Apache/OpenNLP/models/en-pos-maxent.bin");
			// loading the parts-of-speech model from stream
			POSModel posModel = new POSModel(posModelIn);
			// initializing the parts-of-speech tagger with model 
			POSTaggerME posTagger = new POSTaggerME(posModel);
			// Tagger tagging the tokens
			String tags[] = posTagger.tag(tokens);
			// Getting the probabilities of the tags given to the tokens
			double probs[] = posTagger.probs();
			
			System.out.println("Token\t\t:\tTag\t:\tProbability\n---------------------------------------------");
			for(int i=0;i<tokens.length;i++){
				System.out.println(tokens[i]+"\t\t:\t"+tags[i]+"\t:\t"+probs[i]);
			}
			
			System.out.println("\n\n\nStep\t:\tTask Name\n---------------------------------------------");
			int j = 0;
			for(int i = 0; i<tokens.length; i++){
				if (tags[i].startsWith("VB") && probs[i] > 0.1) {
					System.out.println(j + "\t:\t" + tokens[i]);
					j++;
				}
			}
		}
		catch (IOException e) {
			// Model loading failed, handle the error
			e.printStackTrace();
		}
		finally {
			if (tokenModelIn != null) {
				try {
					tokenModelIn.close();
				}
				catch (IOException e) {
				}
			}
			if (posModelIn != null) {
				try {
					posModelIn.close();
				}
				catch (IOException e) {
				}
			}
		}
	}
}