package de.jreality.scene.proxy.scene;

public interface RemoteAppearance extends RemoteSceneGraphNode {

    public abstract Object getAttribute(String key);

    public abstract Object getAttribute(String key, Class type);

    public abstract void setAttribute(String key, Object value);

    public abstract void setAttribute(String key, Object value,
            Class declaredType);

    public abstract void setAttribute(String key, double value);

    public abstract void setAttribute(String key, float value);

    public abstract void setAttribute(String key, int value);

    public abstract void setAttribute(String key, long value);

    public abstract void setAttribute(String key, boolean value);

    public abstract void setAttribute(String key, char value);
}