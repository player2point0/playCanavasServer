package sort;

public class RadixSortIndex 
{
	public float minVal;
	public float maxVal;
	public float delta;
	
	public int[][] bins;
	public int[] binsizes;

	public int numValues;

	public RadixSortIndex(float minVal,float maxVal,int numBins)
	{
		this.minVal = minVal;
		this.maxVal = maxVal;
		delta = (maxVal-minVal)/(numBins-1);
		bins = new int[numBins][];
		binsizes = new int[numBins];
//		for(int i=0;i<binsizes.length;i++)
//		{
//			bins[i] = new int[8];
//		}
		reset();
	}

	public void reset()
	{
		numValues = 0;	
		for(int i=0;i<binsizes.length;i++)
		{
			binsizes[i] = 0;
		}
	}

	public void validate()
	{
	}	
	
	int getBin(float dist)
	{
		dist -= minVal;
		dist /= delta;
		
		if(dist<0)
		{
			return 0;
		}
		if(dist>=binsizes.length)
		{
			return binsizes.length-1;
		}
		return (int)dist;
	}
	
	public void remove(float currentDist,int index)
	{
		int b = getBin(currentDist);
		int bindex = -1;
		for(int i=0;i<binsizes[b];i++)
		{
			if(bins[b][i]==index)
			{
				bindex = i;
				break;
			}
		}
		if(bindex==-1)
		{
			System.out.println("Error (bindex==-1)");
			return;
		}
		if(bindex!=(binsizes[b]-1))
			System.arraycopy(bins[b], (bindex+1), bins[b], bindex, (binsizes[b]-1-(bindex)));
		binsizes[b]--;		
		numValues--;
		/*
		for(int i=0;i<binsizes.length;i++)
		{
			for(int j=0;j<binsizes[i];j++)
			{
				int ind2 = bins[i][j];
				if(ind2==index)
				{
					System.out.println("Error Not removed");
				}
			}
		}	    
		*/
	}
	
	public void move(float currentDist,int index,float newDistance)
	{
		int bold = getBin(currentDist);
		int bnew = getBin(newDistance);
		if(bold==bnew)
			return;
		remove(currentDist,index);
		insert(newDistance,index);
	}
	
	public void insert(float currentDist,int index)
	{
		/*
		for(int i=0;i<binsizes.length;i++)
		{
			for(int j=0;j<binsizes[i];j++)
			{
				int ind2 = bins[i][j];
				if(ind2==index)
				{
					System.out.println("Index duplicate!");
				}
			}
		}	
		*/    
		int b = getBin(currentDist);	
		ensureCapacity(b,binsizes[b]+1);
		bins[b][binsizes[b]] = index;
		binsizes[b]++;
		numValues++;
	}
	
	public int removeClosest()
	{
		int closestBin = -1;
		for(int i=0;i<binsizes.length;i++)
		{
			if(binsizes[i]>0)
			{
				closestBin = i;
				break;
			}
		}
		if(closestBin==-1)
		{
			System.out.println("Error");
			return -1;
		}
		
		binsizes[closestBin]--;		
		numValues--;
		
		/*
		for(int i=0;i<binsizes.length;i++)
		{
			for(int j=0;j<binsizes[i];j++)
			{
				int ind2 = bins[i][j];
				if(ind2==bins[closestBin][binsizes[closestBin]])
				{
					System.out.println("Error Not removed");
				}
			}
		}	    
		*/
		int result = bins[closestBin][binsizes[closestBin]];
		if(binsizes[closestBin]==0)
			bins[closestBin] = null;

		return result;
	}
	
    public void ensureCapacity(int bin,int mincap)
    {
    	if(bins[bin]==null)
    	{
    		bins[bin] = new int[mincap];
    	}
    	else
    	if(mincap>bins[bin].length)
    	{
	        int newcap = (bins[bin].length * 3)/2 + 1;
	        int[] olddata = bins[bin];
	        bins[bin] = new int[newcap < mincap ? mincap : newcap];
	        System.arraycopy(olddata,0,bins[bin],0,binsizes[bin]);
    	}
    }
}
