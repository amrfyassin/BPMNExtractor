package com.amr.bpmextractor.camunda;

import java.io.File;
import java.io.IOException;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.Process;

public class Camunda2 {

	BpmnModelInstance modelInstance;
	
	public Camunda2() {
		// create an empty model
		BpmnModelInstance modelInstance = Bpmn.createEmptyModel();
		Definitions definitions = modelInstance.newInstance(Definitions.class);
		definitions.setTargetNamespace("http://camunda.org/examples");
		modelInstance.setDefinitions(definitions);

		// create the process
		  Process process = modelInstance.newInstance(Process.class);
		  definitions.addChildElement(process);

		// create elements
		StartEvent startEvent = createElement(process, "start", StartEvent.class);
		ParallelGateway fork = createElement(process, "fork", ParallelGateway.class);
		ServiceTask task1 = createElement(process, "task1", ServiceTask.class);
		task1.setName("Service Task");
		UserTask task2 = createElement(process, "task2", UserTask.class);
		task2.setName("User Task");
		ParallelGateway join = createElement(process, "join", ParallelGateway.class);
		EndEvent endEvent = createElement(process, "end", EndEvent.class);

		// create flows
		createSequenceFlow(process, startEvent, fork);
		createSequenceFlow(process, fork, task1);
		createSequenceFlow(process, fork, task2);
		createSequenceFlow(process, task1, join);
		createSequenceFlow(process, task2, join);
		createSequenceFlow(process, join, endEvent);

		// validate and write model to file
		Bpmn.validateModel(modelInstance);
			File file;
			try {
				file = File.createTempFile("out/bpmn-model-api-", ".bpmn");
				Bpmn.writeModelToFile(file, modelInstance);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	    private <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
		  T element = modelInstance.newInstance(elementClass);
		  element.setAttributeValue("id", id, true);
		  parentElement.addChildElement(element);
		  return element;
		}
	    
	    public SequenceFlow createSequenceFlow(Process process, FlowNode from, FlowNode to) {
	    	  String identifier = from.getId() + "-" + to.getId();
	    	  SequenceFlow sequenceFlow = createElement(process, identifier, SequenceFlow.class);
	    	  process.addChildElement(sequenceFlow);
	    	  sequenceFlow.setSource(from);
	    	  from.getOutgoing().add(sequenceFlow);
	    	  sequenceFlow.setTarget(to);
	    	  to.getIncoming().add(sequenceFlow);
	    	  return sequenceFlow;
	    	}


}
