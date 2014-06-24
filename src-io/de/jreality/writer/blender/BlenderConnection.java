package de.jreality.writer.blender;

import static java.lang.Math.PI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.io.JrScene;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jreality.writer.WriterJRS;

/**
 * 
 * @author Stefan Sechelmann, Thilo Rörig
 *
 */
public class BlenderConnection {

	private Logger
		log = Logger.getLogger(BlenderConnection.class.getName());
	private File
		blenderApp = new File("/Applications/Blender/blender.app/Contents/MacOS/blender"),
		rendererScript = null;
	
	public BlenderConnection() {
	}
	
	protected void invokeBlender(File sceneFile, File outBlenderFile, File outImage) throws IOException {
		// run renderer
		File script = null;
		try {
			script = getRendererScript();
		} catch (IOException e1) {
			log.warning("could not write blender renderer script: " + e1.getMessage());
			throw e1;
		}
		String[] args = {
			"./blender",
			"--background",
			"--factory-startup",
			"--render-format", "PNG",
			"--python",
			script.toString(),
			"--",
//			outImage != null ? "--render=" + outImage.getAbsolutePath() : "",
			outBlenderFile != null ? "--save=" + outBlenderFile.getAbsolutePath() : "",
			"--file=" + sceneFile.getAbsolutePath()
		};
		try {
			File dir = blenderApp.getParentFile();
			Process p = Runtime.getRuntime().exec(args, new String[]{}, dir);
			InputStream in = new BufferedInputStream(p.getInputStream());
			InputStream err = new BufferedInputStream(p.getErrorStream());
			int bIn = 0;
			while ((bIn = in.read()) != -1) {
				System.out.write(bIn);
			}
			while ((bIn = err.read()) != -1) {
				System.err.write(bIn);
			}
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected File writeScene(JrScene scene) throws IOException {
		File sceneFile = File.createTempFile("jrealityBlenderExportScene", ".jrs");
		sceneFile.deleteOnExit();
		OutputStream sceneOut = new FileOutputStream(sceneFile); 
		WriterJRS jrsWriter = new WriterJRS();
		jrsWriter.writeScene(scene, sceneOut);
		sceneOut.close();
		return sceneFile;
	}
	
	public void writeFile(JrScene scene, File f) throws IOException {
		File sceneFile = writeScene(scene);
		invokeBlender(sceneFile, f, null);
	}
	
	public void renderImage(JrScene scene, File f) throws IOException {
		File sceneFile = writeScene(scene);
		invokeBlender(sceneFile, null, f);
	}
	
	private File getRendererScript() throws IOException {
		if (rendererScript != null) {
			return rendererScript;
		}
		rendererScript = File.createTempFile("jrealityBlenderRenderer", "py");
		rendererScript.deleteOnExit();
		ReadableByteChannel rbc = Channels.newChannel(getClass().getResourceAsStream("renderer.py"));
		FileOutputStream fin = new FileOutputStream(rendererScript);
		FileChannel outChannel = fin.getChannel();
		outChannel.transferFrom(rbc, 0, Long.MAX_VALUE);
		outChannel.close();
		fin.close();
		return rendererScript;
	}
	
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
