package de.jreality.writer.u3d;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.io.FileOutputStream;
import java.util.Random;

import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jreality.writer.pdf.WriterPDF;

public class TestScene {

	private static Random 
		rnd = new Random();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IndexedFaceSet g = Primitives.icosahedron();
		double[][] vertexColors = new double[12][3];
		for (double[] c : vertexColors) {
			c[0] = rnd.nextDouble();
			c[1] = rnd.nextDouble();
			c[2] = rnd.nextDouble();
		}
		double[][] faceColors = new double[20][3];
		for (double[] c : faceColors) {
			c[0] = rnd.nextDouble();
			c[1] = rnd.nextDouble();
			c[2] = rnd.nextDouble();
		}
		double[][] edgeColors = new double[30][3];
		for (double[] c : faceColors) {
			c[0] = rnd.nextDouble();
			c[1] = rnd.nextDouble();
			c[2] = rnd.nextDouble();
		}
		g.setVertexAttributes(Attribute.COLORS, new DoubleArrayArray.Array(vertexColors));
		g.setEdgeAttributes(Attribute.COLORS, new DoubleArrayArray.Array(edgeColors));
		g.setFaceAttributes(Attribute.COLORS, new DoubleArrayArray.Array(faceColors));
		
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("Test Scene Root");
		root.setGeometry(g);
		root.setLight(new PointLight());
		Appearance app = new Appearance(); 
		app.setAttribute(VERTEX_DRAW, true);
		app.setAttribute(EDGE_DRAW, true);
		app.setAttribute(FACE_DRAW, true);
		app.setAttribute(POINT_SHADER + "." + SPHERES_DRAW, false);
		app.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
		
		root.setAppearance(app);
		WriterPDF writer = new WriterPDF();
		try {
			FileOutputStream out = new FileOutputStream("test.pdf");
			writer.write(root, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
