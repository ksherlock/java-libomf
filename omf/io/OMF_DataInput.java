/*
 * Created on Dec 16, 2006
 * Dec 16, 2006 8:22:04 PM
 */
package omf.io;

import java.io.DataInput;
import java.io.IOException;

import omf.OMF_Segment;

public class OMF_DataInput implements __OMF_Reader
{
    public OMF_DataInput(DataInput input, OMF_Segment seg)
    {
        fNumsex = seg.NumberSex();
        fNumsize = seg.NumberLength();
        fLablen = seg.LabelLength();
        fOK = true;
        fVersion = seg.Version();
        
        fInput = input;
        if (fInput == null) fOK = false;
    }
    public int Version()
    {
        return fVersion;
    }

    public boolean IsOK()
    {
        return fOK;
    }

    public int Read8()
    {
        try
        {
            return fInput.readByte() & 0x00ff;
        }
        catch (IOException e)
        {
            fOK = false;
            return 0;
        }
    }

    public int Read16()
    {
        int a,b;
        try
        {
            a = fInput.readByte() & 0xff;
            b = fInput.readByte() & 0xff;
            
            if (fNumsex == 0)
                return a | (b << 8);
            else 
                return (a << 8) | b;
        }
        catch (IOException e)
        {
            fOK = false;
            return 0;
        }
    }

    public int Read24()
    {
        int a,b,c;
        try
        {
            a = fInput.readByte() & 0xff;
            b = fInput.readByte() & 0xff;
            c = fInput.readByte() & 0xff;
            
            if (fNumsex == 0)
                return a | (b << 8) | (c << 16);
            else 
                return (a << 16) | (b << 8) | c;
        }
        catch (IOException e)
        {
            fOK = false;
            return 0;
        }
    }

    public int Read32()
    {
        int a,b,c,d;
        try
        {
            a = fInput.readByte() & 0xff;
            b = fInput.readByte() & 0xff;
            c = fInput.readByte() & 0xff;
            d = fInput.readByte() & 0xff;
            
            if (fNumsex == 0)
                return a | (b << 8) | (c << 16) | (d << 24);
            else 
                return (a << 24) | (b << 16) | (c << 8) | d;
        }
        catch (IOException e)
        {
            fOK = false;
            return 0;
        }
    }

    public int ReadNumber()
    {
        switch(fNumsize)
        {
        case 1:
            return Read8();
        case 2:
            return Read16();
        case 3:
            return Read24();
        case 4:
            return Read32();
        default: 
            fOK = false;
            return 0;
        }
    }

    public String ReadString()
    {
        try {
           
            int len = fLablen;
            if (len == 0)
                len = fInput.readByte() & 0xff;
            if (len == 0) return "";
            byte[] data = new byte[len];
            fInput.readFully(data, 0, len);
            return new String(data);
        }
        catch (IOException e)
        {
            fOK = false;
            return "";
        }
    }

    public byte[] ReadBytes(int count)
    {
        byte[] out = new byte[count];
        
        try
        {
            fInput.readFully(out, 0, count);
        }
        catch (IOException e)
        {
            fOK = false;
        }
        
        return out;
    }

    private DataInput fInput;
    private int fNumsex;
    private int fNumsize;
    private int fLablen;
    private boolean fOK;
    private int fVersion;
}
