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
 * Handler for delete command
 *
 */
package com.nokia.tracebuilder.action;

import com.nokia.tracebuilder.engine.LastKnownLocation;
import com.nokia.tracebuilder.engine.LastKnownLocationList;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;
import com.nokia.tracebuilder.rules.ReadOnlyObjectRule;
import com.nokia.tracebuilder.rules.TraceParameterRestrictionRule;

/**
 * Handler for delete command
 * 
 */
final class DeleteAction extends TraceBuilderAction {

	/**
	 * Constructor
	 */
	DeleteAction() {
		setText(Messages.getString("DeleteAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("DeleteAction.Tooltip")); //$NON-NLS-1$
		setDefaultProperties(ActionIDs.DELETE_ACTION_ID,
				TraceBuilderHelp.DELETE_OBJECT_HELP_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void doRun() throws TraceBuilderException {
		TraceBuilderGlobals.getTraceBuilder().delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.action.TraceBuilderAction#getEnabledFlag(java.
	 * lang.Object)
	 */
	@Override
	protected boolean getEnabledFlag(Object selectedObject) {
		boolean retval;
		selectedObject = locationToTrace(selectedObject);
		if (selectedObject instanceof TraceGroup) {
			setText(Messages.getString("ActionFactory.DeleteGroupAction")); //$NON-NLS-1$
			retval = true;
		} else if (selectedObject instanceof Trace) {
			setText(Messages.getString("ActionFactory.DeleteTraceAction")); //$NON-NLS-1$
			retval = true;
		} else if (selectedObject instanceof TraceParameter) {
			TraceParameter selectedParameter = (TraceParameter) selectedObject;
			Trace owner = selectedParameter.getTrace();
			TraceParameterRestrictionRule restriction = owner
					.getExtension(TraceParameterRestrictionRule.class);
			ReadOnlyObjectRule readOnly = owner
					.getExtension(ReadOnlyObjectRule.class);
			if (readOnly != null
					|| (restriction != null && !restriction
							.canRemoveParameters())) {
				setText(Messages.getString("DeleteAction.Title")); //$NON-NLS-1$
				retval = false;
			} else {
				setText(Messages
						.getString("ActionFactory.DeleteParameterAction")); //$NON-NLS-1$

				// If there are last known locations, source file is not open
				// and parameters cannot be deleted
				LastKnownLocationList list = owner
						.getExtension(LastKnownLocationList.class);
				String groupName = owner.getGroup().getName();

				// Only optional 32-bit instance identifier can be deleted from
				// State Trace
				GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals
						.getGroupNameHandler();
				boolean isStateTrace = groupName.equals(groupNameHandler
						.getDefaultGroups()[groupNameHandler
						.getStateGroupIdIndex()]);
				if ((list != null && list.hasLocations())
						|| (isStateTrace && selectedParameter.getType().equals(
								TraceParameter.ASCII))) {
					retval = false;
				} else {
					retval = true;
				}
			}
		} else if (selectedObject instanceof TraceConstantTable) {
			if (!((TraceConstantTable) selectedObject).hasParameterReferences()) {
				setText(Messages
						.getString("ActionFactory.DeleteConstantTableAction")); //$NON-NLS-1$
				retval = true;
			} else {
				setText(Messages.getString("DeleteAction.ConstantTableInUse")); //$NON-NLS-1$
				retval = false;
			}
		} else if (selectedObject instanceof TraceConstantTableEntry) {
			setText(Messages.getString("ActionFactory.DeleteConstantAction")); //$NON-NLS-1$
			retval = true;
		} else {
			setText(Messages.getString("DeleteAction.Title")); //$NON-NLS-1$
			retval = false;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.action.TraceBuilderAction#isInPopupMenu(java.lang
	 * .Object)
	 */
	@Override
	protected boolean isInPopupMenu(Object selectedObject) {
		Object parent = locationToTrace(selectedObject);
		return isEnabled() && (parent instanceof TraceObject)
				&& !(selectedObject instanceof TraceLocation)
				&& !(selectedObject instanceof LastKnownLocation);
	}

}