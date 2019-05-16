/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package sparsedatabase;

public class PropertyComparison 
{
	int type = 0;
	static final int T_EQUALS = 0;
	static final int T_NOT_EXIST = 1;
	public static PropertyComparison equalsComparison = new PropertyComparison(T_EQUALS);
	public static PropertyComparison notExistsComparison = new PropertyComparison(T_NOT_EXIST);
	
	PropertyComparison(int t)
	{
		type = t;
	}
	
	boolean compare(Property a,Property b)
	{
		switch(type)
		{
		case T_EQUALS:
			if((a instanceof PropertyString)&&(b instanceof PropertyString))
			{
				return (b!=null)&&((PropertyString)a).value.equalsIgnoreCase(((PropertyString)b).value);
			}
			break;
		case T_NOT_EXIST:
			if(b==null)
				return true;
			break;
		}
		return false;
	}
}
