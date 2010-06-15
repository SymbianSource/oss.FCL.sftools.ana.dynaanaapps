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

namespace HeapLib.Statistics.Tracking.Base
{
    public abstract class TrackingComparisonBase : IComparer<TrackingInfo>
    {
        #region Constructors & destructor
        public TrackingComparisonBase( bool aAscending )
        {
            iAscending = aAscending;
        }
        #endregion

        #region IComparer Members
        public int Compare( TrackingInfo aLeft, TrackingInfo aRight )
        {
            int ret = CompareTrackingInfo( aLeft, aRight );
            //
            if	( iAscending == false )
            {
                ret *= -1;
            }
            //
            return ret;
        }
        #endregion

        #region New framework methods
        public abstract int CompareTrackingInfo( TrackingInfo aLeft, TrackingInfo aRight );
        #endregion

        #region Data members
        private readonly bool iAscending;
        #endregion
    }

    public class TrackingInfoSortBySymbolName : TrackingComparisonBase
    {
        #region Constructors & destructor
        public TrackingInfoSortBySymbolName( bool aAscending )
            : base( aAscending )
        {
        }
        #endregion

        #region TrackingComparisonBase Members
        public override int CompareTrackingInfo( TrackingInfo aLeft, TrackingInfo aRight )
        {
            return aLeft.Symbol.Name.CompareTo( aRight.Symbol.Name );
        }
        #endregion
    }

    public class TrackingInfoSortByAssociatedCellCount : TrackingComparisonBase
    {
        #region Constructors & destructor
        public TrackingInfoSortByAssociatedCellCount( bool aAscending )
            : base( aAscending )
        {
        }
        #endregion

        #region TrackingComparisonBase Members
        public override int CompareTrackingInfo( TrackingInfo aLeft, TrackingInfo aRight )
        {
            int ret = -1;
            //
            if	( aLeft.AssociatedCellCount == aRight.AssociatedCellCount )
            {
                ret = 0;
            }
            else if ( aLeft.AssociatedCellCount > aRight.AssociatedCellCount )
            {
                ret = 1;
            }
            //
            return ret;
        }
        #endregion
    }

    public class TrackingInfoSortByCount : TrackingComparisonBase
    {
        #region Constructors & destructor
        public TrackingInfoSortByCount( bool aAscending )
            : base( aAscending )
        {
        }
        #endregion

        #region TrackingComparisonBase Members
        public override int CompareTrackingInfo( TrackingInfo aLeft, TrackingInfo aRight )
        {
            int ret = -1;
            //
            if	( aLeft.Count == aRight.Count )
            {
                ret = 0;
            }
            else if ( aLeft.Count > aRight.Count )
            {
                ret = 1;
            }
            //
            return ret;
        }
        #endregion
    }

    public class TrackingInfoSortByPayloadLength : TrackingComparisonBase
    {
        #region Constructors & destructor
        public TrackingInfoSortByPayloadLength( bool aAscending )
            : base( aAscending )
        {
        }
        #endregion

        #region TrackingComparisonBase Members
        public override int CompareTrackingInfo( TrackingInfo aLeft, TrackingInfo aRight )
        {
            int ret = -1;
            //
            if	( aLeft.PayloadLength == aRight.PayloadLength )
            {
                ret = 0;
            }
            else if ( aLeft.PayloadLength > aRight.PayloadLength )
            {
                ret = 1;
            }
            //
            return ret;
        }
        #endregion
    }

    public class TrackingInfoSortByAssociatedMemory : TrackingComparisonBase
    {
        #region Constructors & destructor
        public TrackingInfoSortByAssociatedMemory( bool aAscending )
            : base( aAscending )
        {
        }
        #endregion

        #region TrackingComparisonBase Members
        public override int CompareTrackingInfo( TrackingInfo aLeft, TrackingInfo aRight )
        {
            int ret = 1;
            //
            if	( aLeft.AssociatedMemory < aRight.AssociatedMemory )
            {
                ret = -1;
            }
            else if ( aLeft.AssociatedMemory == aRight.AssociatedMemory )
            {
                ret = 0;
            }
            //
            return ret;
        }
        #endregion
    }
}
