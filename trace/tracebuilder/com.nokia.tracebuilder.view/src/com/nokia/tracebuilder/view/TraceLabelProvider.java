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
* Converts tree model objects to strings to be displayed in the view
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.tracebuilder.engine.LastKnownLocation;
import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.engine.TraceViewNameExtension;
import com.nokia.tracebuilder.file.FileUtils;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;

/**
 * Converts tree model objects to strings to be displayed in the view
 * 
 */
final class TraceLabelProvider extends LabelProvider {

	/**
	 * Separator to component name and component id fields
	 */
	final String COMPONENT_SEPARATOR = ", "; //$NON-NLS-1$	

	/**
	 * Default tree node image
	 */
	private Image defaultImage;

	/**
	 * Image for trace
	 */
	private Image traceImage;

	/**
	 * Image for trace group
	 */
	private Image groupImage;

	/**
	 * Image for parameter
	 */
	private Image parameterImage;

	/**
	 * Image for property
	 */
	private Image propertyImage;

	/**
	 * Unrelated location icon
	 */
	private Image unrelatedLocationImage;

	/**
	 * Unrelated locations list icon
	 */
	private Image unrelatedLocationsImage;

	/**
	 * List navigator image
	 */
	private Image navigatorImage;

	/**
	 * Constructor
	 */
	public TraceLabelProvider() {
		defaultImage = TraceViewPlugin.getImageDescriptor(
				"icons/view/default.gif").createImage(); //$NON-NLS-1$
		traceImage = TraceViewPlugin
				.getImageDescriptor("icons/view/trace.gif").createImage(); //$NON-NLS-1$
		groupImage = TraceViewPlugin
				.getImageDescriptor("icons/view/group.gif").createImage(); //$NON-NLS-1$
		parameterImage = TraceViewPlugin.getImageDescriptor(
				"icons/view/parameter.gif").createImage(); //$NON-NLS-1$
		propertyImage = TraceViewPlugin.getImageDescriptor(
				"icons/view/property.gif").createImage(); //$NON-NLS-1$
		unrelatedLocationImage = TraceViewPlugin.getImageDescriptor(
				"icons/view/unrelated_location.gif").createImage(); //$NON-NLS-1$
		unrelatedLocationsImage = TraceViewPlugin.getImageDescriptor(
				"icons/view/unrelated_locations_list.gif").createImage(); //$NON-NLS-1$
		navigatorImage = TraceViewPlugin.getImageDescriptor(
				"icons/view/navigator.gif").createImage(); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		defaultImage.dispose();
		traceImage.dispose();
		groupImage.dispose();
		parameterImage.dispose();
		propertyImage.dispose();
		unrelatedLocationImage.dispose();
		unrelatedLocationsImage.dispose();
		navigatorImage.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object obj) {
		String text;
		if (obj instanceof TraceLocationWrapper) {
			text = traceLocationWrapperToString((TraceLocationWrapper) obj);
		} else if (obj instanceof PropertyWrapper) {
			text = propertyWrapperToString((PropertyWrapper) obj);
		} else if (obj instanceof TraceObjectWrapper) {
			text = traceObjectWrapperToString((TraceObjectWrapper) obj);
		} else if (obj instanceof LastKnownLocationWrapper) {
			text = LastKnownLocationWrapperToString((LastKnownLocationWrapper) obj);
		} else if (obj instanceof TraceLocationListWrapper) {
			text = locationListWrapperToString((TraceLocationListWrapper) obj);
		} else if (obj instanceof LastKnownLocationListWrapper) {
			text = LastKnownLocationListWrapperToString((LastKnownLocationListWrapper) obj);
		} else if (obj instanceof ObjectWrapper) {
			text = ((ObjectWrapper) obj).getObject().toString();
		} else if (obj instanceof ListNavigator) {
			text = listNavigatorToString((ListNavigator) obj);
		} else if (obj instanceof TraceViewExtensionWrapper) {
			text = ((TraceViewExtensionWrapper) obj).getExtension().toString();
		} else if (obj instanceof TraceParameterListWrapper) {
			text = Messages.getString("TraceLabelProvider.Parameters"); //$NON-NLS-1$
		} else if (obj instanceof TraceGroupListWrapper) {
			text = Messages.getString("TraceLabelProvider.TraceGroups"); //$NON-NLS-1$
		} else if (obj instanceof TraceListWrapper) {
			text = Messages.getString("TraceLabelProvider.Traces"); //$NON-NLS-1$
		} else if (obj instanceof ConstantTableListWrapper) {
			text = Messages.getString("TraceLabelProvider.ConstantTables"); //$NON-NLS-1$
		} else if (obj instanceof TraceLocationListsWrapper) {
			text = Messages.getString("TraceLabelProvider.LocationListsTitle"); //$NON-NLS-1$
		} else {
			text = obj.toString();
		}
		return text;
	}

	/**
	 * Converts the list navigator to string
	 * 
	 * @param navigator
	 *            the list navigator
	 * @return the string representation
	 */
	private String listNavigatorToString(ListNavigator navigator) {
		StringBuffer text = new StringBuffer();
		String start;
		String end;
		WrapperBase parent = navigator.getParent();
		if (parent instanceof TraceListWrapper) {
			start = Messages
					.getString("TraceLabelProvider.NavigatorTitleStartTraces"); //$NON-NLS-1$
			end = Messages
					.getString("TraceLabelProvider.NavigatorTitleEndTraces"); //$NON-NLS-1$
		} else if (parent instanceof TraceGroupListWrapper) {
			start = Messages
					.getString("TraceLabelProvider.NavigatorTitleStartGroups"); //$NON-NLS-1$
			end = Messages
					.getString("TraceLabelProvider.NavigatorTitleEndGroups"); //$NON-NLS-1$
		} else if (parent instanceof TraceLocationListWrapper) {
			start = Messages
					.getString("TraceLabelProvider.NavigatorTitleStartLocations"); //$NON-NLS-1$
			end = Messages
					.getString("TraceLabelProvider.NavigatorTitleEndLocations"); //$NON-NLS-1$
		} else {
			start = null;
			end = null;
			text.append(Messages
					.getString("TraceLabelProvider.NavigatorTitleInvalid")); //$NON-NLS-1$
		}
		if (start != null) {
			String separator = Messages
					.getString("TraceLabelProvider.NavigatorTitleValueSeparator"); //$NON-NLS-1$
			text.append(start);
			text.append(navigator.getStartIndex());
			text.append(separator);
			int max = navigator.getStartIndex() + navigator.getVisibleCount();
			text.append(max);
			separator = Messages
					.getString("TraceLabelProvider.NavigatorTitleTotalCountSeparator"); //$NON-NLS-1$
			text.append(separator);
			text.append(navigator.getTotalCount());
			text.append(end);
		}
		return text.toString();
	}

	/**
	 * Converts a property wrapper to string
	 * 
	 * @param wrapper
	 *            the property wrapper
	 * @return the string representation
	 */
	private String propertyWrapperToString(PropertyWrapper wrapper) {
		String text = null;
		// Get component id's from mmp files
		if (wrapper.getType().equals(
				Messages.getString("TraceObjectWrapper.ModelID"))) { //$NON-NLS-1$
			text = getComponentIdString(wrapper.getType());
			// Get component names from mmp files
		} else if (wrapper.getType().equals(
				Messages.getString("TraceObjectWrapper.ModelName"))) { //$NON-NLS-1$
			text = getComponentNameString(wrapper.getType());
		} else {
			text = wrapper.getType()
					+ Messages
							.getString("TraceLabelProvider.PropertySeparator") //$NON-NLS-1$
					+ wrapper.getProperty();
		}

		return text;
	}

	/**
	 * Get component name string
	 * 
	 * @param prefix
	 *            prefix text
	 * @return the component name string
	 */
	private String getComponentNameString(String prefix) {
		IFile file = getActiveFile();
		String propertySeparator = Messages
				.getString("TraceLabelProvider.PropertySeparator"); //$NON-NLS-1$
		
		// Set default text in case that component name is not available
		String text = prefix + propertySeparator
		+ Messages.getString("PropertyWrapper.NoProperty"); //$NON-NLS-1$;
		if (file != null) {
			IProject project = file.getProject();

			// Check that project is Carbide project
			boolean isCarbideProject = CarbideBuilderPlugin.getBuildManager()
					.isCarbideProject(project);
			if (isCarbideProject) {

				// Check if file is not source file
				if (isSourceFile(file)) {
					String componentNameString = TraceBuilderGlobals
							.getCurrentSoftwareComponentName();
					if (componentNameString != null) {
						text = prefix + propertySeparator + componentNameString;
					}
				}

				// Even file is source file it could be that we do know
				// component name, because it could be that file is not included
				// to MMP file
				if (text == null) {
					text = prefix
							+ propertySeparator
							+ Messages
									.getString("PropertyWrapper.NotAvailable"); //$NON-NLS-1$;
				}
			}
		}
		return text;
	}

	/**
	 * Get active file in editor
	 * 
	 * @return the active file in editor
	 */
	private IFile getActiveFile() {

		IFile file = null;

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IEditorPart editor = page.getActiveEditor();
				if (editor != null) {
					IEditorInput input = editor.getEditorInput();
					if (input instanceof IFileEditorInput) {
						file = ((IFileEditorInput) input).getFile();
					}
				}
			}
		}

		return file;
	}

	/**
	 * Get component id string
	 * 
	 * @param prefix
	 *            prefix text
	 * @return the component id string
	 */
	private String getComponentIdString(String prefix) {
		IFile file = getActiveFile();
		String propertySeparator = Messages
				.getString("TraceLabelProvider.PropertySeparator"); //$NON-NLS-1$

		// Set default text in case that component ID is not available
		String text = prefix + propertySeparator
		+ Messages.getString("PropertyWrapper.NoProperty"); //$NON-NLS-1$;;		
		
		if (file != null) {
			IProject project = file.getProject();

			// Check that project is Carbide project
			boolean isCarbideProject = CarbideBuilderPlugin.getBuildManager()
					.isCarbideProject(project);
			if (isCarbideProject) {

				// Check if file is not source file
				if (isSourceFile(file)) {
					String uidString = TraceBuilderGlobals
							.getCurrentSoftwareComponentId();
					if (uidString != null) {
						text = prefix + propertySeparator + uidString;
					}
				}

				// Even file is source file it could be that we do know
				// component id, because it could be that file is not included
				// to MMP file
				if (text == null) {
					text = prefix
							+ propertySeparator
							+ Messages
									.getString("PropertyWrapper.NotAvailable"); //$NON-NLS-1$;
				}
			}
		}
		
		return text; // CodForChk_Dis_Exits
	}

	/**
	 * Converts a trace object wrapper to string
	 * 
	 * @param obj
	 *            the object wrapper
	 * @return the string representation
	 */
	private String traceObjectWrapperToString(TraceObjectWrapper obj) {
		String text;
		if (obj instanceof ConstantTableWrapper) {
			text = constantTableWrapperToString((ConstantTableWrapper) obj);
		} else if (obj instanceof ConstantTableEntryWrapper) {
			text = constantTableEntryWrapperToString((ConstantTableEntryWrapper) obj);
		} else if (obj instanceof TraceParameterWrapper) {
			text = traceParameterWrapperToString((TraceParameterWrapper) obj);
		} else {
			text = commonTraceObjectWrapperToString(obj);
		}
		return text;
	}

	/**
	 * Converts a parameter wrapper to a string
	 * 
	 * @param wrapper
	 *            the parameter wrapper
	 * @return the string representation
	 */
	private String traceParameterWrapperToString(TraceParameterWrapper wrapper) {
		String text = commonTraceObjectWrapperToString(wrapper);
		text += Messages.getString("TraceLabelProvider.ParameterTypeSeparator") //$NON-NLS-1$
				+ TraceViewMessages
						.parameterTypeToString((TraceParameter) wrapper
								.getTraceObject());
		if (wrapper.isArrayType()) {
			text += Messages.getString("TraceParameterWrapper.ArrayTag"); //$NON-NLS-1$
		}
		return text;
	}

	/**
	 * Converts a trace object wrapper to string
	 * 
	 * @param wrapper
	 *            the trace object wrapper
	 * @return the string representation
	 */
	private String commonTraceObjectWrapperToString(TraceObjectWrapper wrapper) {
		String text = null;
		TraceObject object = wrapper.getTraceObject();
		// If object is null, it has been deleted but view is not refreshed yet
		if (object != null) {
			String name = object.getName();
			if (name.length() > 0) {
				TraceViewNameExtension nameExt = wrapper.getTraceObject()
						.getExtension(TraceViewNameExtension.class);
				if (nameExt != null) {
					text = nameExtensionToString(name, nameExt);
				} else {
					text = name;
				}
			}
		}
		if (text == null) {
			text = Messages.getString("TraceLabelProvider.Unnamed"); //$NON-NLS-1$
		}
		return text;
	}

	/**
	 * Merges a name and name extension together
	 * 
	 * @param name
	 *            the object name
	 * @param nameExt
	 *            the name extension
	 * @return the merged name
	 */
	private String nameExtensionToString(String name,
			TraceViewNameExtension nameExt) {
		StringBuffer text = new StringBuffer();
		text.append(name);
		text.append(Messages
				.getString("TraceLabelProvider.NameExtensionPrefix")); //$NON-NLS-1$
		text.append(nameExt.getNameExtension());
		text.append(Messages
				.getString("TraceLabelProvider.NameExtensionPostfix")); //$NON-NLS-1$
		return text.toString();
	}

	/**
	 * Converts a constant table entry wrapper to string
	 * 
	 * @param wrapper
	 *            the constant table entry wrapper
	 * @return the string representation
	 */
	private String constantTableEntryWrapperToString(
			ConstantTableEntryWrapper wrapper) {
		String text;
		// Constant table entry wrappers extend TraceObjectWrapper, but
		// representation is different
		TraceConstantTableEntry entry = (TraceConstantTableEntry) wrapper
				.getTraceObject();
		if (entry != null) {
			StringBuffer sb = new StringBuffer();
			sb.append(entry.getID());
			sb.append(Messages
					.getString("TraceLabelProvider.ConstantValueSeparator")); //$NON-NLS-1$
			sb.append(entry.getName());
			text = sb.toString();
		} else {
			text = Messages.getString("TraceLabelProvider.Unnamed"); //$NON-NLS-1$
		}
		return text;
	}

	/**
	 * Converts a constant table wrapper to string
	 * 
	 * @param wrapper
	 *            the constant table wrapper
	 * @return the string representation
	 */
	private String constantTableWrapperToString(ConstantTableWrapper wrapper) {
		String text = null;
		TraceObject object = wrapper.getTraceObject();
		if (object != null) {
			String name = object.getName();
			if (name != null && name.length() > 0) {
				text = createConstantTableName(wrapper, name);
			}
		}
		if (text == null) {
			text = Messages.getString("TraceLabelProvider.Unnamed"); //$NON-NLS-1$
		}
		return text;
	}

	/**
	 * Creates a constant table name
	 * 
	 * @param wrapper
	 *            the constant table
	 * @param name
	 *            the original table name
	 * @return the name shown in view
	 */
	private String createConstantTableName(ConstantTableWrapper wrapper,
			String name) {
		String text;
		StringBuffer sb = new StringBuffer();
		// Constant tables in the model's list of tables do not need
		// the "Type: " tag
		if (!(wrapper.getParent() instanceof ConstantTableListWrapper)) {
			sb.append(Messages
					.getString("TraceLabelProvider.ConstantTableNameTypeTag")); //$NON-NLS-1$
		}
		sb.append(Messages
				.getString("TraceLabelProvider.ConstantTableNamePrefix")); //$NON-NLS-1$
		sb.append(name);
		sb.append(Messages
				.getString("TraceLabelProvider.ConstantTableNamePostfix")); //$NON-NLS-1$
		text = sb.toString();
		return text;
	}

	/**
	 * Converts a location list wrapper to string
	 * 
	 * @param wrapper
	 *            the location list wrapper
	 * @return the string representation
	 */
	private String locationListWrapperToString(TraceLocationListWrapper wrapper) {
		String text;
		WrapperBase parent = wrapper.getParent();
		if (parent instanceof TraceWrapper) {
			text = Messages.getString("TraceLabelProvider.Locations"); //$NON-NLS-1$
		} else {
			text = wrapper.getLocationList().getListTitle();
			if (text == null) {
				text = Messages
						.getString("TraceLabelProvider.UnrelatedLocations"); //$NON-NLS-1$
			}
		}
		return text;
	}

	/**
	 * Converts a last known location list wrapper to string
	 * 
	 * @param wrapper
	 *            the location list wrapper
	 * @return the string representation
	 */
	private String LastKnownLocationListWrapperToString(
			LastKnownLocationListWrapper wrapper) {
		return Messages.getString("TraceLabelProvider.LastKnownLocations"); //$NON-NLS-1$
	}

	/**
	 * Converts a location wrapper to string
	 * 
	 * @param wrapper
	 *            the location wrapper
	 * @return the string representation
	 */
	private String traceLocationWrapperToString(TraceLocationWrapper wrapper) {
		String text = null;
		TraceLocation loc = wrapper.getLocation();
		if (loc != null) {
			StringBuffer sb = new StringBuffer();
			if (loc.getTrace() == null) {
				sb.append(loc.getFileName());
				sb.append(Messages
						.getString("TraceLabelProvider.FileLineSeparator")); //$NON-NLS-1$
				sb.append(loc.getLineNumber());
				sb.append(Messages
						.getString("TraceLabelProvider.LocationNameStart")); //$NON-NLS-1$
				sb.append(loc.getOriginalName());
				sb.append(Messages
						.getString("TraceLabelProvider.LocationNameEnd")); //$NON-NLS-1$
			} else {
				sb.append(loc.getFileName());
				sb.append(Messages
						.getString("TraceLabelProvider.FileLineSeparator")); //$NON-NLS-1$
				sb.append(loc.getLineNumber());
			}
			if (TraceBuilderConfiguration.SHOW_LOCATION_TAG_IN_VIEW) {
				sb.append(" ["); //$NON-NLS-1$
				sb.append(loc.getTag());
				sb.append("]"); //$NON-NLS-1$
			}
			text = sb.toString();
		}
		if (text == null) {
			text = Messages.getString("TraceLabelProvider.Unnamed"); //$NON-NLS-1$
		}
		return text;
	}

	/**
	 * Converts a last known location wrapper to string
	 * 
	 * @param wrapper
	 *            the location wrapper
	 * @return the string representation
	 */
	private String LastKnownLocationWrapperToString(
			LastKnownLocationWrapper wrapper) {
		String text = null;
		LastKnownLocation loc = wrapper.getLocation();
		if (loc != null) {
			StringBuffer sb = new StringBuffer();
			sb.append(loc.getFileName());
			sb.append(Messages
					.getString("TraceLabelProvider.FileLineSeparator")); //$NON-NLS-1$
			sb.append(loc.getLineNumber());
			String start = Messages
					.getString("TraceLabelProvider.LocationNameSeparatorStart"); //$NON-NLS-1$
			sb.append(start);
			sb.append(loc.getFilePath());
			sb.append(Messages
					.getString("TraceLabelProvider.LocationNameSeparatorEnd")); //$NON-NLS-1$
			text = sb.toString();
		}
		if (text == null) {
			text = Messages.getString("TraceLabelProvider.Unnamed"); //$NON-NLS-1$
		}
		return text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		Image retval = defaultImage;
		if (element instanceof TraceLocationWrapper) {
			TraceLocation loc = ((TraceLocationWrapper) element).getLocation();
			if (loc != null && loc.getTrace() == null) {
				TraceLocationList list = loc.getLocationList();
				if (list != null) {
					String text = list.getListTitle();
					if (text == null) {
						retval = unrelatedLocationImage;
					}
				}
			}
		} else if (element instanceof TraceLocationListWrapper) {
			WrapperBase parent = ((WrapperBase) element).getParent();
			if (!(parent instanceof TraceWrapper)) {
				TraceLocationList list = ((TraceLocationListWrapper) element)
						.getLocationList();
				if (list != null) {
					String text = list.getListTitle();
					if (text == null) {
						retval = unrelatedLocationsImage;
					}
				}
			}
		} else if (element instanceof TraceWrapper) {
			retval = traceImage;
		} else if (element instanceof TraceGroupWrapper) {
			retval = groupImage;
		} else if (element instanceof TraceParameterWrapper) {
			retval = parameterImage;
		} else if (element instanceof PropertyWrapper) {
			retval = propertyImage;
		} else if (element instanceof ListNavigator) {
			retval = navigatorImage;
		}
		return retval;
	}

	/**
	 * Check that is file source file
	 * 
	 * @param file
	 *            file that need to be check
	 * @return true if file is source file, false if not
	 */
	private boolean isSourceFile(IFile file) {
		boolean retVal = false;

		String extension = file.getFileExtension();

		if (extension != null
				&& (extension.equalsIgnoreCase(FileUtils.CPP_EXTENSION) || extension
						.equalsIgnoreCase(FileUtils.C_EXTENSION))) {
			retVal = true;
		}
		return retVal;
	}	
	
}
