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

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import com.amr.bpmextractor.util.Utils;
import com.amr.bpmextractor.bpmnbuilder.Process;
import com.amr.bpmextractor.bpmnbuilder.Role;
import com.amr.bpmextractor.bpmnbuilder.Element;
import com.amr.bpmextractor.bpmnbuilder.Link;;

public class BpmnExtractorEngine_Bkp2 {

    private String inputFile;
    private String outputFile;

    private int taskNo;
    ArrayList <Integer> currentTask;

    String[] tokens;
    String[] tags;
    double[] probs;

    Process process;
    Role currentRole;
    Element previousElement;
    Element currentElement;
    ArrayList<String> parallelTasks;

    public BpmnExtractorEngine_Bkp2(String inputFile, String outputFile) {
        taskNo = 1;
        currentTask = new ArrayList<>();
        process = new Process("Test Process");
        currentRole = new Role(process, "Employee");
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public Process processText() {
        analyzePOS();
        extractBPMN();
        process.writeBMPNFile(outputFile);
        System.out.print(process);
        return process;
    }
    
    private void analyzePOS() {
        InputStream tokenModelIn = null;
        InputStream posModelIn = null;

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String sentence = "";
            String temp;

            while ((temp = br.readLine()) != null) {
                sentence += temp + "\n";
            }

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

            System.out.println("No.\t:\tToken\t\t:\tTag\t:\tProbability\n---------------------------------------------");
            for (int i = 0; i < tokens.length; i++) {
                // if (tags[i].equalsIgnoreCase("NN"))
                System.out.println(i + "\t:\t" + tokens[i] + "\t\t:\t" + tags[i] + "\t:\t" + probs[i]);
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

            int i = 0;
            do {
                i = extractNextTask(i);
            } while (i < tokens.length);

            addEndEventToProcess();
            return process;
    }

    private int extractNextTask(int i) {
        switch (tags[i]) {
            case "VB":
            case "VBD":
            case "VBZ":
                if (i > 0 && tokens[i-1].equalsIgnoreCase("to") )
                    break;
                
                if (tokens[i].equalsIgnoreCase("do") && tokens[i + 1].equalsIgnoreCase("not"))
                    break;;

                if (tokens[i].equalsIgnoreCase("is") || tokens[i].equalsIgnoreCase("are")
                        || tokens[i].equalsIgnoreCase("don’t")|| tokens[i].equalsIgnoreCase("be"))
                    break;
                
                if (currentTask.size() > 0) addTaskToProcess();

                appendToTaskName(i);
                break;
                
            case "VBP":
                appendToTaskName(i);
                break;

            // case "PRP$":
            case "PRP":
            case "NN":
                if (currentTask.size() > 0) appendToTaskName(i);
                break;
            
            case "NNS":
                if (tokens[i].equalsIgnoreCase("–") || tokens[i].equalsIgnoreCase("it")
                        || tokens[i].equalsIgnoreCase("I") || tokens[i].equalsIgnoreCase("you")
                        || tokens[i].equalsIgnoreCase("yesterday") || tokens[i].equalsIgnoreCase("today")
                        || tokens[i].equalsIgnoreCase("tomorrow"))
                    break;
                
                if (tokens[i].equalsIgnoreCase("second") || tokens[i].equalsIgnoreCase("seconds")
                        || tokens[i].equalsIgnoreCase("minute") || tokens[i].equalsIgnoreCase("minutes")
                        || tokens[i].equalsIgnoreCase("hour") || tokens[i].equalsIgnoreCase("hours")
                        || tokens[i].equalsIgnoreCase("day") || tokens[i].equalsIgnoreCase("days")
                        || tokens[i].equalsIgnoreCase("week") || tokens[i].equalsIgnoreCase("weeks")
                        || tokens[i].equalsIgnoreCase("month") || tokens[i].equalsIgnoreCase("months")
                        || tokens[i].equalsIgnoreCase("year") || tokens[i].equalsIgnoreCase("years")) 
                    break;

//                        if (currentTaskTokens.size() > 0)
                    appendToTaskName(i);
                break;

            case "IN":
                if (currentTask.size() == 0 && 
                        (tokens[i].equalsIgnoreCase("of") || tokens[i].equalsIgnoreCase("with")
                        || tokens[i].equalsIgnoreCase("for") || tokens[i].equalsIgnoreCase("through")
                        || tokens[i].equalsIgnoreCase("on") || tokens[i].equalsIgnoreCase("in")
                        || tokens[i].equalsIgnoreCase("at") || tokens[i].equalsIgnoreCase("upon")
                        || tokens[i].equalsIgnoreCase("until")|| tokens[i].equalsIgnoreCase("as"))
                        ) 
                    break;
                
//                        if (currentTaskTokens.size() == 0) 
                    appendToTaskName(i);
                break;
                
            case "JJ":
                if (i > 0 && tags[i-1].equals("IN")) appendToTaskName(i);
                break;
                
            case "NNP":
            case "NNPS":
                if (i > 0 && tags[i-1].equals("JJ"))
                    break;
                
                appendToTaskName(i);
                break;
                
            case "RP":
                if (currentTask.size() > 0) appendToTaskName(i);
              break;
                
            case ".":
            case ":":
                addTaskToProcess();
                break;

            case "TO":
                if (tags[i + 1].equalsIgnoreCase("VB"))
                    i++; // skip next verb
                break;

            case "CC":
                if (getCurrentTaskName().length() > 0 && tokens[i].equalsIgnoreCase("and")) i = handleAND(i);
                if (getCurrentTaskName().length() > 0 && tokens[i].equalsIgnoreCase("or")) i = handleOR(i);
                break;
                
            case "VBN":
                if (i > 0 && tags[i-1].equals("VB")) appendToTaskName(i);
                break;
                
            case "VBG":
                addTaskToProcess();
                break;

            default:
                break;

        }
        
        if (currentTask.size() >= 6) addTaskToProcess();
        i++;
        return i;
    }

    private int handleAND(int i) {
        if (i > 0 && tokens[i].equalsIgnoreCase("and") && tags[i-1].equals("NN") && tags[i+1].equals("NN")) {
            appendToTaskName(i);
            appendToTaskName(i+1);
            i += 2;
        }
        
        if (i > 0 && tokens[i].equalsIgnoreCase("and") && tags[i+1].contains("VB")) {
            addTaskToProcess();
            appendToTaskName(i+1);
            i += 2;
        }
        
        return i;
    }
    
    private int handleOR(int i) {
        if (i > 0 && tokens[i].equalsIgnoreCase("or") && tags[i-1].equals("NN") && tags[i+1].equals("NN")) {
            appendToTaskName(i);
            appendToTaskName(i+1);
            i += 2;
        }
        
        if (i > 0 && tokens[i].equalsIgnoreCase("or") /* && (tags[i-1].equals("VB") || tags[i+1].equals("NN"))*/) {
            if (parallelTasks == null) parallelTasks = new ArrayList<>();
            parallelTasks.add(getCurrentTaskName()); 
            
            i = extractNextTask(i+1);
            if (getCurrentTaskName().length() > 0) {
                parallelTasks.add(getCurrentTaskName()); 
            }
            
            joinParallelTasksToProcess("OR", "Join", parallelTasks);
        }
        return i;
    }

    private void appendToTaskName(int i) {
        //if (currentTask == null) currentTask = new Task(tokens);
        currentTask.add(i);
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

    private void joinParallelTasksToProcess(String splitName, String joinName, ArrayList<String> taskNames) {
        if (taskNames.size() == 0) return;
        
        if (taskNames.size() == 1) {
            previousElement = currentElement;
            Element currentElement = Element.createElement(currentRole, taskNames.get(0), Element.TYPE.PARALLEL_GATEWAY);
            
            Link.createLink(previousElement.getName() + " -> " + currentElement.getName(), previousElement, currentElement);

        } else {    // more than one task
            Element split = Element.createElement(currentRole, splitName, Element.TYPE.PARALLEL_GATEWAY);
            
            Link.createLink(currentElement.getName() + " -> " + split.getName(), currentElement, split);
            
            Element join = Element.createElement(currentRole, joinName, Element.TYPE.PARALLEL_GATEWAY);
            
            for (int i = 0; i < taskNames.size(); i++) {
                Element element = Element.createElement(currentRole, taskNames.get(i), Element.TYPE.SERVICE_TASK);
                
                Link.createLink(split.getName() + " -> " + element.getName(), split, element);
                Link.createLink(element.getName() + " -> " + join.getName(), element, join);
            }
            previousElement = currentElement;
            currentElement = join;
        }
    }
    
    private void addTaskToProcess() {
        String currentTaskName = getCurrentTaskName();
        
        boolean hasVerb = false;
        for (int i = 0; i < currentTask.size(); i++) {
            if (tags[currentTask.get(0)].contains("NNP") || tags[currentTask.get(i)].contains("VB")) {
                hasVerb = true;
                break;
            }
            if (tags[currentTask.get(i)].contains("VB")) {
                hasVerb = true;
                break;
            }
        }
        
        if (currentTask.size() == 1 || !hasVerb) {
            // ignore collected tokens
            currentTask = new ArrayList<>();
            return;
        }
        
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
    
    private String getCurrentTaskName()
    {
        if (currentTask == null || currentTask.size() == 0) {
            return "";
        } else {
            StringBuffer taskName = new StringBuffer();
            for (int i = 0; i < currentTask.size(); i++) {
                taskName.append((i == 0 ? Utils.toCamelCase(tokens[currentTask.get(i)])  : (" " + tokens[currentTask.get(i)])));
            }
            
            return taskName.toString();
        }
    }
}