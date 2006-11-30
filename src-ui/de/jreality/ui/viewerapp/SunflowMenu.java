package de.jreality.ui.viewerapp;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.Statement;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import de.jreality.scene.Viewer;
import de.jtem.beans.DimensionPanel;
import de.jtem.beans.InspectorPanel;

@SuppressWarnings("serial")
public class SunflowMenu extends JMenu {

	private static Class<?> SUNFLOW;
	private static FileFilter[] fileFilters;

	static {
		fileFilters = new FileFilter[3];
		fileFilters[0] = new de.jreality.ui.viewerapp.FileFilter("PNG Image", "png");
		fileFilters[1] = new de.jreality.ui.viewerapp.FileFilter("TGA Image", "tga");
		fileFilters[2] = new de.jreality.ui.viewerapp.FileFilter("HDR Image", "hdr");
	}

	private JFrame settingsFrame;
	private Object previewOptions;
	private Object renderOptions;
	private Object renderSameOptions;
	private DimensionPanel dimPanel;

	private ViewerApp va;

	public SunflowMenu(ViewerApp vapp) {
		super("Sunflow");
		va = vapp;
		try {
			SUNFLOW = AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
				public Class<?> run() throws ClassNotFoundException {
					return Class.forName("de.jreality.sunflow.Sunflow");
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("sunflow init failed", e); 
		}
		
		settingsFrame = new JFrame("Sunflow Settings");
		JTabbedPane tabs = new JTabbedPane();

		EmptyBorder border = new EmptyBorder(10, 10, 10, 10);
		
		previewOptions = createRenderOptions(-2, 0);
		InspectorPanel previewSettings = new InspectorPanel(false);
		previewSettings.setBorder(border);
		previewSettings.setObject(previewOptions, Collections
				.singleton("nothing"));
		tabs.add("thumb", previewSettings);

		renderSameOptions = createRenderOptions(-2, 0);
		InspectorPanel renderSameSettings = new InspectorPanel(false);
		renderSameSettings.setBorder(border);
		renderSameSettings.setObject(renderSameOptions, Collections
				.singleton("nothing"));
		tabs.add("preview", renderSameSettings);

		renderOptions = createRenderOptions(0, 2);

		InspectorPanel renderSettings = new InspectorPanel(false);
		renderSettings.setBorder(border);
		renderSettings.setObject(renderOptions, Collections
				.singleton("nothing"));
		tabs.add("render", renderSettings);

		settingsFrame.add(tabs);
		settingsFrame.pack();

		Action previewAction = new AbstractAction("preview") {
			public void actionPerformed(ActionEvent arg0) {
				render(va.getViewer(), va.getViewer().getViewingComponentSize(), getRenderSameOptions());
			}
		};
		previewAction.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK)
		);
		add(previewAction);
		
		add(new AbstractAction("thumb") {
			public void actionPerformed(ActionEvent arg0) {
				Dimension d = va.getViewer().getViewingComponentSize();
				render(va.getViewer(), new Dimension(d.width/3, d.height/3), getPreviewOptions());
			}
		});

		add(new AbstractAction("render") {
			public void actionPerformed(ActionEvent arg0) {
				renderAndSave(va.getViewer(), getRenderOptions());
			}
		});

		add(new AbstractAction("settings") {
			public void actionPerformed(ActionEvent arg0) {
				showSettingsInspector();
			}
		});
	}
	
	private Object createRenderOptions(int aaMin, int aaMax) {
		try {
			Object renderOptions = Class.forName("de.jreality.sunflow.RenderOptions").newInstance();
			new Statement(renderOptions, "setAaMin", new Object[]{aaMin}).execute();
			new Statement(renderOptions, "setAaMax", new Object[]{aaMax}).execute();
			return renderOptions;
		} catch (Throwable e) {
			throw new RuntimeException("sunflow missing", e);
		}
	}

	protected void renderAndSave(Viewer viewer, Object opts) {
		if (dimPanel == null) {
			dimPanel = new DimensionPanel();
			dimPanel.setDimension(new Dimension(800,600));
			TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dimension");
			dimPanel.setBorder(title);
		}
		File file = FileLoaderDialog.selectTargetFile(null, dimPanel, false, fileFilters);
		final Dimension dim = dimPanel.getDimension();
		try {
			new Statement(SUNFLOW, "renderAndSave", new Object[]{viewer, opts, dim, file}).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void render(Viewer viewer, Dimension dim, Object opts) {
		try {
			new Statement(SUNFLOW, "render", new Object[]{viewer, dim, opts}).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showSettingsInspector() {
		settingsFrame.setVisible(true);
		settingsFrame.toFront();
	}

	public Object getPreviewOptions() {
		return previewOptions;
	}

	public Object getRenderOptions() {
		return renderOptions;
	}

	public Object getRenderSameOptions() {
		return renderSameOptions;
	}

}
