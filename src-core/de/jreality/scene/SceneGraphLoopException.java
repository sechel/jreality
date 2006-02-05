
package de.jreality.scene;

/**
 * Thrown on attempt of creating a cyclic graph.
 * @author holger
 */
public class SceneGraphLoopException extends RuntimeException
{
  public SceneGraphLoopException()
  {
    this("Loop in SceneGraph");
  }
  public SceneGraphLoopException(String message)
  {
    super(message);
  }
}
