package com.amr.bpmextractor.camunda;

import java.io.File;

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

public class Camunda {
	BpmnModelInstance modelInstance;
	
	public  Camunda() {
		  BpmnModelInstance modelInstance = Bpmn.createEmptyModel();
		  Definitions definitions = modelInstance.newInstance(Definitions.class);
		  definitions.setTargetNamespace("http://camunda.org/examples");
		  modelInstance.setDefinitions(definitions);

		  Process process = modelInstance.newInstance(Process.class);
		  definitions.addChildElement(process);

		  StartEvent startEvent = modelInstance.newInstance(StartEvent.class);
		  startEvent.setId("start");
		  process.addChildElement(startEvent);

		  UserTask userTask = modelInstance.newInstance(UserTask.class);
		  userTask.setId("task");
		  userTask.setName("User Task");
		  process.addChildElement(userTask);

		  SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
		  sequenceFlow.setId("flow1");
		  process.addChildElement(sequenceFlow);
		  connect(sequenceFlow, startEvent, userTask);

		  EndEvent endEvent = modelInstance.newInstance(EndEvent.class);
		  endEvent.setId("end");
		  process.addChildElement(endEvent);

		  sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
		  sequenceFlow.setId("flow2");
		  process.addChildElement(sequenceFlow);
		  connect(sequenceFlow, userTask, endEvent);

		  Bpmn.writeModelToFile(new File("out/new-process.bpmn"), modelInstance);
		}

		public void connect(SequenceFlow flow, FlowNode from, FlowNode to) {
		  flow.setSource(from);
		  from.getOutgoing().add(flow);
		  flow.setTarget(to);
		  to.getIncoming().add(flow);
		}


}
