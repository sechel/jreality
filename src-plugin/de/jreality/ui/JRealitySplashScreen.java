package de.jreality.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import de.jreality.plugin.icon.ImageHook;
import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;

public class JRealitySplashScreen extends SplashScreen {

	private static final long 
		serialVersionUID = 1L;
	private Icon
		icon = ImageHook.getIcon("splashJReality01.png");
	private JLabel
		image = new JLabel(icon);
	private JProgressBar
		progressBar = new JProgressBar(0, 100);
	
	
	public JRealitySplashScreen(Icon imageIcon) {
		this.icon = imageIcon;
		this.image = new JLabel(imageIcon);
		makeLayout();
	}
	
	public JRealitySplashScreen() {
		makeLayout();
	}
	
	private void makeLayout() {
		setLayout(new BorderLayout());
		add(image, BorderLayout.CENTER);
		add(progressBar, BorderLayout.SOUTH);
		progressBar.setStringPainted(true);
		Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
		setPreferredSize(size);
	}
	
	@Override
	public void setStatus(String status) {
		progressBar.setString(status);
		paint(getGraphics());
	}

	@Override
	public void setProgress(double progress) {
		progressBar.setValue((int)(progress * 100));
		paint(getGraphics());
	}

}
