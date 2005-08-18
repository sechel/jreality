package de.jreality.scene.proxy.scene;

public interface RemoteSpotLight extends RemotePointLight {
    public abstract void setConeAngle(double coneAngle);
    public abstract void setConeDeltaAngle(double coneDeltaAngle);
    public abstract void setDistribution(double distribution);
}