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
* Trace Builder interface
*
*/
package com.nokia.tracebuilder.engine;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.source.SourceProperties;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Trace Builder interface
 * 
 */
public interface TraceBuilderInterface {

	/**
	 * Set the trace object as an active object. Can be called with null
	 * parameter to deselect the active object. Other functions of this
	 * interface will work with the selected trace object until this method or
	 * extensionSelected is called again.
	 * 
	 * @param object
	 *            the object selected from trace view
	 * @param syncView
	 *            if true, delegates the object to TraceBuilderView.selectObject
	 * @param sourceOpened
	 *            if true, trace from model is used, otherwise trace from selected object           
	 */
	public void traceObjectSelected(TraceObject object, boolean syncView, boolean sourceOpened);

	/**
	 * Selects an TraceLocation or TraceLocationList as the active object. The
	 * functions that expect the selected object to be a TraceObject will not
	 * work until traceObjectSelected is called again.
	 * 
	 * @param list
	 *            the selected location list
	 * @param location
	 *            the selected location from the list or null if only the list
	 *            was selected
	 * @param syncView
	 *            if true, delegates the location to
	 *            TraceBuilderView.selectLocation
	 */
	public void locationSelected(TraceLocationList list,
			TraceLocation location, boolean syncView);

	/**
	 * Creates a new trace.
	 * 
	 * @throws TraceBuilderException
	 *             if a trace cannot be created
	 */
	public void addTrace() throws TraceBuilderException;

	/**
	 * Creates a new trace based on unrelated location found from source.
	 * 
	 * @throws TraceBuilderException
	 *             if a trace cannot be created
	 */
	public void convertTrace() throws TraceBuilderException;

	/**
	 * Creates a new trace parameter and adds it to the currently active trace
	 * object.
	 * 
	 * @throws TraceBuilderException
	 *             if adding fails
	 */
	public void addParameter() throws TraceBuilderException;

	/**
	 * Creates a new constant value and adds it to the currently active constant
	 * table
	 * 
	 * @throws TraceBuilderException
	 *             if adding fails
	 */
	public void addConstant() throws TraceBuilderException;

	/**
	 * Select component to be used in case that one cpp file is included to more
	 * than one mmp file
	 * 
	 * @throws TraceBuilderException
	 *             if adding fails
	 */
	public void selectComponent()
			throws TraceBuilderException;

	/**
	 * Deletes the currently active trace object from the model.
	 * 
	 * @throws TraceBuilderException
	 *             if deleting fails
	 */
	public void delete() throws TraceBuilderException;

	/**
	 * Removes the currently active trace object or location from the source
	 * files
	 * 
	 * @throws TraceBuilderException
	 *             if removing fails
	 */
	public void removeFromSource() throws TraceBuilderException;

	/**
	 * Removes all unrelated trace locations from the source files
	 * 
	 * @throws TraceBuilderException
	 *             if removing fails
	 */
	public void removeUnrelatedFromSource() throws TraceBuilderException;

	/**
	 * Shows the properties of the active trace object.
	 * 
	 * @throws TraceBuilderException
	 *             if properties of active object cannot be shown
	 */
	public void showProperties() throws TraceBuilderException;

	/**
	 * Changes the focus between the view and source editor
	 */
	public void switchFocus();

	/**
	 * Opens the trace project related to given source file
	 * 
	 * @param modelName
	 *            the name for the model
	 * @throws TraceBuilderException
	 *             if startup fails
	 */
	public void openProject(String modelName)
			throws TraceBuilderException;

	/**
	 * Exports the trace project
	 * 
	 * @throws TraceBuilderException
	 *             if export fails
	 */
	public void exportProject() throws TraceBuilderException;

	/**
	 * Closes the trace project
	 */
	public void closeProject();

	/**
	 * Starts the code instrumenter
	 * 
	 * @throws TraceBuilderException
	 *             if startup fails
	 */
	public void startInstrumenter() throws TraceBuilderException;

	/**
	 * Shows a dialog, which can be used to select and delete traces
	 * 
	 * @throws TraceBuilderException
	 *             if deleting fails
	 */
	public void deleteMultipleTraces() throws TraceBuilderException;

	/**
	 * Creates a constant table from the source selection
	 * 
	 * @throws TraceBuilderException
	 *             if processing fails
	 */
	public void createConstantTableFromSelection() throws TraceBuilderException;
	
	/**
	 * Gets the open sources
	 * 
	 * @return the open sources
	 */
	public Iterator<SourceProperties> getOpenSources();
	
	/**
	 * Gets the current selection
	 * 
	 * @return the selection
	 */
	public TraceObject getSelectedObject();

}
