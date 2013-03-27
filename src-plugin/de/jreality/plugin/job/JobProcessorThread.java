package de.jreality.plugin.job;

import static java.util.Collections.synchronizedList;

import java.util.LinkedList;
import java.util.List;

public class JobProcessorThread extends Thread {
	
	private Job
		job = null;
	private List<JobProcessorListener>
		listeners = synchronizedList(new LinkedList<JobProcessorListener>());
	
	public JobProcessorThread(Job job) {
		super(job.getJobName());
		this.job = job;
	}
	
	@Override
	public void run() {
		fireProcessStarted(job);
		try {
			job.execute();
		} catch (Exception e) {
			fireProcessFailed(e, job);
		} catch (Throwable t) {
			System.err.println("Job failed with Error " + t);
		}
		fireProcessFinished(job);
	}
	
	public void addJobProcessorListener(JobProcessorListener l) {
		listeners.add(l);
	}
	public void removeJobProcessorListener(JobProcessorListener l) {
		listeners.remove(l);
	}
	
	
	protected void fireProcessStarted(Job job) {
		synchronized (listeners) {
			for (JobProcessorListener l : listeners) {
				l.processStarted(job);
			}
		}
	}

	protected void fireProcessFinished(Job job) {
		synchronized (listeners) {
			for (JobProcessorListener l : listeners) {
				l.processFinished(job);
			}
		}
	}
	
	protected void fireProcessFailed(Exception e, Job job) {
		synchronized (listeners) {
			for (JobProcessorListener l : listeners) {
				l.processFailed(e, job);
			}
		}
	}

	
}
