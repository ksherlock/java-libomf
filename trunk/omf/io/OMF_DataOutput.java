/*
 * Created on Dec 16, 2006
 * Dec 16, 2006 8:49:08 PM
 */
package omf.io;

import java.io.DataOutput;
import java.io.IOException;

import omf.OMF_Segment;

public class OMF_DataOutput implements __OMF_Writer
{
    public OMF_DataOutput(DataOutput output, OMF_Segment seg)
    {
        fNumsex = seg.NumberSex();
        fNumsize = seg.NumberLength();
        fLablen = seg.LabelLength();
        fOK = true;
        fVersion = seg.Version();
        
        fOutput= output;
        if (fOutput == null) fOK = false;
    }
    
    public OMF_DataOutput(DataOutput output)
    {
        fNumsex = 0;
        fNumsize = 4;
        fLablen = 0;
        fOK = true;
        fVersion = 2;
        
        fOutput= output;
        if (fOutput == null) fOK = false;
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
            fOutput.writeByte(n);
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
                fOutput.writeByte(n & 0xff);
                fOutput.writeByte((n >> 8) & 0xff);
            }
            else
            {
                fOutput.writeByte((n >> 8) & 0xff);  
                fOutput.writeByte(n & 0xff);                             
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
                fOutput.writeByte(n & 0xff);
                fOutput.writeByte((n >> 8) & 0xff);
                fOutput.writeByte((n >> 16) & 0xff);
            }
            else
            {
                fOutput.writeByte((n >> 16) & 0xff);
                fOutput.writeByte((n >> 8) & 0xff);  
                fOutput.writeByte(n & 0xff);                             
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
                fOutput.writeByte(n & 0xff);
                fOutput.writeByte((n >> 8) & 0xff);
                fOutput.writeByte((n >> 16) & 0xff);
                fOutput.writeByte((n >> 24) & 0xff);
            }
            else
            {
                fOutput.writeByte((n >> 24) & 0xff);
                fOutput.writeByte((n >> 16) & 0xff);
                fOutput.writeByte((n >> 8) & 0xff);  
                fOutput.writeByte(n & 0xff);                             
            }
        }
        catch (IOException e)
        {
            fOK = false;
        } 
    }

    public void WriteNumber(int n)
    {
        switch(fNumsize)
        {
        case 1:
            Write8(n);
            break;
        case 2:
            Write16(n);
            break;
        case 3:
            Write24(n);
            break;
        case 4:
            Write32(n);
            break;
        default: fOK = false;
        }
    }

    public void WriteString(String s) 
    {
        if (s == null) s = "";
        int length = s.length();

        try
        {
            if (fLablen == 0)
            {
                fOutput.writeByte(length);
                fOutput.write(s.getBytes(), 0, length);
            }
            else
            {
                WriteString(s, fLablen);
            }
        }
        catch (IOException e)
        {
            fOK = false;
        }       
    }
    public void WriteString(String s, int length)
    {
        if (s == null) s = "";
        int slength = s.length();
        int count = Math.min(length, slength);
        
        try
        {       
            fOutput.write(s.getBytes(), 0, count);
            for(; count < length; count++)
            {
                fOutput.writeByte(' ');
            }
        }
        catch (IOException e)
        {
            fOK = false;
        }        
    }

    public void WriteBytes(byte[] b)
    {
        try
        {
            fOutput.write(b);
        }
        catch (IOException e)
        {
            fOK = false;
        }
    }

    public void WriteBytes(byte[] b, int count)
    {
        try
        {
            fOutput.write(b, 0, count);
        }
        catch (IOException e)
        {
            fOK = false;
        }
    }
    
    private DataOutput fOutput;
    private int fNumsex;
    private int fNumsize;
    private int fLablen;
    private boolean fOK;
    private int fVersion;

}
