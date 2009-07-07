package de.jreality.jogl.shader;

import de.jreality.shader.Texture2D;
import de.jreality.ui.viewerapp.ViewerSwitch;

public interface TextureID extends Texture2D {

	public Object getTextureID();
	public void setTextureID(Object obj);
}
