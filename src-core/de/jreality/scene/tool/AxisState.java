
package de.jreality.scene.tool;

/**
 * 
 */
public final class AxisState
{
    public static final AxisState PRESSED = new AxisState(Integer.MAX_VALUE);
    public static final AxisState ORIGIN  = new AxisState(0);
    private int state;
    
    private static final int MINUS_PRESSED = -Integer.MAX_VALUE;
    
    /**
     * double must be in the range [-1,1]
     * @param value
     */
    public AxisState(double value)
    {
    	if (value < -1 || value > 1) {
//    		throw new IllegalArgumentException("illegal axis state value");
    		value = value < 0?-1:1;
    	}
        state=(int)(value*Integer.MAX_VALUE);
    }
    public AxisState(int value)
    {
        state=value;
    }
    public int intValue()
    {
        return state;
    }
    public double doubleValue()
    {
        return state/(double)Integer.MAX_VALUE;
    }
    public boolean isPressed() {
    	return state==Integer.MAX_VALUE || state == MINUS_PRESSED;
    }
    public boolean isReleased() {
    	return state==0;
    }
    public String toString() {
      switch (state) {
        case 0:
          return "AxisState=ORIGIN";
        case Integer.MAX_VALUE:
          return "AxisState=PRESSED";
        case MINUS_PRESSED:
          return "AxisState=MINUS_PRESSED";
        default:
          return "AxisState="+state+" ["+((int)(doubleValue()*100))/100.+"]";
      }
    }
}
