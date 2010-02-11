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
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

abstract public class TableColorPalette implements ITableColorPalette {

	private final int channelValue[] = { 0x00, 0x33, 0x66, 0x99, 0xCC, 0xFF };
	
	private Map<Object, Color> palette = new HashMap<Object, Color>();
	private int currentColorIndex = 0;
	
	abstract public RGB getConstantRGB(Object entry);

	public TableColorPalette() {
	}
	
	// Windows palette default from here
	//http://www.mozilla.org/docs/refList/user-interface/visual/colorpalette/
	int defaultPalette[][] = {{255,255,0}, {0,255,255}, {255,102,51},
								{128,128,0}, {0,0,128}, {0,255,0},
								{0,128,0}, {128,0,0}, {0,128,128},
								{0,55,60}, {204,153,102}, {0,153,255},
								{0,0,255}, {51,255,153}, {204,204,255},
								{255,0,0}, {153,153,255}, {153,0,102},
								{102,102,204}, {51,51,102}, {255,102,204}, 
								{66,154,167},{153,102,153}};
		
	private void add(Object entry) {
		RGB constantRGB = getConstantRGB(entry);
		if (constantRGB != null){
			// default color for SOS thread/binary/functions
			palette.put(entry, ColorPalette.getColor(constantRGB));
		} else {
			// wrap around the table and assign the same color if we've done once
			int i = currentColorIndex++ % 216;
			int indexR, indexG, indexB;
			if (i < 23){ // pick a bunch of color from Windows default palette first
						// so user won't complain too much
				indexR = defaultPalette[i][0];
				indexG = defaultPalette[i][1];
				indexB = defaultPalette[i][2];
				palette.put(entry, ColorPalette.getColor(new RGB(indexR,indexG,indexB)));
			}else {
				int index;
				i = 215 - i;	// start from lighter color for better contrast
				index = i / 3 + (i % 3) * 72;
				indexR = index % 6;
				indexG = (index / 6) % 6;
				indexB = (index / 36) % 6;
			
			palette.put(entry, ColorPalette.getColor(new RGB(channelValue[indexR],
															channelValue[indexG],
															channelValue[indexB])));
			}
		}
	}
	
	public Color getColor(Object entry) {
		if (palette.get(entry) == null) {
			add(entry);
		}
		return palette.get(entry);
	}

	public java.awt.Color getAWTColor(Object entry) {
		if (palette.get(entry) == null) {
			add(entry);
		}
		Color swtColor = palette.get(entry);
		return new java.awt.Color(
								swtColor.getRed(),
								swtColor.getGreen(),
								swtColor.getBlue()
								);
	}
	
	public Color assignColor(Object entry, Color color) {
		return assignColor(entry, color.getRGB());
	}

	public Color assignColor(Object entry, RGB rgb) {
		if (palette.get(entry) == null) {
			palette.remove(entry);
		}
		Color tmpColor;
		if (rgb == null) {
			// assign from 216 palette if nothing given
			tmpColor = this.getColor (entry);
		}
		else {
			tmpColor = ColorPalette.getColor(rgb);
			palette.put(entry, ColorPalette.getColor(rgb));
		}
		return tmpColor;
	}

	// returns if recolor of palette on specific entry if successful
	public boolean recolorEntryDialog(Shell parent, Object entry) {
		ColorDialog colorDialog = new ColorDialog(parent, SWT.PRIMARY_MODAL);
		colorDialog.setText(entry.toString());
		if (palette.get(entry) != null) {
			colorDialog.setRGB(palette.get(entry).getRGB());
		} else {
			colorDialog.setRGB(new RGB(255, 255, 255));
		}
		RGB newRGB = colorDialog.open();
		if (newRGB != null) {
			// allow duplicated color
			if (palette.get(entry) == null) {
				MessageBox errorDialog = new MessageBox(parent, SWT.ICON_ERROR
						| SWT.OK);
				errorDialog.setText(Messages
						.getString("TableColorPalette.change.denied")); //$NON-NLS-1$
				errorDialog
						.setMessage(Messages
								.getString("TableColorPalette.item.does.not.exist.in.palette")); //$NON-NLS-1$
				errorDialog.open();
			}
			assignColor(entry, newRGB);
			return true;
		}
		return false;
	}
	
	// we can use this for export
	public Map<Object, Color> getPaletteMap()
	{
		return palette;
	}
}
