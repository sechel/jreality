package de.jreality.vr;

import de.jreality.scene.Geometry;
import de.jreality.shader.ImageData;

public class Environment {

	private ImageData[] cubeMap;
	private ImageData texture;
	private double textureScale;
	private boolean flatTerrain;
	
	public Environment(ImageData[] cubeMap, ImageData tex, double texScale, boolean flatTerrain) {
		this.cubeMap=cubeMap;
		this.texture=tex;
		this.textureScale=texScale;
		this.flatTerrain=flatTerrain;
	}

	public ImageData[] getCubeMap() {
		return cubeMap;
	}

	public boolean isFlatTerrain() {
		return flatTerrain;
	}

	public ImageData getTexture() {
		return texture;
	}

	public double getTextureScale() {
		return textureScale;
	}
	
}
