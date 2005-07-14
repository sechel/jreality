/*
 * Created on Jun 18, 2004
 *
 */
package de.jreality.jogl;

import java.awt.Dimension;
import java.util.List;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLEventListener;
import net.java.games.jogl.GLU;
import net.java.games.jogl.util.GLUT;

/**
 * @author Pepijn Van Eeckhoudt
 */
public class InfoOverlay implements GLEventListener {
	/**
	 * @author gunn
	 *
	 */
	public interface InfoProvider {
		public void updateInfoStrings(InfoOverlay io);
	}
	
	InfoProvider infoProvider = null;
	
	public void setInfoProvider(InfoOverlay.InfoProvider ip)	{
		infoProvider = ip;
	}
	
	
	InteractiveViewer viewer;
	
	public int position = UPPER_LEFT;
	private boolean visible = false;
	private GLUT glut = new GLUT();
	private static final int CHAR_HEIGHT = 12;
	private static final int OFFSET = 15;
	private static final int INDENT = 3;
	private List info;
	public static final int UPPER_LEFT = 0;
	public static final int LOWER_LEFT = 1;
	public static final int UPPER_RIGHT = 2;
	public static final int LOWER_RIGHT = 3;

	/**
	 * @param v
	 */
	public InfoOverlay(InteractiveViewer v) {
		viewer = v;
		if ((viewer.getViewingComponent() instanceof GLCanvas))
			((GLDrawable) v.getViewingComponent()).addGLEventListener(this);
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public void setInfoStrings(List s)	{
		info = s;
	}
	
	public void display(GLDrawable glDrawable) {
		if (!visible) return;
		if (infoProvider != null)		infoProvider.updateInfoStrings(this);
		if (info == null || info.size() == 0) return;
		
		//JOGLConfiguration.theLog.log(Level.FINE,"In info display");
		
		GL gl = glDrawable.getGL();
		GLU glu = glDrawable.getGLU();

		// Store old matrices
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		Dimension size = glDrawable.getSize();
		gl.glViewport(0, 0, size.width, size.height);

		// Store enabled state and disable lighting, texture mapping and the depth buffer
		gl.glPushAttrib(GL.GL_ENABLE_BIT);
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glDisable(GL.GL_DEPTH_TEST);
		for (int i = 0; i< 6; ++i) gl.glDisable(i + GL.GL_CLIP_PLANE0);

		// Retrieve the current viewport and switch to orthographic mode
		int viewPort[] = new int[4];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewPort);
		glu.gluOrtho2D(0, viewPort[2], viewPort[3], 0);

		// Render the text
		gl.glColor3f(1, 1, 1);

		int offset = (position == LOWER_LEFT || position == LOWER_RIGHT) ? -OFFSET : OFFSET;
		int x = (position == LOWER_RIGHT || position == UPPER_RIGHT) ? viewPort[2]/2 : OFFSET;
		x += INDENT;
		int maxx = 0;
		int y = (position == LOWER_LEFT || position == LOWER_RIGHT) ? (viewPort[3])-CHAR_HEIGHT : CHAR_HEIGHT;
		y += offset;

		if (info != null && info.size() > 0) {
			gl.glRasterPos2i(x, y);
			//glut.glutBitmapString(gl, GLUT.BITMAP_HELVETICA_12, "Info");
			//maxx = Math.max(maxx, OFFSET + glut.glutBitmapLength(GLUT.BITMAP_HELVETICA_12, KEYBOARD_CONTROLS));

			y += offset;
			x += INDENT;
			for (int i = 0; i < info.size(); i++) {
				gl.glRasterPos2f(x, y);
				String text = null;
				synchronized(info)	{
					text = (String) info.get(i);		
				}
				glut.glutBitmapString(gl, GLUT.BITMAP_HELVETICA_12, text);
				maxx = Math.max(maxx, OFFSET + glut.glutBitmapLength(GLUT.BITMAP_HELVETICA_12, text));
				y += offset;
			}
		}


		gl.glPopAttrib();

		// Restore old matrices
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
	}

	public void displayChanged(GLDrawable glDrawable, boolean b, boolean b1) {
	}

	public void init(GLDrawable glDrawable) {
	}

	public void reshape(GLDrawable glDrawable, int i, int i1, int i2, int i3) {
	}

}
