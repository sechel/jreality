package de.jreality.examples.jogl;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereHelper;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.reader.Input;
import de.jreality.reader.ReaderOOGL;
import de.jreality.reader.ReaderPOLY;
import de.jreality.reader.Readers;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.Pn;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author gunn
 *
 */
public class ReadFromFileDemo extends InteractiveViewerDemo {

	boolean useLOD = false, showSphere = false;
	static boolean hyperbolic = false, elliptic = false;
	static String resourceDir = "/Users/gunn/Documents/Models",
		initialFile = null;
	
	static {
		String foo = System.getProperty("resourceDir");
		if (foo != null) resourceDir = foo;
		foo = System.getProperty("initialFile");
		if (foo != null) initialFile = foo;
		foo = System.getProperty("hyperbolic");
		if (foo != null) 
			if (foo.indexOf("true") != -1) { hyperbolic = true; }
		foo = System.getProperty("elliptic");
		if (foo != null) 
			if (foo.indexOf("true") != -1) { hyperbolic = false; elliptic = true; }
	}
	public JMenuBar createMenuBar()	{
		theMenuBar = super.createMenuBar();
//		JMenu testM = new JMenu("File");
//		JMenuItem jcc = new JMenuItem("Open...");
//		testM.add(jcc);
//		jcc.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent e)	{
//				loadFile();
//				viewer.render();
//			}
//		});
//		theMenuBar.add(testM);
		if (hyperbolic)	{
			JMenu testM = new JMenu("Geometry");
			final JCheckBoxMenuItem jg = new JCheckBoxMenuItem("Show Sphere");
			showSphere = true;
			jg.setSelected(hyperbolic);
			testM.add(jg);
			jg.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					showSphere = jg.isSelected();
					hypersphere.setVisible(showSphere);
					viewer.render();
				}
			});
			theMenuBar.add(testM);
			
		}
		return theMenuBar;
	}
	
	protected void loadFile() {
		JFileChooser fc = new JFileChooser(resourceDir);
		//System.out.println("FCI resource dir is: "+resourceDir);
		int result = fc.showOpenDialog(this);
		resourceDir=fc.getSelectedFile().getAbsolutePath();
		SceneGraphComponent sgc = null;
		try {
			sgc = Readers.read(fc.getSelectedFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		SceneGraphComponent sgc = null;
//		try {
//			sgc = Readers.read("OOGL", Readers.resolveResource(fc.getName()));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if (child != null && world.isDirectAncestor(child)) 	world.removeChild(child);
		child = sgc;
		if (sgc == null) return;
		world.addChild(child);
		if (hyperbolic || elliptic)	{
			SceneGraphUtilities.setSignature(child, hyperbolic ? Pn.HYPERBOLIC : Pn.ELLIPTIC);
			SceneGraphPath pathToLoaded = new SceneGraphPath();
			pathToLoaded.push(viewer.getSceneRoot());
			pathToLoaded.push(world);
			pathToLoaded.push(child);
			viewer.getSelectionManager().setSelection(pathToLoaded);
		} else	
			viewer.getSelectionManager().setSelection(null);
		GeometryUtility.calculateFaceNormals(world);
		GeometryUtility.calculateVertexNormals(world);
		viewer.render();
	}
	
	SceneGraphComponent world = null, child = null, hypersphere;
	/* (non-Javadoc)
	 * @see de.jreality.jogl.InteractiveViewerDemo#makeWorld()
	 */
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtilities.createFullSceneGraphComponent("world");
		//for (int i = 0; i<6; ++i)	{
		if (initialFile != null)	{
			ReaderOOGL or = new ReaderOOGL();	
			try {
        child = or.read(new File(initialFile));
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
		} else {
			if (!hyperbolic && !elliptic)	{
				child = SceneGraphUtilities.createFullSceneGraphComponent("child");
				child.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
				child.addChild(SphereHelper.SPHERE_WAYFINE);				
				world.addChild(child);
			} else {
				if (hyperbolic) {
					hypersphere = Primitives.wireframeSphere();
					world.addChild(hypersphere);
				}
				viewer.setBackgroundColor(new Color(0, 120, 120));
			}
		}
		return world;
	}
	
	public boolean addBackPlane() {return false; }
	
	public boolean isEncompass() { return !elliptic; }
	
	public static void main(String[] args) {
		ReadFromFileDemo test = new ReadFromFileDemo();
		test.begin();
	}
}
