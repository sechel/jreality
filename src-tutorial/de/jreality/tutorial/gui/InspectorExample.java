package de.jreality.tutorial.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.TubeUtility;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tutorial.geom.ParametricSurfaceExample;
import de.jreality.tutorial.geom.TubeFactory02;
import de.jreality.tutorial.util.TextSlider;
import de.jreality.ui.viewerapp.Navigator;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * This class shows how to add GUI elements to the {@link ViewerApp} class. In particular, it
 * <ul>
 * <li>adds an inspection panel to the {@link Navigator}, and</li>
 * <li>adds a key listeners to the viewing component of the ViewerApp instance, and </li>
 * </ul>
 * 
 * @see TubeFactory02 (same class with different name)
 * @author Charles Gunn
 *
 */
public class InspectorExample {

	public static void main(String[] args) {
		TubeFactory02 tf02 = new TubeFactory02();
		tf02.doIt();
	}

}
