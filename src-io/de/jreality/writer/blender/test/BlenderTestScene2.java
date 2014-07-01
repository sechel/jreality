package de.jreality.writer.blender.test;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static java.awt.Color.WHITE;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.jreality.geometry.Primitives;
import de.jreality.io.JrScene;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
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
		
		SceneGraphComponent pointLightRoot = new SceneGraphComponent();
		pointLightRoot.setName("Point Light Component");
		PointLight plight = new PointLight("Point Light");
		plight.setColor(Color.WHITE);
		pointLightRoot.setLight(plight);
		MatrixBuilder.euclidean().translate(-3, 2, 5).assignTo(pointLightRoot);
		root.addChild(pointLightRoot);
		
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
		
		SceneGraphComponent texturedQuad = new SceneGraphComponent("Textured Quad");
		Appearance texApp = new Appearance("Texture Appearance");
		texApp.setAttribute(POLYGON_SHADER + '.' + DIFFUSE_COLOR, WHITE);
		texApp.setAttribute(FACE_DRAW, true);
		texApp.setAttribute(EDGE_DRAW, false);
		texApp.setAttribute(VERTEX_DRAW, false);
		Image image = ImageIO.read(BlenderTestScene2.class.getResourceAsStream("texture01.jpg"));
		TextureUtility.createTexture(texApp, POLYGON_SHADER, new ImageData(image));
		texturedQuad.setAppearance(texApp);
		texturedQuad.setGeometry(Primitives.texturedQuadrilateral());
		MatrixBuilder.euclidean().translate(0, 0, 3).assignTo(texturedQuad);
		root.addChild(texturedQuad);
		
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
