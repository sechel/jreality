package de.jreality.scene.pick;

import de.jreality.math.Pn;
import de.jreality.math.Rn;

public class AffineCoordinateFilter implements HitFilter {
	
	public boolean accept(double[] from, double[] to, PickResult h) {
        return h.getAffineCoordinate() >= 0;
	}

}
