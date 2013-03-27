package de.jreality.plugin.job;

public abstract class AbstractCancellableJob extends AbstractJob implements CancelableJob {

	protected boolean
		cancelRequested = false;
	
	@Override
	public void requestCancel() {
		this.cancelRequested = true;
	}
	public boolean isCancelRequested() {
		return cancelRequested;
	}
	
	protected void fireJobCancelled(Job job) {
		synchronized (listeners) {
			for (JobListener l : listeners) {
				l.jobCancelled(job);
			}
		}
	}

}
