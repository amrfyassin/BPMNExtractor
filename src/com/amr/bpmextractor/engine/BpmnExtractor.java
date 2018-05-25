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

public class BpmnExtractor {
	
	public static void main(String[] args) {
		int i = 1;
//		String inputFile = "data/unstructuredProcess" + i + ".txt";
//		String outputFile = "out/bpmn" + i + ".xml";
		
		String inputFile = "data/employeeevaluation/sample" + i + ".txt";
		String outputFile = "out/employeeevaluation/process" + i + ".xml";
		

		BpmnExtractorEngine pt = new BpmnExtractorEngine(inputFile, outputFile);
		pt.processText();
	}

}