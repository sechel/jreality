
package de.jreality.scene.event;

import java.util.EventListener;

/**
 * Implemented by event listeners that wish to get informed when a
 * camera changes.
 * @see de.jreality.scene.Camera
 * @author steffen
 */
public interface CameraListener extends EventListener
{
  public void cameraChanged(CameraEvent ev);
}
