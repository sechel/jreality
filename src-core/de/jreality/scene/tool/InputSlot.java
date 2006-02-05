package de.jreality.scene.tool;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract input device, addressed via a logical name.
 */
public class InputSlot implements Serializable
{
    static int count;
    static Map name2device = new HashMap();
    private final String name;
    private transient final int id=count++;
    private InputSlot(String name)
    {
        this.name=name;
    }
    /**
     * Get the canonical device for the logical name. Devices with the
     * same name are meant to represent the same device and yield the
     * same instance.
     */
    public static synchronized InputSlot getDevice(String name)
    {
        Object old=name2device.get(name);
        if(old!=null) return (InputSlot)old;
        InputSlot dev=new InputSlot(name);
        name2device.put(name, dev);
        return dev;
    }
    public String getName() {
      return name;
    }
    //TODO: something better here?
    public String toString()
    {
        return name;
    }
    
    Object readResolve() throws ObjectStreamException {
      return getDevice(getName());
    }
}
