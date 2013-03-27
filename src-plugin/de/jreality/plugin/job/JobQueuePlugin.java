package de.jreality.plugin.job;

import java.util.LinkedList;
import java.util.Queue;

import de.jtem.jrworkspace.plugin.Plugin;

public class JobQueuePlugin extends Plugin implements JobProcessorListener {

	protected Queue<Job>
		Q = new LinkedList<Job>();
	protected Job
		runningJob = null;
	
	public void queueJob(Job job) {
		synchronized (Q) {
			Q.offer(job);			
		}
		processJobs();
	}

	protected void processJobs() {
		synchronized (Q) {
			if (Q.isEmpty() || runningJob != null) return;
			Job job = Q.poll();
			JobProcessorThread processor = new JobProcessorThread(job);
			processor.addJobProcessorListener(this);
			processor.start();
			runningJob = job;
		}
	}
	
	@Override
	public void processStarted(Job job) {
	}
	@Override
	public void processFinished(Job job) {
		runningJob = null;
		processJobs();
	}
	@Override
	public void processFailed(Exception e, Job job) {
		runningJob = null;
		processJobs();
	}
	
}
