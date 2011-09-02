package de.jreality.ui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import de.jreality.plugin.icon.ImageHook;
import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;

public class JRealitySplashScreen extends SplashScreen {

	private static final long 
		serialVersionUID = 1L;
	private JLabel
		image = new JLabel(ImageHook.getIcon("splashJReality01.png"));
	private JProgressBar
		progressBar = new JProgressBar(0, 100);
	
	public JRealitySplashScreen() {
		super();
		setLayout(new BorderLayout());
		add(image, BorderLayout.CENTER);
		add(progressBar, BorderLayout.SOUTH);
		progressBar.setStringPainted(true);
	}
	
	@Override
	public void setStatus(String status) {
		progressBar.setString(status);
	}

	@Override
	public void setProgress(double progress) {
		progressBar.setValue((int)(progress * 100));
	}

}
