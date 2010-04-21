package com.nokia.carbide.cpp.pi.peccommon;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
/**
 * The activator class controls the plug-in life cycle
 */
public class PecCommonPlugin extends AbstractPiPlugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "com.nokia.carbide.cpp.pi.peccommon"; //$NON-NLS-1$

	// The shared instance
	private static PecCommonPlugin plugin;

	/**
	 * The constructor
	 */
	public PecCommonPlugin() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PecCommonPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns a File corresponding to the given bundle relative path.
	 * @param path the bundle relative path to resource to locate
	 * @return the File corresponding to the given bundle relative path, or null
	 * @throws IOException
	 */
	public File locateFileInBundle(final String path) throws IOException {
		Bundle myBundle= getDefault().getBundle();
		IPath ppath= new Path(path);
		ppath= ppath.makeRelative();
		URL[] urls= FileLocator.findEntries(myBundle, ppath);
		if(urls.length != 1) {
			return null;
		}
		return new File(FileLocator.toFileURL(urls[0]).getFile());
	}

}
