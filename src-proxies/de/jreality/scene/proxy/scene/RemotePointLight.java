package de.jreality.scene.proxy.scene;

public interface RemotePointLight extends RemoteLight {

    public abstract double getFalloffA0();
    public abstract double getFalloffA1();
    public abstract double getFalloffA2();
    public abstract void setFalloffA0(double falloffA0);
    public abstract void setFalloffA1(double falloffA1);
    public abstract void setFalloffA2(double falloffA2);
    public abstract void setFalloff(double[] atten);
    public abstract String getShadowMap();
    public abstract boolean isUseShadowMap();
    public abstract void setShadowMap(String shadowMap);
    public abstract void setUseShadowMap(boolean useShadowMap);
    public abstract int getShadowMapX();
    public abstract int getShadowMapY();
    public abstract void setShadowMapX(int shadowMapX);
    public abstract void setShadowMapY(int shadowMapY);
}