
package de.jreality.scene.event;

import java.util.EventListener;

/**
 * Event notifaction interface for geometries.
 * <i>Still under development</i>
 * @author pietsch
 */
public interface GeometryListener extends EventListener
{
    public void geometryChanged(GeometryEvent ev);
}
