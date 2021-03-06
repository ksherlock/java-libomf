/*
 * Created on Feb 17, 2006
 * Feb 17, 2006 1:47:25 PM
 */
package omf;

import omf.io.__OMF_Reader;
import omf.io.__OMF_Writer;

public class OMF_LConst extends OMF_Const
{
    
    public OMF_LConst(__OMF_Reader omf)
    {
        super(0xf2, omf);    
    }
    
    public OMF_LConst(byte[] data)
    {
        super(data);
    }
    public OMF_LConst(byte[] data, int length)
    {
        super(data, length);
    }


    @Override
    public void Save(__OMF_Writer out)
    {
        if (fLength == 0) return;
        out.Write8(0xf2);
        out.Write32(fLength);
        out.WriteBytes(fData, fLength);
    }

}
