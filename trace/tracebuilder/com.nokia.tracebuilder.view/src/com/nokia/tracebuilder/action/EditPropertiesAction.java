/*
* Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
* Handler for edit properties command
*
*/
package com.nokia.tracebuilder.action;

import com.nokia.tracebuilder.engine.LastKnownLocation;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;
import com.nokia.tracebuilder.rules.ReadOnlyObjectRule;

/**
 * Handler for edit properties command
 * 
 */
final class EditPropertiesAction extends TraceBuilderAction {

	/**
	 * Constructor
	 */
	EditPropertiesAction() {
		setText(Messages.getString("EditPropertiesAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("EditPropertiesAction.Tooltip")); //$NON-NLS-1$
		setDefaultProperties(ActionIDs.EDIT_PROPERTIES_ACTION_ID,
				TraceBuilderHelp.EDIT_PROPERTIES_HELP_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#doRun()
	 */
	@Override
	protected void doRun() throws TraceBuilderException {
		TraceBuilderGlobals.getTraceBuilder().showProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#getEnabledFlag(java.lang.Object)
	 */
	@Override
	protected boolean getEnabledFlag(Object selectedObject) {
		boolean retval;
		selectedObject = locationToTrace(selectedObject);
		if (selectedObject instanceof TraceGroup) {
			retval = true;
			setText(Messages.getString("ActionFactory.GroupPropertiesAction")); //$NON-NLS-1$
		} else if (selectedObject instanceof Trace) {
			if (((Trace) selectedObject).getExtension(ReadOnlyObjectRule.class) != null) {
				retval = false;
				setText(Messages.getString("EditPropertiesAction.Title")); //$NON-NLS-1$
			} else {
				retval = true;
				setText(Messages
						.getString("ActionFactory.TracePropertiesAction")); //$NON-NLS-1$
			}
		} else if (selectedObject instanceof TraceConstantTable) {
			if (!((TraceConstantTable) selectedObject).hasParameterReferences()) {
				retval = true;
				setText(Messages
						.getString("ActionFactory.ConstantTableProperties")); //$NON-NLS-1$
			} else {
				retval = false;
				setText(Messages
						.getString("EditPropertiesAction.ConstantTableInUse")); //$NON-NLS-1$
			}
		} else if (selectedObject instanceof TraceConstantTableEntry) {
			retval = true;
			setText(Messages
					.getString("ActionFactory.ConstantPropertiesAction")); //$NON-NLS-1$
		} else {
			retval = false;
			setText(Messages.getString("EditPropertiesAction.Title")); //$NON-NLS-1$
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#isInPopupMenu(java.lang.Object)
	 */
	@Override
	protected boolean isInPopupMenu(Object selectedObject) {
		Object parent = locationToTrace(selectedObject);
		boolean retval = isEnabled() && (parent instanceof TraceObject)
				&& !(selectedObject instanceof TraceLocation)
				&& !(selectedObject instanceof TraceParameter)
				&& !(selectedObject instanceof LastKnownLocation);
		if (retval && (selectedObject instanceof TraceLocationList)) {
			// Unrelated locations list does not show this action
			if (!(((TraceLocationList) selectedObject).getOwner() instanceof Trace)) {
				retval = false;
			}
		} else if (retval && (selectedObject instanceof TraceGroup)) {
			TraceGroup group = (TraceGroup) selectedObject;
			String groupName = group.getName();
			
			// Name of TRACE_STATE and TRACE_PERFORMANCE groups can not be changed
			GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
			String[] defaultGroups = groupNameHandler.getDefaultGroups();
			if (groupName
					.equals(defaultGroups[groupNameHandler.getStateGroupIdIndex()])
					|| groupName
							.equals(defaultGroups[groupNameHandler.getPerformanceGroupIdIndex()])) {
				retval = false;
			}

		}
		return retval;
	}

}