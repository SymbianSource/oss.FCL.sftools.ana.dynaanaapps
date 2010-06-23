/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * OST Trace Activator class
 *
 */
package com.nokia.traceviewer.engine.activation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * OST Trace Activator
 * 
 */
public final class OstTraceActivator {

	/**
	 * Hex prefix
	 */
	private static final String HEX_PREFIX = "0x"; //$NON-NLS-1$

	/**
	 * Lead zero
	 */
	private static final String LEAD_ZERO = "0"; //$NON-NLS-1$

	/**
	 * Group separator
	 */
	private static final String GROUP_SEPARATOR = ", "; //$NON-NLS-1$

	/**
	 * OST TraceProvider class name
	 */
	private static final String COM_NOKIA_TRACEVIEWER_OST_ENGINE = "com.nokia.traceviewer.ost.OstEngine"; //$NON-NLS-1$

	/**
	 * Trace activation category
	 */
	private static final String TRACE_ACTIVATION_CATEGORY = Messages
			.getString("OstTraceActivator.TraceActivationCategory"); //$NON-NLS-1$

	/**
	 * Activation group string
	 */
	private static String activationGroupStr;

	/**
	 * Deactivation group string
	 */
	private static String deactivationGroupStr;

	/**
	 * Activates components in this list
	 * 
	 * @param changedComponents
	 *            the changed components
	 */
	public static void activate(
			List<TraceActivationComponentItem> changedComponents) {

		// Loop through components
		for (int i = 0; i < changedComponents.size(); i++) {
			activationGroupStr = ""; //$NON-NLS-1$
			deactivationGroupStr = ""; //$NON-NLS-1$

			int cid = changedComponents.get(i).getId();

			// Split component's groups to activation and deactivation arrays
			List<TraceActivationGroupItem> activationItems = new ArrayList<TraceActivationGroupItem>();
			List<TraceActivationGroupItem> deActivationItems = new ArrayList<TraceActivationGroupItem>();
			splitArray(changedComponents.get(i), activationItems,
					deActivationItems);

			// Sort the arrays so that groups are in ascending order
			int[] activationGroups = sortToAscending(activationItems, true);
			int[] deActivationGroups = sortToAscending(deActivationItems, false);

			// Send activation message even activation groups count can be zero
			TraceViewerGlobals.getTraceProvider().activate(true, true, cid,
					activationGroups);

			// Post to event view if there were activated groups
			if (activationGroups.length > 0) {
				String infoEventMsg = constructInfoEventMsg(cid, true);
				TraceViewerGlobals.postInfoEvent(infoEventMsg,
						TRACE_ACTIVATION_CATEGORY, null);
			}

			// Send deactivation message
			if (deActivationGroups.length > 0) {

				// Send deactivation messages only if OST protocol in use
				if (TraceViewerGlobals.getTraceProvider().getClass().getName()
						.equals(COM_NOKIA_TRACEVIEWER_OST_ENGINE)) {
					TraceViewerGlobals.getTraceProvider().activate(false,
							false, cid, deActivationGroups);
				}

				// Post to event view
				String infoEventMsg = constructInfoEventMsg(cid, false);
				TraceViewerGlobals.postInfoEvent(infoEventMsg,
						TRACE_ACTIVATION_CATEGORY, null);
			}
		}
	}

	/**
	 * Constructs info event message
	 * 
	 * @param cid
	 *            component ID
	 * @param activate
	 *            if true, construct activate message, otherwise deactivate
	 *            message
	 * @return info event message
	 */
	private static String constructInfoEventMsg(int cid, boolean activate) {
		String infoEventMsg = null;
		String componentName = null;

		if (TraceViewerGlobals.getDecodeProvider() != null) {
			componentName = TraceViewerGlobals.getDecodeProvider()
					.getComponentName(cid);
		}

		// No component name was found, use CID
		String groupStr = ""; //$NON-NLS-1$
		if (componentName == null) {
			if (activate) {
				infoEventMsg = Messages
						.getString("OstTraceActivator.ActivateInfoEventCid"); //$NON-NLS-1$
				groupStr = activationGroupStr;
			} else {
				infoEventMsg = Messages
						.getString("OstTraceActivator.DeactivateInfoEventCid"); //$NON-NLS-1$
				groupStr = deactivationGroupStr;
			}
			infoEventMsg += Integer.toHexString(cid)
					+ Messages.getString("OstTraceActivator.GroupsInfoEvent") //$NON-NLS-1$
					+ groupStr;

			// Use component name
		} else {
			if (activate) {
				infoEventMsg = Messages
						.getString("OstTraceActivator.ActivateInfoEventName"); //$NON-NLS-1$
				groupStr = activationGroupStr;
			} else {
				infoEventMsg = Messages
						.getString("OstTraceActivator.DeactivateInfoEventName"); //$NON-NLS-1$
				groupStr = deactivationGroupStr;
			}
			infoEventMsg += componentName
					+ Messages.getString("OstTraceActivator.GroupsInfoEvent") //$NON-NLS-1$
					+ groupStr;
		}

		return infoEventMsg;
	}

	/**
	 * Splits groups from a component to activation and deactiation arrays
	 * 
	 * @param component
	 *            the component object
	 * @param activationItems
	 *            activation groups
	 * @param deActivationItems
	 *            deactivation groups
	 */
	private static void splitArray(TraceActivationComponentItem component,
			List<TraceActivationGroupItem> activationItems,
			List<TraceActivationGroupItem> deActivationItems) {

		// Loop through the groups
		List<TraceActivationGroupItem> groups = component.getGroups();
		for (int i = 0; i < groups.size(); i++) {
			TraceActivationGroupItem group = groups.get(i);
			if (group.isActivated()) {
				activationItems.add(group);
			} else {
				deActivationItems.add(group);
			}
		}
	}

	/**
	 * Sorts groups to ascending order and returns them as a int array
	 * 
	 * @param groups
	 *            the array of groups
	 * @param activate
	 *            if true, sorting activation array. Otherwise sorting
	 *            deactivate array
	 * @return sorted int array in ascending order
	 */
	private static int[] sortToAscending(List<TraceActivationGroupItem> groups,
			boolean activate) {

		// Create a comparator
		Collections.sort(groups, new Comparator<TraceActivationGroupItem>() {

			public int compare(TraceActivationGroupItem o1,
					TraceActivationGroupItem o2) {
				int id1 = (o1).getId();
				int id2 = (o2).getId();

				return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
			}
		});

		// Create a int array and insert objects to it
		int[] arr = new int[groups.size()];
		for (int i = 0; i < groups.size(); i++) {
			int id = groups.get(i).getId();
			arr[i] = id;

			String groupString = Integer.toHexString(id);
			if (groupString.length() == 1) {
				groupString = LEAD_ZERO + groupString;
			}

			// Add to activation string
			if (activate) {
				activationGroupStr += HEX_PREFIX + groupString
						+ GROUP_SEPARATOR;
			} else {
				deactivationGroupStr += HEX_PREFIX + groupString
						+ GROUP_SEPARATOR;
			}
		}

		// Remove last comma
		if (!groups.isEmpty()) {
			if (activate) {
				activationGroupStr = activationGroupStr.substring(0,
						activationGroupStr.length() - GROUP_SEPARATOR.length());
			} else {
				deactivationGroupStr = deactivationGroupStr.substring(0,
						deactivationGroupStr.length()
								- GROUP_SEPARATOR.length());
			}
		}

		return arr;
	}
}
