package font;

import java.io.Serializable;

import application.vectorgraphics.Group;
import sparsedatabase.PropertyArrayString;
import sparsedatabase.PropertyHashtable;
import sparsedatabase.PropertyList;
import sparsedatabase.PropertyMatrixFloat;
import sparsedatabase.SparseDatabase;
import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class VectorFont implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
//	public int numChars = 256;
//	public int numBlocks = 1;
//	public int unicodes[]; //start and stop of runs of unicode chars
//	
//	public int leftTopAdvance[]; //= new int[numChars*2]; advanceWidth, left side bearing 
//	//the movement of the cursor with each character, either right, left, down or up depending on the language
//
//	public int kerning[]; //new int[numchars*2] start and num of kerning rules for each character
//	public int kerningpairs[]; //new int[2] right and offset

	public GeneralMatrixString glyphUnicode = new GeneralMatrixString(1);
	public transient GeneralMatrixObject gglyphs = new GeneralMatrixObject(1);
	public GeneralMatrixObject glyphs = new GeneralMatrixObject(1);
	public GeneralMatrixFloat glyphXOffset = new GeneralMatrixFloat(1);

	public GeneralMatrixString kerningUnicode1 = new GeneralMatrixString(1);
	public GeneralMatrixString kerningUnicode2 = new GeneralMatrixString(1);
	public GeneralMatrixFloat kerningOffset = new GeneralMatrixFloat(1);

	public void createSubset(final VectorFont vf,char[] chars)
	{
		for(int ci=0;ci<chars.length;ci++)
		{
			int ind = vf.getGlyphIndex(chars[ci]);
			if(ind==-1)
				continue;
			glyphUnicode.push_back(""+chars[ci]);
			gglyphs.push_back(vf.gglyphs.value[ind]);
			glyphXOffset.push_back(vf.glyphXOffset.value[ind]);
		}
	}

	public int getGlyphIndex(char c)
	{
		String hex = Integer.toHexString(c | 0x10000).substring(2);
		String unicode0 = "u" + hex;
		if(hex.charAt(0)=='0')
		{
			hex = hex.substring(1);
		}
		String unicode1 = "u" + hex;
		if(hex.charAt(0)=='0')
		{
			hex = hex.substring(1);
		}
		String unicode = "&#x" + hex;
		//String unicode = "u" + Integer.toHexString(c | 0x10000).substring(2);
		unicode += ";";
		for(int i=0;i<glyphUnicode.height;i++)
		{
			String gu = glyphUnicode.value[i];
			if(gu.length()==1)
			{
				char guc = gu.charAt(0);
				if(guc==c)
					return i;
			}
			if(unicode.equalsIgnoreCase(gu))
				return i;
			if(unicode0.equalsIgnoreCase(gu))
				return i;
			if(unicode1.equalsIgnoreCase(gu))
				return i;
		}
		return -1;
	}
	
	//	public int[] glyphs; //new int[numchars*2] the start and stop of edges for each glyph
//	public int[] glyphContours; //point start and number of points total (which implicitly defines the type)
//	public int[] glyphEdges; //point start and number of points total (which implicitly defines the type)
//	public int[] points; //the fixed point points that define all the glyphs of the font
	
	public void fromFile(String path)
	{
		PropertyHashtable proot = SparseDatabase.LoadEntryFromFileOutsideOfDatabase(path);
		fromProperty(proot);
	}

	public void toFile(String path)
	{
		PropertyHashtable proot = new PropertyHashtable();		
		toProperty(proot);
		SparseDatabase.SaveEntryVerbose(proot, path);
	}

    public void toProperty(PropertyHashtable root)
    {
    	PropertyList pglyphs = new PropertyList(root,"glyphs");
    	for(int i=0;i<gglyphs.height;i++)
    	{
    		PropertyHashtable ph = new PropertyHashtable("g"+i);
    		Group g = (Group)gglyphs.value[i];
    		g.toProperty(ph);
    		pglyphs.entries.push_back(ph);
    	}
    	PropertyMatrixFloat pxoff = new PropertyMatrixFloat(root, glyphXOffset, "xoff");
    	PropertyArrayString punicode = new PropertyArrayString(root,glyphUnicode,"unicode");

    	PropertyArrayString pkunicode1 = new PropertyArrayString(root,kerningUnicode1,"ku1");
    	PropertyArrayString pkunicode2 = new PropertyArrayString(root,kerningUnicode2,"ku2");
    	
    	PropertyMatrixFloat pkoff = new PropertyMatrixFloat(root,kerningOffset,"koff");
    }

    public void fromProperty(PropertyHashtable root)
    {
    	PropertyList pglyphs = (PropertyList)root.GetProperty("glyphs");
    			//new PropertyList(root,"glyphs");
    	for(int i=0;i<pglyphs.entries.height;i++)
    	{
    		PropertyHashtable ph = (PropertyHashtable)pglyphs.entries.value[i];
    		Group g = new Group();
    		g.fromProperty(ph);
    		gglyphs.push_back(g);
    	}
    	PropertyMatrixFloat pxoff = (PropertyMatrixFloat)root.GetProperty("xoff");//(root, glyphXOffset, "xoff");
    	glyphXOffset.set(pxoff.matrix);
    	PropertyArrayString punicode = (PropertyArrayString)root.GetProperty("unicode");//(root,glyphUnicode,"unicode");
    	glyphUnicode.set(punicode.value);
    	
    	PropertyArrayString pkunicode1 = (PropertyArrayString)root.GetProperty("ku1");//(root,kerningUnicode1,"ku1");
    	kerningUnicode1.set(pkunicode1.value);
    	PropertyArrayString pkunicode2 = (PropertyArrayString)root.GetProperty("ku2");//(root,kerningUnicode2,"ku2");
    	kerningUnicode2.set(pkunicode2.value);
    	
    	PropertyMatrixFloat pkoff = (PropertyMatrixFloat)root.GetProperty("koff");//(root,kerningOffset,"koff");
    	kerningOffset.set(pkoff.matrix);
    }
}
