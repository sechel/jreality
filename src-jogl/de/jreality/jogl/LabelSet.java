package de.jreality.jogl;
import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;
import net.java.games.jogl.util.GLUT;
import de.jreality.scene.Geometry;
import de.jreality.scene.Graphics3D;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
/*
 * Created on Mar 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LabelSet extends Geometry {
	public static final int GLUT_BITMAP_9_BY_15		= 2;

	public static final int GLUT_BITMAP_8_BY_13		= 3;

	public static final int GLUT_BITMAP_TIMES_ROMAN_10	= 4;

	public static final int GLUT_BITMAP_TIMES_ROMAN_24	= 5;

	public static final int GLUT_BITMAP_HELVETICA_10	= 6;

	public static final int GLUT_BITMAP_HELVETICA_12	= 7;

	public static final int GLUT_BITMAP_HELVETICA_18	= 8;

	String[] labels;
	double[][] objectVerts, screenVerts;
	PointSet positions;
	double[] screenOffset = {10.0, -10.0,-.01};
	private GLUT glut = new GLUT();
	int bitmapFont = GLUT_BITMAP_HELVETICA_12;

	/**
	 * 
	 */
	private LabelSet()	{
		super();
	}
	
	public static LabelSet labelSetFactory(PointSet p, String[] l) {
		LabelSet ls = new LabelSet();
		ls.setPositions(p);
		ls.setLabels(l);
		if (ls.getPositions() == null) return null;
		return ls;
	}
	
	/**
	 * @return
	 */
	public String[] getLabels() {
		return labels;
	}

	/**
	 * @param l
	 */
	private void setLabels(String[] l) {
		if (l == null) { labels = null; return; }
		if (l.length != positions.getNumPoints())	{
			System.out.println("Invalid dimensions");
			return;
		}
	}

	static double[] correctionNDC = null;
	static {
		correctionNDC = Rn.identityMatrix(4);
		correctionNDC[10] = correctionNDC[11] = .5;
	}
	public void render(JOGLRenderer jr)	{
		GL gl = jr.getCanvas().getGL();
		
		Graphics3D gc = jr.getContext();
		
		double[] objectToScreen = Rn.times(null, correctionNDC, gc.getObjectToScreen());
		
		Rn.matrixTimesVector(screenVerts, objectToScreen, objectVerts);
		if (screenVerts[0].length == 4) Pn.dehomogenize(screenVerts, screenVerts);
		int np = positions.getNumPoints();
		//for (int i = 0; i<np; ++i)	{ screenVerts[i][2] = (screenVerts[i][2] + 1)/2.0; }

		// Store enabled state and disable lighting, texture mapping and the depth buffer
		gl.glPushAttrib(GL.GL_ENABLE_BIT);
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_TEXTURE_2D);
		for (int i = 0; i< 6; ++i) gl.glDisable(i + GL.GL_CLIP_PLANE0);

		//gl.glColor3f(1, 1, 1);
		float[] cras = new float[4];
		double[] dras = new double[4];
		for (int i = 0; i<np; ++i)	{
			gl.glRasterPos3d(objectVerts[i][0], objectVerts[i][1], objectVerts[i][2]);
			gl.glGetFloatv(GL.GL_CURRENT_RASTER_POSITION, cras);
			for (int j = 0; j<4; ++j) dras[j] = cras[j];
			gl.glWindowPos3d(screenVerts[i][0]+screenOffset[0], screenVerts[i][1] +screenOffset[1], screenVerts[i][2]+screenOffset[2]);
			String label = (labels == null) ? Integer.toString(i) : labels[i];
			//bitmapFont = 2 + (i%6);
			glut.glutBitmapString(gl, bitmapFont, label);
		}

		gl.glPopAttrib();
	}
	public PointSet getPositions() {
		return positions;
	}
	public void setPositions(PointSet positions) {
		this.positions = positions;
		objectVerts = positions.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		screenVerts = new double[objectVerts.length][objectVerts[0].length];
	}
	public double[] getScreenOffset() {
		return screenOffset;
	}
	public void setScreenOffset(double[] screenOffset) {
		this.screenOffset = screenOffset;
	}

}
