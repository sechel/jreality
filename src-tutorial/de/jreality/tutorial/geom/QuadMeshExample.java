package de.jreality.tutorial.geom;

import java.awt.Color;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tutorial.util.SimpleTextureFactory;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.SceneGraphUtility;

/**
 * This example shows how to use a {@link QuadMeshFactory} to generate an instance of 
 * {@link IndexedFaceSet}.  The specific surface featured here is a ruled surface
 * known as an oloid that arises in the motion of a 3-bar linkage with its two end points
 * fixed.
 * 
 * @author schmies, gunn
 *
 */
public class QuadMeshExample {

	public static IndexedFaceSet createOloid( int N ) {

		QuadMeshFactory factory = new QuadMeshFactory();
			
		double [][][] coords = new double [5][2*N-1][];
		
		for( int i=0; i<N; i++) {
			double alpha = Math.PI/2 * i / (N-1);
			double beta  = Math.asin( -  Math.cos(alpha) / ( Math.cos(alpha)+1));
			
			coords[0][i] = new double[]{ -0.5-Math.cos(alpha), -Math.sin(alpha), 0 };
			coords[1][i] = new double[]{  0.5+Math.sin(beta),   0,  Math.cos(beta) };
			coords[2][i] = new double[]{ -0.5-Math.cos(alpha),  Math.sin(alpha), 0 };
			coords[3][i] = new double[]{  0.5+Math.sin(beta),   0, -Math.cos(beta) };
			coords[4][i] = coords[0][i];
			
			if( i > N-2 ) continue;
			coords[3][2*N-2-i] = new double[]{  0.5+Math.cos(alpha), 0,-Math.sin(alpha) };
			coords[2][2*N-2-i] = new double[]{ -0.5-Math.sin(beta),   Math.cos(beta), 0 };
			coords[1][2*N-2-i] = new double[]{  0.5+Math.cos(alpha), 0, Math.sin(alpha) };
			coords[0][2*N-2-i] = new double[]{ -0.5-Math.sin(beta),  -Math.cos(beta), 0 };
			coords[4][2*N-2-i] = coords[0][2*N-2-i];
		}
		
		factory.setVLineCount(5);		// important: the v-direction is the left-most index
		factory.setULineCount(2*N-1);	// and the u-direction the next-left-most index
		factory.setVertexCoordinates(coords);
		factory.setGenerateFaceNormals(true);
		factory.setGenerateTextureCoordinates(true);
		factory.setGenerateEdgesFromFaces(true);
		
		factory.update();
		
		return factory.getIndexedFaceSet();
	}
	
	public static void main(String[] args) {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("world");
		sgc.setGeometry(createOloid(50));
		SimpleTextureFactory stf = new SimpleTextureFactory();
		stf.setType(SimpleTextureFactory.TextureType.GRAPH_PAPER);
		stf.update();
		Texture2D tex2d = TextureUtility.createTexture(sgc.getAppearance(), "polygonShader", stf.getImageData());
		Matrix texm = new Matrix();
		MatrixBuilder.euclidean().scale(10,10,1).assignTo(texm);
		tex2d.setTextureMatrix(texm);
		Appearance ap = sgc.getAppearance();
		ap.setAttribute(CommonAttributes.SMOOTH_SHADING, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
		ViewerApp.display(sgc );

	}

}
