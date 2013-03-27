package de.jreality.plugin.job;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class JobMonitorPlugin extends ShrinkPanelPlugin {

	private JobQueuePlugin
		Q = null;
	private JobSynchronzerThread
		synchronzerThread = new JobSynchronzerThread();
	private QueueTableModel
		model = new QueueTableModel();
	private JTable
		queueTabel = new JTable();
	private JScrollPane
		queueScroller = new JScrollPane(queueTabel);
	private Map<Job, Double>
		progressMap = new HashMap<Job, Double>();
	private JobAdapter
		jobAdapter = new JobAdapter();
	
	public JobMonitorPlugin() {
		shrinkPanel.setTitle("Job Monitor");
		shrinkPanel.setLayout(new GridLayout());
		shrinkPanel.add(queueScroller);
		
		queueScroller.setPreferredSize(new Dimension(200, 150));
		queueTabel.getTableHeader().setPreferredSize(new Dimension(0, 0));
		queueTabel.setFillsViewportHeight(true);
	}
	
	private class JobSynchronzerThread extends Thread {
		
		public JobSynchronzerThread() {
			super("jReality Job Monitor");
		}
		
		@Override
		public void run() {
			while (true) {
				synchronizeJobQueue();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
		}
		
	}
	
	private class JobAdapter implements JobListener {

		@Override
		public void jobStarted(Job job) {
			synchronizeJobQueue();
		}
		@Override
		public void jobProgress(Job job, double progress) {
			progressMap.put(job, progress);
			synchronizeJobQueue();
		}
		@Override
		public void jobFinished(Job job) {
			synchronizeJobQueue();
		}
		@Override
		public void jobCancelled(Job job) {
			synchronizeJobQueue();
		}
		
	}
	
	private class QueueTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return Q.Q.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Job job = Q.Q.get(row);
			String name = job.getJobName();
			if (progressMap.containsKey(job)) {
				double progress = progressMap.get(job);
				name += ": " + (int)(100 * progress) + "%";
			}
			return name;
		}
		
	}
	
	public void synchronizeJobQueue() {
		queueTabel.revalidate();
		queueTabel.repaint();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		Q = c.getPlugin(JobQueuePlugin.class);
		Q.addJobListener(jobAdapter);
		synchronzerThread.start();
		queueTabel.setModel(model);
	}
	
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.name = "Job Monitor Plugin";
		info.vendorName = "Stefan Sechelmann";
		info.email = "sechel@math.tu-berlin.de";
		return info;
	}

}
