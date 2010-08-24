/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
*/

package com.nokia.s60tools.crashanalyser.model;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;

import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.interfaces.IErrorLibraryObserver;
import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.export.*;
import com.nokia.s60tools.crashanalyser.plugin.*;



/**
 * This class listens MobileCrash files via TraceViewer 
 *
 */
public final class TraceListener implements ITraceDataProcessor, 
										IErrorLibraryObserver  {

	/**
	 * Private timer class. Takes care that we do not start crash
	 * decoding process too many times within the predefined time
	 * interval.
	 */
	private final class TraceTimerTask extends TimerTask
	{
		private final TraceListener traceListener;

		TraceTimerTask(final TraceListener listener) {
			traceListener = listener;
		}
		
		@Override
		public void run() {
			traceListener.timerExpired();
		}	
	}

	
	private final static String MOBILE_CRASH_STARTTAG = "<MB_CR_START>"; //$NON-NLS-1$
	private final static String MOBILE_CRASH_LINE_TAG = "<MB_CD>"; //$NON-NLS-1$
	private final static String MOBILE_CRASH_STOPTAG = "<MB_CR_STOP>"; //$NON-NLS-1$
	private final static String MOBILECRASH_START = "MobileCrash_"; //$NON-NLS-1$
	private final static String EXTENSION_TRACE_PROVIDER = "traceprovider"; //$NON-NLS-1$
	private final static int MAX_DECODER_COUNT = 3;
	private final static int DECODER_TIMER_DELAY = 10000; // 10 secs.
	
	boolean listening = false;
	boolean mobileCrashStarted = false;
	BufferedWriter mcFile = null;
	String dumpFolder = ""; //$NON-NLS-1$
	File dumpFile;
	ErrorLibrary errorLibrary = null;
	boolean decode = false;
	private static ITraceProvider traceProvider = null;
	private Timer timer;

	private int decoderCount = 0;
	
	/**
	 * Constructor
	 */
	public TraceListener() {
		readTraceProvider();
		timer = new Timer();
	}
	
	/**
	 * Starts trace listening asynchronously
	 */
	public void errorLibraryReady() {
		final Runnable refreshRunnable = new Runnable(){
			public void run(){
				startListening();
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);        		
	}
	
	/**
	 * Activates MobileCrash file listening
	 */
	public void startListening() {
		if (errorLibrary == null) {
			errorLibrary = ErrorLibrary.getInstance(this);
			return;
		}
		
		if (listening || traceProvider == null)
			return;

		if (traceProvider.start(this))
			listening = true;
	}
	
	/**
	 * Sets whether we should decode imported files or just import them as undecoded state.
	 * @param decodeFiles 
	 */
	public void setDecode(final boolean decodeFiles) {
		decode = decodeFiles;
	}
	
	/**
	 * De-activates MobileCrash file listening
	 */
	public void stopListening() {
		if (!listening)
			return;
		
		if (traceProvider != null)
			traceProvider.stop();
		listening = false;
	}

	/**
	 * All lines in trace data will be passed to this method. This method
	 * pics up MobileCrash file content from trace data.
	 * @param line trace data line
	 */
	public void processDataLine(final String line) {
		int idx = line.indexOf(MOBILE_CRASH_STARTTAG);
		
		try {
			// Line contained <MC_CR_START>
			if (idx > -1) { 
				mobileCrashStarted = true;
				dumpFolder = FileOperations.addSlashToEnd(DecoderEngine.getNewCrashFolder());
				
				final Calendar cal = Calendar.getInstance();
			    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS"); //$NON-NLS-1$
			    dumpFile = new File(dumpFolder +
			    					MOBILECRASH_START + 
			    					sdf.format(cal.getTime()) + 
			    					"." + 
			    					CrashAnalyserFile.TRACE_EXTENSION);		
				
			    // create a file for this mobilecrash
			    mcFile = new BufferedWriter(new FileWriter(dumpFile));
			    mcFile.write(line);
				mcFile.newLine();
				
			// Line did not contain <MC_CR_START>, but <MB_CR_START> has been found previously (i.e we are reading a file) 
			} else if (mobileCrashStarted){ 
				
				idx = line.indexOf(MOBILE_CRASH_LINE_TAG);
				// Line contained <MB_CD>, add data to file
				if (idx > -1) { 
					mcFile.write(line);
					mcFile.newLine();
				// Line did not contain <MB_CD>
				} else { 
					idx = line.indexOf(MOBILE_CRASH_STOPTAG);
					// Line contained <MC_CR_STOP>, we can finish reading this mobile crash file
					if (idx > -1) {
						mcFile.write(line);
						mcFile.newLine();
						mcFile.close();
						mcFile = null;
						mobileCrashStarted = false;
						
						// give this mobilecrash file for further processing
						final MobileCrashImporter tc = new MobileCrashImporter();
						
						if (decoderCount < MAX_DECODER_COUNT) {
							decoderCount++;
							if (decoderCount == 1) {
								// Start timer when starting decoder at first time
								timer.schedule(new TraceTimerTask(this), DECODER_TIMER_DELAY);
							}
							tc.importFrom(dumpFolder, dumpFile.getName(), errorLibrary, decode);
						} else {
							// Too many crashes in the trace file, do not decode (last parameter is false).
							tc.importFrom(dumpFolder, dumpFile.getName(), errorLibrary, false);
						}
					}
				}
			}
		} catch (Exception e) {
			if (mcFile != null) {
				try {mcFile.close();}catch (Exception E) {E.printStackTrace();}
			}
			mcFile = null;
			mobileCrashStarted = false;
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns whether we have found trace provider plugins.
	 * @return true if trace providers were found, false if not
	 */
	public static boolean traceProviderAvailable() {
		if (traceProvider == null)
			return false;
		return true;
	}
	
	/**
	 * This is called when the timer expires.
	 */
	public final void timerExpired()
	{
		decoderCount = 0;
	}
	
	/**
	 * Tries to find plugins which are Trace Providers. Selected the first found
	 * Trace provider plugin.
	 */
	void readTraceProvider() {
		try {
			final IExtensionRegistry er = Platform.getExtensionRegistry();
			final IExtensionPoint ep = 
				er.getExtensionPoint(CrashAnalyserPlugin.PLUGIN_ID, EXTENSION_TRACE_PROVIDER);
			final IExtension[] extensions = ep.getExtensions();
			
			// if plug-ins were found.
			if (extensions != null && extensions.length > 0) {
				
				// read all found trace providers
				for (int i = 0; i < extensions.length; i++) {
					final IConfigurationElement[] ce = extensions[i].getConfigurationElements();
					if (ce != null && ce.length > 0) {
						try {
							final ITraceProvider provider = (ITraceProvider)ce[0].createExecutableExtension("class");
							// we support only one trace provider
							if (provider != null) {
								traceProvider = provider;
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
