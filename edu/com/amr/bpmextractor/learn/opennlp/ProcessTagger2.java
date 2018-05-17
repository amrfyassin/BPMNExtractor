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
public class ProcessTagger2 {

	public static void main(String[] args) {

		InputStream tokenModelIn = null;
		InputStream posModelIn = null;
		
		try {
			String sentence = "Complete your Form DS-11 Application for U.S. Passport on the State Department website.\n" + 
					"Print your completed application and scan it. DO NOT SIGN YOUR APPLICATION. ...\n" + 
					"Have a passport photo taken. \n" + 
					"Photocopy your proof of identity and U.S. Citizenship documents.\n" + 
					"Calculate your fees.";
			
			System.out.println(sentence);

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
				System.out.println(tokens[i] + "\t\t:\t" + tags[i] + "\t:\t" + probs[i]);
			}
			
			System.out.println("\n\n\nStep\t: Task Name\n---------------------------------------------");
			int j = 1;
			System.out.print(j++ + "\t: Start");
			
			boolean taskEnded = true;
			for(int i = 0; i<tokens.length; i++){
				switch(tags[i]) {
					case "VB":
					case "VBD":
					case "VBG":
					case "VBP":
					case "VBZ":
						if (tokens[i].equalsIgnoreCase("do") && tokens[i+1].equalsIgnoreCase("not") ) continue;
						
						if (probs[i] > 0.3) {
							startTask(j++);
							System.out.print(tokens[i]);
							taskEnded = false;
						}
						break;
						
					case "PRP":
					//case "PRP$":	
					case "NN":
					case "NNS":
						if (!taskEnded) {
							System.out.print(" " + tokens[i]);
						}
						break;
						
					case "NNP":
					case "NNPS":
					case ".":
						taskEnded = true;
						endTask();
						break;
						
					case "CC":
						taskEnded = true;
						endTask();
						if (tokens[i].equalsIgnoreCase("or")) {
							System.out.print("\nOR\n");
						} else if (tokens[i].equalsIgnoreCase("and") && tags[i+1].contains("VB")) {
							System.out.print("\nAND");
						}
						//startTask(j++);
						break;
						
					//case "VBN":
					//	break;
						
				}
			}
		
			System.out.println("\n" + j++ + "\t: Stop");
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
	
	private static void startTask(int j) {
		System.out.print("\n" + j++ + "\t: ");
	}
	
	private static void endTask() {
		//System.out.print("\n");
	}
}