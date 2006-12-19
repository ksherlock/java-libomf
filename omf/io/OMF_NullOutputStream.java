/*
 * This class doesn't do any writing; it merely 
 * calculates the number of bytes that would be written.
 */
package omf.io;

import omf.OMF_Segment;

public final class OMF_NullOutputStream implements __OMF_Writer
{
    private int fNumsize;
    private int fLablen;
    private int fVersion;
    private int fSize;
    
    public OMF_NullOutputStream(OMF_Segment omf)
    {
        fNumsize = omf.NumberLength();
        fLablen = omf.LabelLength();
        fVersion = omf.Version();
        
        fSize = 0;
    }

    public final int Size()
    {
        return fSize;
    }
    
    public final int Version()
    {
        return fVersion;
    }

    public final boolean IsOK()
    {
        return true;
    }

    public final void Write8(int n)
    {
        fSize++;
    }

    public final void Write16(int n)
    {
        fSize += 2;
    }

    public final void Write24(int n)
    {
        fSize += 3;
    }

    public final void Write32(int n)
    {
        fSize += 4;
    }

    public final void WriteNumber(int n)
    {
        fSize += fNumsize;
    }

    public final void WriteString(String s)
    {
        if (s == null) s = "";
        
        if (fLablen == 0) fSize += 1 + s.length();
        else fSize += fLablen;
    }
    public final void WriteString(String s, int length)
    {
        fSize += length;
    }

    public final void WriteBytes(byte[] b)
    {
        if (b != null) fSize += b.length;
    }

    public final void WriteBytes(byte[] b, int count)
    {
        if (b != null) fSize += count;

    }

}
