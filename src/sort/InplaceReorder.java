package sort;

import java.util.Random;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;

public class InplaceReorder 
{
    public static void main(String[] list) throws Exception 
	{
    	GeneralMatrixFloat entries = new GeneralMatrixFloat(1,10);
    	GeneralMatrixInt setindex = new GeneralMatrixInt(1,10);
    	GeneralMatrixInt getindex = new GeneralMatrixInt(1,10);
    	GeneralMatrixFloat stored = new GeneralMatrixFloat(1,1);
    	GeneralMatrixFloat stored2 = new GeneralMatrixFloat(1,1);
    	
    	for (int i=0; i<setindex.height; i++) 
    	{
    		setindex.value[i] = i;
    		entries.value[i] = i;
    	}
    	
    	Random rgen = new Random(1);
    	//--- Shuffle by exchanging each element randomly
    	for (int i=0; i<setindex.height; i++) 
    	{
    	    int randomPosition = rgen.nextInt(setindex.height);
    	    int temp = setindex.value[i];
    	    setindex.value[i] = setindex.value[randomPosition];
    	    setindex.value[randomPosition] = temp;
    		float tempf = entries.value[i];
    		entries.value[i] = entries.value[randomPosition];
    		entries.value[randomPosition] = tempf;
    	}
    	
    	//Calc get index
    	for (int i=0; i<setindex.height; i++) 
    	{
    		int ind = setindex.value[i];
    		getindex.value[ind] = i;
    	}
    	
    	reorderIndexSetLocation(entries, setindex, stored, stored2);
    	//reorderIndexGetLocation(entries, getindex, stored);
    	
    	System.out.println("Test");
    }
    
	static int nextHole(GeneralMatrixFloat nodes, GeneralMatrixInt nodeIndex, GeneralMatrixFloat helditem, int startIndex)
	{
		for(int i=(startIndex+1);i<nodes.height;i++)
		{
			int v = nodeIndex.value[i];
			//Already sorted no problem
			if(v==i)
			{
				nodeIndex.value[i] = -(v+1);
			}
			else
			if(v<0)
			{
				//Already in correct place carry on
			}
			else
			{
				//Stored value to move
				System.arraycopy(nodes.value, i*nodes.width, helditem.value, 0, nodes.width);
				nodes.value[i*nodes.width] = Float.MAX_VALUE;
				return i;
			}
		}
		return -1;
	}
	
	public static void reorderIndexSetLocation(GeneralMatrixFloat nodes, GeneralMatrixInt nodeIndex, GeneralMatrixFloat nodemean, GeneralMatrixFloat nodemean2)
	{
		boolean heldInFirst = true;
		int held = nextHole(nodes, nodeIndex, nodemean, -1);
		int hole = held;

		while(hole!=-1)
		{
			int toput = nodeIndex.value[held];
			if(toput==hole)
			{
				//The value should have been taken
				if(nodes.value[hole*nodes.width]!=Float.MAX_VALUE)
					System.out.println("Error");
				//This value should have been placed already
				if(nodeIndex.value[hole*nodeIndex.width]>=0)
					System.out.println("Error");
				//Entry already cleared just set the value and find a new hole
				if(heldInFirst)
				{
					System.arraycopy(nodemean.value, 0, nodes.value, toput*nodes.width, nodes.width);
				}
				else
				{
					System.arraycopy(nodemean2.value, 0, nodes.value, toput*nodes.width, nodes.width);					
				}
				//The position taken from has been put in the right place
				nodeIndex.value[held] = -(nodeIndex.value[held]+1);

				heldInFirst = true;
				held = nextHole(nodes, nodeIndex, nodemean, hole);
				hole = held;
			}
			else
			if(toput<0)
			{
				//Error toput should always be unsorted if not the original hole entry
				System.out.println("Error");
			}				
			else
			{
				//If the toput value has been cleared (i.e. is <0) then place and find a new starting point
				if(heldInFirst)
				{
					//Grab the stuff that is about to be written to
					System.arraycopy(nodes.value, toput*nodes.width, nodemean2.value, 0, nodes.width);
					//Now copy the toget value into the hole
					System.arraycopy(nodemean.value, 0, nodes.value, toput*nodes.width, nodes.width);
				}
				else
				{
					//Grab the stuff that is about to be written to
					System.arraycopy(nodes.value, toput*nodes.width, nodemean.value, 0, nodes.width);
					//Now copy the toget value into the hole
					System.arraycopy(nodemean2.value, 0, nodes.value, toput*nodes.width, nodes.width);
				}
				heldInFirst = !heldInFirst;
				//The position taken from has been put in the right place
				nodeIndex.value[held] = -(nodeIndex.value[held]+1);
				held = toput;
			}
		}		
		
		for(int i=0;i<nodeIndex.height;i++)
		{
			nodeIndex.value[i] = -(nodeIndex.value[i]+1);
		}
	}

	public static void reorderIndexGetLocation(GeneralMatrixFloat nodes, GeneralMatrixInt nodeIndex, GeneralMatrixFloat nodemean)
	{
		int held = nextHole(nodes, nodeIndex, nodemean, -1);
		int hole = held;
		
		while(held!=-1)
		{
			int toget = nodeIndex.value[hole];
			if(toget<0)
			{
				//Error toget should always be unsorted
			}
			else
			if(toget==held)
			{
				if(nodes.value[hole*nodes.width]!=Float.MAX_VALUE)
					System.out.println("Error");
				System.arraycopy(nodemean.value, 0, nodes.value, hole*nodes.width, nodes.width);
				nodeIndex.value[hole] = -(nodeIndex.value[hole]+1);
				held = nextHole(nodes, nodeIndex, nodemean, held);
				hole = held;
			}
			else
			{
				if(nodes.value[hole*nodes.width]!=Float.MAX_VALUE)
					System.out.println("Error");
				System.arraycopy(nodes.value, toget*nodes.width, nodes.value, hole*nodes.width, nodes.width);	
				nodeIndex.value[hole] = -(nodeIndex.value[hole]+1);
				nodes.value[toget*nodes.width] = Float.MAX_VALUE;
				hole = toget;
			}
		}
		
		for(int i=0;i<nodeIndex.height;i++)
		{
			nodeIndex.value[i] = -(nodeIndex.value[i]+1);
		}
	}
}
