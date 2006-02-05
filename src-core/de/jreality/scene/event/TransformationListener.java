
package de.jreality.scene.event;

import java.util.EventListener;

/**
 * Implemented by event listeners that wish to get informed when a
 * transformation matrix changes.
 * @see de.jreality.scene.Transformation
 * @author holger
 */
public interface TransformationListener extends EventListener
{
  public void transformationMatrixChanged(TransformationEvent ev);
}
