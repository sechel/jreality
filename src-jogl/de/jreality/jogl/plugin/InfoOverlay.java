/*
 * Created on Jun 18, 2004
 *
 */
package de.jreality.jogl.plugin;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.GLUT;

import de.jreality.jogl.AbstractViewer;
import de.jreality.jogl.JOGLViewer;

/**
 * @author Pepijn Van Eeckhoudt
 */
public class InfoOverlay implements GLEventListener {
	public interface InfoProvider {
		public void updateInfoStrings(InfoOverlay io);
	}
	
	InfoProvider infoProvider = null;
	
	public void setInfoProvider(InfoOverlay.InfoProvider ip)	{
		infoProvider = ip;
	}
	
	
	AbstractViewer viewer;
	
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
	static List<InfoOverlay> infoOverlays = new Vector<InfoOverlay>();
	static void setAllVisible(boolean b)	{
		for (InfoOverlay io : infoOverlays)
			io.visible = b;
	}
	public static boolean together = true;
	/**
	 * @param v
	 */
	public InfoOverlay(AbstractViewer v) {
		viewer = v;
		viewer.getDrawable().addGLEventListener(this);
		infoOverlays.add(this);
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		if (together) setAllVisible(visible);
		else this.visible = visible;
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
	
	public void display(GLAutoDrawable glDrawable) {
		if (!visible) return;
		if (infoProvider != null)		infoProvider.updateInfoStrings(this);
		if (info == null || info.size() == 0) return;
		
		//JOGLConfiguration.theLog.log(Level.FINE,"In info display");
		
		GL gl = glDrawable.getGL();
		GLU glu =  new GLU();

		// Store old matrices
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		Dimension size = new Dimension(glDrawable.getWidth(), glDrawable.getHeight());
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
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewPort,0);
		try {
			glu.gluOrtho2D(0, viewPort[2], viewPort[3], 0);			
		} catch (GLException e) {
//			e.printStackTrace();
			return;
		}

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
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, text);
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

	public void displayChanged(GLAutoDrawable glDrawable, boolean b, boolean b1) {
		//TODO document this
	}

	public void init(GLAutoDrawable glDrawable) {
		//TODO document this
	}

	public void reshape(GLAutoDrawable glDrawable, int i, int i1, int i2, int i3) {
		//TODO document this
	}

	public static InfoOverlay perfInfoOverlayFor(final JOGLViewer v)	{
		final InfoOverlay perfInfo = new InfoOverlay(v);
		final List<String> infoStrings = new Vector<String>();
		perfInfo.setPosition(InfoOverlay.LOWER_RIGHT);
		perfInfo.setInfoProvider(new InfoOverlay.InfoProvider() {

			public void updateInfoStrings(InfoOverlay io) {
				InfoOverlay.updateInfoStrings(v, infoStrings, perfInfo);
			}
			
		});
		return perfInfo;
	}
	public static void updateInfoStrings(JOGLViewer v, List<String> s, InfoOverlay io)	{
		//JOGLConfiguration.theLog.log(Level.INFO,"Providing info strings");
		if (s != null) s.clear();
		else s = new ArrayList<String>();
		s.add("Real FPS: "+v.getRenderer().getFramerate());
		s.add("Clock FPS: "+v.getRenderer().getClockrate());
		s.add("Polygon Count:"+v.getRenderer().getPolygonCount());
		s.add(getMemoryUsage());
		io.setInfoStrings(s);
	}

	public static String getMemoryUsage() {
		Runtime r = Runtime.getRuntime();
		int block = 1024;
		return "Memory usage: " + ((r.totalMemory() / block) - (r.freeMemory() / block)) + " kB";
	}


}
