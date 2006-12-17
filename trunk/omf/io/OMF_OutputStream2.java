/*
 * Created on Dec 13, 2006
 * Dec 13, 2006 3:34:28 PM
 */
package omf.io;

import java.io.FileOutputStream;
import java.io.IOException;

import omf.OMF_Segment;

public class OMF_OutputStream2 implements __OMF_Writer
{
    private int fNumsex;
    private int fNumsize;
    private int fLablen;
    private boolean fOK;
    private int fVersion;
    private FileOutputStream fOut;
    
    public OMF_OutputStream2(OMF_Segment seg, FileOutputStream stream)
    {
        fNumsex = seg.NumberSex();
        fNumsize = seg.NumberLength();
        fLablen = seg.LabelLength();
        fOK = true;
        fVersion = seg.Version();
        
        fOut = stream;
    }
    public int Version()
    {
        return fVersion;
    }

    public boolean IsOK()
    {
        return fOK;
    }

    public void Write8(int n)
    {
        try
        {
            fOut.write(n);
        }
        catch (IOException e)
        {
            fOK = false;
        }
    }

    public void Write16(int n)
    {
        try
        {
            if (fNumsex == 0)
            {
                fOut.write(n & 0xff);
                fOut.write((n >> 8) & 0xff);
            }
            else
            {
                fOut.write((n >> 8) & 0xff);
                fOut.write(n & 0xff);
            }
        }
        catch (IOException e)
        {
            fOK = false;
        }
    }

    public void Write24(int n)
    {
        try
        {
            if (fNumsex == 0)
            {
                fOut.write(n & 0xff);
                fOut.write((n >> 8) & 0xff);
                fOut.write((n >> 16) & 0xff);
            }
            else
            {
                fOut.write((n >> 16) & 0xff);
                fOut.write((n >> 8) & 0xff);
                fOut.write(n & 0xff);
            }
        }
        catch (IOException e)
        {
            fOK = false;
        }
    }

    public void Write32(int n)
    {
        try
        {
            if (fNumsex == 0)
            {
                fOut.write(n & 0xff);
                fOut.write((n >> 8) & 0xff);
                fOut.write((n >> 16) & 0xff);
                fOut.write((n >> 24) & 0xff);
            }
            else
            {
                fOut.write((n >> 24) & 0xff);
                fOut.write((n >> 16) & 0xff);
                fOut.write((n >> 8) & 0xff);
                fOut.write(n & 0xff);
            }
        }
        catch (IOException e)
        {
            fOK = false;
        }
    }

    public void WriteNumber(int n)
    {
        switch (fNumsize)
        {
        case 1: Write8(n); break;
        case 2: Write16(n); break;
        case 3: Write24(n); break;
        case 4: Write32(n); break;
        default:
            fOK = false;
        }
    }

    public void WriteString(String s)
    {
        if (s == null) s = "";
        int length = s.length();

        if (fLablen == 0)
        {
            
            try
            {
                fOut.write(length);
                fOut.write(s.getBytes(),0, length);
            }
            catch (IOException e)
            {
                fOK = false;
            }
        }
        else
        {
            WriteString(s, fLablen);
        }
    }
    
    public void WriteString(String s, int len)
    {
        if (s == null)
            s = "";
        int length = s.length();

        try
        {
            if (length == len)
            {
                fOut.write(s.getBytes(), 0, len);
            }
            else if (length > len)
            {
                fOut.write(s.getBytes(), 0, len);
            }
            else
            {
                fOut.write(s.getBytes(), 0, length);
                for (int i = length; i < len; i++)
                    fOut.write(' ');
            }
        }
        catch (IOException e)
        {
            fOK = false;
        }
    }    

    public void WriteBytes(byte[] b)
    {
        if (b == null) return;
        
        try
        {
            fOut.write(b, 0, b.length);
        }
        catch (IOException e)
        {
            fOK = false;
        }
 
    }

    public void WriteBytes(byte[] b, int count)
    {
        if (b == null) return;
        try
        {
            fOut.write(b, 0, count);
        }
        catch (IOException e)
        {
            fOK = false;
        }   

    }

}
