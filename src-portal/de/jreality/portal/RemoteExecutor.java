package de.jreality.portal;

import java.lang.reflect.Method;

import de.jreality.toolsystem.ToolEventReceiver;

public class RemoteExecutor {

	public static ToolEventReceiver startRemote(Class<?> clazz, String... params) {
		ToolEventReceiver ret = null;
		try {
			Method m = clazz.getMethod("remoteMain", String[].class);
			if (params == null) params=new String[0];
			ret = (ToolEventReceiver) m.invoke(null, new Object[]{(String[]) params});
			System.out.println("RET="+ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ret == null || !(ret instanceof ToolEventReceiver)) throw new IllegalArgumentException("no remoteMain method in "+clazz);
		return ret;
	}
	
}
