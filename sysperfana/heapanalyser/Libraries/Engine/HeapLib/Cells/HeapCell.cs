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
using System.Collections;
using System.Collections.Generic;
using System.Text;
using SymbianStructuresLib.Debug.Symbols;
using SymbianUtils.RawItems;
using SymbianUtils.Utilities;
using HeapLib.Array;
using HeapLib.Relationships;
using HeapLib.Cells.Descriptors;

namespace HeapLib.Cells
{
    public class HeapCell : IEnumerable<RawItem>, IEquatable<HeapCell>
    {
        #region Enumerations
        public enum TRegion
        {
            EHeader = 0,
            EPayload,
            EBeforeCell,
            EAfterCell
        }

        public enum TType : byte
        {
            EAllocated = 0,
            EFree
        }

        public enum TBuildType
        {
            ERelease = 0,
            EDebug = 1
        }

        public enum TDescriptorType
        {
            // These are Symbian's descriptor types
            EBufC = 0,
            EPtrC,
            EPtr,
            EBuf,
            EBufCPtr,

            // This are specific to this app
            ENullTerminatedString,
            EUnknown,
        }
        #endregion

        #region Constructors & destructor
        public HeapCell()
        {
            iRelationshipManager = new RelationshipManager(this);
        }

        internal HeapCell(HeapCell aCell)
            : this()
        {
            iTag = aCell.Tag;
            iAddress = aCell.Address;
            iType = aCell.Type;
            iFlags = aCell.iFlags;
            iSymbol[0] = aCell.iSymbol[0];
            iSymbol[1] = aCell.iSymbol[1];
            iSymbol[2] = aCell.iSymbol[2];
            iRawItems = aCell.iRawItems;
            iDescriptorInfo = aCell.iDescriptorInfo;
            iHeaderRawItems = aCell.iHeaderRawItems;
            iLength = aCell.iLength;
            //
            iRelationshipManager = aCell.RelationshipManager;
            iChecksum = new CRC32Checksum(aCell.Checksum);
        }

        internal HeapCell(uint aAddress, uint aLength, TType aType)
            : this()
        {
            iAddress = aAddress;
            iType = aType;
            //
            iHeaderRawItems.Clear();
            AddRawItemHeader(new RawItem(aAddress, aLength));
        }
        #endregion

        #region Constants
        public const uint KHeapCellSizeAllocatedUREL = 4;   // Just length
        public const uint KHeapCellSizeAllocatedUDEB = 12;  // Length + Allocation Number + Nesting Level
        public const uint KHeapCellSizeFree = 8;            // Length + Pointer to next free cell
        #endregion

        #region API
        public void AddRawItem(RawItem aItem)
        {
            RawItems.Add(aItem);
            //
            byte[] dataArray = aItem.DataArray;
            iChecksum.Checksum(dataArray);
        }

        public void AddRawItemHeader(RawItem aItem)
        {
            // Not checksummed
            iHeaderRawItems.Add(aItem);
            if (iHeaderRawItems.Count == 1)
            {
                iLength = iHeaderRawItems[0].Data;
            }
        }

        public uint Remainder(uint aAddress)
        {
            uint ret = (Address + Length) - aAddress;
            return ret;
        }

        public static uint CellHeaderSize(TType aType)
        {
            uint ret = AllocatedCellHeaderSize;
            if (aType == TType.EFree)
            {
                ret = FreeCellHeaderSize;
            }
            return ret;
        }

        public static uint AllocatedCellSizeByBuildType(TBuildType aType)
        {
            uint size = 0;
            //
            switch (aType)
            {
                case TBuildType.EDebug:
                    size = KHeapCellSizeAllocatedUDEB;
                    break;
                case TBuildType.ERelease:
                    size = KHeapCellSizeAllocatedUREL;
                    break;
            }
            //
            return size;
        }

        public TRegion RegionForAddress(uint aAddress)
        {
            uint address = Address;
            //
            TRegion ret = TRegion.EBeforeCell;
            //
            if (aAddress < address)
            {
                ret = TRegion.EBeforeCell;
            }
            else
            {
                uint endAddress = EndAddress;
                //
                if (aAddress > endAddress)
                {
                    ret = TRegion.EAfterCell;
                }
                else
                {
                    uint startOfPayload = StartOfPayloadAddress;
                    //
                    if (aAddress >= startOfPayload)
                    {
                        ret = TRegion.EPayload;
                    }
                    else
                    {
                        ret = TRegion.EHeader;
                    }
                }
            }
            //
            return ret;
        }

        public TRegion RegionForAddress(uint aAddress, out int aRemainingUntilNextBoundary)
        {
            TRegion region = RegionForAddress(aAddress);
            //
            switch (region)
            {
                default:
                    aRemainingUntilNextBoundary = -1;
                    break;
                case TRegion.EHeader:
                case TRegion.EPayload:
                    if (region == TRegion.EHeader)
                    {
                        uint startOfPayload = StartOfPayloadAddress;
                        aRemainingUntilNextBoundary = (int)(startOfPayload - aAddress);
                    }
                    else
                    {
                        uint endAddress = EndAddress;
                        aRemainingUntilNextBoundary = (int)(endAddress - aAddress + 1);
                    }
                    break;
            }
            //
            return region;
        }

        public bool IsIdentical(HeapCell aCell)
        {
            bool identical = false;
            //
            if (aCell.Length == this.Length &&
                 aCell.PossibleVTableAddress == this.PossibleVTableAddress &&
                 aCell.AllocationNumber == this.AllocationNumber &&
                 aCell.NestingLevel == this.NestingLevel &&
                 aCell.RawItems.Count == this.RawItems.Count)
            {
                identical = (aCell.Checksum == this.Checksum);
            }
            //
            return identical;
        }

        internal void ConstructionComplete(Statistics.HeapStatistics aStats)
        {
            if (Type == TType.EAllocated)
            {
                iDescriptorInfo = Descriptors.DescriptorAlgorithmManager.DescriptorInfo(this, aStats);
            }
        }
        #endregion

        #region Properties
        public HeapCell.TType Type
        {
            get { return iType; }
            set { iType = value; }
        }

        public string TypeString
        {
            get
            {
                string ret = string.Empty;
                //
                switch (Type)
                {
                    case TType.EAllocated:
                        ret = "Allocated";
                        break;
                    case TType.EFree:
                        ret = "Free";
                        break;
                }
                //
                return ret;
            }
        }

        // <summary>
        // The index is the unique number assigned to each cell as it is
        // added to the heap data for a given heap. This operation is 
        // performed by the reconstructor during parsing.
        // </summary>
        public uint Index
        {
            get { return iIndex; }
            set { iIndex = value; }
        }

        public uint Address
        {
            get { return iAddress; }
            set { iAddress = value; }
        }

        public uint StartOfPayloadAddress
        {
            get
            {
                uint ret = Address;
                //
                if (Type == TType.EAllocated)
                {
                    ret += HeaderSize;
                }
                else if (Type == TType.EFree)
                {
                    ret += KHeapCellSizeFree;
                }
                else
                {
                    System.Diagnostics.Debug.Assert(false);
                }
                //
                return ret;
            }
        }

        public uint EndAddress
        {
            get { return iAddress + iLength - 1; }
        }

        public uint Length
        {
            // Optimisation: we cache the length when adding the first
            // header raw item to avoid having to repeatedly access
            // the raw items array during initial heap preparation.
            get { return iLength; }
        }

        public uint NestingLevel
        {
            get
            {
                // Only applicable to allocated cells in debug builds
                uint ret = 0;
                //
                if (Type == TType.EAllocated && IsDebugAllocator)
                {
                    System.Diagnostics.Debug.Assert(HeaderRawItems.Count >= 2);
                    RawItem item = HeaderRawItems[1]; // Always 2nd raw item in header
                    ret = item.Data;
                }
                //
                return ret;
            }
        }

        public uint AllocationNumber
        {
            get
            {
                // Only applicable to allocated cells in debug builds
                uint ret = 0;
                //
                if (Type == TType.EAllocated && IsDebugAllocator)
                {
                    System.Diagnostics.Debug.Assert(HeaderRawItems.Count >= 3);
                    RawItem item = HeaderRawItems[2]; // Always 3rd raw item in header
                    ret = item.Data;
                }
                //
                return ret;
            }
        }

        public uint PossibleVTableAddress
        {
            get
            {
                // This is always the first raw item
                uint ret = 0;
                //
                if (RawItems.Count > 0)
                {
                    ret = RawItems[0].Data;
                }
                //
                return ret;
            }
        }

        public Symbol Symbol
        {
            get { return iSymbol[0]; }
            set { iSymbol[0] = value; }
        }

        public Symbol Symbol2
        {
            get { return iSymbol[1]; }
            set { iSymbol[1] = value; }
        }

        public Symbol Symbol3
        {
            get { return iSymbol[2]; }
            set { iSymbol[2] = value; }
        }

        public string SymbolString
        {
            get
            {
                return SymbolStringRef(false);
            }
        }

        private string SymbolStringRef(bool reference)
        {
            StringBuilder ret = new StringBuilder();

            if (Type == TType.EAllocated)
            {
                if (Symbol != null)
                {
                    ret.Append(Symbol.NameWithoutVTablePrefix);
                }
                else if (IsDescriptor)
                {
                    ret.Append("[Descriptor] " + DescriptorTextBeautified);
                }
            }
            else if (Type == TType.EFree)
            {
                ret.Append("[Free]");
                if (Symbol != null)
                {
                    ret.Append(" ");
                    ret.Append(Symbol.NameWithoutVTablePrefix);
                }
            }

            // If no text as yet identified, then return [Unknown], if there is not unique referrer.
            // If the referrer is unique, or if all referrers are of the same known type,
            // instead of [Unknown], you can see [Part of XXX], where XXX is the type of the referrer. 
            if (ret.Length == 0)
            {
                if (!reference)
                {
                    if (iRelationshipManager.ReferencedByUnique != null)
                    {
                        string symbolString = iRelationshipManager.ReferencedByUnique.SymbolStringRef(true);

                        if (!symbolString.Equals("[Unknown]"))
                        {
                            ret.Append("[Part of ");
                            ret.Append(symbolString);
                            ret.Append("]");
                        }
                        else
                        {
                            ret.Append("[Unknown]");
                        }
                    }
                    else
                    {
                        ret.Append("[Unknown]");
                    }
                }
                else
                {
                    ret.Append("[Unknown]");
                }
            }
            return ret.ToString();
        }

        public string SymbolStringWithoutDescriptorPrefix
        {
            get
            {
                string ret = SymbolString;
                //
                if (IsDescriptor)
                {
                    ret = "[" + DescriptorLength.ToString("d4") + "] " + DescriptorTextBeautifiedWithoutLength;
                }
                //
                return ret;
            }
        }

        public uint HeaderSize
        {
            get
            {
                uint size = 0;
                //
                switch (Type)
                {
                    case TType.EAllocated:
                        size = HeapCell.AllocatedCellHeaderSize;
                        break;
                    case TType.EFree:
                        size = KHeapCellSizeFree;
                        break;
                }
                //
                return size;
            }
        }

        public uint PayloadLength
        {
            get { return (EndAddress - StartOfPayloadAddress) + 1; }
        }

        public RawItem this[uint aAddress]
        {
            get
            {
                RawItem ret = null;
                //
                TRegion region = RegionForAddress(aAddress);
                if (region == TRegion.EPayload || region == TRegion.EHeader)
                {
                    if (region == TRegion.EHeader)
                    {
                        uint offset = aAddress - Address;
                        int index = System.Convert.ToInt32(offset / RawItem.KSizeOfOneRawItemInBytes);
                        //
                        if (index < 0 || index >= HeaderRawItems.Count)
                        {
                            throw new ArgumentException("Address 0x" + aAddress.ToString("x8") + " is beyond this cell's header");
                        }
                        //
                        ret = HeaderRawItems[index];
                    }
                    else
                    {
                        // Payload
                        uint offset = aAddress - StartOfPayloadAddress;
                        int index = System.Convert.ToInt32(offset / RawItem.KSizeOfOneRawItemInBytes);
                        //
                        if (index < 0 || index >= RawItems.Count)
                        {
                            throw new ArgumentException("Address 0x" + aAddress.ToString("x8") + " is beyond this cell's payload");
                        }
                        //
                        ret = this[index];
                    }
                }
                else
                {
                    throw new ArgumentException("Address 0x" + aAddress.ToString("x8") + " is not within this cell's extent");
                }
                return ret;
            }
        }

        public RawItem this[int aIndex]
        {
            get
            {
                return iRawItems[aIndex];
            }
        }

        public RawItemCollection RawItems
        {
            get { return iRawItems; }
        }

        public RawItemCollection HeaderRawItems
        {
            get { return iHeaderRawItems; }
        }

        public long CombinedLinkedCellPayloadLengths
        {
            get
            {
                int depth = 0;
                long ret = DoGetCombinedLinkedCellPayloadLengths(ref depth);
                return ret;
            }
        }

        public long PayloadLengthIncludingLinkedCells
        {
            get
            {
                long ret = PayloadLength;
                ret += RelationshipManager.PayloadLengthOfEmbeddedCells;
                //
                return ret;
            }
        }

        public object Tag
        {
            get { return iTag; }
            set { iTag = value; }
        }

        public uint Checksum
        {
            get { return iChecksum.Value; }
        }

        public bool IsUnknown
        {
            get { return Symbol == null && Type == TType.EAllocated; }
        }
        #endregion

        #region Descriptor related functionality
        public TDescriptorType DescriptorType
        {
            get
            {
                TDescriptorType ret = TDescriptorType.EUnknown;
                //
                if (IsDescriptor && iDescriptorInfo != null)
                {
                    ret = iDescriptorInfo.Type;
                }
                //
                return ret;
            }
        }

        public bool IsDescriptorUnicode
        {
            get
            {
                bool ret = false;
                //
                if (IsDescriptor && iDescriptorInfo != null)
                {
                    ret = iDescriptorInfo.IsUnicode;
                }
                //
                return ret;
            }
        }

        public bool IsDescriptor
        {
            get
            {
                return (iDescriptorInfo != null);
            }
        }

        public int DescriptorLength
        {
            get
            {
                int length = 0;
                //
                if (IsDescriptor && iDescriptorInfo != null)
                {
                    length = iDescriptorInfo.Length;
                }
                //
                return length;
            }
        }

        public string DescriptorText
        {
            get
            {
                string ret = string.Empty;
                //
                if (IsDescriptor && iDescriptorInfo != null)
                {
                    ret = iDescriptorInfo.Text;
                }
                //
                return ret;
            }
        }

        public string DescriptorTextBeautifiedWithoutLength
        {
            get
            {
                StringBuilder ret = new StringBuilder();
                //
                string des = DescriptorText;
                if (des.Length > KMaxSymbolStringDescriptorLength)
                {
                    ret.Append(" \"");
                    ret.Append(des.Substring(0, KMaxSymbolStringDescriptorLength));
                    ret.Append("...\"");
                }
                else
                {
                    ret.Append(" \"");
                    ret.Append(des);
                    ret.Append("\"");
                }
                //
                return ret.ToString();
            }
        }

        public string DescriptorTextBeautified
        {
            get
            {
                StringBuilder ret = new StringBuilder();
                //
                ret.Append("{");
                ret.Append(DescriptorLength.ToString("d4"));
                ret.Append("}");
                //
                ret.Append(DescriptorTextBeautifiedWithoutLength);
                //
                return ret.ToString();
            }
        }
        #endregion

        #region Static properties
        public static bool IsDebugAllocator
        {
            get
            {
                return AllocatedCellHeaderSize == AllocatedCellSizeByBuildType(TBuildType.EDebug);
            }
        }

        public static uint FreeCellHeaderSize
        {
            get { return KFreeCellHeaderSize; }
        }

        public static uint AllocatedCellHeaderSize
        {
            get { return iAllocatedCellHeaderSize; }
            set { iAllocatedCellHeaderSize = value; }
        }
        #endregion

        #region Relationships
        public RelationshipManager RelationshipManager
        {
            get { return iRelationshipManager; }
        }
        #endregion

        #region IEnumerable Members
        IEnumerator IEnumerable.GetEnumerator()
        {
            return new HeapCellRawItemEnumerator(this);
        }

        IEnumerator<RawItem> IEnumerable<RawItem>.GetEnumerator()
        {
            return new HeapCellRawItemEnumerator(this);
        }
        #endregion

        #region IEquatable<HeapCell> Members
        public bool Equals(HeapCell aOther)
        {
            bool ret = (aOther.Address == this.Address);
            return ret;
        }
        #endregion

        #region Operators
        public static bool operator ==(HeapCell aLeft, HeapCell aRight)
        {
            bool ret = false;

            // If both are null, or both are same instance, return true.
            if (System.Object.ReferenceEquals(aLeft, aRight))
            {
                ret = true;
            }
            else if (((object)aLeft == null) || ((object)aRight == null))
            {
                // If one is null, but not both, return false.
                ret = false;
            }
            else
            {
                // Return true if the fields match:
                ret = (aLeft.Address == aRight.Address);
            }
            //
            return ret;
        }

        public static bool operator !=(HeapCell aLeft, HeapCell aRight)
        {
            return !(aLeft == aRight);
        }
        #endregion

        #region From System.Object
        public override int GetHashCode()
        {
            return Address.GetHashCode();
        }

        public override bool Equals(object aObject)
        {
            bool ret = false;
            //
            if (aObject is HeapCell)
            {
                HeapCell otherCell = (HeapCell)aObject;
                ret = Equals(otherCell);
            }
            //
            return ret;
        }

        public override string ToString()
        {
            string ret = "[0x" + Address.ToString("x8") + " " + TypeString + "]";
            return ret;
        }

        public string ToStringExtended()
        {
            StringBuilder ret = new StringBuilder();

            // Type
            if (Type == HeapCell.TType.EAllocated)
            {
                if (IsDescriptor)
                {
                    ret.Append("[D]");
                }
                else
                {
                    ret.Append("[A]");
                }
            }
            else
            {
                ret.Append("[F]");
            }

            // Address
            ret.Append(" 0x" + Address.ToString("x8"));

            // Symbol (if present)
            if (Symbol != null)
            {
                ret.Append(" - " + SymbolString);
            }
            else if (IsDescriptor)
            {
                ret.Append(" - " + DescriptorTextBeautified);
            }

            return ret.ToString();
        }
        #endregion

        #region Internal flags
        [Flags]
        private enum TFlags
        {
            EFlagsNone = 0,
            EFlagsInCombiningCheck = 1
        }
        #endregion

        #region Internal constants
        private const int KMaxRecursiveLinkedCellDepth = 5; // levels
        private const int KMaxSymbolStringDescriptorLength = 128;
        private const int KFreeCellHeaderSize = 8; // always
        #endregion

        #region Internal methods
        private long DoGetCombinedLinkedCellPayloadLengths(ref int aDepth)
        {
            long ret = PayloadLength;
            //
            if (aDepth <= KMaxRecursiveLinkedCellDepth)
            {
                iFlags |= TFlags.EFlagsInCombiningCheck;
                //
                foreach (RelationshipInfo relInfo in RelationshipManager.EmbeddedReferencesTo)
                {
                    HeapCell linkedCell = relInfo.ToCell;
                    bool isInLinkCheck = ((linkedCell.iFlags & TFlags.EFlagsInCombiningCheck) == TFlags.EFlagsInCombiningCheck);
                    if (isInLinkCheck == false)
                    {
                        ++aDepth;
                        ret += linkedCell.DoGetCombinedLinkedCellPayloadLengths(ref aDepth);
                    }
                    else
                    {
                    }
                }
                //
                iFlags &= ~TFlags.EFlagsInCombiningCheck;
            }
            //
            return ret;
        }
        #endregion

        #region Data members
        private static uint iAllocatedCellHeaderSize = 0;
        private object iTag;
        private uint iIndex;
        private uint iAddress;
        private uint iLength;
        private TType iType = TType.EAllocated;
        private TFlags iFlags = TFlags.EFlagsNone;
        private Symbol[] iSymbol = new Symbol[3] { null, null, null };
        private CRC32Checksum iChecksum = new CRC32Checksum();
        private DescriptorInfo iDescriptorInfo = null;
        private RawItemCollection iRawItems = new RawItemCollection();
        private RawItemCollection iHeaderRawItems = new RawItemCollection();
        private readonly RelationshipManager iRelationshipManager;
        #endregion
    }
}