package de.jreality.tutorial.plugin;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.PluginUtility;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.View;
import de.jreality.reader.Readers;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

/**
 * Example showing how to write a content providing plugin. Requires vrExamples.jar in the classpath, can
 * be found at www.jreality.de in the Download section.
 * 
 * @author weissman
 *
 */
public class ViewerVRExamples extends ShrinkPanelPlugin {

	String[][] examples = new String[][] {
			{ "Boy surface", "jrs/boy.jrs" },
			{ "Chen-Gackstatter surface", "obj/Chen-Gackstatter-4.obj" },
			{ "helicoid with 2 handles", "jrs/He2WithBoundary.jrs" },
			{ "tetranoid", "jrs/tetranoid.jrs" },
			{ "Wente torus", "jrs/wente.jrs" },
			{ "Schwarz P", "jrs/schwarz.jrs" },
			{ "Matheon baer", "jrs/baer.jrs" }
	};
	private HashMap<String, Integer> exampleIndices = new HashMap<String, Integer>();
	private Content content;

	private void makePanel() {
		if (examples != null) {
			ActionListener examplesListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String selectedBox = e.getActionCommand();
					int selectionIndex = ((Integer) exampleIndices.get(selectedBox)).intValue();
					try {
						SceneGraphComponent read = Readers.read(Input
								.getInput(examples[selectionIndex][1]));
						
						// The examples are aligned with z-axis pointing upwards. So we
						// rotate each example about the x-axis by -90 degrees.
						MatrixBuilder mb = MatrixBuilder.euclidean().rotateX(-Math.PI/2);
						if (read.getTransformation() != null) mb.times(read.getTransformation().getMatrix());
						mb.assignTo(read);
						
						getContent().setContent(read);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			};
			
			JRadioButton first = null;
			Box buttonGroupPanel = new Box(BoxLayout.Y_AXIS);
			ButtonGroup group = new ButtonGroup();
			for (int i = 0; i < examples.length; i++) {
				JRadioButton button = new JRadioButton(examples[i][0]);
				if (first == null) first = button;
				button.addActionListener(examplesListener);
				buttonGroupPanel.add(button);
				group.add(button);
				exampleIndices.put(examples[i][0], new Integer(i));
			}
			shrinkPanel.setLayout(new GridLayout());
			shrinkPanel.add(buttonGroupPanel);
			first.doClick();
		}
	}
	
	private Content getContent() {
		return content;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		content = PluginUtility.getPlugin(c, Content.class);
		makePanel();
		super.install(c);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("VR Examples", "jReality Group");
	}

	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addContentSupport(ContentType.TerrainAligned);
		v.addVRSupport();
		v.registerPlugin(new ViewerVRExamples());
		v.startup();
	}
}
