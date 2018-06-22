/*
 * (C) Copyright 2018-present Amr Yassin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors: 	Amr Yassin
 */
package com.amr.bpmextractor.engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import com.amr.bpmextractor.util.Utils;
import com.amr.bpmextractor.bpmnbuilder.Process;
import com.amr.bpmextractor.bpmnbuilder.Role;
import com.amr.bpmextractor.bpmnbuilder.Element;
import com.amr.bpmextractor.bpmnbuilder.Link;;

public class BpmnExtractorEngine {

    private String inputFile;
    private String outputFile;

    private int taskNo;
    private ArrayList <Integer> currentTask;
    private int currentSentence;
    private boolean taskReady;

    private String[] sentences;
    private ArrayList<String[]> tokens;
    private ArrayList<String[]> tags;
    private ArrayList<String[]> lemmas;
    private ArrayList<double[]> probs;

    private Process process;
    private Role currentRole;
    private Element previousElement;
    private Element currentElement;
    private ArrayList<String> parallelTasks;

    public BpmnExtractorEngine(String inputFile, String outputFile) {
        taskNo = 1;
        currentTask = new ArrayList<>();
        process = new Process("Test Process");
        currentRole = new Role(process, "Employee");
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        tokens = new ArrayList<>();
        tags = new ArrayList<>();
        lemmas = new ArrayList<>();
        probs = new ArrayList<>();
    }

    /**
     * It contains the main flow of analyzing the text, BPMN extraction and writing the output file.
     *  
     * @return the BPMN process after extraction
     */
    public Process processText() {
        analyzePOS();
        extractBPMN();
        process.writeBMPNFile(outputFile);
        System.out.print(process);
        return process;
    }
    
    /**
     * Analyzes the input text by:
     * 1- Split the text into sentences
     * 2- Split the sentence into tokens
     * 3- Add part-of-speech (POS) tag for every token
     * 4- Find root of token (lemma) 
     */
    private void analyzePOS() {
        InputStream tokenModelIn = null;
        InputStream posModelIn = null;

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String inputText = "";
            String temp;

            while ((temp = br.readLine()) != null) {
                inputText += temp + "\n";
            }
            
            // detect sentences in the paragraph
            InputStream is = new FileInputStream("models/en-sent.bin");
            SentenceModel sentenceModel = new SentenceModel(is);
            SentenceDetectorME sdetector = new SentenceDetectorME(sentenceModel);            
            sentences = sdetector.sentDetect(inputText);
     
            for(int i = 0; i < sentences.length; i++){
                System.out.println(sentences[i]);
    
                // tokenize the sentence
                tokenModelIn = new FileInputStream("models/en-token.bin");
                TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
                Tokenizer tokenizer = new TokenizerME(tokenModel);
                tokens.add(tokenizer.tokenize(sentences[i]));
    
                // Parts-Of-Speech Tagging
                posModelIn = new FileInputStream("models/en-pos-maxent.bin");
                POSModel posModel = new POSModel(posModelIn);
                POSTaggerME posTagger = new POSTaggerME(posModel);
                tags.add(posTagger.tag(tokens.get(i)));
                probs.add(posTagger.probs());
                
                // Find lemma for every token
                InputStream dictLemmatizer = new FileInputStream("models/en-lemmatizer.bin");
                DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(dictLemmatizer);
                lemmas.add(lemmatizer.lemmatize(tokens.get(i), tags.get(i)));
    
                System.out.println("No.\t: Tag\t: Probability\t\t: Token\t: Lemma\n"
                        + "------------------------------------------------------------------------------------------");
                for (int j = 0; j < tokens.get(i).length; j++) {
                    System.out.println(j + "\t: " + tags.get(i)[j] + "\t: " + probs.get(i)[j]  
                            + "\t: " + tokens.get(i)[j] + "\t: " + lemmas.get(i)[j]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try { tokenModelIn.close(); } catch (Exception ignore) { }
            try { posModelIn.close(); } catch (Exception ignore) { }
        }
    }
        
    private Process extractBPMN() {

            System.out.println("\n\n\nStep\t: Task Name\n---------------------------------------------");
            addStartEventToProcess();

            currentSentence = 0;
            do {
                int j = 0;
//                System.out.println("currentSentence = " + currentSentence);
                do {
                    j = extractNextTask(currentSentence, j);
                    if (taskReady) {
                    	addTaskToProcess();
                    	taskReady = false;
                    }
                } while (j < tokens.get(currentSentence).length);
                
                addTaskToProcess();	// a task can not span two sentences.
            } while (++currentSentence < sentences.length);

            addEndEventToProcess();
            return process;
    }

    private int extractNextTask(int i, int j) {
    	
    	if (j < tokens.get(currentSentence).length) {
    		do {
    			j = extractNextToken(i, j);
    			
    		} while(!taskReady && j < tokens.get(currentSentence).length && currentTask.size() != 0);
    	}
    	
    	return j;
    }
    
    private int extractNextToken(int i, int j) {
        switch (tags.get(i)[j]) {
            case "VB":
            case "VBD":
            case "VBZ":
            case "VBG":
            case "VBP":
                if (j > 0 && tokens.get(i)[j-1].equalsIgnoreCase("to") )
                    break;
                
                if (tokens.get(i)[j].equalsIgnoreCase("do") && tokens.get(i)[j + 1].equalsIgnoreCase("not"))
                    break;;

                if (tokens.get(i)[j].equalsIgnoreCase("is") || tokens.get(i)[j].equalsIgnoreCase("are")
                        || tokens.get(i)[j].equalsIgnoreCase("don’t")|| tokens.get(i)[j].equalsIgnoreCase("be"))
                    break;
                
                if (currentTask.size() > 0) markTaskReady();

                appendToTaskName(i, j);
                break;

            case "PRP$":
            case "PRP":
            case "NN":
                if (currentTask.size() > 0) appendToTaskName(i, j);
                break;
            
            case "NNS":
                if (tokens.get(i)[j].equalsIgnoreCase("–") || tokens.get(i)[j].equalsIgnoreCase("it")
                        || tokens.get(i)[j].equalsIgnoreCase("I") || tokens.get(i)[j].equalsIgnoreCase("you")
                        || tokens.get(i)[j].equalsIgnoreCase("yesterday") || tokens.get(i)[j].equalsIgnoreCase("today")
                        || tokens.get(i)[j].equalsIgnoreCase("tomorrow"))
                    break;
                
                if (tokens.get(i)[j].equalsIgnoreCase("second") || tokens.get(i)[j].equalsIgnoreCase("seconds")
                        || tokens.get(i)[j].equalsIgnoreCase("minute") || tokens.get(i)[j].equalsIgnoreCase("minutes")
                        || tokens.get(i)[j].equalsIgnoreCase("hour") || tokens.get(i)[j].equalsIgnoreCase("hours")
                        || tokens.get(i)[j].equalsIgnoreCase("day") || tokens.get(i)[j].equalsIgnoreCase("days")
                        || tokens.get(i)[j].equalsIgnoreCase("week") || tokens.get(i)[j].equalsIgnoreCase("weeks")
                        || tokens.get(i)[j].equalsIgnoreCase("month") || tokens.get(i)[j].equalsIgnoreCase("months")
                        || tokens.get(i)[j].equalsIgnoreCase("year") || tokens.get(i)[j].equalsIgnoreCase("years")) 
                    break;

//                        if (currentTaskTokens.size() > 0)
                    appendToTaskName(i,j);
                break;

            case "IN":
                if (currentTask.size() == 0 && 
                        (tokens.get(i)[j].equalsIgnoreCase("of") || tokens.get(i)[j].equalsIgnoreCase("with")
                        || tokens.get(i)[j].equalsIgnoreCase("for") || tokens.get(i)[j].equalsIgnoreCase("through")
                        || tokens.get(i)[j].equalsIgnoreCase("on") || tokens.get(i)[j].equalsIgnoreCase("in")
                        || tokens.get(i)[j].equalsIgnoreCase("at") || tokens.get(i)[j].equalsIgnoreCase("upon")
                        || tokens.get(i)[j].equalsIgnoreCase("until")|| tokens.get(i)[j].equalsIgnoreCase("as"))
                        ) 
                    break;
                
//                        if (currentTaskTokens.size() == 0) 
                    appendToTaskName(i, j);
                break;
                
            case "JJ":
                if (j > 0 && tags.get(i)[j-1].equals("IN")) appendToTaskName(i, j);
                break;
                
            case "NNP":
            case "NNPS":
                if (j > 0 && tags.get(i)[j-1].equals("JJ"))
                    break;
                
                appendToTaskName(i, j);
                break;
                
            case "RP":
                if (currentTask.size() > 0) appendToTaskName(i, j);
              break;
                
            case ".":
            case ":":
                markTaskReady();
                break;

            case "TO":
                if (tags.get(i)[j + 1].equalsIgnoreCase("VB")) {
                    j++; // skip next verb
                    
                } else {
                    appendToTaskName(i, j);
                }
                break;

            case "CC":
            	String ccToken = tokens.get(i)[j];
                if (getCurrentTaskName().length() > 0 && (ccToken.equalsIgnoreCase("and") ||ccToken.equalsIgnoreCase("or"))) {
                	j = handleParallel(i, j, ccToken);
                	joinParallelTasksToProcess(ccToken.toUpperCase(), ccToken.toUpperCase() + "-Join");
                }
                break;
                
            case "VBN":
                if (j > 0 && (tags.get(i)[j-1].equals("VB") || tags.get(i)[j-1].equals("CC"))) 
                	appendToTaskName(i, j);
                break;
                
            default:
                break;

        }
        
        if (currentTask.size() >= 6) markTaskReady();
        
        return ++j;
    }
    
    private int handleParallel(int i, int j, String ccToken) {
    	if (j <= 0) return j;
    	
    	// Or/And between two nouns in the same task
        if ((tags.get(i)[j-1].contains("NN") ||  tags.get(i)[j-1].contains("JJ"))
        		&& (tags.get(i)[j+1].contains("NN") ||  tags.get(i)[j+1].contains("JJ"))) {
            appendToTaskName(i, j);	// append ccToken
            appendToTaskName(i, j + 1);
            j += 2;
        }
        
        if (j > 0 && tokens.get(i)[j].equalsIgnoreCase(ccToken) /* && (tags.get(i)[j-1].equals("VB") || tags.get(i)[j+1].equals("NN"))*/) {
        	if (parallelTasks == null) parallelTasks = new ArrayList<>();
            parallelTasks.add(getCurrentTaskName()); 
            currentTask = new ArrayList<>();
            
            j = extractNextTask(i, j + 1);
            if (taskReady && getCurrentTaskName().length() > 0) {
            	if (validateTask()) parallelTasks.add(getCurrentTaskName());
                currentTask = new ArrayList<>();
                taskReady = false;
            }
            
            if (j < tokens.get(i).length && tokens.get(i)[j].equalsIgnoreCase(ccToken)) j = handleParallel(i, j, ccToken);
        }
        
        return j;
    }
    
    private void addStartEventToProcess() {
        previousElement = null;
        currentElement = Element.createElement(currentRole, "Start", Element.TYPE.START_EVENT);
    }

    private void addEndEventToProcess() {
        previousElement = currentElement;
        currentElement = Element.createElement(currentRole, "Stop", Element.TYPE.END_EVENT);
        Link.createLink(previousElement.getName() + " -> " + currentElement.getName(), previousElement, currentElement);
    }

    private void joinParallelTasksToProcess(String splitName, String joinName) {
        if (parallelTasks == null || parallelTasks.size() == 0) return;
        
        if (parallelTasks.size() == 1) {
            previousElement = currentElement;
            Element newElement = Element.createElement(currentRole, parallelTasks.get(0), Element.TYPE.SERVICE_TASK);
            
            Link.createLink(previousElement.getName() + " -> " + newElement.getName(), previousElement, newElement);
            currentElement = newElement;

        } else {    // more than one task
            Element split = Element.createElement(currentRole, splitName, Element.TYPE.PARALLEL_GATEWAY);
            Link.createLink(currentElement.getName() + " -> " + split.getName(), currentElement, split);
            
            Element join = Element.createElement(currentRole, joinName, Element.TYPE.PARALLEL_GATEWAY);
            
            for (int i = 0; i < parallelTasks.size(); i++) {
                Element newElement = Element.createElement(currentRole, parallelTasks.get(i), Element.TYPE.SERVICE_TASK);
                
                Link.createLink(split.getName() + " -> " + newElement.getName(), split, newElement);
                Link.createLink(newElement.getName() + " -> " + join.getName(), newElement, join);
            }
            previousElement = currentElement;
            parallelTasks = new ArrayList<>();
            currentElement = join;
        }
    }
    
    private void markTaskReady() {
    	taskReady = true;
    }
    
    private void addTaskToProcess() {
    	if (!validateTask()) return;
        String currentTaskName = getCurrentTaskName();
        
        if (currentTaskName.length() > 0 && currentTaskName.contains(" ")) {
            System.out.println(taskNo++ + "\t: " + currentTaskName);

            previousElement = currentElement;
            currentElement = Element.createElement(currentRole, currentTaskName, Element.TYPE.SERVICE_TASK);

            if (process.getElements().size() >= 2 && previousElement != null && currentElement != null) {
                Link.createLink(previousElement.getName() + " -> " + currentElement.getName(),
                        previousElement, currentElement);
            }
            
            currentTask = new ArrayList<>();
        }
    }
    
    private boolean validateTask() {
        if (currentTask == null || currentTask.size() == 0) return false;
        
        boolean hasVerb = false;
        for (int i = 0; i < currentTask.size(); i++) {
            if (tags.get(currentSentence)[currentTask.get(0)].contains("NNP") 
                    || tags.get(currentSentence)[currentTask.get(i)].contains("VB")) {
                hasVerb = true;
                break;
            }
            if (tags.get(currentSentence)[currentTask.get(i)].contains("VB")) {
                hasVerb = true;
                break;
            }
        }
        
        if (currentTask.size() == 1 || !hasVerb) {
            // ignore collected tokens
            currentTask = new ArrayList<>();
            return false;
        }
        
        return true;
    }

	private void appendToTaskName(int i, int j) {
        //if (currentTask == null) currentTask = new Task(tokens);
        currentSentence = i;
        currentTask.add(j);
    }
    
    private String getCurrentTaskName()
    {
        if (currentTask == null || currentTask.size() == 0) {
            return "";
        } else {
            StringBuffer taskName = new StringBuffer();
            String tempString;
            for (int i = 0; i < currentTask.size(); i++) {
                String tag = tags.get(currentSentence)[currentTask.get(i)];
                String token = tokens.get(currentSentence)[currentTask.get(i)];
                String lemma = lemmas.get(currentSentence)[currentTask.get(i)];
                
                tempString = (tag.contains("VB") && !lemma.equals("O")) ? lemma : token;
                
                taskName.append(i == 0 ? Utils.toCamelCase(tempString) : (" " + tempString));
            }
            
            return taskName.toString();
        }
    }
}