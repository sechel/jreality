package de.jreality.writer.u3d;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.Random;

import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
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
		g.setVertexAttributes(U3DAttribute.U3D_NONORMALS, U3DAttribute.U3D_FLAG);
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
		for (double[] c : edgeColors) {
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
		root.setCamera(new Camera("Test Camera"));
		Appearance app = new Appearance(); 
		app.setAttribute(VERTEX_DRAW, true);
		app.setAttribute(EDGE_DRAW, true);
		app.setAttribute(FACE_DRAW, true);
		app.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, Color.WHITE);
		app.setAttribute(POINT_SHADER + "." + SPHERES_DRAW, true);
		app.setAttribute(LINE_SHADER + "." + TUBES_DRAW, true);
		app.setAttribute(POINT_SHADER + "." + POINT_RADIUS, 0.1);
		app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, 0.05);
		root.setAppearance(app);
		WriterU3D writerU3D = new WriterU3D();
		WriterPDF writerPDF = new WriterPDF();
		try {
			FileOutputStream outU3D = new FileOutputStream("test.u3d");
			writerU3D.write(root, outU3D);
			outU3D.close();
			FileOutputStream outPDF = new FileOutputStream("test.pdf");
			writerPDF.write(root, outPDF);
			outPDF.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
