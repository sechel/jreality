/*
 * Created on Oct 31, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.examples.jogl;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.SphereHelper;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.reader.OOGLReader;
import de.jreality.reader.PolymakeParser;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReadFromFileDemo extends InteractiveViewerDemo {

	boolean useLOD = false;
	static String resourceDir = "/Users/gunn/Documents/Models",
		initialFile = null;
	
	static {
		String foo = System.getProperty("resourceDir");
		if (foo != null) resourceDir = foo;
		foo = System.getProperty("initialFile");
		if (foo != null) initialFile = foo;
	}
	public JMenuBar createMenuBar()	{
		theMenuBar = new JMenuBar(); //super.createMenuBar();
		JMenu testM = new JMenu("File");
		final JMenuItem jcc = new JMenuItem("Open...");
		testM.add(jcc);
		jcc.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				loadFile();
				viewer.render();
			}
		});
		theMenuBar.add(testM);
		return theMenuBar;
	}
	
	protected void loadFile() {
		JFileChooser fc = new JFileChooser(resourceDir);
		//System.out.println("FCI resource dir is: "+resourceDir);
		int result = fc.showOpenDialog(this);
		SceneGraphComponent sgc = null;
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			if (file.getName().indexOf(".top") != -1) {
				sgc = PolymakeParser.readFromFile(file);
				IndexedFaceSet ifs = (IndexedFaceSet) sgc.getGeometry();
				ifs = GeometryUtility.binaryRefine(ifs);
				GeometryUtility.calculateAndSetFaceNormals(ifs);
				ifs.buildEdgesFromFaces();
				sgc.setGeometry(ifs);
			} else {
				OOGLReader or = new OOGLReader();	
				//OFFReader.setResourceDir(resourceDir);
				or.setResourceDir(file.getParent()+"/");
				sgc = or.readFromFile(file);
			}
			resourceDir=file.getAbsolutePath();
		} else {
			System.out.println("Unable to open file");
			return;
		}
		world.removeChild(child);
		child = sgc;
		world.addChild(child);
		viewer.getSelectionManager().setSelection(null);
		viewer.render();
	}
	
	SceneGraphComponent world = null, child = null;
	/* (non-Javadoc)
	 * @see de.jreality.jogl.InteractiveViewerDemo#makeWorld()
	 */
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtilities.createFullSceneGraphComponent("world");
		//for (int i = 0; i<6; ++i)	{
		if (initialFile != null)	{
			OOGLReader or = new OOGLReader();	
			//OFFReader.setResourceDir(resourceDir);
			
			child = or.readFromFile(initialFile);
		} else {
			child = SceneGraphUtilities.createFullSceneGraphComponent("child");
			child.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
			child.addChild(SphereHelper.SPHERE_SUPERFINE);
		}
		world.addChild(child);
		return world;
	}
	
	public static void main(String[] args) {
		ReadFromFileDemo test = new ReadFromFileDemo();
		test.begin();
	}
}
