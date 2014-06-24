package de.jreality.writer.blender;

import static java.lang.Math.PI;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.io.JrScene;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jreality.writer.WriterJRS;

public class BlenderTestScene {

	public static void main(String[] args) throws Exception {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("Scene Root");
		SceneGraphComponent icosahedron = new SceneGraphComponent();
		icosahedron.setName("Icosahedron Root");
		icosahedron.setGeometry(Primitives.icosahedron());
		root.addChild(icosahedron);
		SceneGraphComponent icosahedron2 = new SceneGraphComponent();
		icosahedron2.setName("Transformed Icosahedron Root");
		icosahedron2.setGeometry(icosahedron.getGeometry());
		MatrixBuilder.euclidean().translate(2, 2, 2).assignTo(icosahedron2);
		root.addChild(icosahedron2);
		SceneGraphComponent cameraRoot = new SceneGraphComponent();
		cameraRoot.setName("Camera Root");
		Camera cam = new Camera("My Camera");
		cameraRoot.setCamera(cam);
		root.addChild(cameraRoot);
		root.addChild(cameraRoot);
		SceneGraphComponent camera2Root = new SceneGraphComponent();
		camera2Root.setName("Orthographic Camera Root");
		Camera cam2 = new Camera("My Orthographic Camera");
		cam2.setOrientationMatrix(MatrixBuilder.euclidean().rotateX(Math.PI/2).getArray());
		cam2.setPerspective(false);
		camera2Root.setCamera(cam2);
		root.addChild(camera2Root);
		SceneGraphComponent invisible = new SceneGraphComponent();
		invisible.setName("Invisible Object");
		invisible.setVisible(false);
		root.addChild(invisible);
		SceneGraphComponent transformedObject = new SceneGraphComponent();
		transformedObject.setName("Transformed Object");
		double[] pos = {1,2,3,1};
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.rotate(Math.PI / 4, 1, 0, 0);
		Matrix M = mb.getMatrix();
		pos = M.multiplyVector(pos);
		mb.translate(pos);
		mb.assignTo(transformedObject);
		root.addChild(transformedObject);
		JrScene scene = new JrScene(root);
		scene.addPath("cameraPath", new SceneGraphPath(root, camera2Root, cam2));
		
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(4);
		ifsf.setEdgeCount(2);
		ifsf.setFaceCount(1);
		ifsf.setVertexCoordinates(new double[][]{
			{0,0,1,10},
			{10,0,1,10},
			{10,10,1,10},
			{0,10,1,10}
		});
		ifsf.setEdgeIndices(new int[][]{
			{0,1},
			{2,3}
		});
		ifsf.setFaceIndices(new int[][]{
			{0,1,2,3}
		});
		ifsf.update();
		SceneGraphComponent geometryComponent1 = new SceneGraphComponent();
		geometryComponent1.setName("DoubleArrayArray Geometry Component");
		geometryComponent1.setGeometry(ifsf.getGeometry());
		MatrixBuilder.euclidean().translate(0, -3, 0).assignTo(geometryComponent1);
		root.addChild(geometryComponent1);
		
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		ilsf.setVertexCount(100);
		ilsf.setEdgeCount(99);
		double[][] vertData = new double[100][];
		int[][] indexData = new int[vertData.length - 1][];
		for (int i = 0; i < vertData.length; i++) {
			vertData[i] = new double[]{i/10.0, Math.sin(2*PI*i/(double)vertData.length)};
			if (i < indexData.length) {
				indexData[i] = new int[]{i, i+1};
			}
		}
		ilsf.setVertexCoordinates(vertData);
		ilsf.setEdgeIndices(indexData);
		ilsf.update();
		SceneGraphComponent lineSetObject = new SceneGraphComponent();
		lineSetObject.setName("Line Set Geometry Component");
		lineSetObject.setGeometry(ilsf.getGeometry());
		root.addChild(lineSetObject);
		
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(1000);
		double[][] pointData = new double[1000][];
		Random rnd = new Random();
		for (int i = 0; i < pointData.length; i++) {
			pointData[i] = new double[]{rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian(), 1.0};
		}
		psf.setVertexCoordinates(pointData);
		psf.update();
		SceneGraphComponent pointSetRoot = new SceneGraphComponent();
		MatrixBuilder.euclidean().translate(-3, 0, 0).assignTo(pointSetRoot);
		pointSetRoot.setName("Point Set Component");
		pointSetRoot.setGeometry(psf.getGeometry());
		root.addChild(pointSetRoot);
		
		SceneGraphComponent customGeoemtryRoot = new SceneGraphComponent();
		customGeoemtryRoot.setName("Custom Geometry Component");
		CatenoidHelicoid cat = new CatenoidHelicoid(20);
		cat.setAlpha(Math.PI/2);
		customGeoemtryRoot.setGeometry(cat);
		MatrixBuilder.euclidean().translate(0, 0, -4).scale(0.5).assignTo(customGeoemtryRoot);
		root.addChild(customGeoemtryRoot);
		
//		JRViewer.display(root);
		
		// write scene file
		WriterJRS jrsWriter = new WriterJRS();
		jrsWriter.writeScene(scene, new FileOutputStream("test.jrs"));
		
		// write blender file 
		BlenderConnection r = new BlenderConnection();
		File blenderFile = new File("test.blend");
		r.writeFile(scene, blenderFile);
//		BufferedImage image = ImageIO.read(imageFile);
//		if (image != null) {
//			JFrame f = new JFrame();
//			f.add(new JLabel(new ImageIcon(image)));
//			f.pack();
//			f.setVisible(true);
//			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		}
	}
	
}
