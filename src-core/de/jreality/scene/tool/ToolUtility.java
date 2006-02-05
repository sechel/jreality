package de.jreality.scene.tool;

import de.jreality.math.Matrix;
import de.jreality.scene.SceneGraphPath;

public class ToolUtility {

  private ToolUtility() {}
  
  public static Matrix worldToAvatar(ToolContext tc, Matrix worldMatrix) {
    Matrix world2avatar = new Matrix(tc.getTransformationMatrix(InputSlot.getDevice("AvatarTransformation")));
    world2avatar.invert();
    world2avatar.multiplyOnRight(worldMatrix);
    return world2avatar;
  }
  public static double[] worldToAvatar(ToolContext tc, double[] worldVector) {
    Matrix world2avatar = new Matrix(tc.getTransformationMatrix(InputSlot.getDevice("AvatarTransformation")));
    world2avatar.invert();
    return world2avatar.multiplyVector(worldVector);
  }

  public static Matrix avatarToWorld(ToolContext tc, Matrix localMatrix) {
    Matrix avatar2world = new Matrix(tc.getTransformationMatrix(InputSlot.getDevice("AvatarTransformation")));
    avatar2world.multiplyOnRight(localMatrix);
    return avatar2world;
  }
  public static double[] avatarToWorld(ToolContext tc, double[] localVector) {
    Matrix avatar2world = new Matrix(tc.getTransformationMatrix(InputSlot.getDevice("AvatarTransformation")));
    return avatar2world.multiplyVector(localVector);
  }

  public static Matrix worldToLocal(ToolContext tc, Matrix worldMatrix) {
    return worldToLocal(tc.getRootToLocal(), worldMatrix);//worldToLocal(tc.getRootToLocal(), worldMatrix);
  }

  public static double[] worldToLocal(ToolContext tc, double[] worldVector) {
    return worldToLocal(tc.getRootToLocal(), worldVector);
  }
  public static Matrix worldToTool(ToolContext tc, Matrix worldMatrix) {
    return worldToLocal(tc.getRootToToolComponent(), worldMatrix);
  }
  public static double[] worldToTool(ToolContext tc, double[] worldVector) {
    return worldToLocal(tc.getRootToToolComponent(), worldVector);
  }

  public static Matrix localToWorld(ToolContext tc, Matrix localMatrix) {
    return localToWorld(tc.getRootToLocal(), localMatrix);
  }
  public static double[] localToWorld(ToolContext tc, double[] localVector) {
    return localToWorld(tc.getRootToLocal(), localVector);
  }
  
  public static Matrix toolToWorld(ToolContext tc, Matrix toolMatrix) {
    return localToWorld(tc.getRootToToolComponent(), toolMatrix);
  }
  public static double[] toolToWorld(ToolContext tc, double[] toolVector) {
    return localToWorld(tc.getRootToToolComponent(), toolVector);
  }
  
  public static Matrix worldToLocal(SceneGraphPath rootToLocal, Matrix worldMatrix) {
    Matrix world2local = new Matrix();
    rootToLocal.getInverseMatrix(world2local.getArray());
    world2local.multiplyOnRight(worldMatrix);
    return world2local;
  }
  public static double[] worldToLocal(SceneGraphPath rootToLocal, double[] worldVector) {
    Matrix world2local = new Matrix();
    rootToLocal.getInverseMatrix(world2local.getArray());
    return world2local.multiplyVector(worldVector);
  }
  public static Matrix localToWorld(SceneGraphPath rootToLocal, Matrix localMatrix) {
    Matrix local2world = new Matrix();
    rootToLocal.getMatrix(local2world.getArray());
    local2world.multiplyOnRight(localMatrix);
    return local2world;
  }
  public static double[] localToWorld(SceneGraphPath rootToLocal, double[] localVector) {
    Matrix local2world = new Matrix();
    rootToLocal.getMatrix(local2world.getArray());
    return local2world.multiplyVector(localVector);
  }

}
