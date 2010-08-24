/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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
package com.nokia.s60tools.crashanalyser.containers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class OstTrace {

	private List<OstTraceLine> ostTraces;
	
	/**
	 * Private constructor.
	 * @param traceLines Trace lines.
	 */
	private OstTrace(List<OstTraceLine> traceLines) {
		ostTraces = traceLines;
	}
	
	/**
	 * Writes traces in to a buffer (e.g. a text file)
	 * @param out where to write
	 * @throws IOException
	 */
	public void writeTo(BufferedWriter out) throws IOException {
		writeLine(out,"");
		writeLine(out, "OST TRACE LOG:");
		writeLine(out, "-----------------");
		// if there are any events
		if (ostTraces != null && !ostTraces.isEmpty()) {
			// print events
			for (OstTraceLine traceLine : ostTraces) {
				String format = "%s : %s : %s : %s : %s : %s : %s : %s : %s : %s";
				String line = String.format(format, traceLine.getTimestamp(), traceLine.getTraceText(), 
						traceLine.getFile(), traceLine.getLineNumber(), traceLine.getType(), 
						traceLine.getContextId(), traceLine.getPrefix(),
						traceLine.getComponent(), traceLine.getGroup(), traceLine.getId());
				writeLine(out, line);
			}
		}
	}

	/**
	 * Writes given line plus a line break.
	 * @param out where to write
	 * @param line what to write
	 * @throws IOException
	 */
	void writeLine(BufferedWriter out, String line) throws IOException {
		out.write(line);
		out.newLine();
	}

	/**
	 * Reads OST traces from an XML element
	 * @param elementSegTraces seg_traces tag
	 * @return a created OstTrace class or null
	 */
	public static OstTrace read(Element elementSegOstTrace) {
		List<OstTraceLine> traces = new ArrayList<OstTraceLine>();
		try {
			
			// get all trace nodes
			NodeList children = elementSegOstTrace.getChildNodes();
			if (children != null && children.getLength() > 0) {
				// go through all line nodes
				for (int i = 0; i < children.getLength(); i++) {
					Node traceNode = children.item(i);
					OstTraceLine traceLine = OstTraceLine.read(traceNode);
					if(traceLine != null) {
						traces.add(traceLine);
					}
				}
			}
			
		} catch (Exception e) {
			// Do nothing.
		}
		return new OstTrace(traces);
	}


	/**
	 * Returns the trace lines.
	 * @return Trace lines
	 */
	public List<OstTraceLine> getTraces() {
		return ostTraces;
	}
}
