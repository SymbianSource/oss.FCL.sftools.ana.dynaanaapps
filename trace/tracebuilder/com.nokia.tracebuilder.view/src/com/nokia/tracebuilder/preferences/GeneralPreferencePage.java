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
* General preferences page
*
*/
package com.nokia.tracebuilder.preferences;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.project.TraceProjectAPI;
import com.nokia.tracebuilder.project.TraceProjectAPIList;
import com.nokia.tracebuilder.view.TraceViewPlugin;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 * 
 */
public class GeneralPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * Number of columns in format radio group
	 */
	private static final int FORMAT_COLUMNS = 1; // CodForChk_Dis_Magic

	/**
	 * Constructor
	 */
	public GeneralPreferencePage() {
		super(GRID);
		setPreferenceStore(TraceViewPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.getString("GeneralPreferencePage.Description")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	@Override
	protected void createFieldEditors() {
		String formatGroupTitle = Messages
				.getString("GeneralPreferencePage.TraceFormatTitle"); //$NON-NLS-1$
		TraceProjectAPIList apilist = TraceBuilderGlobals.getTraceModel()
				.getExtension(TraceProjectAPIList.class);
		if (apilist != null) {
			Iterator<TraceProjectAPI> apis = apilist.getAPIs();
			ArrayList<String[]> list = new ArrayList<String[]>();
			while (apis.hasNext()) {
				TraceProjectAPI api = apis.next();
				if (api.isVisibleInConfiguration()) {
					String name = api.getName();
					String title = api.getTitle();
					list.add(new String[] { title, name });
				}
			}
			if (list.size() > 1) {
				String[][] formatters = new String[list.size()][];
				list.toArray(formatters);
				addField(new RadioGroupFieldEditor(
						TraceBuilderConfiguration.FORMATTER_NAME,
						formatGroupTitle, FORMAT_COLUMNS, formatters,
						getFieldEditorParent(), true));
			}
		}
		addField(new BooleanFieldEditor(
				TraceBuilderConfiguration.PRINTF_SUPPORT,
				Messages
						.getString("GeneralPreferencePage.PrintfParserSupportTitle"), //$NON-NLS-1$
				getFieldEditorParent()));
		addField(new StringFieldEditor(
				TraceBuilderConfiguration.PRINTF_EXTENSION, Messages
						.getString("GeneralPreferencePage.PrintfMacroTitle"), //$NON-NLS-1$
				StringFieldEditor.UNLIMITED, getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	@Override
	protected void initialize() {
		super.initialize();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				getControl(),
				TraceBuilderHelp.HELP_CONTEXT_BASE
						+ TraceBuilderHelp.GENERAL_PREFERENCES_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}