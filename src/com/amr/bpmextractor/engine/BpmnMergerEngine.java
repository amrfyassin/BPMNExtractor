package com.amr.bpmextractor.engine;

import java.util.ArrayList;

import com.amr.bpmextractor.bpmnbuilder.Element;
import com.amr.bpmextractor.bpmnbuilder.Link;
import com.amr.bpmextractor.bpmnbuilder.Process;
import com.amr.bpmextractor.bpmnbuilder.Role;

public class BpmnMergerEngine {
    
    public static Process mergeProcesses(Process process1, Process process2) {
        Process mainProcess = process1;
        Process secondaryProcess = process2;
        
        if (process1.getElements().size() < process2.getElements().size()) {
            mainProcess = process2;
            secondaryProcess = process1;    
        }
        
        System.out.println("Merge Processes " + mainProcess.getName() + " and " + secondaryProcess.getName());
        Process mergedProcess = new Process(mainProcess.getName());
        Role role = new Role(mergedProcess, mainProcess.getRoles().get(0).getName());
        
        Element previousElement = null;
        
        for (Element taskProc1 : mainProcess.getElements()) {
            String taskName = taskProc1.getName();
            ArrayList<Element> process2elements = secondaryProcess.getElements();
            Element taskProc2 = findTaskInPorcess(taskProc1, secondaryProcess);
            
            if(taskProc2 == null) {
                Element newElemenet = Element.createElement(role, taskName, taskProc1.getType());
                
                if (previousElement != null) {
                    Link.createLink(previousElement.getName() + " -> " + newElemenet.getName(), previousElement, newElemenet);
                }
                
                previousElement = newElemenet;
                
            } else  if (taskProc2.getOutGoingLinks() != null && taskProc2.getOutGoingLinks().size() > 0) {  // merge tasks??????
                for (Link link : taskProc2.getOutGoingLinks()) {
                    
                    if (isEquivilant(taskProc1, link.getTargetElement())){
                        Element newElemenet1 = Element.createElement(role, taskName, taskProc1.getType());
                        Element newElemenet2 = Element.createElement(role, taskName, taskProc2.getType());
                        Element split = Element.createElement(role, "split", Element.TYPE.PARALLEL_GATEWAY);
                        Link.createLink(split + " -> " + newElemenet1.getName(), split, newElemenet1);
                        Link.createLink(split + " -> " + newElemenet2.getName(), split, newElemenet2);

                        if (previousElement != null) {
                            Link.createLink(previousElement.getName() + " -> split", previousElement, split);
                        }
                        
                        Element join = Element.createElement(role, "join", Element.TYPE.PARALLEL_GATEWAY);
                        Link.createLink(newElemenet1.getName() + " -> join",newElemenet1, join);
                        Link.createLink(newElemenet2.getName() + " -> join",newElemenet2, join);
                        previousElement = join;
                    }
                }

            } else {
                
                Element newElemenet1 = Element.createElement(role, taskName, taskProc1.getType());

                if (previousElement != null) {
                    Link.createLink(previousElement.getName() + " -> " + newElemenet1.getName(), previousElement, newElemenet1);
                }
                
                previousElement = newElemenet1;
            }
            
            process2elements.remove(taskProc2);
        }
        
        return mergedProcess;
    }

    private static Element findTaskInPorcess(Element inputTask, Process process) {
        for (Element processTask : process.getElements()) {
            if (isEquivilant(inputTask, processTask)) return processTask;
        }

        return null;
    }

    private static boolean isEquivilant(Element task1, Element task2) {
        return task2.getName().equalsIgnoreCase(task1.getName());   // TODO compare on meaning bases
    }

}
