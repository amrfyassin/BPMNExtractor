package com.amr.bpmextractor.camunda;

import java.io.File;
import java.io.IOException;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.GatewayDirection;
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

public class Camunda4 {

	Camunda4(){
		BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("invoice")
			      .name("BPMN API Invoice Process")
			      .startEvent()
			        .name("Invoice received")
			      .userTask()
			        .name("Assign Approver")
			        .camundaAssignee("demo")
			      .userTask()
			        .id("approveInvoice")
			        .name("Approve Invoice")
			      .exclusiveGateway()
			        .name("Invoice approved?")
			        .gatewayDirection(GatewayDirection.Diverging)
			      .condition("yes", "${approved}")
			      .userTask()
			        .name("Prepare Bank Transfer")
			        .camundaFormKey("embedded:app:forms/prepare-bank-transfer.html")
			        .camundaCandidateGroups("accounting")
			      .serviceTask()
			        .name("Archive Invoice")
			        .camundaClass("org.camunda.bpm.example.invoice.service.ArchiveInvoiceService")
			      .endEvent()
			        .name("Invoice processed")
			      .moveToLastGateway()
			      .condition("no", "${!approved}")
			      .userTask()
			        .name("Review Invoice")
			        .camundaAssignee("demo")
			      .exclusiveGateway()
			        .name("Review successful?")
			        .gatewayDirection(GatewayDirection.Diverging)
			      .condition("no", "${!clarified}")
			      .endEvent()
			        .name("Invoice not processed")
			      .moveToLastGateway()
			      .condition("yes", "${clarified}")
			      .connectTo("approveInvoice")
			      .done();
		Bpmn.writeModelToFile(new File("out/camunda4.xml"), modelInstance);
	}
	
}
