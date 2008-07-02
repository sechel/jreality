package de.jreality.scene.newpick;

import de.jreality.scene.pick.PickResult;

public interface HitFilter {

	public boolean accept(double[] from, double[] to, PickResult h);
}
