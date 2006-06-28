package de.jreality.renderman.shader;

import java.awt.Color;

import de.jreality.math.Rn;
import de.jreality.renderman.RIBVisitor;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;

public class RopePolygonShader extends DefaultPolygonShader {
	Color band1color = new Color(1f,1f,1f), 
		band2color = new Color(.8f, .8f, .8f), 
		shadowcolor = new Color(0f,0f,0f), 
		gapcolor = new Color(0f,0f,0f,0f);
	double bandwidth, shadowwidth, blendfactor, gapalpha;
	double[] textureMatrix = Rn.identityMatrix(4);
	
	public void setFromEffectiveAppearance(RIBVisitor ribv, EffectiveAppearance eap, String name) {
		super.setFromEffectiveAppearance(ribv, eap, name);
		shaderName = "ropeshader";			// name of RIB shader
        bandwidth = eap.getAttribute(name+"."+"bandwidth", .7);
        shadowwidth = eap.getAttribute(name+"."+"shadowwidth", .12);
        blendfactor = eap.getAttribute(name+"."+"blendfactor", 1.0);
        gapalpha = eap.getAttribute(name+"."+"gapalpha", 1.0);
        band1color =(Color) eap.getAttribute(name+"."+"band1color", band1color);
        band2color =(Color) eap.getAttribute(name+"."+"band2color", band2color);
        shadowcolor =(Color) eap.getAttribute(name+"."+"shadowcolor", shadowcolor);
        gapcolor =(Color) eap.getAttribute(name+"."+"gapcolor", gapcolor);
        textureMatrix =(double[]) eap.getAttribute(name+"."+"textureMatrix", textureMatrix);
        map.put("bandwidth",new Float((float) bandwidth));
        map.put("shadowwidth",new Float((float) shadowwidth));
        map.put("blendfactor",new Float((float) blendfactor));
        map.put("gapalpha",new Float((float) gapalpha));
        map.put("band1color", band1color);
        map.put("band2color", band2color);
        map.put("shadowcolor", shadowcolor);
        map.put("gapcolor", gapcolor);
        map.put("textureMatrix", textureMatrix);
	}

}
