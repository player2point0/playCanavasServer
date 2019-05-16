package memory;

import sparsedatabase.PropertyHashtable;
import sparsedatabase.PropertyMatrixInt;
import mathematics.GeneralMatrixInt;

public class UsedAndFreeLists 
{
	public int[] nextValues;
	public int[] previousValues;

	public int numUsedValues = 0;
	public int numFreeValues = 0;

	public int firstUsedValue = 0xFFFFFFFF;
	public int lastUsedValue = 0xFFFFFFFF;

	public int firstFreeValue = 0xFFFFFFFF;
	public int lastFreeValue = 0xFFFFFFFF;

	public static final int INVALID = 0xFFFFFFFF;
	
	public boolean log = false;
	
	public UsedAndFreeLists()
	{
		validate(false);		
	}
	
	public void load(PropertyHashtable proot)
	{
		PropertyMatrixInt pmeta = (PropertyMatrixInt)proot.GetProperty("meta");
		GeneralMatrixInt meta = pmeta.matrix;
		numUsedValues = meta.value[0];
		numFreeValues = meta.value[1];

		firstUsedValue = meta.value[2];
		lastUsedValue = meta.value[3];

		firstFreeValue = meta.value[4];
		lastFreeValue = meta.value[5];

		PropertyMatrixInt pnext = (PropertyMatrixInt)proot.GetProperty("next");
		PropertyMatrixInt pprev = (PropertyMatrixInt)proot.GetProperty("prev");
		nextValues = pnext.matrix.value;
		previousValues = pprev.matrix.value;
	}
	
	public boolean isequal(PropertyHashtable proot)
	{
		PropertyMatrixInt pmeta = (PropertyMatrixInt)proot.GetProperty("meta");
		GeneralMatrixInt meta = pmeta.matrix;
		if(numUsedValues != meta.value[0])
			return false;
		if(numFreeValues != meta.value[1])
			return false;

		if(firstUsedValue != meta.value[2])
			return false;
		if(lastUsedValue != meta.value[3])
			return false;

		if(firstFreeValue != meta.value[4])
			return false;
		if(lastFreeValue != meta.value[5])
			return false;

		PropertyMatrixInt pnext = (PropertyMatrixInt)proot.GetProperty("next");
		PropertyMatrixInt pprev = (PropertyMatrixInt)proot.GetProperty("prev");
		if(!pnext.matrix.isequal(nextValues))
			return false;
		if(!pprev.matrix.isequal(previousValues))
			return false;
		return true;
	}
	
	public void save(PropertyHashtable proot)
	{
		GeneralMatrixInt meta = new GeneralMatrixInt(2,3);
		meta.value[0] = numUsedValues;
		meta.value[1] = numFreeValues;

		meta.value[2] = firstUsedValue;
		meta.value[3] = lastUsedValue;

		meta.value[4] = firstFreeValue;
		meta.value[5] = lastFreeValue;

		GeneralMatrixInt mnextvalues = new GeneralMatrixInt(1,nextValues.length,nextValues);
		GeneralMatrixInt mpreviousValues = new GeneralMatrixInt(1,previousValues.length,previousValues);

		new PropertyMatrixInt(proot, meta, "meta");
		new PropertyMatrixInt(proot, mnextvalues, "next");
		new PropertyMatrixInt(proot, mpreviousValues, "prev");
	}
	
	public void lassert(boolean val)
	{
		if(!val)
			System.out.println("lassert false");
	}
	
	public boolean isUsed(int x)
	{
		int node = firstUsedValue;
		for(int n=0;n<numUsedValues;n++)
		{
			if(node==x)
				return true;
			if(n==(numUsedValues-1))
				assert(node==lastUsedValue);
			node = nextValues[node];
		}
		return false;
	}
	
	public boolean isFree(int x)
	{
		int node = firstFreeValue;
		for(int n=0;n<numFreeValues;n++)
		{
			if(node==x)
				return true;
			if(n==(numFreeValues-1))
				assert(node==lastFreeValue);
			node = nextValues[node];
		}
		return false;
	}
	
	public void validate(boolean oneUnallocated)
	{
		/*
		int numVals = numFreeValues+numUsedValues;
		if(oneUnallocated)
		{
			numVals++;
			lassert(nextValues.length==numVals);
			lassert(previousValues.length==numVals);
		}
		else
		{
			if(numVals==0)
			{
				lassert(nextValues==null);
				lassert(previousValues==null);
			}
			else
			{
				lassert(nextValues.length>=numVals);
				lassert(previousValues.length>=numVals);
			}
		}
		
		if(numFreeValues==0)
		{
			lassert(firstFreeValue == 0xFFFFFFFF);
			lassert(lastFreeValue == 0xFFFFFFFF);
		}
		else
		if(numFreeValues==1)
		{
			lassert(firstFreeValue == lastFreeValue);
			lassert(lastFreeValue != 0xFFFFFFFF);
		}
		else
		{	
			lassert(firstFreeValue != lastFreeValue);
			lassert(firstFreeValue != 0xFFFFFFFF);
			lassert(lastFreeValue != 0xFFFFFFFF);
		}	
		if(numUsedValues==0)
		{
			lassert(firstUsedValue == 0xFFFFFFFF);
			lassert(lastUsedValue == 0xFFFFFFFF);
		}
		else
		if(numUsedValues==1)
		{
			lassert(firstUsedValue == lastUsedValue);
			lassert(lastUsedValue != 0xFFFFFFFF);
		}
		else
		{	
			lassert(firstUsedValue != lastUsedValue);
			lassert(firstUsedValue != 0xFFFFFFFF);
			lassert(lastUsedValue != 0xFFFFFFFF);
		}	
		
		GeneralMatrixInt used = null;
		if(nextValues!=null)
		{
			used = new GeneralMatrixInt(1,nextValues.length);
			used.clear(0);
		}
		
		int node = firstFreeValue;
		for(int n=0;n<numFreeValues;n++)
		{
			if(n==(numFreeValues-1))
				lassert(node==lastFreeValue);
			lassert(used.value[node]==0);
			used.value[node] = 1;
			node = nextValues[node];
		}
		lassert(node==0xFFFFFFFF);
		node = firstUsedValue;
		for(int n=0;n<numUsedValues;n++)
		{
			if(n==(numUsedValues-1))
				assert(node==lastUsedValue);
			lassert(used.value[node]==0);
			used.value[node] = 1;
			node = nextValues[node];
		}
		lassert(node==0xFFFFFFFF);
		
		if(used==null)
			return;
		
		int numFree = 0;
		for(int i=0;i<used.height;i++)
		{
			if(used.value[i]==0)
				numFree++;
		}
		if(oneUnallocated)
			lassert(numFree==1);
		else
			lassert(numFree==0);
			*/
	}
	
	public void set(UsedAndFreeLists o)
	{
		nextValues = new int[o.nextValues.length];
		previousValues = new int[o.previousValues.length];
		System.arraycopy(o.nextValues, 0, nextValues, 0, o.nextValues.length);
		System.arraycopy(o.previousValues, 0, previousValues, 0, o.previousValues.length);
		numUsedValues = o.numUsedValues;
		numFreeValues = o.numFreeValues;
		firstFreeValue = o.firstFreeValue;
		lastFreeValue = o.lastFreeValue;
		firstUsedValue = o.firstUsedValue;
		lastUsedValue = o.lastUsedValue;
	}
	
//	public void lassert(boolean value) 
//	{
//		if(!value)
//		{
//			System.out.println("Errorororror!");
//		}
//	}

	public void clear()
	{
//		if(startingcap>initialCapacity)
//			initialCapacity = startingcap;
		nextValues = null;
		previousValues = null;
		numUsedValues = 0;
		numFreeValues = 0;
		firstUsedValue = 0xFFFFFFFF;
		lastUsedValue = 0xFFFFFFFF;
		firstFreeValue = 0xFFFFFFFF;
		lastFreeValue = 0xFFFFFFFF;
	}
	
	public void ensureCapacity(int size)
	{
		int startSize = (numUsedValues+numFreeValues);
		if(size<=startSize)
		{
			//lassert(false);
			return;
		}
		
		if(nextValues==null)
		{
			nextValues = new int[size];
			previousValues = new int[size];
		}
		
		if(nextValues.length<size)
		{
			int[] nnext = new int[size];
			System.arraycopy(nextValues, 0, nnext, 0, startSize);
			nextValues = nnext;
			int[] nprev = new int[size];
			System.arraycopy(previousValues, 0, nprev, 0, startSize);
			previousValues = nprev;
		}
		
		if(numFreeValues==0)
		{
			firstFreeValue = startSize;
			lastFreeValue = startSize;
			
			nextValues[startSize] = 0xFFFFFFFF;
			previousValues[startSize] = 0xFFFFFFFF;
			lastFreeValue = startSize;
		}
		else
		{
			nextValues[lastFreeValue] = startSize;
			previousValues[startSize] = lastFreeValue;
			nextValues[startSize] = 0xFFFFFFFF;
		}
		//Now connect together all the new samples
		for(int i=startSize;i<size;i++)
		{
			if(i!=startSize)
				previousValues[i] = i-1;
			if(i!=(size-1))
				nextValues[i] = i+1;
			lastFreeValue = i;
			numFreeValues++;
		}
	}

	public int safeallocate()
	{
		validate(false);
		ensureCapacity(numUsedValues+1);
		validate(false);
		return allocate();
	}
	
	public int safeallocateFront()
	{
		validate(false);
		ensureCapacity(numUsedValues+1);
		validate(false);
		return allocateFront();
	}
	
	public int allocateFront()
	{
		lassert(numFreeValues>0);

		validate(false);
		if(numFreeValues==0)
		{
			lassert(false);
			return -1;
		}

		int x = lastFreeValue;
		if(log)
			System.out.println("Start allocate "+x+" numused="+numUsedValues+" numfree="+numFreeValues);

		if(numFreeValues==1)
		{
			firstFreeValue = 0xFFFFFFFF;
			lastFreeValue = 0xFFFFFFFF;
		}
		else
		{
			lastFreeValue = previousValues[x];
			nextValues[lastFreeValue] = 0xFFFFFFFF;	
		}
		
		if(numFreeValues<=0)
			lassert(false);
		
		nextValues[x] = 0xFFFFFFFF;
		previousValues[x] = 0xFFFFFFFF;
		
		numFreeValues--;

		if(numFreeValues==0)
		{
			firstFreeValue = 0xFFFFFFFF;
			lastFreeValue = 0xFFFFFFFF;
		}
		else
		{
			lassert(lastFreeValue!=0xFFFFFFFF);
		}
		
		validate(true);
		appendToStartOfUsedList(x);

		if(log)
			System.out.println("Finish allocate "+x+" numused="+numUsedValues+" numfree="+numFreeValues);
		validate(false);
		return x;
	}
	
	public int allocate()
	{
		lassert(numFreeValues>0);

		validate(false);
		if(numFreeValues==0)
		{
			lassert(false);
			return -1;
		}

		int x = lastFreeValue;
		if(log)
			System.out.println("Start allocate "+x+" numused="+numUsedValues+" numfree="+numFreeValues);

		if(numFreeValues==1)
		{
			firstFreeValue = 0xFFFFFFFF;
			lastFreeValue = 0xFFFFFFFF;
		}
		else
		{
			lastFreeValue = previousValues[x];
			nextValues[lastFreeValue] = 0xFFFFFFFF;	
		}
		
		if(numFreeValues<=0)
			lassert(false);
		
		nextValues[x] = 0xFFFFFFFF;
		previousValues[x] = 0xFFFFFFFF;
		
		numFreeValues--;

		if(numFreeValues==0)
		{
			firstFreeValue = 0xFFFFFFFF;
			lastFreeValue = 0xFFFFFFFF;
		}
		else
		{
			lassert(lastFreeValue!=0xFFFFFFFF);
		}
		
		validate(true);
		appendToEndOfUsedList(x);

		if(log)
			System.out.println("Finish allocate "+x+" numused="+numUsedValues+" numfree="+numFreeValues);
		validate(false);
		return x;
	}
	
	public boolean free(int x)
	{
		//lassert(isUsed(x));
//		if(x==1)
//			System.out.println("test");
			
		validate(false);
		if(log)
			System.out.println("Start free "+x+" numused="+numUsedValues+" numfree="+numFreeValues);
		if(x==firstUsedValue)
		{
			firstUsedValue = nextValues[x];
			if(firstUsedValue!=0xFFFFFFFF)
				previousValues[firstUsedValue] = 0xFFFFFFFF;
		}
		else
		if(x==lastUsedValue)
		{
			lastUsedValue = previousValues[x];
			if(lastUsedValue!=0xFFFFFFFF)
				nextValues[lastUsedValue] = 0xFFFFFFFF;
		}
//		else
//		if(previousValues[x]==0xFFFFFFFF)
//		{
//			lassert(nextValues[x] == 0xFFFFFFFF);
//			return false;
//		}
		else
		{
			int prev = previousValues[x];
			int next = nextValues[x];
			if(prev!=0xFFFFFFFF)
				nextValues[prev] = next;
			if(next!=0xFFFFFFFF)
				previousValues[next] = prev;
		}
		if(numUsedValues<=0)
			lassert(false);
		
		nextValues[x] = 0xFFFFFFFF;
		previousValues[x] = 0xFFFFFFFF;

		numUsedValues--;

		if(numUsedValues==0)
		{
			firstUsedValue = 0xFFFFFFFF;
			lastUsedValue = 0xFFFFFFFF;
		}
		else
		if(numUsedValues==1)
		{
			if(firstUsedValue!=0xFFFFFFFF)
				lastUsedValue = firstUsedValue;
			else
				firstUsedValue = lastUsedValue;
		}
		
		validate(true);
		appendToEndOfFreeList(x);
		
		if(log)
			System.out.println("Stop free "+x+" numused="+numUsedValues+" numfree="+numFreeValues);
		validate(false);
		//lassert(isFree(x));
		return true;
	}
	
	public void appendToEndOfFreeList(int x)
	{
		if(numFreeValues == 0)
		{
			nextValues[x] = 0xFFFFFFFF;
			previousValues[x] = 0xFFFFFFFF;
			firstFreeValue = x;
			lastFreeValue = x;
			numFreeValues++;
		}
		else
		{
			lassert(nextValues[lastFreeValue]==0xFFFFFFFF);
			nextValues[lastFreeValue] = x;
			nextValues[x] = 0xFFFFFFFF;
			previousValues[x] = lastFreeValue;
			lastFreeValue = x;
			numFreeValues++;
		}
	}
	
	public void appendToEndOfUsedList(int x)
	{
		if(numUsedValues == 0)
		{
			nextValues[x] = 0xFFFFFFFF;
			previousValues[x] = 0xFFFFFFFF;
			firstUsedValue = x;
			lastUsedValue = x;
			numUsedValues++;
		}
		else
		{
			lassert(nextValues[lastUsedValue]==0xFFFFFFFF);
			nextValues[lastUsedValue] = x;
			nextValues[x] = 0xFFFFFFFF;
			previousValues[x] = lastUsedValue;
			lastUsedValue = x;
			numUsedValues++;
		}
	}

	public void appendToStartOfUsedList(int x)
	{
		if(numUsedValues == 0)
		{
			nextValues[x] = 0xFFFFFFFF;
			previousValues[x] = 0xFFFFFFFF;
			firstUsedValue = x;
			lastUsedValue = x;
			numUsedValues++;
		}
		else
		{
			lassert(previousValues[firstUsedValue]==0xFFFFFFFF);
			previousValues[firstUsedValue] = x;
			previousValues[x] = 0xFFFFFFFF;
			nextValues[x] = firstUsedValue;
			firstUsedValue = x;
			numUsedValues++;
		}
	}
}
