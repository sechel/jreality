package de.jreality.scene.proxy.scene;

public interface RemoteSpotLight extends RemotePointLight {

    public abstract double getConeAngle();
    public abstract void setConeAngle(double coneAngle);
    public abstract double getConeDeltaAngle();
    public abstract double getDistribution();
    public abstract void setConeDeltaAngle(double coneDeltaAngle);
    public abstract void setDistribution(double distribution);
}