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

import java.util.ArrayList;
import java.util.UUID;

public abstract class Element {

    public static class TYPE {
        public static final int START_EVENT = 0;
        public static final int END_EVENT = 1;
        public static final int PARALLEL_GATEWAY = 2;
        public static final int SERVICE_TASK = 3;
        public static final int USER_TASK = 4;
        // public static final int START_EVENT = 5;
    }

    public static class CONN_LOCATION {
        public static final int TOP = 0;
        public static final int BOTTOM = 1;
        public static final int LEFT = 2;
        public static final int RIGHT = 3;
    }

    protected int type;
    private String name;
    private String uuid;
    private String uuidDiagram;
    private ArrayList<Link> incomingLinks;
    private ArrayList<Link> outGoingLinks;

    protected int locationX;
    protected int locationY;
    protected int height;
    protected int width;

    protected int inLocation;
    protected int outLocation;

    Element(Role role, String name) {
        this.name = name;
        this.uuid = UUID.randomUUID().toString();
        this.uuidDiagram = UUID.randomUUID().toString();
        role.addElement(this);
        incomingLinks = new ArrayList<>();
        outGoingLinks = new ArrayList<>();

        inLocation = CONN_LOCATION.LEFT;
        outLocation = CONN_LOCATION.RIGHT;
    }

    public String getName() {
        return name;
    }

    public String getUUID() {
        return uuid;
    }
    
    public int getType() {
        return type;
    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getInConnectionX() {
        switch (inLocation) {
            case CONN_LOCATION.TOP:     return locationX + (width / 2);
            case CONN_LOCATION.BOTTOM:  return locationX + (width / 2);
            case CONN_LOCATION.LEFT:    return locationX;
            case CONN_LOCATION.RIGHT:   return locationX + width;
            default:                    return 0;
        }
    }

    public int getInConnectionY() {
        switch (inLocation) {
            case CONN_LOCATION.TOP:     return locationY;
            case CONN_LOCATION.BOTTOM:  return locationY + height;
            case CONN_LOCATION.LEFT:    return locationY + (height / 2);
            case CONN_LOCATION.RIGHT:   return locationY + (height / 2);
            default:                    return 0;
        }
    }

    public int getOutConnectionX() {
        switch (outLocation) {
            case CONN_LOCATION.TOP:     return locationX + (width / 2);
            case CONN_LOCATION.BOTTOM:  return locationX + (width / 2);
            case CONN_LOCATION.LEFT:    return locationX;
            case CONN_LOCATION.RIGHT:   return locationX + width;
            default:                    return 0;
        }
    }

    public int getOutConnectionY() {
        switch (outLocation) {
            case CONN_LOCATION.TOP:     return locationY;
            case CONN_LOCATION.BOTTOM:  return locationY + height;
            case CONN_LOCATION.LEFT:    return locationY + (height / 2);
            case CONN_LOCATION.RIGHT:   return locationY + (height / 2);
            default:                    return 0;
        }
    }

    public ArrayList<Link> getIncomingLinks() {
        return incomingLinks;
    }

    public ArrayList<Link> getOutGoingLinks() {
        return outGoingLinks;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setInLocation(int nLocation) {
        this.inLocation = nLocation;
    }

    public void setOutLocation(int outLocation) {
        this.outLocation = outLocation;
    }

    public void setLocation(int locationX, int locationY) {
        this.locationX = locationX;
        this.locationY = locationY;
    }

    String toXMLProcess() {
        String xmlTag;

        switch (type) {
            case TYPE.START_EVENT:
                xmlTag = "startEvent";
                break;
            case TYPE.END_EVENT:
                xmlTag = "endEvent";
                break;
            case TYPE.PARALLEL_GATEWAY:
                xmlTag = "exclusiveGateway";
                break;
            case TYPE.SERVICE_TASK:
                xmlTag = "task";
                break;
            case TYPE.USER_TASK:
                xmlTag = "task";
                break;
            default:
                xmlTag = "ERROR";
                break;
        }

        String xml = "\t\t<bpmn:" + xmlTag + " id=\"" + uuid + "\" name=\"" + name + "\">\n";
        if (incomingLinks != null && incomingLinks.size() > 0) {
            for (int i = 0, len = incomingLinks.size(); i < len; i++) {
                xml += "\t\t\t<bpmn:incoming>" + incomingLinks.get(i).getUuid() + "</bpmn:incoming>\n";
            }
        }

        if (outGoingLinks != null && outGoingLinks.size() > 0) {
            for (int i = 0, len = outGoingLinks.size(); i < len; i++) {
                xml += "\t\t\t<bpmn:outgoing>" + outGoingLinks.get(i).getUuid() + "</bpmn:outgoing>\n";
            }
        }

        xml += "\t\t</bpmn:" + xmlTag + ">\n";
        return xml;
    }

    String toXMLDiagram() {
        String xml = "\t\t\t<bpmndi:BPMNShape id=\"" + uuidDiagram + "\" bpmnElement=\"" + uuid + "\">\n"
                + "\t\t\t\t<dc:Bounds x=\"" + locationX + "\" y=\"" + locationY + "\" width=\"" + width + "\" height=\""
                + height + "\" />\n";

        if (type == TYPE.START_EVENT || type == TYPE.END_EVENT) {
            int labelX = locationX - 1;
            int labelY = locationY + 36;
            xml += "\t\t\t\t<bpmndi:BPMNLabel>\n" + "\t\t\t\t\t<dc:Bounds x=\"" + labelX + "\" y=\"" + labelY
                    + "\" width=\"38\" height=\"13\" />\n" + "\t\t\t\t</bpmndi:BPMNLabel>\n";
        }

        xml += "\t\t\t</bpmndi:BPMNShape>\n";
        return xml;
    }

    public static Element createElement(Role role, String name, int type) {
        switch (type) {
            case TYPE.START_EVENT:      return new StartEvent(role, name);
            case TYPE.END_EVENT:        return new EndEvent(role, name);
            case TYPE.PARALLEL_GATEWAY: return new ParallelGateway(role, name);
            case TYPE.SERVICE_TASK:     return new ServiceTask(role, name);
            case TYPE.USER_TASK:        return new UserTask(role, name);
            default:                    return null;
        }
    }

    public void addIncomingLink(Link link) {
        incomingLinks.add(link);
    }

    public void addOutgoingLink(Link link) {
        outGoingLinks.add(link);
    }

    public void removeIncomingLink(Link link) {
        incomingLinks.remove(link);        
    }
    
    public void removeOutgoingLink(Link link) {
        outGoingLinks.remove(link);        
    }
}

class StartEvent extends Element {
    StartEvent(Role role, String name) {
        super(role, name);
        type = TYPE.START_EVENT;
        width = 36;
        height = 36;
    }

    @Override
    public void setLocation(int locationX, int locationY) {
        super.setLocation(locationX, locationY + 20);
    }
}

class EndEvent extends Element {
    EndEvent(Role role, String name) {
        super(role, name);
        type = TYPE.END_EVENT;
        width = 36;
        height = 36;
    }

    @Override
    public void setLocation(int locationX, int locationY) {
        super.setLocation(locationX, locationY + 20);
    }
}

class ParallelGateway extends Element {
    ParallelGateway(Role role, String name) {
        super(role, name);
        type = TYPE.PARALLEL_GATEWAY;
        width = 50;
        height = 50;
    }

    @Override
    public void setLocation(int locationX, int locationY) {
        super.setLocation(locationX, locationY + 15);
    }
}

class ServiceTask extends Element {
    ServiceTask(Role role, String name) {
        super(role, name);
        type = TYPE.SERVICE_TASK;
        width = 100;
        height = 80;
    }
}

class UserTask extends Element {
    UserTask(Role role, String name) {
        super(role, name);
        type = TYPE.USER_TASK;
        width = 100;
        height = 80;
    }
}