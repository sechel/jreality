package de.jreality.scene.event;

import de.jreality.scene.Light;

/**
 * @author steffen
 */
public class LightEvent extends SceneEvent
{
  final Light sourceLight;

  /**
   * Constructor for LightEvent.
   * @param Light
   */
  public LightEvent(Light source)
  {
    super(source);
    sourceLight=source;
  }

  /**
   * Returns the sourceLight.
   * @return Light
   */
  public Light getLight()
  {
    return sourceLight;
  }

}
