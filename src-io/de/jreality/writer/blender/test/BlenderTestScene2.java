package de.jreality.writer.blender.test;

import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.jreality.geometry.Primitives;
import de.jreality.io.JrScene;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.writer.WriterJRS;
import de.jreality.writer.blender.BlenderConnection;

public class BlenderTestScene2 {

	public static void main(String[] args) throws IOException {
		SceneGraphComponent root = new SceneGraphComponent();
		MatrixBuilder.euclidean().scale(2).assignTo(root);
		Appearance rootApp = new Appearance("Root Appearance");
		rootApp.setAttribute(POINT_SHADER + '.' + RADII_WORLD_COORDINATES, true);
		rootApp.setAttribute(LINE_SHADER + '.' + RADII_WORLD_COORDINATES, true);
		rootApp.setAttribute(VERTEX_DRAW, false);
		rootApp.setAttribute(FACE_DRAW, false);
		root.setAppearance(rootApp);
		root.setName("Scene Root");
		
		SceneGraphComponent cameraRoot = new SceneGraphComponent();
		cameraRoot.setName("Camera Root");
		Camera cam = new Camera("My Camera");
		cam.setOrientationMatrix(MatrixBuilder.euclidean().translate(0, 3, 8).rotateX(-0.3).getArray());
		cameraRoot.setCamera(cam);
		root.addChild(cameraRoot);
		
		SceneGraphComponent refTestRoot = new SceneGraphComponent("References Test Root");
		Appearance refApp = new Appearance("Referenced App");
		refTestRoot.setAppearance(refApp);
		refTestRoot.setGeometry(Primitives.coloredCube());
		root.addChild(refTestRoot);
		
		SceneGraphComponent refTestRoot2 = new SceneGraphComponent("References Test Root 2");
		Appearance refApp2 = new Appearance("Referenced App");
		refTestRoot2.setAppearance(refApp2);
		refTestRoot2.setGeometry(refTestRoot.getGeometry());
		root.addChild(refTestRoot2);
		
		SceneGraphPath camPath = new SceneGraphPath(root, cameraRoot, cam);
		
//		Viewer v = JRViewer.display(root);
//		v.setCameraPath(camPath);
		
		JrScene scene = new JrScene(root);
		scene.addPath("cameraPath", camPath);

		WriterJRS jrsWriter = new WriterJRS();
		jrsWriter.writeScene(scene, new FileOutputStream("test2.jrs"));
		
		// write blender file 
		BlenderConnection r = new BlenderConnection();
		File blenderFile = new File("test2.blend");
		r.writeFile(scene, blenderFile);
	}
	
}
