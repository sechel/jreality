/*
 * Created on Apr 29, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.geometry.QuadMeshUtility;
import de.jreality.geometry.TubeUtility;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.IntArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * @author Charles Gunn
 *
 */
public class DefaultLineShader extends AbstractPrimitiveShader implements LineShader  {
	int 	tubeStyle = TubeUtility.PARALLEL;
	double	tubeRadius = 0.05,
		 	lineWidth = 1.0,
			depthFudgeFactor = 0.9999d;
	boolean interpolateVertexColors = false, lighting;
	int	lineFactor = 1;
	int 	lineStipplePattern = 0x1c47; 
	 
	boolean lineStipple = false;
	boolean tubeDraw = false;
			
	Color diffuseColor = java.awt.Color.BLACK;
	private PolygonShader polygonShader;
	 
		/**
		 * 
		 */
	public DefaultLineShader() {
			super();
		}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap, name);
		tubeDraw = eap.getAttribute(ShaderUtility.nameSpace(name, CommonAttributes.TUBES_DRAW), CommonAttributes.TUBES_DRAW_DEFAULT);
		tubeRadius = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TUBE_RADIUS),CommonAttributes.TUBE_RADIUS_DEFAULT);
		tubeStyle = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TUBE_STYLE),CommonAttributes.TUBE_STYLE_DEFAULT);
		depthFudgeFactor = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DEPTH_FUDGE_FACTOR), depthFudgeFactor);
		interpolateVertexColors = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.INTERPOLATE_VERTEX_COLORS), CommonAttributes.INTERPOLATE_VERTEX_COLORS_DEFAULT);
		lighting = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHTING_ENABLED), false);
		lineStipple = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_STIPPLE), lineStipple);
		lineWidth = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_WIDTH), CommonAttributes.LINE_WIDTH_DEFAULT);
		lineFactor = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_FACTOR),lineFactor);
		lineStipplePattern = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_STIPPLE_PATTERN),lineStipplePattern);
		diffuseColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.LINE_DIFFUSE_COLOR_DEFAULT);
		double transp = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		setDiffuseColor( ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, transp));
		polygonShader = (PolygonShader) ShaderLookup.getShaderAttr(eap, name, "polygonShader");
		//JOGLConfiguration.theLog.log(Level.FINE,"Line shader is smooth: "+smoothShading);
		//JOGLConfiguration.theLog.log(Level.FINE,"Line shader's polygon shader is smooth: "+(polygonShader.isSmoothShading() ? "true" : "false"));
		//polygonShader.setDiffuseColor(diffuseColor);
	}

	public double getDepthFudgeFactor() {
		return depthFudgeFactor;
	}
	/**
	 * @return
	 */
	public double getLineWidth() {
		return lineWidth;
	}

	/**
	 * @return
	 */
	public boolean isLineStipple() {
		return lineStipple;
	}

	/**
	 * @return
	 */
	public int getLineStipplePattern() {
		return lineStipplePattern;
	}

	/**
	 * @return
	 */
	public int getLineFactor() {
		return lineFactor;
	}

	/**
	 * @return
	 */
	public boolean isTubeDraw() {
		return tubeDraw;
	}

	public Color getDiffuseColor() {
		return diffuseColor;
	}
	float[] diffuseColorAsFloat;
	public float[] getDiffuseColorAsFloat() {
		return diffuseColorAsFloat;
	}

	public void setDiffuseColor(Color diffuseColor2) {
		diffuseColor = diffuseColor2;
		diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
	}

	public double getTubeRadius() {
		return tubeRadius;
	}
	public void preRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, diffuseColorAsFloat);
		gl.glColor4fv( diffuseColorAsFloat);
		System.arraycopy(diffuseColorAsFloat, 0, jr.openGLState.diffuseColor, 0, 4);
	
		gl.glLineWidth((float) getLineWidth());
		if (isLineStipple()) {
			gl.glEnable(GL.GL_LINE_STIPPLE);
			gl.glLineStipple(getLineFactor(), (short) getLineStipplePattern());
		} 
		else gl.glDisable(GL.GL_LINE_STIPPLE);

		if (tubeDraw)	{
			Geometry g = jrs.getCurrentGeometry();
			jrs.setCurrentGeometry(null);
			polygonShader.render(jrs);
			jrs.setCurrentGeometry(g);
			lighting=true;
		} //else lighting = false;
		//if (jr.openGLState.lighting != lighting)	{
		//else {
			jr.openGLState.lighting = lighting;
			if (lighting) gl.glEnable(GL.GL_LIGHTING);
			else gl.glDisable(GL.GL_LIGHTING);
			
		//}

		// this little bit of code forces tubes to be opaque: could add
		// transparency-enable flag to the line shader to allow this to be controlled
		gl.glDepthMask(true);
		gl.glDisable(GL.GL_BLEND);

		if (!tubeDraw) gl.glDepthRange(0.0d, depthFudgeFactor);
	}

	public void postRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
		if (!tubeDraw) jr.getCanvas().getGL().glDepthRange(0.0d, 1d);
	}

	public boolean providesProxyGeometry() {		
		return tubeDraw;
	}
	
	public int proxyGeometryFor(JOGLRenderingState jrs)	{
		final Geometry original = jrs.getCurrentGeometry();
		final JOGLRenderer jr = jrs.getRenderer();
		final int sig = jrs.getCurrentSignature();
		final boolean useDisplayLists = jrs.isUseDisplayLists();
		if ( !(original instanceof IndexedLineSet)) return -1;
		if (tubeDraw && original instanceof IndexedLineSet)	{
      final int[] dlist = new int[1];
      Scene.executeReader(original, new Runnable() {
        public void run() {
    			dlist[0] = createTubesOnEdgesAsDL((IndexedLineSet) original, tubeRadius, 1.0, jr, sig, jr.isPickMode(), useDisplayLists);
			    //JOGLConfiguration.theLog.log(Level.FINE,"Creating tubes with radius "+tubeRadius);
        }
      });
			return dlist[0];
		}
		return -1;
	}
	
	int[] tubeDL = null;
	boolean testQMS = true;
	boolean smoothShading = true;		// force tubes to be smooth shaded ?
	public int createTubesOnEdgesAsDL(IndexedLineSet ils, double rad,  double alpha, JOGLRenderer jr, int sig, boolean pickMode, boolean useDisplayLists)	{
		GL gl = jr.globalGL;
		double[] p1 = new double[4],
			p2 = new double[4];
		p1[3] = p2[3] = 1.0;
		double[][] oneCurve = null;
		double[][] crossSection = TubeUtility.octagonalCrossSection;
		if (jr.openGLState.levelOfDetail == 0.0) crossSection = TubeUtility.diamondCrossSection;
		int n = ils.getNumEdges();
		DataList vertices = ils.getVertexAttributes(Attribute.COORDINATES);
		if (ils.getNumPoints() <= 1) return -1;
//		JOGLConfiguration.theLog.log(Level.FINE,"Creating tubes for "+ils.getName());
		if (tubeDL == null)	{
			tubeDL = new int[3];
		}
		if (tubeDL[sig+1] == 0)	{
			tubeDL[sig+1] = gl.glGenLists(1);
			gl.glNewList(tubeDL[sig+1], GL.GL_COMPILE);
			jr.helper.drawFaces(TubeUtility.urTube[sig+1], smoothShading , alpha );
			gl.glEndList();	
		}
		int nextDL = -1;
		if (useDisplayLists) {
			nextDL = gl.glGenLists(1);
			gl.glNewList(nextDL, GL.GL_COMPILE);
		}
		boolean isQuadMesh = false;
		Object qmatt = ils.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
		Dimension dm = null;
		if (qmatt != null && qmatt instanceof Dimension)	{
			dm = (Dimension) qmatt;
			isQuadMesh = true;
		} 
		if (pickMode)	gl.glPushName(JOGLPickAction.GEOMETRY_LINE);
		if (!pickMode && isQuadMesh)	{
			int u, v, count=0;
			boolean closedU, closedV;
			u = dm.width;
			v = dm.height;
			closedU = closedV = false;
			double[][] curve = null;
			IndexedFaceSet tube = null;
			for (int i = 0; i<u+v; ++i)	{
				int uv = 0;
				int curvenum = i;
				boolean closed = closedU;
				if (i>=u) { 
					uv = 1; curvenum = i-u; 
					closed = closedV;
				}
				curve = QuadMeshUtility.extractParameterCurve(curve,(IndexedFaceSet) ils, u, v, curvenum,uv);
				PolygonalTubeFactory ptf = new PolygonalTubeFactory(curve);
				ptf.setClosed(closed);
				ptf.setCrossSection(crossSection);
				ptf.setFrameFieldType(tubeStyle);
				ptf.setSignature(sig);
				ptf.setRadius(rad);
				ptf.update();
				tube = ptf.getTube();
				//JOGLConfiguration.theLog.log(Level.FINE,"Tube has "+tube.getNumPoints()+" points");
				if (pickMode)	gl.glPushName(count++);
				jr.helper.drawFaces(tube,smoothShading, alpha);
				if (pickMode) 	gl.glPopName();
			}
		} else {
			int  k, l;
			DoubleArray da;
			double[] mat = new double[16];
			for (int i = 0; i<n; ++i)	{
			IntArray ia = ils.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray();
			DataList edgec =  ils.getEdgeAttributes(Attribute.COLORS);
			int m = ia.size();
			if (pickMode)	gl.glPushName(i);
			if (m == 2 || pickMode)	{		// probably an IndexedFaceSet 
				DoubleArray edgecolor = null;
				int clength = 3;
				if (edgec != null) {
					edgecolor = edgec.item(i).toDoubleArray();
					clength = edgecolor.size();
					if (clength == 3) gl.glColor3d(edgecolor.getValueAt(0), edgecolor.getValueAt(1), edgecolor.getValueAt(2));
					else gl.glColor4d(edgecolor.getValueAt(0), edgecolor.getValueAt(1), edgecolor.getValueAt(2), edgecolor.getValueAt(3));
				}
				
				for (int j = 0; j<m-1; ++j)	{
					k = ia.getValueAt(j);
					da = vertices.item(k).toDoubleArray();
					l = da.size();
					for (int xx=0; xx<l; ++xx) p1[xx] = da.getValueAt(xx);
					k = ia.getValueAt(j+1);
					da = vertices.item(k).toDoubleArray();
					l = da.size();
					for (int xx=0; xx<l; ++xx) p2[xx] = da.getValueAt(xx);
					SceneGraphComponent cc = TubeUtility.tubeOneEdge(p1, p2, rad, crossSection, sig);
					if (pickMode) gl.glPushName(j);
					gl.glPushMatrix();
					gl.glMultTransposeMatrixd(cc.getTransformation().getMatrix(mat));
					gl.glCallList(tubeDL[sig+1]);
					gl.glPopMatrix();
					if (pickMode) 	gl.glPopName();					
					
				}
			}
			else {		// the assumption is that this is a genuine IndexedLineSet (not subclass with faces)
				oneCurve = IndexedLineSetUtility.extractCurve(oneCurve, ils, i);
				PolygonalTubeFactory ptf = new PolygonalTubeFactory(oneCurve);
				ptf.setClosed(false);
				ptf.setCrossSection(crossSection);
				ptf.setFrameFieldType(tubeStyle);
				ptf.setSignature(sig);
				ptf.setRadius(rad);
				ptf.update();
				IndexedFaceSet tube = ptf.getTube();
				if (tube != null)	{
					jr.helper.drawFaces(tube, smoothShading, alpha);			
				}
			}
			if (pickMode) 	gl.glPopName();					
		}
		}
		if (pickMode) gl.glPopName();
		
		if (useDisplayLists) gl.glEndList();
		return nextDL;
	}

	int dList = -1, dListProxy =- 1;
	public void render(JOGLRenderingState jrs)	{
		Geometry g = jrs.getCurrentGeometry();
		JOGLRenderer jr = jrs.getRenderer();
		boolean useDisplayLists = jrs.isUseDisplayLists();
		if ( !(g instanceof IndexedLineSet))	{
			throw new IllegalArgumentException("Must be IndexedLineSet");
		}
		preRender(jrs);
		if (g != null)	{
			if (providesProxyGeometry())	{
				if (!useDisplayLists || jr.pickMode || dListProxy == -1) {
					dListProxy  = proxyGeometryFor(jrs);
				}
				jr.globalGL.glCallList(dListProxy);
			}
			else 	{
				if (!useDisplayLists || jr.pickMode) {
					jr.helper.drawLines((IndexedLineSet) g,  false, jr.openGLState.diffuseColor[3]);
				} else {
					if (useDisplayLists && dList == -1)	{
						dList = jr.globalGL.glGenLists(1);
						jr.globalGL.glNewList(dList, GL.GL_COMPILE); //_AND_EXECUTE);
						jr.helper.drawLines((IndexedLineSet) g,  false, jr.openGLState.diffuseColor[3]);
						jr.globalGL.glEndList();	
					}
					jr.globalGL.glCallList(dList);
				} 
			}
		}
	}

	public void flushCachedState(JOGLRenderer jr) {
		if (dList != -1) jr.globalGL.glDeleteLists(dList, 1);
		if (dListProxy != -1) jr.globalGL.glDeleteLists(dListProxy,1);
		dList = dListProxy = -1;
	}
	

}
