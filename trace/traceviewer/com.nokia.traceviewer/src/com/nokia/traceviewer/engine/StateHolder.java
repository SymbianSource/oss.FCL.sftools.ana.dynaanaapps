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
 * State Holder class
 *
 */
package com.nokia.traceviewer.engine;

/**
 * Holds view's states
 */
public class StateHolder {

	/**
	 * Enum indicating the current state of the view
	 */
	public enum State {

		/**
		 * Scrolling with scrollbar
		 */
		SCROLLING_WITH_SCROLLBAR,

		/**
		 * Scrolling with arrow keys
		 */
		SCROLLING_WITH_ARROWS,

		/**
		 * Scrolling with page up / down
		 */
		SCROLLING_WITH_PAGEUPDOWN,

		/**
		 * Searching
		 */
		SEARCHING,

		/**
		 * Search completed
		 */
		SEARCHED,

		/**
		 * Normal state
		 */
		NORMAL;
	}

	/**
	 * Current state
	 */
	private State state;

	/**
	 * Get state
	 * 
	 * @return current state
	 */
	public State getState() {
		return state;
	}

	/**
	 * Set state
	 * 
	 * @param state
	 *            state to set
	 */
	public void setState(State state) {
		this.state = state;
	}

	/**
	 * Tells if scrolling or not
	 * 
	 * @return scrolling or not
	 */
	public boolean isScrolling() {
		boolean scrolling;
		if (state == State.SCROLLING_WITH_SCROLLBAR
				|| state == State.SCROLLING_WITH_ARROWS
				|| state == State.SCROLLING_WITH_PAGEUPDOWN
				|| state == State.SEARCHING || state == State.SEARCHED) {
			scrolling = true;
		} else {
			scrolling = false;
		}
		return scrolling;
	}

}
