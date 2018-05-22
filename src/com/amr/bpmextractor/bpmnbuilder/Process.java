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
 * Contributors:     Amr Yassin
 */

package com.amr.bpmextractor.bpmnbuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class Process {

    private final int incrementX = 200;
    private final int incrementY = 300;
	
    private String name;
    private String uuid;
    private ArrayList<Role> roles;

    public Process(String name){
        this.name = name;
        this.uuid = UUID.randomUUID().toString();
        this.roles = new ArrayList<>();
    }
    
    public String getName() { return name; }
    public ArrayList<Role> getRoles() { return roles; }
    
    public ArrayList<Element> getElements() {
        ArrayList<Element> elements = new ArrayList<>();
        for (Role role : roles) elements.addAll(role.getElements());
        return elements; 
    }

    public ArrayList<Link> getLinks() {
        ArrayList<Link> links = new ArrayList<>();;
        for (Element element : getElements()) links.addAll(element.getOutGoingLinks());
        return links; 
    }

    final void addRole(Role role) {
        roles.add(role);
    }
    
    public String toXML() {
        calcLocations();
        ArrayList<Element> processElements = getElements();
//        ArrayList<Element> processElements = getElements();
        ArrayList<Link> processLinks = getLinks();

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"Definitions_1c6otba\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" + 
                " \t<bpmn:collaboration id=\"BMPN_Extracted_Process\">\n";

        if (roles != null) for (int i = 0, len = roles.size(); i < len; i++) xml += roles.get(i).toXMLCollaboration();

        xml += "\t</bpmn:collaboration>\n" +
                "\t<bpmn:process id=\"" + name + "\" isExecutable=\"false\">\n";

        if (getElements() != null) for (int i = 0, len = processElements.size(); i < len; i++) xml += processElements.get(i).toXMLProcess();
        if (processLinks != null) for (int i = 0, len = processLinks.size(); i < len; i++) xml += processLinks.get(i).toXMLProcess();

        xml += "\t</bpmn:process>\n" + 
                "\t<bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "\t\t<bpmndi:BPMNPlane id=\"" + UUID.randomUUID().toString() + "\" bpmnElement=\"BMPN_Extracted_Process\">\n";

        if (roles != null)    for (int i = 0, len = roles.size(); i < len; i++)   xml += roles.get(i).toXMLDiagram();
        if (processElements != null) for (int i = 0, len = processElements.size(); i < len; i++) xml += processElements.get(i).toXMLDiagram();        
        if (processLinks != null)    for (int i = 0, len = processLinks.size(); i < len; i++)   xml += processLinks.get(i).toXMLDiagram();

        xml += "\t\t</bpmndi:BPMNPlane>\n" +
                "\t</bpmndi:BPMNDiagram>\n" + 
        "</bpmn:definitions>";

        return xml;
    }

    public void writeBMPNFile(String outputFile) {
        if (outputFile == null || outputFile.equals("")) return;
        
        File directory = new File(outputFile.substring(0, outputFile.lastIndexOf(File.separatorChar)));
        if (! directory.exists()) {
            directory.mkdirs();
        }
        
        verify();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));) {
            writer.write(this.toXML());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void verify() {
        for(Element element : getElements()) {
            
            int inlinks = element.getIncomingLinks().size();
            if(inlinks != 1 && !element.getName().equalsIgnoreCase("Start") && !element.getName().equalsIgnoreCase("join")) {
                System.err.println(element.getName() + " inComingLinks = " + inlinks);
                
                Link[] incomingLinks = new Link[element.getIncomingLinks().size()];
                incomingLinks = element.getIncomingLinks().toArray(incomingLinks);
                
                for (int i = 0; i < inlinks; i++) {
                    for (int j = i + 1; j < inlinks; j++)
                        if (incomingLinks[i].getSourceElement().getName().equals(incomingLinks[j].getSourceElement().getName()))
                            incomingLinks[i].delete();
                }
            }
            
            int outlinks = element.getOutGoingLinks().size();
            if(outlinks != 1 && !element.getName().equalsIgnoreCase("Stop") && !element.getName().equalsIgnoreCase("split")) {
                System.err.println(element.getName() + " outGoingLink = " + outlinks);
                
                Link[] outgoingLinks = new Link[element.getOutGoingLinks().size()];
                outgoingLinks = element.getOutGoingLinks().toArray(outgoingLinks);
            
	            for (int i = 0; i < outlinks; i++) {
	                for (int j = i + 1; j < outlinks; j++)
	                    if (outgoingLinks[i].getTargetElement().getName().equals(outgoingLinks[j].getTargetElement().getName()))
	                    	outgoingLinks[i].delete();
	            }
            }
        }
    }

    private void calcLocations() {
        final int originalX = 100;
        final int originalY = 100;
        int locationX = originalX;
        int locationY = originalY;

        
        
        if (roles != null && roles.size() > 0) {
            for (int i = 0, len = roles.size(); i < len; i++ ) {
				ArrayList<Element> roleElements = roles.get(i).getElements();
				int minX = originalX;
				int minY = originalY;				
				int maxX = locationX;
				int maxY = locationY;

				Element startElement = null;
				for (Element currentElement : roleElements) {
					if (currentElement.getName().equalsIgnoreCase("Start"))
						startElement = currentElement;
				}
				
				locationX = originalX;
				locationY = originalY;
				if (startElement != null) {
					startElement.setLocation(locationX, locationY);
					for (Link link : startElement.getOutGoingLinks()) {
						calcElement(link.getTargetElement());
					}
				}
				
				for (Element currentElement : roleElements) {
					if (currentElement.getLocationX() > maxX) maxX = currentElement.getLocationX();
					if (currentElement.getLocationY() > maxY) maxY = currentElement.getLocationY();
				}
				
				
                roles.get(i).setLocation(minX - 100, minY - 100);
                roles.get(i).setWidth(maxX - minX + 300);
                roles.get(i).setHeight(maxY - minY + 600);
            }
        }
    }
    
 
    private void calcElement(Element currentElement) {
    	ArrayList<Link> inLinks = currentElement.getIncomingLinks();

    	if (inLinks.size() == 0) {
    		return;
    		
    	} else if (inLinks.size()  == 1) { // Single Predecessor
			Element previousElement = inLinks.get(0).getSourceElement();
			int numberOFSiblings = 0;
			int orderWithinSiblings = 0;
			for (Link link : previousElement.getOutGoingLinks()) {
				numberOFSiblings++;
				if (link.getTargetElement().equals(currentElement)) orderWithinSiblings = numberOFSiblings;
			}
			
			int locationX = previousElement.getLocationX() + incrementX;
			int locationY = previousElement.getLocationY() + ((orderWithinSiblings - 1) * incrementY);
			currentElement.setLocation(locationX, locationY);

		} else {
			Element previousElement = inLinks.get(0).getSourceElement();
			int locationX = previousElement.getLocationX() + incrementX;
			int locationY = (previousElement.getLocationY() + ((inLinks.size() - 1) * incrementY)) / inLinks.size();
			currentElement.setLocation(locationX, locationY);
			
		}
    	
    	for (Link link : currentElement.getOutGoingLinks()) {
    		calcElement(link.getTargetElement());
    	}
	}

	@Override
    public String toString() {
        String str = "";
        ArrayList<Element> processElements = getElements();
        ArrayList<Link> processLinks = getLinks();

        if (processElements != null && processElements.size() > 0) {
            int i = 1;
            str += "\nTask No. |\tTasks Name: \n---------------------------------------------\n";
             for (Element element : processElements)  str += "Task [" + i++ + "] : \t" + element.getName() + "\n";
        } else {
            str += "No Tasks Available\n";    
        }
        
        if (processLinks != null && processLinks.size() > 0) {
            int i = 1;
            str += "\nLink No. |\tLinks\n---------------------------------------------\n";
            for (Link link: processLinks) str += "Link [" + i++ + "] : \t" + link.getName() + "\n";
            
        } else {
            str += "No Links Available\n";    
        }
        
        return str;
    }
}