package org.sunflow.core.shader;

import java.util.Arrays;

import org.sunflow.SunflowAPI;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Ray;
import org.sunflow.core.Shader;
import org.sunflow.core.ShadingState;
import org.sunflow.image.Color;
import org.sunflow.math.Point2;
import org.sunflow.math.Vector3;

import de.jreality.shader.CubeMap;
import de.jreality.shader.RenderingHintsShader;
import de.jreality.softviewer.SimpleTexture;
import de.jreality.softviewer.Texture;

public class DefaultPolygonShader implements Shader {

	private de.jreality.shader.DefaultPolygonShader dps;
	private Texture tex;

	double[] color=new double[4];
	private RenderingHintsShader rhs;
	private CubeMap cm;

	public DefaultPolygonShader(de.jreality.shader.DefaultPolygonShader dps, RenderingHintsShader rhs) {
		this.dps=dps;
		if (dps.getTexture2d() != null) tex = new SimpleTexture(dps.getTexture2d());
		this.rhs=rhs;
		this.cm = dps.getReflectionMap();
	}
	
	public Color getRadiance(ShadingState state) {
        // make sure we are on the right side of the material
        state.faceforward();
        // setup lighting
        state.initLightSamples();
        state.initCausticSamples();
        
        Color d = getDiffuse(state);
        Color ret = rhs.getLightingEnabled() ? state.diffuse(d) : d.toLinear();
        
        if (false && state.includeSpecular()) {
        	Color spec = state.specularPhong(convert(dps.getSpecularColor(), dps.getSpecularCoefficient()), dps.getSpecularExponent().floatValue(), 4);
        	ret.add(spec);
        }
        
        if (cm != null) {
            float cos = state.getCosND();
            float dn = 2 * cos;
            Vector3 refDir = new Vector3();
            refDir.x = (dn * state.getNormal().x) + state.getRay().getDirection().x;
            refDir.y = (dn * state.getNormal().y) + state.getRay().getDirection().y;
            refDir.z = (dn * state.getNormal().z) + state.getRay().getDirection().z;
            Ray refRay = new Ray(state.getPoint(), refDir);
            
//            // compute Fresnel term
//            cos = 1 - cos;
//            float cos2 = cos * cos;
//            float cos5 = cos2 * cos2 * cos;
//
//            Color ret = Color.white();
//            Color r = d.copy().mul(convert(dps.getSpecularColor(), dps.getSpecularCoefficient()));
//            ret.sub(r);
//            ret.mul(cos5);
//            ret.add(r);

            
        	Color ref = state.traceRefraction(refRay, 0).toLinear();
        	float l = cm.getBlendColor().getAlpha()/255f;
        	ret.mul(1-l).madd(l, ref);
        }
        
        if (rhs.getTransparencyEnabled() && dps.getTransparency() != 0) {
        	double t=dps.getTransparency();
        	ret.mul((float)(1-t)).madd((float)t, state.traceReflection(new Ray(state.getPoint(), state.getRay().getDirection()), 0));
        }
                
        return ret;
	}

	private Color getDiffuse(ShadingState state) {
		getColor(color, dps.getDiffuseColor(), dps.getDiffuseCoefficient());
		if (tex != null) {
			Point2 uv = state.getUV();
			tex.getColor(uv.x, uv.y, 0, 0, 0, 0, 0, color);
		}
		return new Color((float) color[0], (float) color[1], (float) color[2]);
	}

	private void getColor(double[] d, java.awt.Color c, double f) {
		d[0] = c.getRed()*f/255;
		d[1] = c.getGreen()*f/255;
		d[2] = c.getBlue()*f/255;
		d[3] = c.getAlpha()/255;
	}

	private Color convert(java.awt.Color c, double f) {
		float ff = (float) (f/255);
		return new Color(c.getRed()*ff, c.getGreen()*ff, c.getBlue()*ff);
	}

	public void scatterPhoton(ShadingState state, Color power) {
		// TODO Auto-generated method stub

	}

	public boolean update(ParameterList pl, SunflowAPI api) {
		return true;
	}

}
