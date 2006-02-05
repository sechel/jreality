
package de.jreality.scene.event;

import java.util.EventListener;

/**
 * Implemented by event listeners that wish to get informed when a
 * light changes.
 * @see de.jreality.scene.Light
 * @author holger
 */
public interface LightListener extends EventListener
{
  public void lightChanged(LightEvent ev);
}
