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
* Content provider for tree viewer
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.nokia.tracebuilder.engine.LastKnownLocationList;
import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.engine.TraceViewExtension;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceModelExtensionListener;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceModelResetListener;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.rules.ArrayParameterRule;
import com.nokia.tracebuilder.rules.HiddenTraceObjectRule;

/**
 * Content provider for tree viewer
 * 
 */
final class TraceContentProvider implements ITreeContentProvider,
		TraceModelListener, TraceModelResetListener,
		TraceModelExtensionListener {

	/**
	 * Root wrapper for trace model
	 */
	private TraceModelWrapper modelWrapper;

	/**
	 * Thread that performs view updates
	 */
	private TraceViewUpdater updater;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// This is called when user navigates into a sub-tree of the viewer.
		// However, the updater is created only once.
		if (updater == null) {
			TraceModel model = TraceBuilderGlobals.getTraceModel();
			updater = new TraceViewUpdater((TreeViewer) viewer);
			modelWrapper = new TraceModelWrapper(model, null, updater);
			updater.setRoot(modelWrapper);
			addListeners(model);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		if (updater != null) {
			updater.stopUpdater();
			updater = null;
		}
		if (modelWrapper != null) {
			removeListeners();
			modelWrapper.delete();
			modelWrapper = null;
		}
	}

	/**
	 * Adds listeners to the model
	 * 
	 * @param model
	 *            the model
	 */
	private void addListeners(TraceModel model) {
		model.addModelListener(this);
		model.addExtensionListener(this);
		model.addResetListener(this);
	}

	/**
	 * Removes the model listeners
	 */
	private void removeListeners() {
		TraceModel model = (TraceModel) modelWrapper.getTraceObject();
		model.removeModelListener(this);
		model.removeExtensionListener(this);
		model.removeResetListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object child) {
		return ((WrapperBase) child).getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object parent) {
		return ((WrapperBase) parent).hasChildren();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		Object[] ret;
		// If the tree is navigated into a sub-element, the parent will be an
		// instance of WrapperBase
		if (parent instanceof WrapperBase) {
			ret = getChildren(parent);
		} else {
			ret = modelWrapper.getChildren();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		return ((WrapperBase) parent).getChildren();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelResetting()
	 */
	public void modelResetting() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelReset()
	 */
	public void modelReset() {
		modelWrapper.modelReset();
		refreshView(modelWrapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelValid(boolean)
	 */
	public void modelValid(boolean valid) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectAdded(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectAdded(TraceObject owner, TraceObject object) {
		WrapperBase wrapper = null;
		if (object instanceof TraceGroup) {
			wrapper = modelWrapper.addGroup((TraceGroup) object);
		} else if (object instanceof Trace) {
			TraceGroupWrapper groupWrapper = owner
					.getExtension(TraceGroupWrapper.class);
			wrapper = groupWrapper.addTrace((Trace) object);
		} else if (object instanceof TraceParameter) {
			TraceWrapper objectWrapper = owner.getExtension(TraceWrapper.class);
			wrapper = objectWrapper.addParameter((TraceParameter) object);
		} else if (object instanceof TraceConstantTable) {
			// Constant tables are added to the model and to the parameters
			// referencing the tables. The parameter wrapper takes care of the
			// reference
			wrapper = modelWrapper
					.addConstantTable((TraceConstantTable) object);
		} else if (object instanceof TraceConstantTableEntry) {
			// Constant tables are referenced from multiple parameters and the
			// list of constant tables stored into the model wrapper. Thus
			// getExtensions needs to be used
			Iterator<ConstantTableWrapper> itr = owner
					.getExtensions(ConstantTableWrapper.class);
			while (itr.hasNext()) {
				refreshView(itr.next().addConstantTableEntry(
						(TraceConstantTableEntry) object));
			}
		}
		if (wrapper != null) {
			refreshView(wrapper);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectRemoved(TraceObject owner, TraceObject object) {
		WrapperBase wrapper = null;
		if (object instanceof TraceGroup) {
			wrapper = modelWrapper.removeGroup((TraceGroup) object);
		} else if (object instanceof Trace) {
			TraceGroupWrapper groupWrapper = owner
					.getExtension(TraceGroupWrapper.class);
			wrapper = groupWrapper.removeTrace((Trace) object);
		} else if (object instanceof TraceParameter) {
			TraceWrapper objectWrapper = owner.getExtension(TraceWrapper.class);
			wrapper = objectWrapper.removeParameter((TraceParameter) object);
		} else if (object instanceof TraceConstantTable) {
			wrapper = modelWrapper
					.removeConstantTable((TraceConstantTable) object);
		} else if (object instanceof TraceConstantTableEntry) {
			// Constant tables are referenced from multiple parameters and the
			// list of constant tables stored into the model wrapper. Thus
			// getExtensions needs to be used
			Iterator<ConstantTableWrapper> itr = owner
					.getExtensions(ConstantTableWrapper.class);
			while (itr.hasNext()) {
				refreshView(itr.next().removeConstantTableEntry(
						(TraceConstantTableEntry) object));
			}
		}
		if (wrapper != null) {
			refreshView(wrapper);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectCreationComplete(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectCreationComplete(TraceObject object) {
		// Not interested, since all individual additions and updates are
		// monitored. TraceViewUpdater takes care of the optimizations if there
		// are lots of sequential updates
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      propertiesUpdated(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void propertyUpdated(TraceObject object, int property) {
		Iterator<TraceObjectWrapper> itr = object
				.getExtensions(TraceObjectWrapper.class);
		while (itr.hasNext()) {
			refreshView(itr.next().refreshProperty(property));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtensionListener#
	 *      extensionAdded(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceModelExtension)
	 */
	public void extensionAdded(TraceObject object, TraceModelExtension extension) {
		// Location lists and locations have their own listeners, which are
		// used for update notifications
		if (extension instanceof TraceLocationList) {
			if (object instanceof Trace) {
				TraceWrapper traceWrapper = object
						.getExtension(TraceWrapper.class);
				refreshView(traceWrapper
						.setLocationList((TraceLocationList) extension));
			} else if (object instanceof TraceModel) {
				refreshView(modelWrapper
						.addLocationList((TraceLocationList) extension));
			}
		} else if (extension instanceof LastKnownLocationList) {
			TraceWrapper traceWrapper = object.getExtension(TraceWrapper.class);
			refreshView(traceWrapper
					.setLastKnownLocationList((LastKnownLocationList) extension));
		} else if (extension instanceof TraceViewExtension) {
			TraceObjectWrapper objectWrapper = object
					.getExtension(TraceObjectWrapper.class);
			refreshView(objectWrapper
					.addExtension((TraceViewExtension) extension));
		} else if (extension instanceof HiddenTraceObjectRule) {
			if (!TraceBuilderConfiguration.SHOW_HIDDEN_PARAMETERS_IN_VIEW) {
				TraceObjectWrapper objectWrapper = object
						.getExtension(TraceObjectWrapper.class);
				refreshView(objectWrapper.hideFromView());
			}
		} else if (extension instanceof ArrayParameterRule) {
			TraceObjectWrapper objectWrapper = object
					.getExtension(TraceObjectWrapper.class);
			if (objectWrapper instanceof TraceParameterWrapper) {
				refreshView(((TraceParameterWrapper) objectWrapper)
						.setArrayType(true));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtensionListener#
	 *      extensionRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceModelExtension)
	 */
	public void extensionRemoved(TraceObject object,
			TraceModelExtension extension) {
		if (extension instanceof TraceLocationList) {
			if (object instanceof Trace) {
				TraceWrapper traceWrapper = object
						.getExtension(TraceWrapper.class);
				refreshView(traceWrapper.setLocationList(null));
			} else if (object instanceof TraceModel) {
				refreshView(modelWrapper
						.removeLocationList((TraceLocationList) extension));
			}
		} else if (extension instanceof LastKnownLocationList) {
			TraceWrapper traceWrapper = object.getExtension(TraceWrapper.class);
			refreshView(traceWrapper.setLastKnownLocationList(null));
		} else if (extension instanceof TraceViewExtension) {
			TraceObjectWrapper objectWrapper = object
					.getExtension(TraceObjectWrapper.class);
			refreshView(objectWrapper
					.removeExtension((TraceViewExtension) extension));
		} else if (extension instanceof ArrayParameterRule) {
			TraceObjectWrapper objectWrapper = object
					.getExtension(TraceObjectWrapper.class);
			if (objectWrapper instanceof TraceParameterWrapper) {
				refreshView(((TraceParameterWrapper) objectWrapper)
						.setArrayType(false));
			}
		}
	}

	/**
	 * Refreshes the view
	 * 
	 * @param wrapper
	 *            the wrapper that needs to be updated
	 */
	private void refreshView(WrapperBase wrapper) {
		if (wrapper != null) {
			updater.queueUpdate(wrapper);
		}
	}

	/**
	 * Dumps the model to System.out
	 * 
	 * @param labelProvider
	 *            the label provider for objects
	 */
	void dumpToSystemOut(TraceLabelProvider labelProvider) {
		if (TraceBuilderConfiguration.TRACE_VIEW_STATE) {
			modelWrapper.dumpToSystemOut(0, labelProvider);
		}
	}

}
