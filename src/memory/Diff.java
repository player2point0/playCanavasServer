package memory;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixString;

public class Diff 
{
	public static void apply(GeneralMatrixString from,GeneralMatrixString to,GeneralMatrixInt indexes,GeneralMatrixString newvalues, int newheight)
	{
		if(newheight==-1)
			return;
		if(newvalues.width!=to.width)
			System.out.println("Error in Diff.apply");
		to.setDimensions(from.width, newheight);

		System.arraycopy(from.value, 0, to.value, 0, to.width*to.height);
		for(int y=0;y<indexes.height;y++)
		{
			int ind = indexes.value[y]*to.width;
			System.arraycopy(newvalues.value, y*to.width, to.value, ind, to.width);
		}
	}
	
	public static void apply(GeneralMatrixFloat from,GeneralMatrixFloat to,GeneralMatrixInt indexes,GeneralMatrixFloat newvalues, int newheight)
	{
		if(newheight==-1)
			return;
		if(newvalues.width!=to.width)
			System.out.println("Error in Diff.apply");
		to.setDimensions(from.width, newheight);

		System.arraycopy(from.value, 0, to.value, 0, to.width*to.height);
		for(int y=0;y<indexes.height;y++)
		{
			int ind = indexes.value[y]*to.width;
			System.arraycopy(newvalues.value, y*to.width, to.value, ind, to.width);
		}
	}
	
	public static void apply(GeneralMatrixInt from,GeneralMatrixInt to,GeneralMatrixInt indexes,GeneralMatrixInt newvalues, int newheight)
	{
		if(newheight==-1)
			return;
		if(newvalues.width!=to.width)
			System.out.println("Error in Diff.apply");
		to.setDimensions(from.width, newheight);

		System.arraycopy(from.value, 0, to.value, 0, to.width*to.height);
		for(int y=0;y<indexes.height;y++)
		{
			int ind = indexes.value[y]*to.width;
			System.arraycopy(newvalues.value, y*to.width, to.value, ind, to.width);
		}
	}
	
	public static int diff(GeneralMatrixString from,GeneralMatrixString to,GeneralMatrixInt indexes,GeneralMatrixString newvalues)
	{
		newvalues.width = to.width;
		int newheight = to.height;
		int offset = 0;
		for(int i=0;i<to.height;i++)
		{
			boolean isdiff = false;
			if(i>=from.height)
			{
				isdiff = true;
			}
			else
			{
				for(int x=0;x<to.width;x++)
				{
					if(!from.value[offset+x].equals(to.value[offset+x]))
					{
						isdiff = true;
						break;
					}
				}
			}
			if(isdiff)
			{
				indexes.push_back(i);
				int ind = newvalues.appendRow();
				for(int x=0;x<to.width;x++)
				{
					newvalues.value[ind*to.width+x] = to.value[offset+x];
				}
			}
			offset += to.width;
		}
		if((indexes.height==0)&&(from.height==to.height))
			return -1;
		return newheight;
	}
	public static int diff(GeneralMatrixFloat from,GeneralMatrixFloat to,GeneralMatrixInt indexes,GeneralMatrixFloat newvalues)
	{
		newvalues.width = to.width;
		int newheight = to.height;
		int offset = 0;
		for(int i=0;i<to.height;i++)
		{
			boolean isdiff = false;
			if(i>=from.height)
			{
				isdiff = true;
			}
			else
			{
				for(int x=0;x<to.width;x++)
				{
					if(from.value[offset+x]!=to.value[offset+x])
					{
						isdiff = true;
						break;
					}
				}
			}
			if(isdiff)
			{
				indexes.push_back(i);
				int ind = newvalues.appendRow();
				for(int x=0;x<to.width;x++)
				{
					newvalues.value[ind*to.width+x] = to.value[offset+x];
				}
			}
			offset += to.width;
		}
		if((indexes.height==0)&&(from.height==to.height))
			return -1;
		return newheight;
	}
	public static int diff(GeneralMatrixInt from,GeneralMatrixInt to,GeneralMatrixInt indexes,GeneralMatrixInt newvalues)
	{
		newvalues.width = to.width;
		int newheight = to.height;
		int offset = 0;
		for(int i=0;i<to.height;i++)
		{
			boolean isdiff = false;
			if(i>=from.height)
			{
				isdiff = true;
			}
			else
			{
				for(int x=0;x<to.width;x++)
				{
					if(from.value[offset+x]!=to.value[offset+x])
					{
						isdiff = true;
						break;
					}
				}
			}
			if(isdiff)
			{
				indexes.push_back(i);
				int ind = newvalues.appendRow();
				for(int x=0;x<to.width;x++)
				{
					newvalues.value[ind*to.width+x] = to.value[offset+x];
				}
			}
			offset += to.width;
		}
		if((indexes.height==0)&&(from.height==to.height))
			return -1;
		return newheight;
	}
}
