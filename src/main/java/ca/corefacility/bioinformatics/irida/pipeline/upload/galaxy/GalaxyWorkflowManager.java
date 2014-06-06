package ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.corefacility.bioinformatics.irida.exceptions.UploadException;
import ca.corefacility.bioinformatics.irida.exceptions.WorkflowException;
import ca.corefacility.bioinformatics.irida.exceptions.galaxy.GalaxyDatasetNotFoundException;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory;
import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryContentsProvenance;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryDetails;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputDefinition;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

/**
 * Handles submission of workflows to Galaxy.
 * @author Aaron Petkau <aaron.petkau@phac-aspc.gc.ca>
 *
 */
public class GalaxyWorkflowManager {
	
	private static final Logger logger = LoggerFactory.getLogger(GalaxyWorkflowManager.class);
		
	private GalaxyHistory galaxyHistory;
	private GalaxyInstance galaxyInstance;
	
	private enum WorkflowState {
		OK("ok"),
		RUNNING("running"),
		QUEUED("queued"),
		UNKNOWN("unknown");
		
		private static Map<String, WorkflowState> stateMap = new HashMap<String, WorkflowState>();
		private String stateString;
		
		static {
			stateMap.put(OK.toString(), OK);
			stateMap.put(RUNNING.toString(), RUNNING);
			stateMap.put(QUEUED.toString(), QUEUED);
		}
		
		private WorkflowState(String stateString){
			this.stateString = stateString;
		}
		
		public static WorkflowState stringToState(String stateString) {
			WorkflowState state = stateMap.get(stateString);
			if (state == null) {
				state = UNKNOWN;
			}
			
			return state;
		}
		
		@Override
		public String toString() {
			return stateString;
		}
	}
	
	private class WorkflowStatus {
		private WorkflowState state;
		private float percentComplete;
		
		public WorkflowStatus(WorkflowState state, float percentComplete) {
			checkNotNull(state, "state is null");
			
			this.state = state;
			this.percentComplete = percentComplete;
		}

		public WorkflowState getState() {
			return state;
		}

		public float getPercentComplete() {
			return percentComplete;
		}
	}
	
	/**
	 * Constructs a new GalaxyWorkflowSubmitter with the given information.
	 * @param galaxyInstance  A Galaxyinstance defining the Galaxy to submit to.
	 * @param galaxyHistory  A GalaxyHistory for methods on operating with Galaxy histories.
	 */
	public GalaxyWorkflowManager(GalaxyInstance galaxyInstance, GalaxyHistory galaxyHistory) {
		checkNotNull(galaxyInstance, "galaxyInstance is null");
		checkNotNull(galaxyHistory, "galaxyHistory is null");
		
		this.galaxyHistory = galaxyHistory;
		this.galaxyInstance = galaxyInstance;
	}
	
	private void checkWorkflowIdValid(String workflowId) throws WorkflowException {
		checkNotNull(workflowId, "workflow id is null");
		boolean invalid = false;
		
		WorkflowsClient workflowsClient = galaxyInstance.getWorkflowsClient();
		try {
			invalid = workflowsClient.showWorkflow(workflowId) == null;
		} catch (RuntimeException e) {
			invalid = true;
		}
		
		if (invalid) {
			throw new WorkflowException("workflow with id " + workflowId + " does not exist in Galaxy instance");
		}
	}
	
	/**
	 * Starts the execution of a workflow with a single fastq file and the given workflow id.
	 * @param inputFile  An input file to start the workflow. 
	 * @param workflowId  The id of the workflow to start.
	 * @throws GalaxyDatasetNotFoundException If there was an error uploading the Galaxy dataset.
	 * @throws UploadException If there was an error uploading the Galaxy dataset.
	 * @throws IOException If there was an error with the file.
	 * @throws WorkflowException If there was an error with running the workflow.
	 */
	public WorkflowOutputs runSingleFileWorkflow(Path inputFile, String workflowId, String workflowInputLabel)
			throws UploadException, GalaxyDatasetNotFoundException, IOException, WorkflowException {
		checkNotNull(inputFile, "file is null");
		checkNotNull(workflowInputLabel, "workflowInputLabel is null");
				
		checkArgument(inputFile.toFile().exists(), "inputFile " + inputFile + " does not exist");
		checkWorkflowIdValid(workflowId);
		
		WorkflowsClient workflowsClient = galaxyInstance.getWorkflowsClient();
		
		History workflowHistory = galaxyHistory.newHistoryForWorkflow();
		WorkflowDetails workflowDetails = workflowsClient.showWorkflow(workflowId);
		
		// upload dataset to history
		Dataset inputDataset = galaxyHistory.fileToHistory(inputFile, workflowHistory);
		
		// setup workflow inputs
		String workflowInputId = null;
		for(final Map.Entry<String, WorkflowInputDefinition> inputEntry : workflowDetails.getInputs().entrySet()) {
			final String label = inputEntry.getValue().getLabel();
			if(label.equals(workflowInputLabel)) {
				workflowInputId = inputEntry.getKey();
			}
		}

		if (workflowInputId != null) {
			WorkflowInputs inputs = new WorkflowInputs();
			inputs.setDestination(new WorkflowInputs.ExistingHistory(workflowHistory.getId()));
			inputs.setWorkflowId(workflowDetails.getId());
			inputs.setInput(workflowInputId, new WorkflowInputs.WorkflowInput(inputDataset.getId(), WorkflowInputs.InputSourceType.HDA));
			
			// execute workflow
			WorkflowOutputs output = workflowsClient.runWorkflow(inputs);
	
			logger.debug("Running workflow in history " + output.getHistoryId());
			
			return output;
		} else {
			throw new WorkflowException("Could not find workflow input: " + workflowInputId);
		}
	}
	
	private int getTotalHistoryItems(Map<String, List<String>> stateIds) {
		int sum = 0;
		
		for (String stateKey : stateIds.keySet()) {
			sum += stateIds.get(stateKey).size();
		}
		
		return sum;
	}
	
	private int getHistoryItemsInState(Map<String, List<String>> stateIds, WorkflowState state) {
		return stateIds.get(state.toString()).size();
	}
	
	private float getPercentComplete(Map<String, List<String>> stateIds) {
		return 100.0f*(getHistoryItemsInState(stateIds, WorkflowState.OK)/(float)getTotalHistoryItems(stateIds));
	}
	
	public WorkflowStatus getStatusFor(String historyId) {
		WorkflowStatus workflowStatus;
		
		WorkflowState workflowState;
		float percentComplete;
		
		HistoriesClient historiesClient = galaxyInstance.getHistoriesClient();
		HistoryDetails details = historiesClient.showHistory(historyId);
		workflowState = WorkflowState.stringToState(details.getState());
		
		Map<String, List<String>> stateIds = details.getStateIds();
		percentComplete = getPercentComplete(stateIds);
		
		workflowStatus = new WorkflowStatus(workflowState, percentComplete);
		
		logger.debug("Details for history " + details.getId() + ": state=" + details.getState());
		return workflowStatus;
	}
	
	public String getWorkflowInformationFor(WorkflowOutputs output) {
		HistoriesClient historiesClient = galaxyInstance.getHistoriesClient();
		String outputInformation = "";
		
		for(String outputId : output.getOutputIds()) {
			Dataset dataset = historiesClient.showDataset(output.getHistoryId(), outputId);
			HistoryContentsProvenance provenance = historiesClient.showProvenance(output.getHistoryId(), outputId);
			
			outputInformation += "output " + dataset.getName() + " generated from " +
					provenance.getToolId() + " with parameters " + provenance.getParameters();
		}
		
		return outputInformation;
	}
	
	public List<File> getWorkflowOutputFiles(WorkflowOutputs output) throws IOException {
		HistoriesClient historiesClient = galaxyInstance.getHistoriesClient();
		List<File> files = new ArrayList<File>();
		
		// workflow complete
		for(String outputId : output.getOutputIds()) {
			Dataset dataset = historiesClient.showDataset(output.getHistoryId(), outputId);
			URL downloadURL = new URL(dataset.getFullDownloadUrl());
			logger.debug("download URL=" + downloadURL);
			File outputFile = downloadURLToTempFile(downloadURL);
			
			logger.debug("output file " + dataset.getName() + " written to " + outputFile);
			files.add(outputFile);
		}
		
		return files;
	}
	
	private File downloadURLToTempFile(URL url) throws IOException {
		Path path = Files.createTempFile("galaxy_output", null);
		InputStream in = url.openStream();
		FileOutputStream fos = new FileOutputStream(path.toFile());

		int length = -1;
		byte[] buffer = new byte[4096];// buffer for portion of data from
		                                // connection
		while ((length = in.read(buffer)) > -1) {
		    fos.write(buffer, 0, length);
		}
		fos.close();
		in.close();
		
		return path.toFile();
	}
	
	public static void main(String[] args) throws UploadException, GalaxyDatasetNotFoundException, InterruptedException, IOException, WorkflowException {
		File fastqDir = new File("/home/aaron/workspace/irida-api/cholera-files-subsample/fastq");
		File[] fastqFiles = fastqDir.listFiles(new FileFilter(){
			public boolean accept(File pathname) {
				return pathname.getName().endsWith("fastq");
			}
		});
		fastqFiles = new File[]{fastqFiles[0]};
		Path fastqPath = fastqFiles[0].toPath();
		
		GalaxyInstance galaxyInstance = GalaxyInstanceFactory.get("http://localhost:8090", "4a88bc2bc57fe0ebf4cb4c6d8a464c02");
		GalaxySearch galaxySearch = new GalaxySearch(galaxyInstance);
		GalaxyHistory galaxyHistory = new GalaxyHistory(galaxyInstance, galaxySearch);
		GalaxyWorkflowManager workflowSubmitter = new GalaxyWorkflowManager(galaxyInstance, galaxyHistory);
		
		// for all input files
		List<WorkflowOutputs> outputsList = new ArrayList<WorkflowOutputs>();
		WorkflowOutputs outputs = workflowSubmitter.runSingleFileWorkflow(fastqPath, "f2db41e1fa331b3e", "fastq");
		outputsList.add(outputs);

		// poll history until all workflows completed
		do {
			List<WorkflowOutputs> outputsCopyList = new ArrayList<WorkflowOutputs>(outputsList);
			for (WorkflowOutputs output : outputsCopyList) {
				
				WorkflowStatus status =  workflowSubmitter.getStatusFor(output.getHistoryId());
				if (WorkflowState.OK.equals(status.getState())) {
					outputsList.remove(output);
					
					logger.debug(workflowSubmitter.getWorkflowInformationFor(output));
					logger.debug("all download files " + workflowSubmitter.getWorkflowOutputFiles(output));
				} else {
					logger.debug("workflow " + output.getHistoryId() + " complete " + status.getPercentComplete());
				}
			}
			
			logger.debug("Workflows remaining: " + outputsList.size());
			Thread.sleep(2000);
		} while (outputsList.size() > 0);
	}
}
