/*
 * OMF.java
 *
 * Created on December 21, 2005, 4:29 PM
 */

package omf;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import omf.io.OMF_DataOutput;
import omf.io.OMF_InputStream;
import omf.io.OMF_OutputStream;
import omf.io.__OMF_Writer;

/**
 *
 * @author Kelvin
 */
public class OMF_Segment {
            
    ArrayList<OMF_Opcode> fOpcodes;
    
    private boolean fError;
    private int fFile;
    
    private boolean fReloc;
    
    
    protected int fNumlen;
    protected int fNumsex;
    protected int fAlignment;
    protected int fLablen;
    protected int fVersion;
    protected int fBytecount;
    protected int fResspace;
    protected int fLength;
    protected int fType;
    protected int fKind;
    protected int fBanksize;
    protected int fOrg;
    protected int fLCBank;
    protected int fSegnum;
    protected int fEntry;
    protected String fLoadname;
    protected String fSegname;
    
    
    /** Creates a new instance of OMF */
    public OMF_Segment(InputStream io) {
    	
        this();

    	byte[] tmp;
    	int i;
    	
    	
    	
    	try {
    		// bytecount
    		for (i = 0; i < 4; i++)
    		{   			
    			fBytecount = fBytecount << 8 | io.read();   			
    		}
    		
    		// reserved space
    		for (i = 0; i < 4; i++)
    		{   			
    			fResspace = fResspace << 8 | io.read();   			
    		}
    		// length
    		for (i = 0; i < 4; i++)
    		{   			
    			fLength = fLength << 8 | io.read();   			
    		}
    		fType = io.read();
    		fLablen = io.read();
    		fNumlen = io.read();
    		fVersion = io.read();
    		
    		// bank size
    		for (i = 0; i < 4; i++)
    		{   			
    			fBanksize = fBanksize << 8 | io.read();   			
    		}
    		// follwing fields vary by version.
    		int offset = 0;
    		if (fVersion == 0)
    		{
        		// org
        		for (i = 0; i < 4; i++)
        		{   			
        			fOrg = fOrg << 8 | io.read();   			
        		}
        		// alignment
        		for (i = 0; i < 4; i++)
        		{   			
        			fAlignment = fAlignment << 8 | io.read();   			
        		}
        		fNumsex = io.read();
        		io.skip(7);
        		
        		offset = 0x24;
        		//segname
        		i = fLablen;
        		if (i == 0)
        		{
        			i = io.read();
        			offset++;
        		}
        		
        		tmp = new byte[i];
        		io.read(tmp);
        		fSegname = new String(tmp);
        		offset += i;
    		}
    		else if (fVersion == 1 || fVersion == 2)
    		{
    			if (fVersion == 2)
    			{
    				fKind = (short) (io.read() << 8 | io.read());
    				io.skip(2);
    			}
    			else
    			{
    				io.skip(4);		// unused;
    			}
    			
        		// org
        		for (i = 0; i < 4; i++)
        		{   			
        			fOrg = fOrg << 8 | io.read();   			
        		}
        		// alignment
        		for (i = 0; i < 4; i++)
        		{   			
        			fAlignment = fAlignment << 8 | io.read();   			
        		}
        		fNumsex = io.read();
        		fLCBank = io.read();
        		
        		// segment number - 16 bit 			
        		fSegnum = io.read() << 8 | io.read();   			

        		// entry - 32 bit
        		for (i = 0; i < 4; i++)
        		{   			
        			fEntry = fEntry << 8 | io.read();   			
        		}
        		short dispname;
        		short dispdata;
        		
        		// dispname number - 16 bit 			
        		dispname = (short) (io.read() << 8 | io.read());   			
      		
        		// dispdata - 16 bit
        		dispdata = (short) (io.read() << 8 | io.read());
        		
        		if (fNumsex == 0)
        		{
        			dispdata = Short.reverseBytes(dispdata);
        			dispname = Short.reverseBytes(dispname);
        		}
        		
        		// loadname 
        		offset = 0x2c;
        		if (dispname > offset)
        		{
        			io.skip(dispname - offset);
        		}
        		offset = dispname;
        		tmp = new byte[10];
        		io.read(tmp);
        		offset += 10;
        		fLoadname = new String(tmp);
        		// segname - variable;
        		if (fLablen == 0)
        		{
        			i = io.read();
        			offset++;
        		}
        		else i = fLablen;
                if (i == 0)
                {
                    fSegname = "";
                }
                else
                {
                    tmp = new byte[i];
                    io.read(tmp);
                    offset += i;
                    fSegname = new String(tmp);
                }
        		
        		// read the actual data....
        		if (dispdata > offset)
        		{
        			io.skip(dispdata - offset);
        		}
        		offset = dispdata;
    		}
    		else
    		{
    			fError = true;
    		}
    		
    		

    		if (fNumsex == 0)
    		{
    			fBytecount = Integer.reverseBytes(fBytecount);
    			fResspace = Integer.reverseBytes(fResspace);
    			fLength = Integer.reverseBytes(fLength);
    			fBanksize = Integer.reverseBytes(fBanksize);
    			fOrg = Integer.reverseBytes(fOrg);
    			fAlignment = Integer.reverseBytes(fAlignment);
    			fEntry = Integer.reverseBytes(fEntry);
    			
    			fKind = Short.reverseBytes((short)fKind);
                fSegnum = Short.reverseBytes((short)fSegnum);
    		}
  
    		if (fVersion < 2)
    		{
    			fBytecount *= 512;
                
                // convert the TYPE to Kind.
                
                fKind = fType & 0x0f;
                int attr = 0;
                if (fKind == OMF.TYPE_ABSBANK)
                {
                    attr |= OMF.KIND_ABSBANK;
                }
                // bits 5-7 can be shifted to bits 13-15
                attr |= ((fType & 0xe0) << 8);
                fKind |= attr;
    		}    		
    		
    		tmp = new byte[fBytecount - offset];
    		io.read(tmp);
            this.ParseOpcodes(tmp);
   		
    		
    	} catch (IOException e) {
    		fError = true;
    	}    	
    	
    	
    }
    
    public OMF_Segment(DataInput io) {
        
        this();

        byte[] tmp;
        int i;
        
        
        
        try {
            fBytecount = io.readInt();
            fResspace = io.readInt();
            fLength = io.readInt();

            fType = io.readUnsignedByte();
            fLablen = io.readUnsignedByte();
            fNumlen = io.readUnsignedByte();
            fVersion = io.readUnsignedByte();
            
            fBanksize = io.readInt();

            // follwing fields vary by version.
            int offset = 0;
            if (fVersion == 0)
            {
                fOrg = io.readInt();
                fAlignment = io.readInt();

                fNumsex = io.readUnsignedByte();
                io.skipBytes(7);              
                offset = 0x24;
                //segname
                i = fLablen;
                if (i == 0)
                {
                    i = io.readUnsignedByte();
                    offset++;
                }
                
                tmp = new byte[i];
                io.readFully(tmp);
                fSegname = new String(tmp);
                offset += i;
            }
            else if (fVersion == 1 || fVersion == 2)
            {
                if (fVersion == 2)
                {
                    fKind = io.readUnsignedShort();
                    io.skipBytes(2);
                }
                else
                {
                    io.skipBytes(4);     // unused;
                }
                
                fOrg = io.readInt();
                fAlignment = io.readInt();

                fNumsex = io.readUnsignedByte();
                fLCBank = io.readUnsignedByte();
                
                fSegnum = io.readUnsignedShort();
                fEntry = io.readInt();

                short dispname;
                short dispdata;
                
                // dispname number - 16 bit             
                dispname = (short)io.readUnsignedShort();
            
                // dispdata - 16 bit
                dispdata = (short)io.readUnsignedShort();
                
                if (fNumsex == 0)
                {
                    dispdata = Short.reverseBytes(dispdata);
                    dispname = Short.reverseBytes(dispname);
                }
                
                // loadname 
                offset = 0x2c;
                if (dispname > offset)
                {
                    io.skipBytes(dispname - offset);
                }
                offset = dispname;
                tmp = new byte[10];
                io.readFully(tmp);
                offset += 10;
                fLoadname = new String(tmp);
                // segname - variable;
                if (fLablen == 0)
                {
                    i = io.readUnsignedByte();
                    offset++;
                }
                else i = fLablen;
                if (i == 0)
                {
                    fSegname = "";
                }
                else
                {
                    tmp = new byte[i];
                    io.readFully(tmp);
                    offset += i;
                    fSegname = new String(tmp);
                }
                
                // read the actual data....
                if (dispdata > offset)
                {
                    io.skipBytes(dispdata - offset);
                }
                offset = dispdata;
            }
            else
            {
                fError = true;
            }
            
            

            if (fNumsex == 0)
            {
                fBytecount = Integer.reverseBytes(fBytecount);
                fResspace = Integer.reverseBytes(fResspace);
                fLength = Integer.reverseBytes(fLength);
                fBanksize = Integer.reverseBytes(fBanksize);
                fOrg = Integer.reverseBytes(fOrg);
                fAlignment = Integer.reverseBytes(fAlignment);
                fEntry = Integer.reverseBytes(fEntry);
                
                fKind = Short.reverseBytes((short)fKind);
                fSegnum = Short.reverseBytes((short)fSegnum);
            }
  
            if (fVersion < 2)
            {
                fBytecount *= 512;
                
                // convert the TYPE to Kind.
                
                fKind = fType & 0x0f;
                int attr = 0;
                if (fKind == OMF.TYPE_ABSBANK)
                {
                    attr |= OMF.KIND_ABSBANK;
                }
                // bits 5-7 can be shifted to bits 13-15
                attr |= ((fType & 0xe0) << 8);
                fKind |= attr;
            }           
            
            tmp = new byte[fBytecount - offset];
            io.readFully(tmp);
            this.ParseOpcodes(tmp);
        
            
        } catch (IOException e) {
            fError = true;
        }       
        
        
    }    
    
    
    public OMF_Segment()
    {
    	fLength = 0;
    	fBytecount = 0;
    	fLablen = 0;
    	fNumlen = 4;
    	fNumsex = 0;
    	fVersion = 2;
    	fResspace = 0;
    	fType = 0;
    	fKind = 0;
    	fBanksize = 0x010000;
    	fAlignment = 0;
    	fOrg = 0;
    	fLCBank = 0;
    	fSegnum = 0;
    	fEntry = 0;
    	fLoadname = "";
    	fSegname = "";

        fError = false;
        fFile = 1;
        fOpcodes = new ArrayList<OMF_Opcode>();
        
        fReloc = false;
        
    }
    
    private void ParseOpcodes(byte[] data)
    {
        OMF_InputStream in = new OMF_InputStream(data, this);
        boolean done = false;
        
        while (!done)
        {
            if (in.available() == 0) return;
            
            int op = in.Read8();
           
            
            OMF_Opcode omf = null;
            switch(op)
            {
            case OMF.OMF_EOF:
                omf = new OMF_Eof();
                done = true;
                //apple's makelib inserts 4 extra bytes
                // past the omf-eof, so that should be 
                // ignored.
                break;
                
            case OMF.OMF_ALIGN:
                omf = new OMF_Align(in);
                break;
                
            case OMF.OMF_ORG:
                omf = new OMF_Org(in);
                break;
                
            case OMF.OMF_RELOC:
                fReloc = true;
                omf = new OMF_Reloc(in);
                break;
                
            case OMF.OMF_CRELOC:
                fReloc = true;
                omf = new OMF_CReloc(in);
                break;
                
            case OMF.OMF_INTERSEG:
                fReloc = true;
                omf = new OMF_Interseg(in);
                break;
                
            case OMF.OMF_CINTERSEG:
                fReloc = true;
                omf = new OMF_CInterseg(in);
                break; 
                
            case OMF.OMF_STRONG:
                omf = new OMF_Strong(in);
                break;
                
            case OMF.OMF_MEM:
                omf = new OMF_Mem(in);
                break;
                
            case OMF.OMF_EXPR:
            case OMF.OMF_ZPEXPR:
            case OMF.OMF_BKEXPR:
            case OMF.OMF_LEXPR:
                omf = new OMF_Expr(op, in);
                break;
                
            case OMF.OMF_RELEXPR:
                omf = new OMF_RelExpr(in);
                break;
                
            case OMF.OMF_USING:
                omf = new OMF_Using(in);
                break;
                
            case OMF.OMF_LOCAL:
            case OMF.OMF_GLOBAL:
                omf = new OMF_Local(op, in);
                break;
                
            case OMF.OMF_EQU:
            case OMF.OMF_GEQU:
                omf = new OMF_Equ(op, in);
                break;
                
            case OMF.OMF_DS:
                omf = new OMF_DS(in);
                break;
                
            case OMF.OMF_LCONST:
                omf = new OMF_LConst(in);
                break;
                
            case OMF.OMF_ENTRY:
                omf = new OMF_Entry(in);
                break;
                
            case OMF.OMF_SUPER:
                fReloc = true;
                omf = new OMF_Super(in);
                break;
                
            default:
                if (op >= 0x01 && op <= 0xdf)
                {
                    omf = new OMF_Const(op, in);
                }
                else
                {
                    System.out.println("Unknown opcode " 
                            + op);  
                    fError = true;
                }          
            }
            if (!in.IsOK())
            {
                fError = true;
                return;
            }
            fOpcodes.add(omf);
        } // while !done   	
    }
    
    
    
    
    public boolean Save(FileOutputStream io)
    {
        OMF_OutputStream out = new OMF_OutputStream(this);
        OMF_OutputStream header = new OMF_OutputStream(this);
        
        int length = 0;
        int offset = 0;
        
        if (fVersion < 2) return false; // NO.
        
        for (Iterator<OMF_Opcode> iter = fOpcodes.iterator(); iter.hasNext(); )
        {
            OMF_Opcode op = iter.next();
            length += op.CodeSize();
            op.Save(out);
        }
        //
        
        if (fSegname.length() == 0) fSegname = "          ";
        if (fLoadname.length() == 0) fLoadname = "          ";
        this.fLength = length + this.fResspace;
        offset = 0x2c + 10;
        if (fLablen == 0)
        {
            offset += 1 + fSegname.length();
        }
        else offset += fLablen;
        
        this.fBytecount = out.size() + offset;

        // write the header...
        header.Write32(fBytecount);
        header.Write32(fResspace);
        header.Write32(fLength);
        header.Write8(0);
        header.Write8(fLablen);
        header.Write8(fNumlen);
        header.Write8(fVersion);
        header.Write32(fBanksize);
        header.Write16(fKind);
        header.Write16(0);
        header.Write32(fOrg);
        header.Write32(fAlignment);
        header.Write8(fNumsex);
        header.Write8(0);
        header.Write16(fSegnum);
        header.Write32(fEntry);
        header.Write16(0x2c);
        header.Write16(offset);
        header.WriteString(fLoadname, 10);
        header.WriteString(fSegname);
        

        try
        {
            header.writeTo(io);
            out.writeTo(io);
        } catch (IOException e)
        {
            return false;
        }
        
        return true;
    }

    public boolean Save(RandomAccessFile f)
    {
        if (fVersion < 2) return false;
        __OMF_Writer header = new OMF_DataOutput(f, this);
        
        try
        {
    
            int offset;
            long pos = f.getFilePointer();
            
            
            if (fSegname.length() == 0) fSegname = "          ";
            if (fLoadname.length() == 0) fLoadname = "          ";
            this.fLength = 0;
            offset = 0x2c + 10;
            if (fLablen == 0)
            {
                offset += 1 + fSegname.length();
            }
            else offset += fLablen;
            
            // seek past the header, then write all the opcodes.
            f.seek(pos + offset);
            for (OMF_Opcode op : fOpcodes)
            {
                fLength += op.CodeSize();
                op.Save(header);
            }
            
            
            // calculate the total size, then seek back and write the header.
            this.fBytecount = (int)(f.getFilePointer() - pos);
            f.seek(pos);
            // write the header...
            header.Write32(fBytecount);
            header.Write32(fResspace);
            header.Write32(fLength);
            header.Write8(0);
            header.Write8(fLablen);
            header.Write8(fNumlen);
            header.Write8(fVersion);
            header.Write32(fBanksize);
            header.Write16(fKind);
            header.Write16(0);
            header.Write32(fOrg);
            header.Write32(fAlignment);
            header.Write8(fNumsex);
            header.Write8(0);
            header.Write16(fSegnum);
            header.Write32(fEntry);
            header.Write16(0x2c);
            header.Write16(offset);
            header.WriteString(fLoadname, 10);
            header.WriteString(fSegname);
            
        }
        catch (IOException e)
        {
            return false;
        }
        
        return header.IsOK();
    }
   
    
    public ListIterator<OMF_Opcode> Opcodes()
    {
        return fOpcodes.listIterator();
    }
    public void AddOpcode(OMF_Opcode op)
    {
        if (op != null)
        {
            fOpcodes.add(op);
            fLength += op.CodeSize();
            if (fReloc == false)
            {
                switch (op.Opcode())
                {
                case OMF.OMF_SUPER:
                case OMF.OMF_RELOC:
                case OMF.OMF_CRELOC:
                case OMF.OMF_INTERSEG:
                case OMF.OMF_CINTERSEG:
                    fReloc = true;
                }
            }
        }
    }
    
    public int LabelLength()
    {
        return fLablen;
    }
    public int NumberLength()
    {
    	return fNumlen;
    }
    public int NumberSex()
    {
    	return fNumsex;
    }
    public boolean LittleEndian()
    {
    	return fNumsex == 0;
    }
    public boolean BigEndian()
    {
    	return fNumsex != 0;
    }
    public boolean Private()
    {
    	return (fKind & OMF.KIND_PRIVATE) == OMF.KIND_PRIVATE;
    	//if (fVersion < 2) return (fType & TYPE_PRIVATE) == TYPE_PRIVATE;
		//return false;
    }
    public int Kind()
    {
        return fKind & 0x1f;
    }
    public void SetKind(int kind)
    {
        fKind = (kind & 0x1f) | (fKind & 0xffe0);
    }
    public int Attributes()
    {
        return fKind & 0xffe0;
    }
    public void SetAttributes(int attr)
    {
        fKind = (fKind & 0x1f) | (attr & 0xffe0);        
    }
    
    public int Version()
    {
    	return fVersion;
    }
    
    public String SegmentName()
    {
    	return fSegname;
    }
    public String LoadName()
    {
    	return fLoadname;
    }
    
    public void SetSegmentName(String name)
    {
    	fSegname = name;
    }
    
    public void SetLoadName(String name)
    {
       fLoadname = name;
    }
    
    public int ReservedSpace()
    {
        return fResspace;
    }
    public void SetReservedSpace(int space)
    {
        fLength = fLength + space - fResspace;
        fResspace = space;
    }
    
    public void SetSegmentNumber(int num)
    {
        fSegnum = num;
    }
    public int SegmentNumber()
    {
        return fSegnum;
    }
    public int BankSize()
    {
        return fBanksize;
    }
    public void SetBankSize(int banksize)
    {
        fBanksize = banksize;
    }

    public int Org()
    {
        return fOrg;
    }
    public void SetOrg(int org)
    {
        fOrg = org;
    }
    
    public int Entry()
    {
        return fEntry;
    }
    public void SetEntry(int entry)
    {
        fEntry = entry;
    }
    
    public int Alignment()
    {
        return fAlignment;
    }
    public void SetAlignment(int align)
    {
        fAlignment = align;
    }
    
    
    // not always correct -- should go through fOpcodes, then add in resspace.
    public int Length()
    {
        return fLength;
    }
    // ditto.
    public int ByteCount()
    {
        return fBytecount;
    }
    
    // convert trailing DS to reserved space.
    public void Compress()
    {
        int i;
        int space = 0;
        OMF_Opcode op;
        
        i = fOpcodes.size();
        if (i < 1) return;
        
        op = fOpcodes.get(i - 1);
        if (! (op instanceof OMF_Eof)) return;
        
        i--;
        
        for (; i > 0; i--)
        {
            op = fOpcodes.get(i - 1);
            if (op instanceof OMF_DS)
            {
                space += op.CodeSize();
                fOpcodes.remove(i - 1);
            }
            else break;           
        }
        // length not affected.
        fResspace += space;
    }
    
    /*
     * convert DS segments to LCONST segments.
     * 
     */
    public void Flatten()
    {
        int size = fOpcodes.size();
  
        /*
         * if there is reserved space, convert it 
         * to an OMF_DS record (if there's also an EOF)
         */
        if (fResspace > 0)
        {
            if (size > 0)
            {
                OMF_Opcode op = fOpcodes.get(size - 1);
                if (op instanceof OMF_Eof)
                {
                    fOpcodes.add(size - 1, new OMF_DS(fResspace));
                    fResspace = 0;        
                }
            }
            
        }
        
        for (int i = 0; i < size; i++)
        {
            OMF_Opcode op = fOpcodes.get(i);
            if (op instanceof OMF_DS)
            {
                int length = op.CodeSize();
                fOpcodes.set(i, new OMF_LConst(new byte[length]));
            }
            
        }
        
    }
    
    /*
     * combine adjacent DS records, 
     * adjacent CONST (and LCONST) records
     * 
     */
    public void Normalize()
    {
        int i;
        int size = fOpcodes.size();
        
        for (i = 0; i < size; )
        {
            OMF_Opcode o = fOpcodes.get(i);
            if (o instanceof OMF_DS)
            {
                int j;
                int space = o.CodeSize();
                for (j = i + 1; j < size; j++)
                {
                    OMF_Opcode o2 = fOpcodes.get(j);
                    if (!(o2 instanceof OMF_DS))
                        break;
                    space += o.CodeSize();
                }
                
                j--;
                if (j > i)
                {
                    fOpcodes.set(i, new OMF_DS(space));
                    while (j > i)
                    {
                        fOpcodes.remove(j);
                        size--;
                        j--;
                    }
                }
            }
            else if (o instanceof OMF_Const)
            {
                int j;
                int space = o.CodeSize();
                for (j = i + 1; j < size; j++)
                {
                    OMF_Opcode o2 = fOpcodes.get(j);
                    if (!(o2 instanceof OMF_Const))
                        break;
                    space += o.CodeSize();
                }
                
                j--;
                if (j > i)
                {
                    byte[] data = new byte[space];
                    int offset = 0;
                    for (int k = i; k < (j + 1); k++)
                    {
                        OMF_Const o2 = (OMF_Const)fOpcodes.get(k);
                        System.arraycopy(data, offset, o2.Data(), 0, o2.CodeSize());
                        offset += o2.CodeSize();                       
                    }

                    fOpcodes.set(i, new OMF_LConst(data, space));
                    while (j > i)
                    {
                        fOpcodes.remove(j);
                        size--;
                        j--;
                    }
                }                  
            }
            
            i++;
        }
    }
    
    
    
    public boolean Error()
    {
        return fError;
    }
    
    
    public int File()
    {
        return fFile;
    }
    public void SetFile(int file)
    {
        fFile = file;
    }

}
