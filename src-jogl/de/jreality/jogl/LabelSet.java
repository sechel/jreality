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
	String[] labels;
	double[][] objectVerts, screenVerts;
	PointSet positions;
	private GLUT glut = new GLUT();

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

	public void render(JOGLRenderer jr)	{
		GL gl = jr.getCanvas().getGL();
		GLU glu = jr.getCanvas().getGLU();
		
		Graphics3D gc = jr.getContext();
		
		double[] objectToScreen = gc.getObjectToScreen();
		
		Rn.matrixTimesVector(screenVerts, objectToScreen, objectVerts);
		if (screenVerts[0].length == 4) Pn.dehomogenize(screenVerts, screenVerts);
		int np = positions.getNumPoints();
		for (int i = 0; i<np; ++i)	{ screenVerts[i][2] = (screenVerts[i][2] + 1)/2.0; }

		// Store enabled state and disable lighting, texture mapping and the depth buffer
		gl.glPushAttrib(GL.GL_ENABLE_BIT);
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_TEXTURE_2D);
		for (int i = 0; i< 6; ++i) gl.glDisable(i + GL.GL_CLIP_PLANE0);

		gl.glColor3f(1, 1, 1);
		float[] cras = new float[4];
		double[] dras = new double[4];
		for (int i = 0; i<np; ++i)	{
			gl.glRasterPos3d(objectVerts[i][0], objectVerts[i][1], objectVerts[i][2]);
			gl.glGetFloatv(GL.GL_CURRENT_RASTER_POSITION, cras);
			for (int j = 0; j<4; ++j) dras[j] = cras[j];
			gl.glWindowPos3d(screenVerts[i][0] + 10.0, screenVerts[i][1] - 10.0, screenVerts[i][2]);
//			System.out.println("Actual position "+Rn.toString(dras));
//			System.out.println("I think it should be is "+Rn.toString(screenVerts[i],6));
//			System.out.println("current - theoretical = "+Rn.toString(Rn.subtract(null, dras,screenVerts[i]),6));
//			System.out.println("z-ratio: "+dras[2]/screenVerts[i][2]);
			String label = (labels == null) ? Integer.toString(i) : labels[i];
			glut.glutBitmapString(gl, GLUT.BITMAP_HELVETICA_12, label);
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
}
