package de.jreality.writer.u3d;

import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;

public class U3DTexture {

	private ImageData
		image = null;
	
	public U3DTexture(Texture2D tex) {
		this.image = tex.getImage();
	}
	
	public U3DTexture(ImageData image) {
		this.image = image;
	}
	
	@Override
	public int hashCode() {
		return image.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		U3DTexture tex2 = (U3DTexture)obj;
		return getImage().equals(tex2.getImage());
	}
	
	public ImageData getImage() {
		return image;
	}
	
}
