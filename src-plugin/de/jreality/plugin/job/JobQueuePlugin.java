package de.jreality.plugin.job;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jtem.jrworkspace.plugin.Plugin;

public class JobQueuePlugin extends Plugin implements JobProcessorListener {

	protected List<Job>
		Q = Collections.synchronizedList(new LinkedList<Job>());
	protected Job
		runningJob = null;

	public void queueJob(Job job) {
		synchronized (Q) {
			Q.add(job);			
		}
		processQueue();
	}
	
	protected void processQueue() {
		synchronized (Q) {
			if (Q.isEmpty() || runningJob != null) {
				return;
			}
			Job job = Q.get(0);
			processJob(job);
		}
	}
	
	protected void processJob(Job job) {
		JobProcessorThread processor = new JobProcessorThread(job);
		processor.addJobProcessorListener(this);
		processor.start();
		runningJob = job;
	}

	protected void finalizeJob(Job job) {
		assert runningJob == job;
		synchronized (Q) {
			Q.remove(runningJob);
			runningJob = null;
		}
		processQueue();
	}
	
	@Override
	public void processStarted(Job job) {
	}
	@Override
	public void processFinished(Job job) {
		finalizeJob(job);
	}
	@Override
	public void processFailed(Exception e, Job job) {
		finalizeJob(job);
	}
	
}
