/*
 * Created on Apr 30, 2004
 *
 */
package de.jreality.scene.event;

import java.util.EventListener;

/**
 * @author Charles Gunn
 *
 */
public interface AppearanceListener  extends EventListener{
	public void appearanceChanged(AppearanceEvent ev);

}
