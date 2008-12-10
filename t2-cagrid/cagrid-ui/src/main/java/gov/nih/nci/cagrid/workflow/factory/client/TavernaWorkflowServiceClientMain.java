package gov.nih.nci.cagrid.workflow.factory.client;

import gov.nih.nci.cagrid.common.Utils;

import java.io.File;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;

import workflowmanagementfactoryservice.WorkflowOutputType;
import workflowmanagementfactoryservice.WorkflowStatusType;

public class TavernaWorkflowServiceClientMain {
	
	public static void main(String [] args){
		
		System.out.println("Running the Grid Service Client");		
		System.out.println("OS: " + System.getProperty("user.home"));

		Map map = new HashMap();
		for (int i = 0; i< args.length; i++)
		{
			if(args[i].startsWith("-"))
			{
				map.put(args[i], args[++i]);						
			} else {
				TavernaWorkflowServiceClient.usage();
				System.exit(1);
			}
		}

		try{
			if(!(args.length < 4)){
				if(args[0].equals("-url")){

					
					// 1. Create Workflow Operations invoked.

					if(!map.containsKey("-scuflDoc"))
					{
						TavernaWorkflowServiceClient.usage();
					}
					String url = (String) map.get("-url");
					String scuflDoc = (String) map.get("-scuflDoc");
					TavernaWorkflowServiceClient client = new TavernaWorkflowServiceClient(url);

					String workflowName = "Test1";


					System.out.println("\n1. Running createWorkflow ..");

					//WMSOutputType wMSOutputElement =  client.createWorkflow(input);
					EndpointReferenceType resourceEPR = TavernaWorkflowServiceClient.setupWorkflow(url, scuflDoc, workflowName);

					System.out.println("Created a resource with EPR ..");
					System.out.println("Writing EPR to file ..");
					TavernaWorkflowServiceClient.writeEprToFile(resourceEPR, workflowName);

					//************************************************************************// 

					// 2. Start Workflow Operations Invoked.
					//

					String[] inputArgs = {"Taverna", " and caGrid"}; 
					
					//String[] inputArgs = {"caCore"}; 
					
					System.out.println(inputArgs.length);
					
					System.out.println("\n2. Now starting the workflow ..");
					System.out.println("Reading EPR from file ..");
					EndpointReferenceType readEPR = new EndpointReferenceType();
					try {
						System.out.println(System.getProperty("user.dir") + "/" + workflowName + ".epr");
						readEPR = TavernaWorkflowServiceClient.readEprFromFile(System.getProperty("user.dir") + "/"+ workflowName + ".epr");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					//This method runs the workflow with the resource represented by the EPR.
					// If there is no inputFile for the workflow, give "null"
					WorkflowStatusType workflowStatusElement =  TavernaWorkflowServiceClient.startWorkflow(inputArgs, readEPR);
					
					if (workflowStatusElement.equals(WorkflowStatusType.Done))
					{
						System.out.println("Workflow successfully executed..");
					}
					else
					{
						throw new Exception("Failed to execute the workflow! Please try again.");
					}
					
					

					// 3. Get Status operation invoked.
					System.out.println("\n3. Checking the status of the workflow..");
					WorkflowStatusType workflowStatus = TavernaWorkflowServiceClient.getStatus(readEPR);
					if(workflowStatus.equals(WorkflowStatusType.Done))
					{
						System.out.println("Workflow Executions is Completed.");
					}
					else if (workflowStatus.equals(WorkflowStatusType.Failed))
					{
						System.out.println("Workflow failed to execute.");
					}
					else if (workflowStatus.equals(WorkflowStatusType.Pending))
					{
						System.out.println("Workflow execution is pending.");
					}

					//4. Get output of workflow.

					System.out.println("\n4. Getting back the output file..");
					WorkflowOutputType workflowOutput = TavernaWorkflowServiceClient.getOutput(readEPR);
					
					String[] outputs = workflowOutput.getOutputFile();
					for (int i=0; i < outputs.length; i++)
					{
						String outputFile = System.getProperty("user.dir") + "/Test1-output-" + i + ".xml";
						Utils.stringBufferToFile(new StringBuffer(outputs[i]), outputFile);
						System.out.println("Output file " + i + " : " + outputFile);
					}

				} else {
					TavernaWorkflowServiceClient.usage();
					System.exit(1);
				}
			} else {
				TavernaWorkflowServiceClient.usage();
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
