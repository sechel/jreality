package de.jreality.scene.newpick;

import de.jreality.scene.pick.PickResult;


public class AffineCoordinateFilter implements HitFilter {
	
	public boolean accept(double[] from, double[] to, PickResult h) {
        double affCoord = AABBPickSystem.affineCoord(from, to, h.getWorldCoordinates());
        return affCoord >= 0;
	}

}
