package de.jreality.plugin.job;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractJob implements Job {

	protected List<JobListener>
		listeners = Collections.synchronizedList(new LinkedList<JobListener>());
	
	@Override
	public void addJobListener(JobListener l) {
		listeners.add(l);
	}
	@Override
	public void removeJobListener(JobListener l) {
		listeners.remove(l);
	}

	protected void fireJobStarted(Job job) {
		synchronized (listeners) {
			for (JobListener l : listeners) {
				l.jobStarted(this);
			}
		}
	}
	
	protected void fireJobProgress(Job job, double progress) {
		synchronized (listeners) {
			for (JobListener l : listeners) {
				l.jobProgress(job, progress);
			}
		}
	}

	protected void fireJobFinished(Job job) {
		synchronized (listeners) {
			for (JobListener l : listeners) {
				l.jobFinished(job);
			}
		}
	}
	
	@Override
	public String toString() {
		return getJobName();
	}
	
}
