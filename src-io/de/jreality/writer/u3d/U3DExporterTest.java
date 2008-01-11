package de.jreality.writer.u3d;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.jreality.io.JrScene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;

public class U3DExporterTest {

	
	public static void main(String[] args) {
//		ReaderJRS reader = new ReaderJRS();
//		SceneGraphComponent c = null;
//		try {
//			c = reader.read(new File("testfiles/enneper01.jrs"));
//		} catch (IOException e1) {
//			e1.printStackTrace();
//			return;
//		}
//		JrScene scene = new JrScene(c);
		
//		Random rnd = new Random();
//		rnd.setSeed(0);
//		double[][] points = new double[3000][];
//		for (int i = 0; i < points.length; i++) {
//			points[i] = new double[]{rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5};
//			Rn.times(points[i], 1 / Rn.euclideanNorm(points[i]), points[i]);
//		}
//		PointSetFactory psf = new PointSetFactory();
//		psf.setVertexCount(points.length);
//		psf.setVertexCoordinates(points);
//		psf.update();
//		SceneGraphComponent c = new SceneGraphComponent();
//		c.setGeometry(psf.getGeometry());
//		SceneGraphComponent root = new SceneGraphComponent();
//		root.addChild(c);
//		JrScene scene = new JrScene(root);
		
		SceneGraphComponent root = new SceneGraphComponent();
		SceneGraphComponent c = new SceneGraphComponent();
		root.addChild(c);
		root.setGeometry(new Sphere());
		c.setGeometry(new Sphere());
		JrScene scene = new JrScene(root);
		
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream("test.u3d");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		WriterU3D writer = new WriterU3D();
		try {
			writer.writeScene(scene, fout);
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
}
