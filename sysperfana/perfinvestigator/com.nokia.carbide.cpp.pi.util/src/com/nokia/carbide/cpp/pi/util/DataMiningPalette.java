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

package com.nokia.carbide.cpp.pi.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class DataMiningPalette {

	private Map<Object, RGB> palette = new HashMap<Object, RGB>();

	private boolean haveEntry(Object entry) {
		return palette.containsKey(entry);
	}

	private boolean haveColor(RGB color) {
		return palette.containsValue(color);
	}

	private void add(Object entry, RGB color) {
		palette.put(entry, color);
	}

	private void update(Object entry, RGB color) {
		palette.remove(entry);
		palette.put(entry, color);
	}

	private RGB hsv2rgb(int hue, double saturation, double value) {

		if (hue >= 360)
			hue %= 360;
		int Hi = (hue / 60) % 6;
		double f = (double) hue / (double) 60 - Hi;

		int p = (int) ((value * (1 - saturation)) * 255);
		int q = (int) ((value * (1 - f * saturation)) * 255);
		int t = (int) ((value * (1 - (1 - f) * saturation)) * 255);
		int V = (int) (value * 255);

		switch (Hi) {
		case 0:
			return (new RGB(V, t, p));
		case 1:
			return (new RGB(q, V, p));
		case 2:
			return (new RGB(p, V, t));
		case 3:
			return (new RGB(p, q, V));
		case 4:
			return (new RGB(t, p, V));
		case 5:
			return (new RGB(V, p, q));
		}
		return (new RGB(0, 0, 0));
	}

	// returns if recolor of palette on specific entry if successful
	public boolean recolorEntryDialog(Shell parent, Object entry) {
		ColorDialog colorDialog = new ColorDialog(parent, SWT.NONE);
		colorDialog.setRGB(new RGB(255, 255, 255));
		RGB newRGB = colorDialog.open();
		if (newRGB != null) {
			if (haveColor(newRGB)) {
				MessageBox errorDialog = new MessageBox(parent, SWT.ICON_ERROR
						| SWT.OK);
				errorDialog.setText(Messages
						.getString("DataMiningPalette.change.denied")); //$NON-NLS-1$
				errorDialog.setMessage(Messages
						.getString("DataMiningPalette.color.exist")); //$NON-NLS-1$
				errorDialog.open();
			}
			if (!haveEntry(entry)) {
				MessageBox errorDialog = new MessageBox(parent, SWT.ICON_ERROR
						| SWT.OK);
				errorDialog.setText(Messages
						.getString("DataMiningPalette.change.denied")); //$NON-NLS-1$
				errorDialog
						.setMessage(Messages
								.getString("DataMiningPalette.item.does.not.exist.in.palette")); //$NON-NLS-1$
				errorDialog.open();
			}
			update(entry, newRGB);
			return true;
		}
		return false;
	}

	public RGB getRGB(Object entry) {
//		if (!haveEntry(entry)) {
//		}
		return palette.get(entry);
	}

	public void assignSOSColor(List entryList) {
//		if (entryList.size() > 256) {
//			// We only want to reserve 216 color web-safe palette to SOS
//			// thread/binary
//		}
		// Web-safe palette is a permuation of 0x00,0x33,0x66,0x99,0xCC,0xFF in
		// RGB
		int channelValue[] = { 0x00, 0x33, 0x66, 0x99, 0xCC, 0xFF };
		
		Iterator entryListItr = entryList.iterator();

		Object entry = entryListItr.next();

		for (int i = 0; i < 216; i++)
		{
			
//			if (haveEntry(entry)) {
//			}
					
			int index = i / 3 + (i % 3) * 72;
			int indexR = index % 6;
			int indexG = (index / 6) % 6;
			int indexB = (index / 36) % 6;
			
			RGB myRGB = new RGB(channelValue[indexR], channelValue[indexG], channelValue[indexB]);

			if (haveColor(myRGB))
				continue;

			add(entry, myRGB);
			
			if (!entryListItr.hasNext())
				return;
			else
				entry = entryListItr.next();
		}
	}

	/*
	 * 
	 * Use H and S (V = S) in HSV for different colors Attempt to evenly divide
	 * into even amount of H circle, with different layer of S depth
	 * 
	 */

	public void assignColor(List entryList) {
		final double maxSaturation = 1;
		final double minSaturation = 0.5;

		// red, orange, yellow, green, cyan, blue, magenta
		int hueEntry[] = { 0, 30, 60, 120, 180, 240, 300 };

		double saturation = maxSaturation;
		Iterator entryListItr = entryList.iterator();

		Object entry = entryListItr.next();
		int remain = entryList.size();

		while (saturation > minSaturation && entryListItr.hasNext()) {

			for (int i = 0; i < hueEntry.length; i++) {
				
//				if (haveEntry(entry)) {
//				}

				RGB myRGB = hsv2rgb(hueEntry[i], saturation, saturation);

				if (haveColor(myRGB))
					continue;

				add(entry, myRGB);
				--remain;

				if (!entryListItr.hasNext())
					return;
				else
					entry = entryListItr.next();
			}

			// Figure out size of S layer by expected layers + 1
			// Always saving one layer in case we see more overlapping color
			int expected_layers = (int) Math.ceil((double) (remain)
					/ (double) hueEntry.length);
			saturation -= (saturation - minSaturation) / (expected_layers + 1);
		}

//		if (entryList.size() != palette.size()) {
//		}
	}
}
