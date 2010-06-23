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
 * Show trace info command
 *
 */
package com.nokia.traceviewer.action;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.dialog.ShowTraceInfoDialog;
import com.nokia.traceviewer.engine.BTraceInformation;
import com.nokia.traceviewer.engine.TraceMetaData;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerUtils;

/**
 * Handler for show trace info command
 * 
 */
public final class ShowTraceInfoAction extends TraceViewerAction {

	/**
	 * Characters for hex string
	 */
	private final static char hexChars[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * One byte takes this many characters to show when as hex string
	 */
	private static final int BYTE_AS_HEX_STRING_LENGTH = 3;

	/**
	 * Empty string
	 */
	private static final String EMPTY = ""; //$NON-NLS-1$

	/**
	 * Hex prefix
	 */
	private static final String HEX_PREFIX = "0x"; //$NON-NLS-1$

	/**
	 * Lead zero
	 */
	private static final String LEAD_ZERO = "0"; //$NON-NLS-1$

	/**
	 * Start parenthesis
	 */
	private static final char START_PARENTHESIS = '(';

	/**
	 * End parenthesis
	 */
	private static final char END_PARENTHESIS = ')';

	/**
	 * Line break
	 */
	private static final String LINE_BREAK = "\n"; //$NON-NLS-1$

	/**
	 * Colon + space combination
	 */
	private static final String COLON_SPACE = ": "; //$NON-NLS-1$

	/**
	 * Image for the action
	 */
	private static ImageDescriptor image;

	/**
	 * Trace properties
	 */
	private TraceProperties trace;

	/**
	 * Constructor
	 */
	ShowTraceInfoAction() {
		image = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_OBJS_INFO_TSK);
		setText(Messages.getString("ShowTraceInfoAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("ShowTraceInfoAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.ACTIONS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		List<StyleRange> styleRanges = new ArrayList<StyleRange>();
		int lineNumber = 0;
		if (trace != null) {
			lineNumber = trace.traceNumber;
		}

		String traceNumberString = Messages
				.getString("ShowTraceInfoAction.TraceNumberString") + COLON_SPACE //$NON-NLS-1$
				+ (lineNumber) + LINE_BREAK;
		String idString = EMPTY;
		String locationString = EMPTY;
		String classMethodString = EMPTY;
		String bTraceString = EMPTY;
		String hexString = EMPTY;
		String hexTrace = EMPTY;

		if (trace != null) {
			TraceMetaData traceMetadata = TraceViewerGlobals
					.getDecodeProvider().getTraceMetaData(trace.information);

			// Construct ID strings
			if (trace.information.isDefined()) {
				idString = constructIdString();
			}

			// Construct metadata strings if it's available
			if (traceMetadata != null) {

				// Get defined in path
				String path = traceMetadata.getPath();
				if (path != null) {
					String locationStr = Messages
							.getString("ShowTraceInfoAction.LocationStr"); //$NON-NLS-1$
					locationString += LINE_BREAK + locationStr + COLON_SPACE
							+ path + LINE_BREAK;
				}

				// Get defined in line number
				int definedInLine = traceMetadata.getLineNumber();
				if (definedInLine != 0) {
					String lineNrStr = Messages
							.getString("ShowTraceInfoAction.LineNumberStr"); //$NON-NLS-1$
					locationString += lineNrStr + COLON_SPACE + definedInLine
							+ LINE_BREAK;
				}

				// Get class name
				String className = traceMetadata.getClassName();
				if (className != null && !className.equals(EMPTY)) {
					String classNameStr = Messages
							.getString("ShowTraceInfoAction.ClassnameStr"); //$NON-NLS-1$
					classMethodString += classNameStr + COLON_SPACE + className
							+ LINE_BREAK;
				}

				// Get method name
				String methodName = traceMetadata.getMethodName();
				if (methodName != null && !methodName.equals(EMPTY)) {
					String methodNameStr = Messages
							.getString("ShowTraceInfoAction.MethodnameStr"); //$NON-NLS-1$
					classMethodString += methodNameStr + COLON_SPACE
							+ methodName + LINE_BREAK;
				}
			}

			// Construct BTrace string
			if (trace.bTraceInformation.getRecordSize() != 0) {
				int totalStringLength = (traceNumberString + idString
						+ locationString + classMethodString).length();
				bTraceString = constructBTraceString(totalStringLength,
						styleRanges);
			}

			// Get trace as HEX
			if (trace.byteBuffer != null) {
				hexString = LINE_BREAK
						+ Messages.getString("ShowTraceInfoAction.HexString"); //$NON-NLS-1$
				hexString += COLON_SPACE + START_PARENTHESIS
						+ trace.messageLength;
				hexString += Messages
						.getString("ShowTraceInfoAction.BytesString"); //$NON-NLS-1$
				hexString += END_PARENTHESIS + LINE_BREAK;
				hexTrace = TraceViewerUtils.getTraceAsHexString(
						trace.byteBuffer, trace.messageStart,
						trace.messageLength, true);
			}
		}

		int headerLength = 0;

		// Create the contents
		hexString += hexTrace + LINE_BREAK;
		String contents = traceNumberString + idString + locationString
				+ classMethodString + bTraceString + hexString;
		int headerStartOffset = contents.length() - hexTrace.length() - 1;
		if (trace != null) {

			// Calculate header color offset
			headerLength = (trace.dataStart - trace.messageStart)
					* BYTE_AS_HEX_STRING_LENGTH;
			StyleRange headerRange = new StyleRange(headerStartOffset,
					headerLength, Display.getDefault().getSystemColor(
							SWT.COLOR_RED), null);
			styleRanges.add(headerRange);

			// Calculate data color offset
			if (headerStartOffset + headerLength + 1 < contents.length()) {
				StyleRange dataRange = new StyleRange(headerStartOffset
						+ headerLength, contents.length()
						- (headerStartOffset + headerLength + 1) - 1, Display
						.getDefault().getSystemColor(SWT.COLOR_BLUE), null);
				styleRanges.add(dataRange);
			}
		}

		// Show information message
		new ShowTraceInfoDialog(PlatformUI.getWorkbench().getDisplay()
				.getActiveShell(), contents, styleRanges).openDialog();
	}

	/**
	 * Constructs ID string
	 * 
	 * @return ID string
	 */
	private String constructIdString() {

		// Get names from Dictionary
		String[] names = TraceViewerGlobals.getDecodeProvider()
				.getComponentGroupTraceName(trace.information.getComponentId(),
						trace.information.getGroupId(),
						trace.information.getTraceId());

		// Component ID and name
		String idStr = Messages.getString("ShowTraceInfoAction.CidStr"); //$NON-NLS-1$
		String idString = LINE_BREAK
				+ idStr
				+ COLON_SPACE
				+ idToHexNameString(trace.information.getComponentId(),
						names[0]);

		// Group ID and name
		idStr = Messages.getString("ShowTraceInfoAction.GidStr"); //$NON-NLS-1$
		idString += LINE_BREAK + idStr + COLON_SPACE
				+ idToHexNameString(trace.information.getGroupId(), names[1]);

		// Trace ID and name
		idStr = Messages.getString("ShowTraceInfoAction.TidStr"); //$NON-NLS-1$
		idString += LINE_BREAK + idStr + COLON_SPACE
				+ idToHexNameString(trace.information.getTraceId(), names[2])
				+ LINE_BREAK;
		return idString;
	}

	/**
	 * Constructs BTrace string
	 * 
	 * @param totalStringLength
	 *            total string length before this string
	 * @param styleRanges
	 *            list of style ranges
	 * @return BTrace string
	 */
	private String constructBTraceString(int totalStringLength,
			List<StyleRange> styleRanges) {
		BTraceInformation bTraceInf = trace.bTraceInformation;

		StringBuffer bTraceString = new StringBuffer();
		bTraceString.append(LINE_BREAK);
		bTraceString.append(Messages
				.getString("ShowTraceInfoAction.BTraceInformation")); //$NON-NLS-1$
		bTraceString.append(LINE_BREAK);

		// BTrace header
		bTraceString.append(Messages
				.getString("ShowTraceInfoAction.RecordSize")); //$NON-NLS-1$
		bTraceString.append(byteToString(bTraceInf.getRecordSize()));
		bTraceString.append(LINE_BREAK);
		bTraceString.append(Messages.getString("ShowTraceInfoAction.Flags")); //$NON-NLS-1$
		bTraceString.append(byteToString(bTraceInf.getFlags()));
		bTraceString.append(LINE_BREAK);
		bTraceString.append(Messages.getString("ShowTraceInfoAction.Category")); //$NON-NLS-1$
		bTraceString.append(byteToString(bTraceInf.getCategory()));
		bTraceString.append(LINE_BREAK);
		bTraceString.append(Messages
				.getString("ShowTraceInfoAction.SubCategory")); //$NON-NLS-1$
		bTraceString.append(byteToString(bTraceInf.getSubCategory()));
		bTraceString.append(LINE_BREAK);

		// BTrace variables
		if (bTraceInf.isHeader2Present()) {
			bTraceString
					.append(Messages.getString("ShowTraceInfoAction.CpuId")); //$NON-NLS-1$
			bTraceString.append(bTraceInf.getCpuId());
			bTraceString.append(LINE_BREAK);
		}
		if (bTraceInf.isTimestampPresent()) {
			bTraceString.append(Messages
					.getString("ShowTraceInfoAction.Timestamp")); //$NON-NLS-1$
			bTraceString.append(idToHexNameString(bTraceInf.getTimestamp(),
					null));
			bTraceString.append(LINE_BREAK);
		}
		if (bTraceInf.isTimestamp2Present()) {
			bTraceString.append(Messages
					.getString("ShowTraceInfoAction.Timestamp2")); //$NON-NLS-1$
			bTraceString.append(idToHexNameString(bTraceInf.getTimestamp2(),
					null));
			bTraceString.append(LINE_BREAK);
		}
		if (bTraceInf.isContextIdPresent()) {
			bTraceString.append(Messages
					.getString("ShowTraceInfoAction.ContextId")); //$NON-NLS-1$
			bTraceString
					.append(idToHexNameString(bTraceInf.getThreadId(), null));

			// Check context
			if ((bTraceInf.getThreadId() & (1 << 0)) == 0) {

				// NThread
				if ((bTraceInf.getThreadId() & (1 << 1)) == 0) {
					bTraceString.append(Messages
							.getString("ShowTraceInfoAction.NThread")); //$NON-NLS-1$
					// FIQ Context
				} else {
					bTraceString.append(Messages
							.getString("ShowTraceInfoAction.FIQ")); //$NON-NLS-1$
				}
			} else {
				// IRQ Context
				if ((bTraceInf.getThreadId() & (1 << 1)) == 0) {
					bTraceString.append(Messages
							.getString("ShowTraceInfoAction.IRQ")); //$NON-NLS-1$

					// IDFC Context
				} else {
					bTraceString.append(Messages
							.getString("ShowTraceInfoAction.IDFC")); //$NON-NLS-1$
				}
			}
			bTraceString.append(LINE_BREAK);
		}
		if (bTraceInf.isProgramCounterPresent()) {
			bTraceString.append(Messages
					.getString("ShowTraceInfoAction.ProgramCounter")); //$NON-NLS-1$
			bTraceString.append(idToHexNameString(
					bTraceInf.getProgramCounter(), null));
			bTraceString.append(LINE_BREAK);
		}
		if (bTraceInf.isExtraValuePresent()) {
			bTraceString.append(Messages
					.getString("ShowTraceInfoAction.ExtraValue")); //$NON-NLS-1$
			bTraceString.append(idToHexNameString(bTraceInf.getExtraValue(),
					null));
			bTraceString.append(LINE_BREAK);
		}
		if (bTraceInf.isTruncated()) {
			bTraceString.append(Messages
					.getString("ShowTraceInfoAction.Truncated")); //$NON-NLS-1$
			bTraceString.append(LINE_BREAK);
		}
		if (bTraceInf.isTraceMissing()) {
			bTraceString.append(Messages
					.getString("ShowTraceInfoAction.RecordMissing")); //$NON-NLS-1$
			bTraceString.append(LINE_BREAK);
		}

		// Multipart stuff
		if (bTraceInf.getMultiPart() != 0) {
			bTraceString.append(Messages
					.getString("ShowTraceInfoAction.MultiPart")); //$NON-NLS-1$
			if (bTraceInf.getMultiPartTraceParts() != null) {
				bTraceString.append(LINE_BREAK);
				bTraceString.append(Messages
						.getString("ShowTraceInfoAction.AssembledFromParts")); //$NON-NLS-1$
				bTraceString.append(LINE_BREAK);
				bTraceString.append(LINE_BREAK);
				Iterator<byte[]> i = bTraceInf.getMultiPartTraceParts()
						.getTraceParts().iterator();
				Iterator<Integer> headerLenIterator = bTraceInf
						.getMultiPartTraceParts().getTracePartHeaderSizes()
						.iterator();

				// Loop through trace parts
				int partNumber = 1;
				while (i.hasNext()) {
					byte[] byteArr = i.next();
					bTraceString.append(Messages
							.getString("ShowTraceInfoAction.Part")); //$NON-NLS-1$
					bTraceString.append(partNumber++);
					bTraceString.append(COLON_SPACE + START_PARENTHESIS
							+ byteArr.length);
					bTraceString.append(Messages
							.getString("ShowTraceInfoAction.BytesString")); //$NON-NLS-1$
					bTraceString.append(END_PARENTHESIS + LINE_BREAK);
					String hexTrace = TraceViewerUtils.getTraceAsHexString(
							ByteBuffer.wrap(byteArr), 0, byteArr.length, true);

					int headerLen = headerLenIterator.next().intValue();
					int colorHeaderStartOffset = totalStringLength
							+ bTraceString.length();
					int colorDataStartOffset = colorHeaderStartOffset
							+ (headerLen * BYTE_AS_HEX_STRING_LENGTH);

					// Add offsets to style range
					StyleRange headerRange = new StyleRange(
							colorHeaderStartOffset, headerLen
									* BYTE_AS_HEX_STRING_LENGTH,
							Display.getDefault().getSystemColor(SWT.COLOR_RED),
							null);
					StyleRange dataRange = new StyleRange(
							colorDataStartOffset,
							hexTrace.length()
									- (headerLen * BYTE_AS_HEX_STRING_LENGTH),
							Display.getDefault().getSystemColor(SWT.COLOR_BLUE),
							null);
					styleRanges.add(headerRange);
					styleRanges.add(dataRange);

					bTraceString.append(hexTrace);
					bTraceString.append(LINE_BREAK);
					bTraceString.append(LINE_BREAK);
				}

			} else if (bTraceInf.getMultiPart() == 1) {
				bTraceString.append(Messages
						.getString("ShowTraceInfoAction.FirstPart")); //$NON-NLS-1$
			} else if (bTraceInf.getMultiPart() == 2) {
				bTraceString.append(Messages
						.getString("ShowTraceInfoAction.MiddlePart")); //$NON-NLS-1$
			} else if (bTraceInf.getMultiPart() == 3) {
				bTraceString.append(Messages
						.getString("ShowTraceInfoAction.LastPart")); //$NON-NLS-1$
			}
			bTraceString.append(LINE_BREAK);
		}

		return bTraceString.toString();
	}

	/**
	 * Converts ID to hex string and name
	 * 
	 * @param id
	 *            ID
	 * @param name
	 *            name
	 * @return ID as hex and name string
	 */
	private String idToHexNameString(int id, String name) {
		String idString = Integer.toHexString(id);
		if (idString.length() == 1) {
			idString = LEAD_ZERO + idString;
		}
		idString = HEX_PREFIX + idString;

		if (name != null) {
			idString += " (" + name + ")"; //$NON-NLS-1$//$NON-NLS-2$
		}

		return idString;
	}

	/**
	 * Converts byte to string
	 * 
	 * @param b
	 *            byte to be converted
	 * @return byte as a string
	 */
	private String byteToString(byte b) {
		StringBuffer out = new StringBuffer();
		int v = b & 0xFF;
		out.append(hexChars[v >>> 4]);
		out.append(hexChars[v & 0xF]);
		return out.toString();
	}

	/**
	 * Sets trace metadata
	 * 
	 * @param trace
	 *            the trace properties
	 */
	public void setTrace(TraceProperties trace) {
		this.trace = trace;
	}
}
