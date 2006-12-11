/*
 * Created on Feb 13, 2006
 * Feb 13, 2006 11:46:59 PM
 */
package omf;

/*
 * $F7 SUPER - Super records contain a series of cRELOC, cINTERSEG and INTERSEG
 * records, compacted into a short, tabular form. The difference between a
 * compacted OMF file and an uncompacted OMF file is that compacted OMF files
 * use SUPER records to reduce space and cut down on load time. SUPER records
 * are not covered in this appendix. For details on the format of SUPER records,
 * see volume 2 of Apple IIGS GS/OS Reference.
 * 
 */
public class OMF_Super extends OMF_Opcode
{
    public static final int RELOC2 = 0;
    public static final int RELOC3 = 1;
    public static final int INTERSEG1 = 2;
    public static final int INTERSEG2 = 3;
    public static final int INTERSEG3 = 4;
    public static final int INTERSEG4 = 5;
    public static final int INTERSEG5 = 6;
    public static final int INTERSEG6 = 7;
    public static final int INTERSEG7 = 8;
    public static final int INTERSEG8 = 9;
    public static final int INTERSEG9 = 10;
    public static final int INTERSEG10 = 11;
    public static final int INTERSEG11 = 12;
    public static final int INTERSEG12 = 13;
    public static final int INTERSEG13 = 14;
    public static final int INTERSEG14 = 15;
    public static final int INTERSEG15 = 16;
    public static final int INTERSEG16 = 17;
    public static final int INTERSEG17 = 18;
    public static final int INTERSEG18 = 19;
    public static final int INTERSEG19 = 20;
    public static final int INTERSEG20 = 21;
    public static final int INTERSEG21 = 22;
    public static final int INTERSEG22 = 23;
    public static final int INTERSEG23 = 24;
    public static final int INTERSEG24 = 25;
    public static final int INTERSEG25 = 26;
    public static final int INTERSEG26 = 27;
    public static final int INTERSEG27 = 28;
    public static final int INTERSEG28 = 29;
    public static final int INTERSEG29 = 30;
    public static final int INTERSEG30 = 31;
    public static final int INTERSEG31 = 32;
    public static final int INTERSEG32 = 33;
    public static final int INTERSEG33 = 34;
    public static final int INTERSEG34 = 35;
    public static final int INTERSEG35 = 36;
    public static final int INTERSEG36 = 37;
    
    
    private int fLength;
    private int fType;
    private byte[] fData;
    public OMF_Super(__OMF_Reader omf)
    {
        super(0xf7);
        fLength = omf.Read32();
        fType = omf.Read8();
        fData = omf.ReadBytes(fLength - 1);
    }
    public int Length()
    {
        return fLength;
    }
    public byte[] Data()
    {
        return fData;
    }
    public int Type()
    {
        return fType;
    }
    
    @Override
    public int CodeSize()
    {
        return 0;
    }
    @Override
    public void Save(__OMF_Writer out)
    {
        out.Write8(fOpcode);
        out.Write32(fLength);
        out.Write8(fType);
        out.WriteBytes(fData);
        
    }
    
    /*
     * remap any segment numbers.
     * 
     */
    public boolean Remap(int[] array)
    {
        if (fType == RELOC2 || fType == RELOC3)
            return true;

        if (fType >= INTERSEG25 && fType <= INTERSEG36)
        {
            int osegnum = fType - 24;
            int nsegnum = array[osegnum];
            if (nsegnum > 12) return false;
            fType = 24 + nsegnum;
            return true;
        }
        
        if (fType >= INTERSEG13 && fType <= INTERSEG24)
        {
            int osegnum = fType - 12;
            int nsegnum = array[osegnum];
            if (nsegnum > 12) return false;
            fType = 12 + nsegnum;
            return true;           
        }
        if (fType == INTERSEG1)
        {
            // the segment number is stored in the LCONST record.
            return false;
        }
        if (fType >= INTERSEG2 && fType <= INTERSEG12)
        {
            // the segment number and file number are stored in the LCONST
            // record.
            return false;
        }
        
        // should never get here...
        return false;
    }
    
}
