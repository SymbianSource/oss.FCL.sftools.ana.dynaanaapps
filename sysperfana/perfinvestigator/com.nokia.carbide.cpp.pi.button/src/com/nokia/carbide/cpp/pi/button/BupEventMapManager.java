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

package com.nokia.carbide.cpp.pi.button;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.carbide.cpp.internal.pi.button.ui.BupPreferenceConstants;
import com.nokia.carbide.cpp.internal.pi.util.config.PIConfigXMLLoader;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileListType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.MappingType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigFactory;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.carbide.cpp.sdk.core.SDKCorePlugin;

public class BupEventMapManager {
	static private IBupEventMapProfile DEFAULT_PROFILE;		// this is initialized at constructor, static public is bad
	static private IBupEventMapProfile TECHVIEW_PROFILE;	// this is initialized at constructor, static public is bad
	static private IBupEventMapProfile S60_PROFILE;			// this is initialized at constructor, static public is bad
	static private IBupEventMapProfile MOAP_PROFILE;		// this is initialized at constructor, static public is bad
	static private IBupEventMapProfile UIQ_PROFILE;			// this is initialized at constructor, static public is bad
	static private IBupEventMapProfile LEGACY_PROFILE;		// this is initialized at constructor, static public is bad
	static private String WORKSPACE_PROFILE_FILENAME = "keymap.xml";	//$NON-NLS-1$
	static public String TECHVIEW_PROFILE_ID = "Symbian_TechView";	//$NON-NLS-1$
	static public String S60_PROFILE_ID = "S60";	//$NON-NLS-1$
	static public String MOAP_PROFILE_ID = "MOAP";	//$NON-NLS-1$
	static public String UIQ_PROFILE_ID = "UIQ";	//$NON-NLS-1$
	static public String LEGACY_PROFILE_ID = "PI pre-2.0";	//$NON-NLS-1$
	static public URI DEFAULT_PROFILE_URI;
	static public URI WORKSPACE_PREF_KEY_MAP_URI;
	static private BupEventMapManager instance = null;
	static HashMap <IBupEventMapProfile, IBupEventMap> loadedKeyMap = new HashMap <IBupEventMapProfile, IBupEventMap>();
	
	private class BupEventMapProfile implements IBupEventMapProfile {

		String profileId = null;
		ISymbianSDK sdk = null;
		URI uri = null;
		
		/**
		 * @param profileId	ID string for the profile in XML
		 * @param sdk SDK associated, could be null
		 * @param xmlUri URI for the XML file(SDK, workspace pref, built in location)
		 */
		public BupEventMapProfile (String profileId, ISymbianSDK sdk, URI xmlURI) {
			this.profileId = profileId;
			this.sdk = sdk;
			this.uri = xmlURI;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BupEventMapProfile other = (BupEventMapProfile) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (profileId == null) {
				if (other.profileId != null)
					return false;
			} else if (!profileId.equals(other.profileId))
				return false;
			if (sdk == null) {
				if (other.sdk != null)
					return false;
			} else if (!sdk.equals(other.sdk))
				return false;
			if (uri == null) {
				if (other.uri != null)
					return false;
			} else if (!uri.equals(other.uri))
				return false;
			return true;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((profileId == null) ? 0 : profileId.hashCode());
			result = prime * result + ((sdk == null) ? 0 : sdk.hashCode());
			result = prime * result + ((uri == null) ? 0 : uri.hashCode());
			return result;
		}

		
		public String toString() {
			if (sdk == null) {
				if (uri.equals(DEFAULT_PROFILE_URI))
					return profileId + Messages.getString("BupEventMapManager.34"); //$NON-NLS-1$
				return profileId + Messages.getString("BupEventMapManager.35"); //$NON-NLS-1$
			} else
				return profileId + Messages.getString("BupEventMapManager.37") + sdk.getUniqueId() + Messages.getString("BupEventMapManager.39"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public String getProfileId() {
			return profileId;
		}

		public ISymbianSDK getSDK() {
			return sdk;
		}
		
		public URI getURI() {
			return uri;
		}

		private BupEventMapManager getOuterType() {
			return BupEventMapManager.this;
		}
		
	}
	
	private class BupEventMap implements IBupEventMap{
		private class MapEntry {
			String enumString;
			String label;
		}
		
		private String profileName = null;
		private IBupEventMapProfile profile = null;
		
		private int checkOutCount = 1;	// count myself too
		private TreeMap<Integer, MapEntry> keyCodeMap = new TreeMap<Integer, MapEntry>();
		
		public BupEventMap(ButtonEventProfileType buttonProfileType, IBupEventMapProfile buttonProfile) {
			profileName = buttonProfileType.getProfileId();
			profile = buttonProfile;
			EList<MappingType> mappingList = buttonProfileType.getMapping();
			for (MappingType mapping : mappingList) {
				addMapping(new Long(mapping.getKeyCode()).intValue(), mapping.getEnumString(), mapping.getLabel());
			}
		}
		
		public int getCheckOutCount() {
			return checkOutCount;
		}
		
		public void upCheckOutCount() {
			++checkOutCount;
		}
		
		public void downCheckOutCount() {
			--checkOutCount;
		}
		
		public IBupEventMapProfile getProfile() {
			return profile;
		}
		
		public String getProfileName () {
			return profileName;
		}
				
		public String getLabel(int keyCode)
		{
			MapEntry entry = keyCodeMap.get(keyCode);
		    
		    if (entry == null) {
		    	return "" + keyCode;	// default is just the decimal value string //$NON-NLS-1$
		    }

		    return entry.label;
		}
		
		public Set<Integer> getKeyCodeSet() {
			return keyCodeMap.keySet();
		}
		
		public String getEnum(int keyCode)
		{
			MapEntry entry = keyCodeMap.get(keyCode);
		    
		    if (entry == null) {
		    	return ""; //$NON-NLS-1$
		    }

		    return entry.enumString;
		}

		public void addMapping(int keyCode, String enumString, String label) {
			MapEntry entry = keyCodeMap.get(keyCode);	// support override of mapping
			if (entry == null) {
				entry = new MapEntry();
				keyCodeMap.put(new Integer(keyCode), entry);
			}
			entry.enumString = enumString;
			entry.label = label;
		}
		
		public void removeMapping(int keyCode) {
			MapEntry entry = keyCodeMap.get(keyCode);
			if (entry != null) {
				keyCodeMap.remove(new Integer(keyCode));
			}
		}
		
		public ButtonEventProfileType toEmfModel() {
			ButtonEventProfileType profile = PIConfigFactory.eINSTANCE.createButtonEventProfileType();

			profile.setProfileId(getProfileName());
			EList<MappingType> mappingList = profile.getMapping();
			for (Entry<Integer, MapEntry> entry : keyCodeMap.entrySet()) {
				MappingType mappingType = PIConfigFactory.eINSTANCE.createMappingType();
				mappingType.setKeyCode(entry.getKey().longValue());
				mappingType.setEnumString(entry.getValue().enumString);
				mappingType.setLabel(entry.getValue().label);
				mappingList.add(mappingType);
			}
			
			return profile;
		}
	}
	
	private BupEventMapManager () {
		// singleton		
		try {
			DEFAULT_PROFILE_URI = FileLocator.find(ButtonPlugin.getDefault().getBundle(), new Path("/data/default.xml"), null).toURI(); //$NON-NLS-1$
			WORKSPACE_PREF_KEY_MAP_URI = new File(ButtonPlugin.getDefault().getStateLocation().append(WORKSPACE_PROFILE_FILENAME).toOSString()).toURI();
		} catch (URISyntaxException e) {
			GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.41")); //$NON-NLS-1$
		}	
		ArrayList<IBupEventMapProfile> profiles = getProfilesFromBuiltin();
		for (IBupEventMapProfile profile: profiles) {
			if (profile.getProfileId().equals(TECHVIEW_PROFILE_ID)) {
				TECHVIEW_PROFILE = profile;
			}
			if (profile.getProfileId().equals(S60_PROFILE_ID)) {
				S60_PROFILE = profile;
			}
			if (profile.getProfileId().equals(MOAP_PROFILE_ID)) {
				MOAP_PROFILE = profile;
			}
			if (profile.getProfileId().equals(UIQ_PROFILE_ID)) {
				UIQ_PROFILE = profile;
			}
			if (profile.getProfileId().equals(LEGACY_PROFILE_ID)) {
				LEGACY_PROFILE = profile;
			}
		}
		DEFAULT_PROFILE = S60_PROFILE;
	}
	
	public Object clone() throws CloneNotSupportedException {
	    throw new CloneNotSupportedException(); // singleton
	}
	
	public static BupEventMapManager getInstance() {
		if (instance == null) {
			instance = new BupEventMapManager();
		}
		return instance;
	}
	
	private ArrayList<IBupEventMapProfile> getProfilesFromURI(URI uri, ISymbianSDK sdk) {
		ArrayList<IBupEventMapProfile> allProfiles = new ArrayList<IBupEventMapProfile>();
		ButtonEventProfileListType settings;
		try {
			settings = PIConfigXMLLoader.loadPiSettings(uri.toURL());
			EList<ButtonEventProfileType> profileList = settings.getButtonEventProfile();
			for (ButtonEventProfileType profile : profileList) {
				allProfiles.add(new BupEventMapProfile(profile.getProfileId(), sdk, uri));
			}
		} catch (MalformedURLException e) {
			// just skip this file if it failed, sdk relative and hardcoded path should never fail
		} catch (IOException e) {
			// just skip this file if it failed, sdk relative and hardcoded path should never fail
		}
		
		return allProfiles;
	}
	
	/**
	 * @return Key press profiles available from built in.
	 */
	public ArrayList<IBupEventMapProfile> getProfilesFromBuiltin() {
		return getProfilesFromURI(DEFAULT_PROFILE_URI, null);
	}
	
	/**
	 * @return Key press profiles available from workspace preference.
	 */
	public ArrayList<IBupEventMapProfile> getProfilesFromWorkspacePref() {
		if (new File(WORKSPACE_PREF_KEY_MAP_URI).exists()) {
			return getProfilesFromURI(WORKSPACE_PREF_KEY_MAP_URI, null);
		} else {
			try {
				new File(WORKSPACE_PREF_KEY_MAP_URI).createNewFile();
			} catch (IOException e) {
				// just try our best creating new file
			}
			return new ArrayList<IBupEventMapProfile>();
		}
	}
	
	public String profileLocatationInSDK(ISymbianSDK sdk) {
		String profileString = sdk.getEPOCROOT();
		
		if (profileString.endsWith(File.separator))
			profileString += "epoc32" + File.separator + "data" + File.separator + "pikeymap.xml";	//$NON-NLS-1$" //$NON-NLS-2$" //$NON-NLS-3$"
		else
			profileString += File.separator + "epoc32" + File.separator + "data" + File.separator + "pikeymap.xml";	//$NON-NLS-1$" //$NON-NLS-2$" //$NON-NLS-3$"

		return profileString;
	}
	
	/**
	 * @param sdk Any SDK from devices.xml, null workspace pref
	 * @return Key press profiles available in a SDK.
	 */
	public ArrayList<IBupEventMapProfile> getProfilesFromSDK(ISymbianSDK sdk) {		
		File profileFile = new File (profileLocatationInSDK(sdk));
		
		if (!profileFile.exists()) {	//$NON-NLS-1$"
			return new ArrayList<IBupEventMapProfile>();	// just return blank if there is no XML file
		}
		
		return getProfilesFromURI(profileFile.toURI(), sdk);
	}	
	
	/**
	 * @return A List of all known good key press profiles available to Carbide.
	 */
	public ArrayList<IBupEventMapProfile> getAllProfiles() {
		ArrayList<IBupEventMapProfile> allProfiles = new ArrayList<IBupEventMapProfile>();
		
		allProfiles.addAll(getProfilesFromBuiltin());
		allProfiles.addAll(getProfilesFromWorkspacePref());
		
		List<ISymbianSDK> sdkList = SDKCorePlugin.getSDKManager().getSDKList();
		for (ISymbianSDK sdk: sdkList) {
			allProfiles.addAll(getProfilesFromSDK(sdk));
		}
		
		return allProfiles;
	}
	
	/**
	 * 
	 * We hide mapping profile creation inside BupEventMapManager on purpose, user should
	 * obtain list of profile from BupEventMapManager and use them immediately, this save
	 * us from error handling user specified profile ID unavailable (e.g. removed from SDK)
	 * 
	 * @param uid The UID for the instance(e.g. current editor) to bind mapping to.
	 * @param profile The mapping obtain from getProfilesFromSDK/getAllProfiles.
	 * @return The key press map captured.
	 */
	public IBupEventMap captureMap(IBupEventMapProfile profile) {
		synchronized(loadedKeyMap) {
			IBupEventMap map = loadedKeyMap.get(profile);
			if (map == null) {
				try {
					ButtonEventProfileListType settings = PIConfigXMLLoader.loadPiSettings(profile.getURI().toURL());
					EList<ButtonEventProfileType> profileList = settings.getButtonEventProfile();
					for (ButtonEventProfileType buttonProfileType : profileList) {
						if (buttonProfileType.getProfileId().equals(profile.getProfileId())) {
							map = new BupEventMap(buttonProfileType, profile);
							loadedKeyMap.put(profile, map);
						}
					}
				} catch (MalformedURLException e) {
					GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.44") + profile.getURI()); //$NON-NLS-1$
				} catch (IOException e) {
					GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.45") + profile.getURI()); //$NON-NLS-1$
				}
				if (map == null) {
					GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.42") + profile); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				((BupEventMap)map).upCheckOutCount();
			}
			return map;
		}
	}
	
	/**
	 * Release the key mapping when the UID is going away
	 * 
	 * @param uid The UID for the instance you want to release key mapping binding
	 */
	public void releaseMap(IBupEventMap map) {
		synchronized(loadedKeyMap) {
			if (map != null) {
				((BupEventMap)map).downCheckOutCount();
				if (((BupEventMap)map).getCheckOutCount() <= 0) {
					loadedKeyMap.remove(((BupEventMap)map).getProfile());
				}
			}
		}
	}
	
	public void addToWorkspace (String profileId) {
		
		try {
			ButtonEventProfileListType workspace_settings = PIConfigXMLLoader.loadPiSettings(WORKSPACE_PREF_KEY_MAP_URI.toURL());
			EList<ButtonEventProfileType> workspace_profileList = workspace_settings.getButtonEventProfile();
			
			ButtonEventProfileType buttonEventProfileType = PIConfigFactory.eINSTANCE.createButtonEventProfileType();
			buttonEventProfileType.setProfileId(profileId);
			buttonEventProfileType.getMapping().clear();
			
			workspace_profileList.add(buttonEventProfileType);
			// write back the model
			PIConfigXMLLoader.writePiSettings(workspace_settings, WORKSPACE_PREF_KEY_MAP_URI.toURL());
		} catch (MalformedURLException e) {
			GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.47") + profileId); //$NON-NLS-1$
		} catch (IOException e) {
			GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.48") + profileId); //$NON-NLS-1$
		}

	}
	
	public void removeFromWorkspace (IBupEventMapProfile removedProfile) {
		synchronized(loadedKeyMap) {
			try {
				ButtonEventProfileListType workspace_settings = PIConfigXMLLoader.loadPiSettings(WORKSPACE_PREF_KEY_MAP_URI.toURL());
				EList<ButtonEventProfileType> workspace_profileList = workspace_settings.getButtonEventProfile();
				// profile with the same ID will replace workspace pref
				ButtonEventProfileType remove = null;
				for (ButtonEventProfileType profile : workspace_profileList) {
					if (profile.getProfileId().equals(removedProfile.getProfileId())) {
						remove = profile;
					}
				}
				if (remove != null) {
					int i = workspace_profileList.indexOf(remove);
					workspace_profileList.remove(i);
				}
				// write back the model
				PIConfigXMLLoader.writePiSettings(workspace_settings, WORKSPACE_PREF_KEY_MAP_URI.toURL());
			} catch (MalformedURLException e) {
				GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.49") + removedProfile.getProfileId()); //$NON-NLS-1$
			} catch (IOException e) {
				GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.50") + removedProfile.getProfileId()); //$NON-NLS-1$
			}
		}
	}
	
	public void commitEditToWorkspace (IBupEventMap map) {
		synchronized(loadedKeyMap) {
			try {
				ButtonEventProfileListType workspace_settings = PIConfigXMLLoader.loadPiSettings(WORKSPACE_PREF_KEY_MAP_URI.toURL());
				EList<ButtonEventProfileType> workspace_profileList = workspace_settings.getButtonEventProfile();

				// profile with the same ID will replace workspace pref
				ButtonEventProfileType replace = null;
				for (ButtonEventProfileType profile : workspace_profileList) {
					if (profile.getProfileId().equals(map.getProfile().getProfileId())) {
						replace = profile;
					}
				}
				if (replace != null) {
					int i = workspace_profileList.indexOf(replace);
					workspace_profileList.remove(i);
					workspace_profileList.add(i, map.toEmfModel());
				} else {
					workspace_profileList.add(map.toEmfModel());
				}
				// write back the model
				PIConfigXMLLoader.writePiSettings(workspace_settings, WORKSPACE_PREF_KEY_MAP_URI.toURL());
			} catch (MalformedURLException e) {
				GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.51") + map.getProfile().getProfileId()); //$NON-NLS-1$
			} catch (IOException e) {
				GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.52") + map.getProfile().getProfileId()); //$NON-NLS-1$
			}
		}
	}
	
	public void importMergeToWorkspace (URI uri) {
		try {
			ButtonEventProfileListType workspace_settings = PIConfigXMLLoader.loadPiSettings(WORKSPACE_PREF_KEY_MAP_URI.toURL());
			EList<ButtonEventProfileType> workspace_profileList = workspace_settings.getButtonEventProfile();
			
			ButtonEventProfileListType settings = PIConfigXMLLoader.loadPiSettings(uri.toURL());
			EList<ButtonEventProfileType> profileList = settings.getButtonEventProfile();
			
			// profile with the same ID will replace workspace pref
			while(profileList.size() > 0) {
				ButtonEventProfileType replace = null;
				for (ButtonEventProfileType profile : workspace_profileList) {
					if (profile.getProfileId().equals(profileList.get(0).getProfileId())) {
						replace = profile;
					}
				}
				// containment == true in XML, profileList will remove the element
				if (replace != null) {
					int i = workspace_profileList.indexOf(replace);
					workspace_profileList.remove(i);
					workspace_profileList.add(i, profileList.get(0));
				} else {
					workspace_profileList.add(profileList.get(0));
				}
			}
			// write back the model
			PIConfigXMLLoader.writePiSettings(workspace_settings, WORKSPACE_PREF_KEY_MAP_URI.toURL());
		} catch (MalformedURLException e) {
			GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.53") + uri); //$NON-NLS-1$
		} catch (IOException e) {
			GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.54") + uri); //$NON-NLS-1$
		}
	}
	
	public void saveMap (URI uri, ArrayList<ButtonEventProfileType> mapList) {
		ButtonEventProfileListType profileList = PIConfigFactory.eINSTANCE.createButtonEventProfileListType();
		profileList.setButtonEventProfileVersion(new BigDecimal("1.0")); //$NON-NLS-1$
		profileList.getButtonEventProfile().addAll(mapList);
		try {
			PIConfigXMLLoader.writePiSettings(profileList, uri.toURL());
		} catch (MalformedURLException e) {
			GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.56") + uri); //$NON-NLS-1$
		} catch (IOException e) {
			GeneralMessages.showErrorMessage(Messages.getString("BupEventMapManager.57") + uri); //$NON-NLS-1$
		}
	}

	/**
	 * @param uri
	 * @return
	 */
	public ArrayList<IBupEventMapProfile> getOverLapWithWorkspace(URI uri) {
		ArrayList<IBupEventMapProfile> result = new ArrayList<IBupEventMapProfile>();
			
		ArrayList<IBupEventMapProfile> uriProfiles = getProfilesFromURI(uri, null);
		ArrayList<IBupEventMapProfile> workspaceProfiles = getProfilesFromWorkspacePref();
		
		for (IBupEventMapProfile workspaceProfile : workspaceProfiles) {
			for (IBupEventMapProfile uriProfile : uriProfiles) {
				if (workspaceProfile.getProfileId().equals(uriProfile.getProfileId())) {
					result.add(workspaceProfile);
				}
			}
		}
		
		return result;
	}

	/**
	 * Read from global preference for profile selection
	 * 
	 * @return
	 */
	public IBupEventMapProfile getPrefSelectedProfile() {
		IPreferenceStore store = ButtonPlugin.getBupPrefsStore();

		String profileID = store.getString(BupPreferenceConstants.KEY_MAP_PROFILE_STRING);
		
		if (profileID != null) {
			ArrayList<IBupEventMapProfile> profiles = getAllProfiles();
			for (IBupEventMapProfile profile : profiles) {
				if (profile.toString().equals(profileID)) {
					return profile;
				}
			}
		}

		return DEFAULT_PROFILE;
	}

	/**
	 * @param profile
	 * @return
	 */
	public boolean canRemoveProfile(IBupEventMapProfile profile) {
		return loadedKeyMap.get(profile) == null;
	}

	/**
	 * @return
	 */
	public IBupEventMapProfile getLegacyProfile() {
		return LEGACY_PROFILE;
	}

	/**
	 * @return
	 */
	public IBupEventMapProfile getDefaultProfile() {
		return DEFAULT_PROFILE;
	}
	
	/**
	 * @return
	 */
	public IBupEventMapProfile getTechViewProfile() {
		return TECHVIEW_PROFILE;
	}

	/**
	 * @return
	 */
	public IBupEventMapProfile getBuiltinS60Profile() {
		return S60_PROFILE;
	}
}
