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
 * Search utils
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.util.List;

import com.nokia.traceviewer.engine.TraceInformation;

/**
 * Search utils
 */
public final class SearchUtils {

	/**
	 * Empty String
	 */
	public static final String EMPTY = ""; //$NON-NLS-1$

	/**
	 * Dash string
	 */
	private static final String DASH = "-"; //$NON-NLS-1$

	/**
	 * Indicates not found
	 */
	public static final int NOT_FOUND = -1;

	/**
	 * Semicolon
	 */
	private static final char SEMICOLON = ';';

	/**
	 * Component ID String
	 */
	private static final String CID_STR = "cid:"; //$NON-NLS-1$

	/**
	 * Group ID String
	 */
	private static final String GID_STR = "gid:"; //$NON-NLS-1$

	/**
	 * Trace ID String
	 */
	private static final String TID_STR = "tid:"; //$NON-NLS-1$

	/**
	 * Timestamp String
	 */
	private static final String TIMESTAMP_STR = "{timestamp_srch}:"; //$NON-NLS-1$

	/**
	 * Parses trace information class from the seach string
	 * 
	 * @param searchStr
	 *            the search string
	 * @return trace information containing cid, gid and tid
	 */
	public static TraceInformation parseIDsFromString(String searchStr) {
		TraceInformation inf = new TraceInformation();

		// Parse component ID
		int cidIndex = searchStr.indexOf(CID_STR);
		String cidStr = searchStr.substring(cidIndex + CID_STR.length(),
				searchStr.indexOf(SEMICOLON, cidIndex));
		int cid = Integer.parseInt(cidStr);

		// Parse group ID
		int gidIndex = searchStr.indexOf(GID_STR);
		String gidStr = searchStr.substring(gidIndex + GID_STR.length(),
				searchStr.indexOf(SEMICOLON, gidIndex));
		int gid = Integer.parseInt(gidStr);
		searchStr = searchStr.substring(searchStr.indexOf(SEMICOLON) + 1);

		// Parse trace ID
		int tidIndex = searchStr.indexOf(TID_STR);
		String tidStr = searchStr.substring(tidIndex + TID_STR.length(),
				searchStr.indexOf(SEMICOLON, tidIndex));
		int tid = Integer.parseInt(tidStr);

		// Set ID's and return
		inf.setComponentId(cid);
		inf.setGroupId(gid);
		inf.setTraceId(tid);

		return inf;
	}

	/**
	 * Parses searchable timestamp from the seach string
	 * 
	 * @param searchStr
	 *            the search string
	 * @param getFirstTimestamp
	 *            if true, get first timestamp. If false, get second timestamp
	 * @return searchable timestamp
	 */
	public static String parseTimestampFromString(String searchStr,
			boolean getFirstTimestamp) {

		int timestampStartIndex = searchStr.indexOf(TIMESTAMP_STR);
		int startIndex = timestampStartIndex + TIMESTAMP_STR.length();
		int endIndex = searchStr.length();

		int dashIndex = searchStr.indexOf(DASH);
		if (dashIndex != -1) {

			// Get first timestamp
			if (getFirstTimestamp) {
				endIndex = dashIndex;
			} else {
				startIndex = dashIndex + 1;
			}
		}

		// Parse timestamp
		String timestampStr = searchStr.substring(startIndex, endIndex);

		return timestampStr;
	}

	/**
	 * Finds trace line from informations array
	 * 
	 * @param startTrace
	 *            start trace where to start search
	 * @param inf
	 *            trace information to search for
	 * @param informations
	 *            trace informations arraylist
	 * @param searchForward
	 *            if true, search forward. Otherwise search backward
	 * @return line of found trace or -1 if not found
	 */
	public static int findTraceOffsetFromInformations(int startTrace,
			TraceInformation inf, List<TraceInformation> informations,
			boolean searchForward) {
		int line = NOT_FOUND;

		// Search forward
		if (searchForward && informations != null) {
			// Start from start line and go through ID's
			for (int i = startTrace; i < informations.size(); i++) {
				TraceInformation listInf = informations.get(i);
				if (idsMatch(inf, listInf)) {
					line = i;
					break;
				}
			}

			// Search backwards
		} else if (informations != null) {
			// Start from start line and go through ID's backwards
			for (int i = startTrace; i >= 0; i--) {
				TraceInformation listInf = informations.get(i);
				if (idsMatch(inf, listInf)) {
					line = i;
					break;
				}
			}
		}

		return line;
	}

	/**
	 * Finds trace line from timestamps array
	 * 
	 * @param startTrace
	 *            start trace where to start search
	 * @param timestamp
	 *            timestamp to search for
	 * @param timestamps
	 *            timestamps arraylist
	 * @param searchForward
	 *            if true, search forward. Otherwise search backward
	 * @return line of found trace or -1 if not found
	 */
	public static int findTraceOffsetFromTimestamps(int startTrace,
			String timestamp, List<String> timestamps, boolean searchForward) {
		int line = NOT_FOUND;

		// Search forward
		if (searchForward && timestamps != null) {

			// Start from start line and go through timestamps
			for (int i = startTrace; i < timestamps.size(); i++) {
				if (i < timestamps.size()) {
					String ts = timestamps.get(i);
					if (ts.compareTo(timestamp) >= 0) {
						line = i;
						break;
					}
				}
			}

			// Search backwards
		} else if (timestamps != null) {
			// Start from start line and go through timestamps backwards
			for (int i = startTrace; i >= 0; i--) {
				if (i < timestamps.size()) {
					String ts = timestamps.get(i);
					if (ts.compareTo(timestamp) <= 0) {
						line = i;
						break;
					}
				}
			}
		}

		return line;
	}

	/**
	 * Checks if all ID's match
	 * 
	 * @param inf
	 *            first ID list
	 * @param listInf
	 *            second ID list
	 * @return true if all ID's match
	 */
	private static boolean idsMatch(TraceInformation inf,
			TraceInformation listInf) {
		return listInf.getComponentId() == inf.getComponentId()
				&& listInf.getGroupId() == inf.getGroupId()
				&& listInf.getTraceId() == inf.getTraceId();
	}

	/**
	 * Checks does the search string contain ID query
	 * 
	 * @param searchString
	 *            the search string
	 * @return true if search string contains ID query
	 */
	public static boolean containsIdQuery(String searchString) {
		boolean containsIdQuery = false;

		// Check that string contains cid, gid and tid strings
		if (searchString.contains(CID_STR) && searchString.contains(GID_STR)
				&& searchString.contains(TID_STR)) {

			// Parse ID's as strings
			int cidIdx = searchString.indexOf(CID_STR);
			String cid = searchString.substring(cidIdx + CID_STR.length(),
					searchString.indexOf(SEMICOLON, cidIdx));
			int gidIdx = searchString.indexOf(GID_STR);
			String gid = searchString.substring(gidIdx + GID_STR.length(),
					searchString.indexOf(SEMICOLON, gidIdx));
			int tidIdx = searchString.indexOf(TID_STR);
			String tid = searchString.substring(tidIdx + TID_STR.length(),
					searchString.indexOf(SEMICOLON, tidIdx));

			// Check that ID's are real integers
			if (!cid.equals(EMPTY) && !gid.equals(EMPTY) && !tid.equals(EMPTY)) {
				try {
					Integer.parseInt(cid);
					Integer.parseInt(gid);
					Integer.parseInt(tid);
					containsIdQuery = true;
				} catch (NumberFormatException e) {
					containsIdQuery = false;
				}
			}
		}

		return containsIdQuery;
	}

	/**
	 * Checks does the search string contain timestamp query
	 * 
	 * @param searchString
	 *            the search string
	 * @return true if search string contains timestamp query
	 */
	public static boolean containsTimestampQuery(String searchString) {
		boolean containsTimestampQuery = false;

		// Check that string contains timestamp strings
		if (searchString.contains(TIMESTAMP_STR)) {
			containsTimestampQuery = true;
		}

		return containsTimestampQuery;
	}

	/**
	 * Checks does the search string contain timestamp range query. User should
	 * first call "containsTimestampQuery" before this to actually ensure this
	 * is timestamp range query.
	 * 
	 * @param searchString
	 *            the search string
	 * @return true if search string contains timestamp query
	 */
	public static boolean containsTimestampRangeQuery(String searchString) {
		boolean containsTimestampRangeQuery = false;

		// Check that string contains timestamp range string
		int dashIndex = searchString.indexOf(DASH);
		if (dashIndex != -1 && searchString.length() > dashIndex + 1) {
			containsTimestampRangeQuery = true;
		}

		return containsTimestampRangeQuery;
	}

	/**
	 * Creates a search string from ID's
	 * 
	 * @param cid
	 *            Component ID
	 * @param gid
	 *            Group ID
	 * @param tid
	 *            Trace ID
	 * @return a search string that can be used to search with ID's
	 */
	public static String createStringFromIDs(int cid, int gid, int tid) {
		StringBuffer buf = new StringBuffer();
		// Add component ID
		buf.append(CID_STR);
		buf.append(cid);
		buf.append(SEMICOLON);

		// Add group ID
		buf.append(GID_STR);
		buf.append(gid);
		buf.append(SEMICOLON);

		// Add trace ID
		buf.append(TID_STR);
		buf.append(tid);
		buf.append(SEMICOLON);

		return buf.toString();
	}

	/**
	 * Creates a search string from timestamp
	 * 
	 * @param startTimestamp
	 *            start timestamp
	 * @param endTimestamp
	 *            end timestamp
	 * @return a search string that can be used to search with timestamp
	 */
	public static String createStringFromTimestamp(String startTimestamp,
			String endTimestamp) {
		StringBuffer buf = new StringBuffer();

		// Add start timestamp
		buf.append(TIMESTAMP_STR);
		buf.append(startTimestamp);

		// If end timestamp exists, add it
		if (endTimestamp != null) {
			buf.append(DASH);
			buf.append(endTimestamp);
		}

		return buf.toString();
	}
}
