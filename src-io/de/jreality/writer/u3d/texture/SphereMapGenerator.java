package de.jreality.writer.u3d.texture;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import de.jreality.shader.CubeMap;
import de.jreality.softviewer.EnvironmentTexture;

public class SphereMapGenerator {

	public static int i = 0;
	
	public static BufferedImage create(CubeMap map, int width, int height) {
		BufferedImage r = new BufferedImage(width, height, TYPE_INT_ARGB);
		EnvironmentTexture tex = new EnvironmentTexture(map, null);
		WritableRaster raster = r.getRaster();
		for (double y = 0; y < height; y++) {
			double v = PI * y / height;
			double ry = cos(v);
			double sinV = sin(v);
			for (double x = 0; x < width; x++) {
				double u = PI * x / width;
				double rx = cos(2*u) * sinV;
				double rz = sin(2*u) * sinV;
				double[] ray = new double[]{rx, ry, rz};
				double[] color = new double[4];
				tex.getColor(0, 0, ray[0], ray[1], ray[2], 0, 0, color);
				double[] iColor = new double[]{color[0], color[1], color[2], 255.0};
				raster.setPixel((int)x, (int)y, iColor);
			}
		}
//		try {
//			ImageIO.write(r, "PNG", new File("spheremap" + i++ + ".png"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return r;
	}
	
}
