package com.nokia.carbide.cpp.pi.ipc;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.nokia.carbide.cpp.pi.ipc.messages"; //$NON-NLS-1$
	public static String IpcPlugin_0;
	public static String IpcPlugin_1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
