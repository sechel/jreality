package de.jreality.scene.event;

import de.jreality.scene.Transformation;

/**
 * @author holger
 */
public class TransformationEvent extends SceneEvent
{
  final Transformation sourceTransformation;

  /**
   * Constructor for TransformationEvent.
   * @param transformation
   */
  public TransformationEvent(Transformation source)
  {
    super(source);
    sourceTransformation=source;
  }

  /**
   * Returns the sourceTransformation.
   * @return Transformation
   */
  public Transformation getTransformation()
  {
    return sourceTransformation;
  }

  public double[] getTransformationMatrix()
  {
    return sourceTransformation.getMatrix();
  }

  /**
   * Returns the matrix in the provided target array.
   * @param target an array with at least 16 entries
   */
  public void getMatrix(double[] target)
  {
    sourceTransformation.getMatrix(target);
  }

}
