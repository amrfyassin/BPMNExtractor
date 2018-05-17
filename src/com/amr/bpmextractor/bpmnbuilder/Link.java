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

package com.amr.bpmextractor.bpmnbuilder;

import java.util.UUID;

public class Link {
	
	private String name;
	private String uuid;
	private String uuidDiagram;
	private Element sourceElement;
	private Element targetElement;
	
	public static Link createLink(String name, Element sourceElement, Element targetElement) {
	    return new Link(name, sourceElement, targetElement);
	}
	
	private Link(String name, Element sourceElement, Element targetElement) {
		this.name = name;
		this.uuid = UUID.randomUUID().toString();
		this.uuidDiagram = UUID.randomUUID().toString();
		this.sourceElement = sourceElement;
		this.targetElement = targetElement;
		sourceElement.addOutgoingLink(this);
		targetElement.addIncomingLink(this);
	}

	public String getName() { return name; }
	public String getUuid() { return uuid; }
	public Element getSourceElement() { return sourceElement; }
	public Element getTargetElement() { return targetElement; }
	
	String toXMLProcess() {
		String xml = "\t\t<bpmn:sequenceFlow id=\"" + uuid + "\" sourceRef=\"" + sourceElement.getUUID() + "\" targetRef=\"" + targetElement.getUUID() + "\" />\n";
		return xml;
	}
	
	String toXMLDiagram() {
		String xml = "\t\t\t<bpmndi:BPMNEdge id=\"" + uuidDiagram + "\" bpmnElement=\"" + uuid + "\">\n";
		if (targetElement.getInConnectionX() > sourceElement.getOutConnectionX()) {		
			xml += "\t\t\t\t<di:waypoint x=\"" + sourceElement.getOutConnectionX() + "\" y=\"" + sourceElement.getOutConnectionY() + "\" />\n"
			+ "\t\t\t\t<di:waypoint x=\"" + targetElement.getInConnectionX() + "\" y=\"" + targetElement.getInConnectionY() + "\" />\n";
			
		} else {
			int midPointY = (sourceElement.getOutConnectionY() + targetElement.getInConnectionY()) / 2;
			xml += "\t\t\t\t<di:waypoint x=\"" + sourceElement.getOutConnectionX() + "\" y=\"" + sourceElement.getOutConnectionY() + "\" />\n"
					+ "\t\t\t\t<di:waypoint x=\"" + sourceElement.getOutConnectionX() + "\" y=\"" + midPointY + "\" />\n"
					+ "\t\t\t\t<di:waypoint x=\"" + targetElement.getInConnectionX() + "\" y=\"" + midPointY + "\" />\n"
					+ "\t\t\t\t<di:waypoint x=\"" + targetElement.getInConnectionX() + "\" y=\"" + targetElement.getInConnectionY() + "\" />\n";
		}
		
		xml += "\t\t\t\t<bpmndi:BPMNLabel>\n" + 
				"\t\t\t\t\t<dc:Bounds x=\"384\" y=\"98\" width=\"0\" height=\"13\" />\n" + 
				"\t\t\t\t</bpmndi:BPMNLabel>\n" + 
				"\t\t\t</bpmndi:BPMNEdge>\n";
		return xml;
	}

    public void delete() {
        sourceElement.removeOutgoingLink(this);
        targetElement.removeIncomingLink(this);    
    }
}
