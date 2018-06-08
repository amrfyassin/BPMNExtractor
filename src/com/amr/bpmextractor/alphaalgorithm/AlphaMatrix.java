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
 * Contributors:    Amr Yassin
 */
package com.amr.bpmextractor.alphaalgorithm;

import java.util.ArrayList;

import com.amr.bpmextractor.bpmnbuilder.Element;
import com.amr.bpmextractor.bpmnbuilder.Link;
import com.amr.bpmextractor.bpmnbuilder.Process;
import com.amr.bpmextractor.bpmnbuilder.Role;


public class AlphaMatrix {

    public static class CONNECTION{
        public static final char NONE        = '#';
        public static final char RIGHT       = '⟹';
        public static final char LEFT        = '⟸';
        public static final char PARALLEL    = '‖';
    }
    
    private char[][] navCube;
    private String processName;
    private String roleName;
    Element[] elements;
    Element[] inElements;
    Element[] outElements;
    
    private ArrayList<String> tasks;
    
    public AlphaMatrix(Process process){
        tasks = new ArrayList<>();
        navCube = new char[5][5];
        for (int i = 0 ; i < navCube.length; i++) {
            for(int j = 0; j < navCube[i].length; j++) navCube[i][j] = '#';
        }
        processName = process.getName();
        roleName = process.getRoles().get(0).getName();
        addProcess(process);
    }

    public void addProcess(Process process) { 
        for (Element element : process.getElements()) {
            addElement(element);
            System.out.println("Adding " + element.getName());
        }
        
        for (Element element : process.getElements()) {
            addElementNavigation(element);        
        }
        
        System.out.println(toString());
    }

    private void addElement(Element element) {
        int index = tasks.indexOf(element.getName());
        if (index < 0) {
            tasks.add(element.getName());
            verifyMatrixSize(tasks.size());
        }
    }

    private void addElementNavigation(Element element) {
        int fromIndex = tasks.indexOf(element.getName());
        for (Link link : element.getOutGoingLinks()) {
            int toIndex = tasks.indexOf(link.getTargetElement().getName());
            
            switch (navCube[fromIndex][toIndex]) {
                case CONNECTION.NONE: 
                    navCube[fromIndex][toIndex] = CONNECTION.RIGHT;
                    navCube[toIndex][fromIndex] = CONNECTION.LEFT;
                    break;
                 
                case CONNECTION.LEFT: 
                    navCube[fromIndex][toIndex] = CONNECTION.PARALLEL;
                    navCube[toIndex][fromIndex] = CONNECTION.PARALLEL;
                    break;
                    
                case CONNECTION.RIGHT:
                case CONNECTION.PARALLEL: 
                default:
                    break;
            }
        }
    }

    private void verifyMatrixSize(int newSize) {
        int oldSize = navCube.length;
        if (newSize > oldSize) { 
            char[][] newMatrix = new char[newSize][newSize];
            
            for (int i = 0; i < newMatrix.length; i++) {
                for(int j = 0; j < newMatrix[i].length; j++) newMatrix[i][j] = '#';
            }
            
            for (int i = 0; i < navCube.length; i++) {
                for(int j = 0; j < navCube[i].length; j++) newMatrix[i][j] = navCube[i][j];
            }
           
            navCube = newMatrix;
        }
    }
    
    @Override
    public String toString() {
        String result = "\nSize of the Cube = " +  navCube.length + "\n\n";
        for (int i = 0 ; i < tasks.size(); i++) {
            result += "[" + (i + 1) + "] " + tasks.get(i) + "\n";
        }
        
        for (int i = 1 ; i < navCube.length + 1; i++) result += "\t" + i;
        result += "\n";

        for (int i = 0 ; i < navCube.length; i++) {
            result += (i + 1);
            for(int j = 0; j < navCube[i].length; j++) result += "\t" + navCube[i][j]; 
            result += "\n";
        }
        
        return result;
    }

    public Process getProcess() {
        int size = tasks.size();
        Process process = new Process(processName);
        Role role = new Role(process, roleName);
        elements = new Element[size];
        inElements = new Element[size];
        outElements = new Element[size];
        
        int i = 0;
        for (String taskName : tasks) {
            int taskType;
            switch (taskName.toLowerCase()) {
                case "start":     taskType = Element.TYPE.START_EVENT; break;
                case "end":     taskType = Element.TYPE.END_EVENT; break;
                default: taskType = Element.TYPE.START_EVENT; break;
            }
            
            elements[i] = Element.createElement(role, taskName, taskType);
            inElements[i] = elements[i];
            outElements[i] = elements[i];
            i++;
        }
        
        for(i = 0; i < size; i++) {
            for(int j = 0; j < i; j ++) {
                switch (navCube[i][j]) {
                    case CONNECTION.LEFT:                           
                        addInputJoin(role, i); // TODO Check if needed
                        addOutputSplit(role, j); // TODO Check if needed
                        Link.createLink(elements[j].getName() + " -> " + elements[i].getName(), outElements[j], inElements[i]);
                        break;
                        
                    case CONNECTION.RIGHT:
                        addOutputSplit(role, i); // TODO Check if needed
                        addInputJoin(role, j); // TODO Check if needed
                        Link.createLink(elements[i].getName() + " -> " + elements[j].getName(), outElements[i], inElements[j]);
                        break;

                    case CONNECTION.PARALLEL:
                        Element split;
                        if (inElements[i].getName().equalsIgnoreCase("split")) {
                            split = inElements[i];
                            
                        } else if (inElements[j].getName().equalsIgnoreCase("split")) {
                            split = inElements[j];
                            
                        } else {
                            split = Element.createElement(role, "split", Element.TYPE.PARALLEL_GATEWAY);
                        }
                        
                        Element join; 
                        if (outElements[i].getName().equalsIgnoreCase("join")){
                            join = outElements[i];
                            
                        } else if (outElements[j].getName().equalsIgnoreCase("join")) {
                            join = outElements[j];
                            
                        } else {
                            join = Element.createElement(role, "join", Element.TYPE.PARALLEL_GATEWAY);
                        }
                        
                           replaceLinks(elements[i], split, join);
                           replaceLinks(elements[j], split, join);
                        
                        Link.createLink(split + " -> " + elements[i].getName(), split, inElements[i]);
                        Link.createLink(split + " -> " + elements[j].getName(), split, inElements[j]);
                        
                        Link.createLink(elements[i].getName() + " -> join", outElements[i], join);
                        Link.createLink(elements[j].getName() + " -> join", outElements[j], join);
                        
                        inElements[i] = split; 
                        inElements[j] = split;
                        outElements[i] = join; 
                        outElements[j] = join;                        
                        break;
                        
                    case CONNECTION.NONE: 
                    default:
                        break;
                }
            }
        }
        
        return process;
    }

    private void addOutputSplit(Role role, int index) {
        if (outElements[index].getOutGoingLinks().size() == 1 && outElements[index] == elements[index]) {
            Element split = null;
//            Link link = elements[index].getOutGoingLinks().get(0);
//            if (link.getSourceElement().getName().equalsIgnoreCase("split")) {
//                split = link.getSourceElement();
//            }
            
            split = split == null ? Element.createElement(role, "split", Element.TYPE.PARALLEL_GATEWAY) : split;
            replaceLinks(elements[index], inElements[index], split);
            Link.createLink("split" + " -> " + elements[index].getName(), outElements[index], split);
            outElements[index] = split;
        }
    }

    private void addInputJoin(Role role, int index) {
        if (inElements[index].getIncomingLinks().size() == 1 && inElements[index] == elements[index]) {
            Element join = null;
//            Link link = elements[index].getIncomingLinks().get(0);
//            if (link.getSourceElement().getName().equalsIgnoreCase("join")) {
//                join = link.getSourceElement();
//            }
            
            join = join == null ? Element.createElement(role, "join", Element.TYPE.PARALLEL_GATEWAY) : join;
            replaceLinks(elements[index], join, outElements[index]);
            Link.createLink("join" + " -> " + elements[index].getName(), join, inElements[index]);
            inElements[index] = join;
        }
    }
    
    private void replaceLinks(Element task, Element input, Element output) {
        Link[] incomingLinks = new Link[task.getIncomingLinks().size()];
        incomingLinks = task.getIncomingLinks().toArray(incomingLinks);
        
        Link[] outgoingLinks = new Link[task.getOutGoingLinks().size()];
        outgoingLinks = task.getOutGoingLinks().toArray(outgoingLinks);

        for (Link link : incomingLinks) {
            Element sourceElement = link.getSourceElement();
            Link.createLink(input.getName(), sourceElement, input);
            link.delete();
        }
        
        for (Link link : outgoingLinks) {
            Element targetElement = link.getTargetElement();
            Link.createLink(output.getName(), output, targetElement);
            link.delete();
        }
    }
    
    private int findTaskInPorcess(String inputTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (isEquivilant(inputTask, tasks.get(i))) return i;
        }

        return -1;
    }

    private boolean isEquivilant(String task1, String task2) {
        return task2.equalsIgnoreCase(task1);   // TODO compare on meaning bases
    }
}
