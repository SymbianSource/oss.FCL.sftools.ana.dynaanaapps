/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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

package com.nokia.carbide.cpp.pi.function;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IAnalysisItem;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;


/**
 * The main plugin class to be used in the desktop.
 */
public class FunctionPlugin extends AbstractPiPlugin
		implements IAnalysisItem, IEventListener
{
	//The shared instance.
	private static FunctionPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public FunctionPlugin() {
		plugin = this;
	}

	public Action getAnalysisAction() {
		return null;
	}
	
	public void action()
	{
		
	}
	
//	private boolean checkGfcRequirements(PICompositePanel compositePanel, GfcTrace gfcTrace)
//	{
//		boolean startOk = true;
//		boolean endOk = true;
//		
//		if ( compositePanel.getSelectionEnd() < gfcTrace.getFirstSampleNumber() ||
//				compositePanel.getSelectionStart() > gfcTrace.getLastSampleNumber())
//		{
//			GeneralMessages.showErrorMessage("There is no Gfc Trace Data for time "+
//					(double)compositePanel.getSelectionStart()/1000+"s-"+
//					((double)compositePanel.getSelectionEnd())/1000+
//					"s Current GFC trace is "+
//					(double)gfcTrace.getFirstSampleNumber()/1000+"s-"+
//					(double)gfcTrace.getLastSampleNumber()/1000+"s");
//			return false;				
//		}
//		
//		if (compositePanel.getSelectionStart() < gfcTrace.getFirstSampleNumber())
//		{
//			boolean result = GeneralMessages.showProblemMessage("There is no Gfc Trace Data for time "+
//					(double)compositePanel.getSelectionStart()/1000+"s-"+
//					((double)gfcTrace.getFirstSampleNumber()-1)/1000+
//					"s Current GFC trace is "+
//					(double)gfcTrace.getFirstSampleNumber()/1000+"s-"+
//					(double)gfcTrace.getLastSampleNumber()/1000+"s"+
//					" Do you want to move selection start to "+(double)gfcTrace.getFirstSampleNumber()/1000+"s ?");
//			
//			if (result == true)
//			{
//				compositePanel.setSelectionFields(
//						gfcTrace.getFirstSampleNumber(),(int) compositePanel.getSelectionEnd());
//			}
//			else startOk =false;
//		}
//		if (compositePanel.getSelectionEnd() > gfcTrace.getLastSampleNumber())
//		{
//			boolean result = GeneralMessages.showProblemMessage("There is no Gfc Trace Data for time "+
//					((double)gfcTrace.getLastSampleNumber()+1)/1000+"s-"+
//					(double)compositePanel.getSelectionEnd()/1000+
//					"s Current GFC trace is "+
//					(double)gfcTrace.getFirstSampleNumber()/1000+"s-"+
//					(double)gfcTrace.getLastSampleNumber()/1000+"s"+
//					" Do you want to move selection end to "+(double)gfcTrace.getLastSampleNumber()/1000+"s ?");	
//			
//			if (result == true)
//			{
//				compositePanel.setSelectionFields(
//						(int) compositePanel.getSelectionStart(),gfcTrace.getLastSampleNumber());
//			}
//			else endOk =false;
//		}
//		if (startOk == false || endOk == false) return false;
//		else return true;
//	}

	public void receiveEvent(String action, Event event) 
	{
		if (action.equals("functionAnalysis")) //$NON-NLS-1$
		{
    		try {
/*
			AnalyseTab tab = PIPageEditor.currentTab();
			PICompositePanel compositePanel =  tab.getProfilePage(3).getTopSwingPanel(); 
			
			if (compositePanel.getSelectionStart() == -1 || compositePanel.getSelectionEnd() == -1 ||
	    			(compositePanel.getSelectionStart() == compositePanel.getSelectionEnd()))
	    	{
				GeneralMessages.showErrorMessage("Make a selection from the graph");
	    		return;
	    	}
	    	else
	    	{
	    		ParsedTraceData ptd = TraceDataRepository.getTrace(tab.getTabId(), "com.nokia.carbide.cpp.pi.address.GppTrace");
	    		
	    		int mode = ((GppTraceGraph)((GppTrace)ptd.traceData).getTraceGraph()).getDrawMode();
	    	    String[] selectedItems = null;
	    	    if (mode == Defines.THREADS || mode == Defines.THREADS_FUNCTIONS)
	    	        selectedItems = compositePanel.getSharedData().GPP_SelectedThreadNames;
	    	    else
	    	        selectedItems = compositePanel.getSharedData().GPP_SelectedBinaryNames;

	    	    ptd = TraceDataRepository.getTrace(tab.getTabId(), "com.nokia.carbide.cpp.pi.call.GfcTrace");
	    	    //GfcTrace gfcTrace = (GfcTrace)ptd.traceData;
	        	if (ptd == null ||
	        			this.checkGfcRequirements(compositePanel, (GfcTrace)ptd.traceData) == false)
	        	{
	        		GeneralMessages.showNotificationMessage("Linked function analysis is disabled");
	        		new NewFunctionAnalyse(compositePanel, selectedItems, false, mode);
	        	}
	        	else
	        	{
	        		new NewFunctionAnalyse(compositePanel, selectedItems, true, mode);
	        	}
	    	}
*/
	    			// this menu is not used in eclipse, replaced by function call
	    			throw new Exception (Messages.getString("FunctionPlugin.fixException")); //$NON-NLS-1$
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    	}
		}
	}
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static FunctionPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractPiPlugin.imageDescriptorFromPlugin("com.nokia.carbide.cpp.pi.function", path); //$NON-NLS-1$
	}
}
