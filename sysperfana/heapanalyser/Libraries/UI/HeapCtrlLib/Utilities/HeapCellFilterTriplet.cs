/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright notice,
*   this list of conditions and the following disclaimer.
* - Redistributions in binary form must reproduce the above copyright notice,
*   this list of conditions and the following disclaimer in the documentation
*   and/or other materials provided with the distribution.
* - Neither the name of Nokia Corporation nor the names of its contributors
*   may be used to endorse or promote products derived from this software
*   without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
* 
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/

using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;
using HeapLib.Statistics.Tracking.Base;

namespace HeapCtrlLib.Utilities
{
    internal class HeapCellFilterTriplet
    {
        #region Constructors & destructor
        public HeapCellFilterTriplet()
            : this( string.Empty, KDisabledColour, false, null )
        {
        }

        public HeapCellFilterTriplet( string aEntity, Color aColor, bool aEnabled, TrackingInfo aTrackingInfo )
        {
            iEntity = aEntity;
            iColor = aColor;
            iEnabled = aEnabled;
            iTrackingInfo = aTrackingInfo;
        }
        #endregion

        #region API
        #endregion

        #region Properties
        public bool Enabled
        {
            get { return iEnabled; }
            set { iEnabled = value; }
        }

        public Color Color
        {
            get
            {
                Color ret = iColor;
                //
                if ( iEnabled == false )
                {
                    ret = KDisabledColour;
                }
                //
                return ret;
            }
            set { iColor = value; }
        }

        public Color ColorAbsolute
        {
            get { return iColor; }
        }

        public string Entity
        {
            get { return iEntity; }
        }

        public TrackingInfo TrackingInfo
        {
            get { return iTrackingInfo; }
        }
        #endregion

        #region Constants
        public static Color KDisabledColour = Color.WhiteSmoke;
        #endregion

        #region Data members
        private bool iEnabled = true;
        private Color iColor = KDisabledColour;
        private readonly string iEntity;
        private readonly TrackingInfo iTrackingInfo;
        #endregion
    }

    internal class HeapCellFilterTripletDictionary : IEnumerable<KeyValuePair<string,HeapCellFilterTriplet>>
    {
        #region Constructors & destructor
        public HeapCellFilterTripletDictionary()
        {
        }

        public HeapCellFilterTripletDictionary( HeapCellFilterTripletDictionary aCopy )
        {
            foreach( KeyValuePair<string, HeapCellFilterTriplet> entry in aCopy )
            {
                Add( entry.Key, entry.Value.Entity, entry.Value.ColorAbsolute, entry.Value.Enabled, entry.Value.TrackingInfo );
            }
        }
        #endregion

        #region API
        public void Clear()
        {
            iEntries.Clear();
        }

        public void Add( string aEntity, string aCaption, Color aColor, TrackingInfo aTrackingInfo )
        {
            HeapCellFilterTriplet triplet = new HeapCellFilterTriplet( aCaption, aColor, true, aTrackingInfo );
            iEntries.Add( aEntity, triplet );
        }

        public void Add( string aEntity, string aCaption, Color aColor, bool aEnabled, TrackingInfo aTrackingInfo )
        {
            HeapCellFilterTriplet triplet = new HeapCellFilterTriplet( aCaption, aColor, aEnabled, aTrackingInfo );
            iEntries.Add( aEntity, triplet );
        }
        #endregion

        #region Properties
        public HeapCellFilterTriplet this[ string aEntity ]
        {
            get
            {
                bool contained = iEntries.ContainsKey( aEntity );
                System.Diagnostics.Debug.Assert( contained );
                HeapCellFilterTriplet ret = iEntries[ aEntity ];
                return ret;
            }
        }

        public int Count
        {
            get { return iEntries.Count; }
        }
        #endregion

        #region IEnumerable<KeyValuePair<string,HeapCellFilterTriplet>> Members
        public IEnumerator<KeyValuePair<string, HeapCellFilterTriplet>> GetEnumerator()
        {
            return iEntries.GetEnumerator();
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return iEntries.GetEnumerator();
        }
        #endregion

        #region Data members
        private Dictionary<string, HeapCellFilterTriplet> iEntries = new Dictionary<string, HeapCellFilterTriplet>();
        #endregion
    }
}
