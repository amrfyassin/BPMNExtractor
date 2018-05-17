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

import java.util.ArrayList;
import java.util.UUID;

public class Role {

    private String name;
    private String uuid;
    private String uuidDiagram;
    private ArrayList<Element> elements;
    private String processName;

    protected int locationX;
    protected int locationY;
    protected int height;
    protected int width;
    
    public Role(Process process, String name) { 
        this.name = name;
        this.uuid = UUID.randomUUID().toString();
        this.uuidDiagram = UUID.randomUUID().toString();
        this.elements = new ArrayList<>();
        process.addRole(this);
        processName = process.getName();
    }

    public String getName() { return name; }
    public String getUuid() { return uuid; }
    public void setHeight(int height) { this.height = height; }
    public void setWidth(int width) { this.width = width; }

    public void setLocation(int locationX, int locationY) { 
        this.locationX = locationX; 
        this.locationY = locationY; 
    }
    
    public ArrayList<Element> getElements() { return elements; }

    public void addElement(Element element) {
        elements.add(element);
    }
    
    public String toXMLCollaboration() {
        String xml =  "\t\t<bpmn:participant id=\"" + uuid + "\" name=\"" + name + "\" processRef=\"" + processName + "\" />\n";
        return xml;
    }
    
//    public String toXMLProcess() {
//        String xml = "\t\t<bpmn:laneSet>\n" + 
//                "\t\t\t<bpmn:lane id=\"Lane_1nk0egh\" name=\"1\">\n";
//        
//        for (Element element : elements)
//        	xml += "\t\t\t\t<bpmn:flowNodeRef>" + element.getUUID() + "</bpmn:flowNodeRef>\n"; 
//        
//        xml += "\t\t\t</bpmn:lane>\n" + 
//                "\t\t</bpmn:laneSet>\n"; 
//        return xml;
//    }

    public String toXMLDiagram() {
        String xml = "\t\t\t<bpmndi:BPMNShape id=\"" + uuidDiagram + "i\" bpmnElement=\"" + uuid + "\">\n" + 
                "\t\t\t\t<dc:Bounds x=\"" + locationX + "\" y=\"" + locationY + "\" width=\"" + width + "\" height=\"" + height + "\" />\n" + 
                "\t\t\t</bpmndi:BPMNShape>\n";
        
        return xml;
    }
}
