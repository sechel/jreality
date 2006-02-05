package de.jreality.scene.event;

import de.jreality.scene.Camera;

/**
 * @author steffen
 */
public class CameraEvent extends SceneEvent
{
  final Camera sourceCamera;

  /**
   * Constructor for CameraEvent.
   * @param transformation
   */
  public CameraEvent(Camera source)
  {
    super(source);
    sourceCamera=source;
  }

  /**
   * Returns the sourceCamera.
   * @return Camera
   */
  public Camera getCamera()
  {
    return sourceCamera;
  }

}
