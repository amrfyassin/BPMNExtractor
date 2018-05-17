package com.amr.bpmextractor.learn.opennlp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import com.amr.bpmextractor.util.Utils;

/**
 * www.tutorialkart.com
 * POS Tagger Example in Apache OpenNLP using Java
 */
public class ProcessTagger3 {
	
	private int taskNo;
	private String taskName;
	
	String[] tokens;
	String[] tags;
	double[] probs;
	
	public static void main(String[] args) {
		ProcessTagger3 pt = new ProcessTagger3();
		pt.startProcessing();
	}
	
	ProcessTagger3(){
		taskNo = 1;
		taskName = "";
	}
	
	void startProcessing() {
		InputStream tokenModelIn = null;
		InputStream posModelIn = null;
		String inputFilePath = "data/unstructuredProcess1.txt";
		
		try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
			String sentence = "";
			String temp;
			//sentence += br.readLine();
			while((temp = br.readLine()) != null) {
                //System.out.println(temp);
				sentence += temp + "\n";
            } 
			//System.out.println(sentence);

			// tokenize the sentence
			tokenModelIn = new FileInputStream("/Users/amr/Documents/Downloads/Apache/OpenNLP/models/en-token.bin");
			TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
			Tokenizer tokenizer = new TokenizerME(tokenModel);
			tokens = tokenizer.tokenize(sentence);

			// Parts-Of-Speech Tagging
			posModelIn = new FileInputStream("/Users/amr/Documents/Downloads/Apache/OpenNLP/models/en-pos-maxent.bin");
			POSModel posModel = new POSModel(posModelIn);
			POSTaggerME posTagger = new POSTaggerME(posModel);
			tags = posTagger.tag(tokens);
			probs = posTagger.probs();
			
			System.out.println("Token\t\t:\tTag\t:\tProbability\n---------------------------------------------");
			for(int i=0;i<tokens.length;i++){
				//if (tags[i].equalsIgnoreCase("NN"))
				System.out.println(tokens[i] + "\t\t:\t" + tags[i] + "\t:\t" + probs[i]);
			}
			
			System.out.println("\n\n\nStep\t: Task Name\n---------------------------------------------");
			printTask("Start");
			
			boolean taskEnded = true;
			for(int i = 0; i < tokens.length; i++){
				switch(tags[i]) {
					case "VB":
					case "VBD":
					case "VBP":
					case "VBZ":
						if (tokens[i].equalsIgnoreCase("do") && tokens[i+1].equalsIgnoreCase("not")) continue;
						
						if (tokens[i].equalsIgnoreCase("is") || tokens[i].equalsIgnoreCase("are")
								|| tokens[i].equalsIgnoreCase("")) continue;
						
						if (probs[i] > 0.3) {
							printTask();
							appendVerbToTaskName(tokens[i]);
							taskEnded = false;
						}
						break;
						
					case "PRP":
					//case "PRP$":	
					case "NN":
					case "NNS":
						if (tokens[i].equalsIgnoreCase("–") || tokens[i].equalsIgnoreCase("it") 
								|| tokens[i].equalsIgnoreCase("I") || tokens[i].equalsIgnoreCase("you")
								|| tokens[i].equalsIgnoreCase("yesterday") || tokens[i].equalsIgnoreCase("today") || tokens[i].equalsIgnoreCase("tomorrow")) {
							continue;
						}
						
						if (!taskEnded) {
							appendNonVerbToTaskName(tokens[i]);
						}
						break;
						
					case "NNP":
					case "NNPS":
					case ".":
						taskEnded = true;
						printTask();
						break;
						
					case "TO":
						if (tags[i+1].equalsIgnoreCase("VB")) i++;	 //skip next verb
						break;
						
					//case "VBN":
					//case "VBG":
					//	break;
						
				}
			}
		
			printTask("Stop");
		}
		catch (IOException e) {
			// Model loading failed, handle the error
			e.printStackTrace();
		}
		finally {
			try { tokenModelIn.close(); } catch (Exception ignore) {}
			try { posModelIn.close(); } catch (Exception ignore) {}
		}
	}
	
	private void appendVerbToTaskName(String token) {
		if (token == null || token.equals("")) return;
		
		if (taskName != null && taskName.length() > 0) {
			return;
	
		} else {
			taskName = Utils.toCamelCase(token);
		}
	}
	
	private void appendNonVerbToTaskName(String token) {
		if (token == null || token.equals("")) return;
		
		if (taskName != null && taskName.length() > 0) {
			taskName += " " + token;
	
		} else {
			return;
		}
	}
	
	private void printTask(String taskName) {
		System.out.println(taskNo++ + "\t: " + taskName);
//		printTask();
	}
	
	private void printTask() {
		if (taskName != null && taskName.length() > 0 && taskName.contains(" ") ) {
			System.out.println(taskNo++ + "\t: " + taskName);
		}
		taskName = "";
	}
	
}