/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
package com.nokia.s60tools.swmtanalyser.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.nokia.s60tools.swmtanalyser.SwmtAnalyserPlugin;
import com.nokia.s60tools.swmtanalyser.analysers.ResultElements;
import com.nokia.s60tools.swmtanalyser.analysers.ResultsParentNodes;
import com.nokia.s60tools.swmtanalyser.editors.GraphedItemsInput;
import com.nokia.s60tools.swmtanalyser.editors.TableViewerInputObject;
import com.nokia.s60tools.ui.StringArrayClipboardCopyHandler;
import com.nokia.s60tools.ui.actions.S60ToolsBaseAction;
import com.nokia.s60tools.util.console.IConsolePrintUtility;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Action class for Copy text data from tables to clipboard
 */
public class CopyToClipboardAction extends S60ToolsBaseAction {
	
	/**
	 * Table viewer instance where items are located is given by observer,
	 * also call back is send back through observer 
	 */	
	private final ISelectionProvider observer;
		
	/**
	 * Constructor
	 * @param observer to provide selection
	 */
	public CopyToClipboardAction(ISelectionProvider observer) {
		super("Copy", 
				"Copy to clipboard", 
				IAction.AS_PUSH_BUTTON, null);
		this.observer = observer;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		try {
			
			//Get selection made in UI through observer
			ISelection selection = observer.getUserSelection();
			if (selection == null || selection.isEmpty()){
				return;//If no selections made, just return
			}
			// go through all selected files and add file path to clipboard
			// ISelection comes from {@link TableViewer} so, there's nothing we
			// can do about type safety.
			@SuppressWarnings("unchecked")
			Iterator<TableViewerInputObject> iter = ((IStructuredSelection) selection)
					.iterator();
			List<Object> items = new ArrayList<Object>();
			
			String tab = "\t";
			boolean isHeaderFound = false;
			String headers = null;
			//Get all selected items
			while (iter.hasNext()) {
				Object o = iter.next();	
				String str = null;
				//Copy from Graphs
				if(o instanceof TableViewerInputObject){
					TableViewerInputObject to = (TableViewerInputObject)o;				
					str = to.getName();
				}
				//Copy from Graphed items
				else if(o instanceof GraphedItemsInput){
					GraphedItemsInput gi = (GraphedItemsInput)o;
					str = gi.getTabSeparatedValues();
					//Adding headers when found out at least one data item
					if(!isHeaderFound){
						headers = gi.getTabSeparatedHeaders();
						isHeaderFound = true;
					}					
				}
				//Copy from Analysis (data)
				else if(o instanceof ResultElements){
					ResultElements re = (ResultElements)o;
					str = tab + re.getTabSeparatedValues();
					//Adding headers when found out at least one data item
					if(!isHeaderFound){
						headers = tab + re.getTabSeparatedHeaders();
						isHeaderFound = true;
					}
				}
				//Copy from Analysis (parent node)
				else if(o instanceof ResultsParentNodes){
					ResultsParentNodes rpn = (ResultsParentNodes)o;
					str = rpn.toString();
				}
				//Else it's internal error if we can't found proper handle to class given to Copy.
				else{
					DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Unable to found Copy functionality to Object: (" +o + "), Class: " +o.getClass());
				}
				
				if(str != null){
					items.add(str);
				}

			}
			
			//If at least one data item was found, also data headers will be added, but only once.
			if(headers != null){
				items.add(0, headers);
			}
			
			StringArrayClipboardCopyHandler copyHandler = new StringArrayClipboardCopyHandler();
			copyHandler.acceptAndCopy(items);
		
		} catch (Exception e) {
			e.printStackTrace();
			SwmtAnalyserPlugin.getConsole().println(e.getMessage(),
					IConsolePrintUtility.MSG_ERROR);
		}
	}	
	
}
