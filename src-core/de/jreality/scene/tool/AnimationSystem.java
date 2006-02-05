package de.jreality.scene.tool;

public interface AnimationSystem {
    public void schedule(Object key, AnimatorTask task);
    public void deschedule(Object key);
}
