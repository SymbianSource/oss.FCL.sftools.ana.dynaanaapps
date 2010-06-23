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

package com.nokia.carbide.cpp.pi.core;

public final class SessionPreferences {
	private static SessionPreferences instance;

	private static final int WINDOW_DEFAULT_HEIGHT = 778;
	private static final int WINDOW_DEFAULT_WIDTH = 1024;

	private boolean barMode;
	private boolean toolTipsEnabled;
	private boolean fillAllEnabled;
	private int windowWidth;
	private int windowHeight;

	private SessionPreferences() {
//		 singleton
//		setup defaults here
		setWindowWidth(WINDOW_DEFAULT_WIDTH);
		setWindowHeight(WINDOW_DEFAULT_HEIGHT);
		setBarMode(false);
		setToolTipsEnabled(true);
		setFillAllEnabled(true);
	}
	
	static public SessionPreferences getInstance() {
		if (instance == null) {
			instance = new SessionPreferences();
		}
		
		return instance;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public int getWindowWidth() {
		return windowWidth;
	}
	
	public void setWindowWidth(int width) {
		windowWidth = width;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int height) {
		windowHeight = height;
	}
	
	public boolean getBarMode() {
		return barMode;
	}
	
	public void setBarMode(boolean enabled) {
		barMode = enabled;
	}

	public boolean getToolTipsEnabled() {
		return toolTipsEnabled;
	}

	public void setToolTipsEnabled(boolean enabled) {
		toolTipsEnabled = enabled;
	}

	public boolean getFillAllEnabled() {
		return fillAllEnabled;
	}

	public void setFillAllEnabled(boolean enabled) {
		fillAllEnabled = enabled;
	}

}
