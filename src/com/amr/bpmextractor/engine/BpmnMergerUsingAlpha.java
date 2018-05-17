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

import java.io.File;
import java.util.ArrayList;

import com.amr.bpmextractor.alphaalgorithm.AlphaMatrix;
import com.amr.bpmextractor.bpmnbuilder.Process;
import com.amr.bpmextractor.bpmnbuilder.Role;

public class BpmnMergerUsingAlpha {
	
	public static void main(String[] args) {
        String processFolder = "carloan";
//        String processFolder = "sameProcess";
	    String inputPathPrefix = "data/" + processFolder + "/sample";
        String outputPathPrefix = "out/" + processFolder + "/sample";
        ArrayList<Process> processes = new ArrayList<>();
        Process mergedProcess = new Process("Merged");
        Role role = new Role(mergedProcess, "Employee");

        int i = 0;
	    while (new File (inputPathPrefix + ++i + ".txt").exists()) {
    		String inputFile = inputPathPrefix + i + ".txt";
    		String outputFile = outputPathPrefix + i + ".xml";
    
    		BpmnExtractorEngine extractor = new BpmnExtractorEngine(inputFile, outputFile);
    	    processes.add(extractor.processText());
	    }
	    
	    AlphaMatrix matrix = new AlphaMatrix(mergedProcess);
	    for (Process process : processes) {
	        matrix.addProcess(process);
	    }
	    
	    matrix.toString();
	    matrix.getProcess().writeBMPNFile(outputPathPrefix + "mergedprocess.xml");
	}
}