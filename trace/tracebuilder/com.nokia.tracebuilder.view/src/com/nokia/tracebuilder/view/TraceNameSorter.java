/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Tree view sorter implementation
*
*/
package com.nokia.tracebuilder.view;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Tree view sorter implementation
 * 
 */
final class TraceNameSorter extends ViewerSorter {

	/**
	 * Comparator for trace object names
	 * 
	 */
	final class NameSorter implements Comparator<Object> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			int res;
			if (o1 != null && o2 != null) {
				if (o1 instanceof TraceObjectWrapper
						&& o2 instanceof TraceObjectWrapper) {
					TraceObject obj1 = ((TraceObjectWrapper) o1)
							.getTraceObject();
					TraceObject obj2 = ((TraceObjectWrapper) o2)
							.getTraceObject();
					if (obj1 != null && obj2 != null) {
						res = obj1.getName().compareTo(obj2.getName());
					} else {
						res = 0;
					}
				} else if (o1 instanceof ListNavigator) {
					res = -1;
				} else if (o2 instanceof ListNavigator) {
					res = 1;
				} else {
					res = 0;
				}
			} else {
				res = 0;
			}
			return res;
		}

	}

	/**
	 * Generic comparator for wrappers
	 * 
	 */
	private class WrapperComparator implements Comparator<Object> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object arg0, Object arg1) {
			int res;
			// List navigator is always last element
			if (arg0 instanceof ListNavigator) {
				res = 1;
			} else if (arg1 instanceof ListNavigator) {
				res = -1;
			} else {
				// Properties are always first
				if (arg0 instanceof PropertyWrapper) {
					res = -1;
				} else if (arg1 instanceof PropertyWrapper) {
					res = 1;
				} else {
					// Extensions after properties
					if (arg0 instanceof TraceViewExtensionWrapper) {
						res = -1;
					} else if (arg1 instanceof TraceViewExtensionWrapper) {
						res = 1;
					} else {
						res = 0;
					}
				}
			}
			return res;
		}
	}

	/**
	 * Comparator for trace locations
	 * 
	 */
	private final class LineNumberComparator implements Comparator<Object> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object arg0, Object arg1) {
			int res;
			// List navigator is always last element
			if (arg0 instanceof ListNavigator) {
				res = 1;
			} else if (arg1 instanceof ListNavigator) {
				res = -1;
			} else {
				TraceLocation loc0 = ((TraceLocationWrapper) arg0)
						.getLocation();
				TraceLocation loc1 = ((TraceLocationWrapper) arg1)
						.getLocation();
				if (loc0 != null && loc1 != null) {
					String file0 = loc0.getFileName();
					String file1 = loc1.getFileName();
					if (file0 != null && file1 != null) {
						res = file0.compareTo(file1);
						if (res == 0) {
							int pos0 = loc0.getLineNumber();
							int pos1 = loc1.getLineNumber();
							res = pos0 > pos1 ? 1 : pos0 < pos1 ? -1 : 0;
						}
					} else {
						res = 0;
					}
				} else {
					res = 0;
				}
			}
			return res;
		}
	}

	/**
	 * Comparator for trace model
	 * 
	 */
	private final class ModelSorter extends WrapperComparator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.view.TraceNameSorter.WrapperComparator#
		 *      compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Object o1, Object o2) {
			int res;
			if (o1 instanceof TraceLocationListsWrapper) {
				res = 1;
			} else if (o2 instanceof TraceLocationListsWrapper) {
				res = -1;
			} else {
				res = super.compare(o1, o2);
			}
			return res;
		}
	}

	/**
	 * Comparator used when sorting locations
	 */
	private Comparator<Object> lineNumberSorter = new LineNumberComparator();

	/**
	 * Comparator used when sorting wrapper objects
	 */
	private Comparator<Object> wrapperSorter = new WrapperComparator();

	/**
	 * Comparator used when sorting model elements
	 */
	private Comparator<Object> modelSorter = new ModelSorter();

	/**
	 * Comparator for traces and groups
	 */
	private Comparator<Object> nameSorter = new NameSorter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerSorter#sort(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object[])
	 */
	@Override
	public void sort(Viewer viewer, Object[] elements) {
		if (elements != null && elements.length > 0) {
			if (elements[0] instanceof TraceParameterWrapper) {
			} else if (elements[0] instanceof TraceWrapper
					|| elements[0] instanceof TraceGroupWrapper) {
				Arrays.sort(elements, nameSorter);
			} else if (elements[0] instanceof TraceLocationWrapper) {
				Arrays.sort(elements, lineNumberSorter);
			} else if (((WrapperBase) elements[0]).getParent() instanceof TraceModelWrapper) {
				Arrays.sort(elements, modelSorter);
			} else {
				Arrays.sort(elements, wrapperSorter);
			}
		}
	}

}
