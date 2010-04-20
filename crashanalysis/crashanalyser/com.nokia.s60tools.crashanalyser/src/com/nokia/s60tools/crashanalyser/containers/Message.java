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

package com.nokia.s60tools.crashanalyser.containers;

import java.io.BufferedWriter;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import com.nokia.s60tools.crashanalyser.model.XmlUtils;

/**
 * Message class represents one xml message. A message can 
 * be an error message, warning message or a comment.
 *
 */
public final class Message {
	
	// XML tags
	public static final String TAG_ID = "id";
	public static final String TAG_TYPE = "type";
	public static final String TAG_TITLE = "title";
	public static final String TAG_LINE = "line";

	public enum MessageTypes {ERROR, WARNING, COMMENT, MESSAGE};
	
	// message data
	private final int messageId;
	private final String messageTitle;
	private final String messageMessage;
	private final MessageTypes messageType;
	
	/**
	 * Constructor
	 * @param id message id
	 * @param title message title
	 * @param message message text
	 * @param type message type
	 */
	private Message(int id, String title, String message, MessageTypes type) {
		messageId = id;
		messageTitle = title;
		messageMessage = message;
		messageType = type;
	}
	
	/**
	 * Creates a new message with given parameters
	 * @param id message id
	 * @param title message title
	 * @param message message text
	 * @return created message
	 */
	public static Message newMessage(int id, String title, String message, MessageTypes type) {
		return new Message(id, title, message, type);
	}
	
	/**
	 * Writes message into given buffer (e.g. into file)
	 * @param out buffer to write to
	 * @throws IOException
	 */
	public void writeTo(BufferedWriter out) throws IOException {
		if (MessageTypes.WARNING.equals(messageType)) {
			writeMessage(out, "WARNING:");
		} else if (MessageTypes.ERROR.equals(messageType)) {
			writeMessage(out, "ERROR:");
		}
	}
	
	void writeMessage(BufferedWriter out, String type) throws IOException {
		writeLine(out,"");
		writeLine(out, type);
		writeLine(out, "--------");
		writeLine(out, messageTitle);
		writeLine(out, messageMessage);		
	}
	
	void writeLine(BufferedWriter out, String line) throws IOException {
		out.write(line);
		out.newLine();
	}
	
	/**
	 * Reads and creates a message from message xml element
	 * @param elementMessage
	 * @return created message or null
	 */
	public static Message read(Element elementMessage) {
		try {
			// read code segment id
			String messageId = XmlUtils.getTextValue(elementMessage, TAG_ID);
			if (messageId == null)
				return null;
			
			// convert code segment id to integer
			int id;
			try {
				id = Integer.parseInt(messageId);
			} catch (Exception e) {
				return null;
			}
			
			// read message type
			String type = XmlUtils.getTextValue(elementMessage, TAG_TYPE);
			if (type == null)
				return null;
			MessageTypes messageType = getMessageType(type);
			
			// get title and msg nodes
			NodeList children = elementMessage.getChildNodes();
			if (children == null || children.getLength() < 1)
				return null;
			
			// read title and message
			String title = "";
			String message = "";
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (TAG_TITLE.equals(child.getNodeName())) {
					title = XmlUtils.getNodeValue(child);
				} else if (TAG_LINE.equals(child.getNodeName())) {
					if (!"".equals(message))
						message += System.getProperty("line.separator");
					message += XmlUtils.getNodeValue(child);
				}
			}
			
			if ("".equals(title) && "".equals(message))
				return null;
			
			return new Message(id, title, message, messageType);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Gets the message type from given string
	 * @param type e.g. "Error" or "Warning"
	 * @return message type
	 */
	static MessageTypes getMessageType(String type) {
		if ("Error".equals(type))
			return MessageTypes.ERROR;
		else if ("Warning".equals(type))
			return MessageTypes.WARNING;
		else if ("Message".equals(type))
			return MessageTypes.MESSAGE;
		else
			return MessageTypes.COMMENT;
	}
	
	public int getId() {
		return messageId;
	}

	public MessageTypes getMessageType() {
		return messageType;
	}
	
	public String getTitle() {
		return messageTitle;
	}
	
	public String getMessage() {
		return messageMessage;
	}
}
