package de.jreality.scene.pick;


public class AffineCoordinateFilter implements HitFilter {
	
	public boolean accept(double[] from, double[] to, PickResult h) {
        double affCoord = AABBPickSystem.affineCoord(from, to, h.getWorldCoordinates());
        return affCoord >= 0;
	}

}
