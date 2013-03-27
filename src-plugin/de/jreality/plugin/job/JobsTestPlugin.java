package de.jreality.plugin.job;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jreality.ui.JRealitySplashScreen;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;

public class JobsTestPlugin extends ShrinkPanelPlugin implements ActionListener {

	private JButton
		button = new JButton("Queue Test Job"),
		cancelButton = new JButton("Cancel Last Job");
	private JobQueuePlugin
		Q = null;
	private CancelableJob
		lastJob = null;
	
	public JobsTestPlugin() {
		shrinkPanel.setTitle("Job Test Plugin");
		shrinkPanel.setLayout(new GridLayout(2, 1));
		shrinkPanel.add(button);
		shrinkPanel.add(cancelButton);
		button.addActionListener(this);
		cancelButton.addActionListener(this);
	}
	
	public class TestJob extends AbstractCancellableJob {
		
		@Override
		public String getJobName() {
			return "Test Job";
		}
		
		@Override
		public void execute() throws Exception {
			fireJobStarted(this);
			for (int i = 0; i < 100; i++) {
				if (isCancelRequested()) {
					fireJobCancelled(this);
					return;
				}
				Thread.sleep(50);
				double progress = (i + 1) / 100.0;
				System.out.println("job progress: " + progress);
				fireJobProgress(this, progress);
			}
			fireJobFinished(this);
		}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (button == e.getSource()) {
			lastJob = new TestJob();
			Q.queueJob(lastJob);
		}
		if (cancelButton == e.getSource()) {
			if (lastJob == null) return;
			lastJob.requestCancel();
		}
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		Q = c.getPlugin(JobQueuePlugin.class);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	public static void main(String[] args) {
		SplashScreen splash = new JRealitySplashScreen();
		splash.setVisible(true);
		JRViewer v = new JRViewer();
		v.setShowPanelSlots(true, false, false, false);
		v.getController().setPropertyEngineEnabled(false);
		v.addBasicUI();
		v.addContentUI();
		v.registerPlugin(JobMonitorPlugin.class);
		v.registerPlugin(JobsTestPlugin.class);
		v.setSplashScreen(splash);
		v.startup();
		splash.setVisible(false);
	}
	
	
}
