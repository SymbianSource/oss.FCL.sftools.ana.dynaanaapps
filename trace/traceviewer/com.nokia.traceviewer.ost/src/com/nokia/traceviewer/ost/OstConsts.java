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
 * OST Constants
 *
 */
package com.nokia.traceviewer.ost;

/**
 * OST Constants
 */
interface OstConsts {

	// -----OST-----------
	// -----HEADER--------
	// -----SPECIFIC------
	// -----CONSTANTS-----

	/**
	 * OST Version 0.0 indication
	 */
	final byte OST_V00 = 0x00;

	/**
	 * OST Version 0.1 indication
	 */
	final byte OST_V01 = 0x01;

	/**
	 * OST Version 0.5 indication
	 */
	final byte OST_V05 = 0x05;

	/**
	 * OST Version 1.0 indication
	 */
	final byte OST_V10 = 0x10;

	/**
	 * OST Version indication offset in OST message
	 */
	final int OST_VERSION_OFFSET = 0;

	/**
	 * OST Protocol ID offset in OST message
	 */
	final int OST_V00_PROTOCOLID_OFFSET = 1;

	/**
	 * OST Protocol ID offset in OST message
	 */
	final int OST_V01_PROTOCOLID_OFFSET = 1;

	/**
	 * OST Protocol ID offset in OST message
	 */
	final int OST_V05_PROTOCOLID_OFFSET = 2;

	/**
	 * OST Protocol ID offset in OST message
	 */
	final int OST_V10_PROTOCOLID_OFFSET = 2;

	/**
	 * Offset to length byte 1 in OSTV0.1
	 */
	final int OST_V01_LENGTH_OFFSET_1 = 2;

	/**
	 * Offset to length byte 2 in OSTV0.1
	 */
	final int OST_V01_LENGTH_OFFSET_2 = 3;

	/**
	 * Offset to length byte in OSTV0.5
	 */
	final int OST_V05_LENGTH_OFFSET = 3;

	/**
	 * Offset to extended length in OSTV0.5
	 */
	final int OST_V05_EXT_LENGTH_OFFSET = 4;

	/**
	 * OST header V0.0 length
	 */
	final int OST_V00_HEADER_LENGTH = 4;

	/**
	 * OST header V0.1 length
	 */
	final int OST_V01_HEADER_LENGTH = 4;

	/**
	 * OST header V0.5 length
	 */
	final int OST_V05_HEADER_LENGTH = 4;

	/**
	 * OST header V0.5 extended length length
	 */
	final int OST_V05_EXT_LENGTH_LENGTH = 4;

	// -----OST-----------
	// -----TRACE---------
	// -----ACTIVATION----
	// -----SPECIFIC------
	// -----CONSTANTS-----

	/**
	 * OST Trace Activation protocol ID
	 */
	final byte OST_TRACE_ACTIVATION_ID = 0x01;

	/**
	 * OST Trace Activation header length
	 */
	final int OST_TRACE_ACTIVATION_HEADER_LENGTH = 2;

	/**
	 * OST Trace Activation transaction ID offset. Offset starts after the base
	 * header.
	 */
	final int OST_TRACE_ACTIVATION_TRANSID_OFFSET = 0;

	/**
	 * OST Trace Activation message ID offset. Offset starts after the base
	 * header.
	 */
	final int OST_TRACE_ACTIVATION_MESSAGEID_OFFSET = 1;

	/**
	 * OST Trace Activation set activation status request
	 */
	final byte OST_TRACE_ACTIVATION_SET_STATUS_REQUEST = 0x06;

	/**
	 * OST Trace Activation component ID offset. Offset starts after the base
	 * header.
	 */
	final int OST_TRACE_ACTIVATION_COMPONENTID_OFFSET = 2;

	/**
	 * OST Trace Activation set activation status offset. Offset starts after
	 * the base header.
	 */
	final int OST_TRACE_ACTIVATION_STATUS_OFFSET = 6;

	/**
	 * OST Trace Activation set filler offset. Offset starts after the header.
	 */
	final int OST_TRACE_ACTIVATION_FILLER_OFFSET = 7;

	/**
	 * OST Trace Activation group start offset. Offset starts after the header.
	 */
	final int OST_TRACE_ACTIVATION_GROUPID_START_OFFSET = 8;

	/**
	 * Activation indicator
	 */
	final byte OST_TRACE_ACTIVATION_ACTIVATE_INDICATOR = 0x01;

	/**
	 * Deactivation indicator
	 */
	final byte OST_TRACE_ACTIVATION_DEACTIVATE_INDICATOR = 0x00;

	// -----OST-----------
	// -----ASCII---------
	// -----TRACE---------
	// -----SPECIFIC------
	// -----CONSTANTS-----

	/**
	 * OST ASCII TRACE protocol ID
	 */
	final byte OST_ASCII_TRACE_ID = 0x02;

	/**
	 * Offset to timestamp in OST Ascii Trace. Offset starts after the base
	 * header.
	 */
	final int OST_ASCII_TRACE_TIMESTAMP_OFFSET = 0;

	/**
	 * OST ASCII timestamp length in bytes
	 */
	final int OST_ASCII_TRACE_TIMESTAMP_LENGTH = 8;

	/**
	 * Offset to data in OST Ascii Trace. Offset starts after the base header.
	 */
	final int OST_ASCII_TRACE_DATA_OFFSET = 8;

	// -----OST-----------
	// -----SIMPLE--------
	// -----TRACE---------
	// -----SPECIFIC------
	// -----CONSTANTS-----

	/**
	 * OST SIMPLE TRACE protocol ID
	 */
	final byte OST_SIMPLE_TRACE_ID = 0x03;

	/**
	 * Offset to timestamp in OST Simple Trace. Offset starts after the base
	 * header.
	 */
	final int OST_SIMPLE_TRACE_TIMESTAMP_OFFSET = 0;

	/**
	 * Offset to application (component) ID in OST Simple Trace. Offset starts
	 * after the base header.
	 */
	final int OST_SIMPLE_TRACE_COMPONENTID_OFFSET = 8;

	/**
	 * Offset to group ID in OST Simple Trace. Offset starts after the base
	 * header.
	 */
	final int OST_SIMPLE_TRACE_GROUPID_OFFSET = 12;

	/**
	 * Offset to trace ID in OST Simple Trace. Offset starts after the base
	 * header.
	 */
	final int OST_SIMPLE_TRACE_TRACEID_OFFSET = 14;

	// -----OST-----------
	// -----TRACECORE--------
	// -----PROTOCOL---------
	// -----SPECIFIC------
	// -----CONSTANTS-----

	/**
	 * OST TraceCore protocol header length
	 */
	final int OST_TRACECORE_PROTOCOL_HEADER_LENGTH = 4;

	/**
	 * Protocol ID offset in TraceCore protocol
	 */
	final int OST_TRACECORE_PROTOCOL_ID_OFFSET = 0;

	/**
	 * TraceCore Subscriber message ID offset in header if
	 * OST_TRACECORE_PROTOCOL_ID is OST_TRACECORE_SUBSCRIBER_PROTOCOL_ID
	 */
	final int OST_TRACECORE_SUBSCRIBER_ID_OFFSET = 1;

	/**
	 * OST TraceCore Protocol Subscriber Protocol ID
	 */
	final int OST_TRACECORE_SUBSCRIBER_PROTOCOL_ID = 0x01;

	/**
	 * OST TraceCore protocol ID
	 */
	final byte OST_TRACECORE_PROTOCOL_ID = (byte) 0x91;

	// -----OTHER---------
	// -----NON-----------
	// -----OST-----------
	// -----SPECIFIC------
	// -----CONSTANTS-----

	/**
	 * Timestamp mask
	 */
	final long TIMESTAMP_MASK = 0x3FFFFFFFFFFFFFFFL;

	/**
	 * Component ID length in bytes
	 */
	final int COMPONENT_ID_LENGTH = 4;

	/**
	 * Group ID length in bytes
	 */
	final int GROUP_ID_LENGTH = 2;

	/**
	 * Trace ID length in bytes
	 */
	final int TRACE_ID_LENGTH = 2;

}
