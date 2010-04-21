/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class BuilderUtil
 *
 */

package com.nokia.s60tools.analyzetool.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

/**
 * Add and removes AnalyzeTool build natures to project natures.
 *
 * @author kihe
 *
 */
public class BuilderUtil {

	/**
	 * Constructor.
	 */
	public BuilderUtil() {
		// MethodDeclaration/Block[count(BlockStatement) = 0 and
		// @containsComment = 'false']
	}

	/**
	 * Adds AnalyzeTool build natures.
	 *
	 * @param project
	 *            Project reference
	 * @return True no errors otherwise False
	 */
	private boolean addAnalysisNatures(final IProject project) {
		try {

			// get existing natures
			String[] natures = project.getDescription().getNatureIds();

			// get project description
			IProjectDescription description = project.getDescription();

			// create array for the new natures
			String[] newNatures = new String[natures.length + 2];


			//if QT nature found we must adjust pre- and post natures to correct place
			if( description.hasNature(com.trolltech.qtcppproject.QtNature.QT_NATURE_ID) ) {

				//find QT nature location
				int qtNatureIndex = 0;
				for( int i=0; i<natures.length; i++ ) {
					if( natures[i].equals(com.trolltech.qtcppproject.QtNature.QT_NATURE_ID ) ) {
						qtNatureIndex = i;
						break;
					}
				}

				//QT nature id found and it is first nature=> now start to copy existing id and add AT id
				if( qtNatureIndex == 0 ) {
					//add natures
					newNatures[0] = natures[0];
					newNatures[1] = PreNature.NATURE_ID;

					//copy rest of the existing natures
					System.arraycopy(natures, 1, newNatures, 2, natures.length-1);

					// add post-builder nature
					newNatures[natures.length + 1] = PostNature.NATURE_ID;
				}
				//QT nature id found but there are some other natures
				//before QT nature
				else {
					//copy existing natures
					System.arraycopy(natures, 0, newNatures, 0, qtNatureIndex+1);
					newNatures[qtNatureIndex+1] = PreNature.NATURE_ID;

					//copy rest of the existing natures
					System.arraycopy(natures, qtNatureIndex+1, newNatures, qtNatureIndex+2, natures.length-qtNatureIndex);

					// add post-builder nature
					newNatures[natures.length + 1] = PostNature.NATURE_ID;
				}
			}
			//no QT nature found just add pre nature first and post nature last
			else {

				// set pre-builder nature
				newNatures[0] = PreNature.NATURE_ID;

				// copy existing natures
				System.arraycopy(natures, 0, newNatures, 1, natures.length);

				// add post-builder nature
				newNatures[natures.length + 1] = PostNature.NATURE_ID;
			}

			// update project description
			description.setNatureIds(newNatures);
			project.setDescription(description, null);

			return true;
		} catch (CoreException ce) {
			ce.printStackTrace();
			return false;
		}
	}



	/**
	 * Disable AnalyzeTool build natures.
	 *
	 * @param project
	 *            Project reference
	 */
	public final void disableNatures(final IProject project) {
		try {
			// both natures found
			if (isNatureEnabled(project)) {
				removeNature(project, 2);
			} else {
				IProjectDescription description = project.getDescription();

				// find natures
				boolean foundPreNature = description
						.hasNature(PreNature.NATURE_ID);
				boolean foundPostNature = description
						.hasNature(PostNature.NATURE_ID);

				//either pre- or post builder nature found => remove it
				if (foundPreNature || foundPostNature) {
					removeNature(project, 1);
				}

			}
		} catch (CoreException ce) {
			ce.printStackTrace();
		}

	}

	/**
	 * Add AnalyzeTool custom builder nature to project builder natures.
	 *
	 * @param project
	 *            Project reference
	 * @return True if natures are added otherwise False
	 */
	public final boolean enableNatures(final IProject project) {
		try {
			// check is nature enable
			if( isNatureEnabled(project)) {
				return true;
			}
			// get project description
			IProjectDescription description = project.getDescription();

			// find natures
			boolean foundPreNature = description
					.hasNature(PreNature.NATURE_ID);
			boolean foundPostNature = description
					.hasNature(PostNature.NATURE_ID);

			// only one analyzetool nature found => remove it
			if (foundPostNature || foundPreNature) {
				removeNature(project, 1);
			}

			// add right analysis natures
			return addAnalysisNatures(project);

		} catch (CoreException ce) {
			ce.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks is AnalyzeTool custom nature enabled.
	 *
	 * @param projRef
	 *            Project reference
	 * @return True both pre or post nature enabled otherwise False
	 */
	public final boolean isNatureEnabled(final IProject projRef) {
		boolean preNatureFound = false;
		boolean postNatureFound = false;

		//check project validity
		if (projRef == null || !projRef.isOpen()) {
			return false;
		}

		try {
			IProjectDescription description = projRef.getDescription();
			preNatureFound = description.hasNature(PreNature.NATURE_ID);
			postNatureFound = description.hasNature(PostNature.NATURE_ID);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (preNatureFound && postNatureFound) {
			return true;
		}
		return false;

	}

	/**
	 * Removes AnalyzeTool custom builder natures.
	 *
	 * @param project
	 *            Project reference
	 * @param count
	 *            How many natures to remove
	 */
	private void removeNature(final IProject project, final int count) {
		try {
			IProjectDescription desc = project.getDescription();
			String[] natures = desc.getNatureIds();
			String[] newNatures = new String[natures.length - count];

			int index = 0;

			//thru natures
			//if pre- or post nature found skip it
			for (int i = 0; i < natures.length; i++) {
				if (!natures[i].equals(PreNature.NATURE_ID)
						&& !natures[i].equals(PostNature.NATURE_ID)) {
					newNatures[index] = natures[i];
					index++;
				}
			}
			desc.setNatureIds(newNatures);
			project.setDescription(desc, null);

		} catch (CoreException ce) {
			ce.printStackTrace();
		}

	}
}
