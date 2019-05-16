package registration;

import mathematics.GeneralMatrixInt;

public class SparseAccumulationBufferPosition 
{
	//Returns mask for the ids set at this location
	public long findGreatestPeak(GeneralMatrixInt position)
	{
		float max = -Float.MAX_VALUE;
		long idmask = 0;
		for(int i=0;i<num_buckets;i++)
		{
			if(values[i]>max)
			{
				max = values[i];
				position.value[0] = table[i*3+0];
				position.value[1] = table[i*3+1];
				position.value[2] = table[i*3+2];
				idmask = idMasks[i];
			}
		}
		return idmask;
	}

	long[] idMasks; //Masks indicating potential id use in this bin
	float[] values; //Magnitudes
	int[] table;	//3 ints per entry

	int num_buckets;
	int num_elements;

	  // How full we let the table get before we resize.  Knuth says .8 is
	  // good -- higher causes us to probe too much, though saves memory
	  static final float HT_OCCUPANCY_FLT = 0.8f;
	  // How empty we let the table get before we resize lower.
	  // (0.0 means never resize lower.)
	  // It should be less than OCCUPANCY_FLT / 2 or we thrash resizing
	  static final float HT_EMPTY_FLT = 0.4f * HT_OCCUPANCY_FLT;
	  // Minimum size we're willing to let hashtables be.
	  // Must be a power of two, and at least 4.
	  // Note, however, that for a given hashtable, the initial size is a
	  // function of the first constructor arg, and may be >HT_MIN_BUCKETS.
	  static final int HT_MIN_BUCKETS  = 32;

	  //Hashtable state
	int delval;                        // which key marks deleted entries
	int emptyval;                      // which key marks unused entries

	int num_deleted;        			// how many occupied buckets are marked deleted
	float enlarge_resize_percent;       // how full before resize
	float shrink_resize_percent;        // how empty before resize
	int shrink_threshold;            	// num_buckets * shrink_resize_percent
	int enlarge_threshold;          	// num_buckets * enlarge_resize_percent
	boolean consider_shrink;   			// true if we should try to shrink before next insert	
	
	public SparseAccumulationBufferPosition()
	{
		consider_shrink = false;
		num_deleted = 0;
		delval = Integer.MAX_VALUE;
		emptyval = Integer.MIN_VALUE;
		enlarge_resize_percent = HT_OCCUPANCY_FLT;
		shrink_resize_percent = HT_EMPTY_FLT;
		
		num_buckets = min_size(0, 0);
		table = new int[num_buckets*3];
		values = new float[num_buckets];
		idMasks = new long[num_buckets];

		clearToEmpty();
		num_elements = 0;
		reset_thresholds();
	}

	public int size()
	{
		return num_elements-num_deleted;
	}
	
	public void validateNums()
	{
		int e = 0;
		int d = 0;
		for(int i=0;i<num_buckets;i++)
		{
			if(table[i*3]==delval)
				d++;
			if(table[i*3]==emptyval)
				e++;
		}
		if((num_buckets-num_elements)!=e)
			System.out.println("err");
		if(num_deleted!=d)
			System.out.println("err");
	}
	
	  // This is the normal insert routine, used by the outside world
	void insert(final int x,final int y,final int z, final float obj, final long id) 
	{
		validateNums();
		resize_delta(1);                      // adding an object, grow if need be
	    insert_noresize(x,y,z,obj,id);
		validateNums();
	}

	void insert_setMask(final int x,final int y,final int z, final float obj, final long id) 
	{
		validateNums();
		resize_delta(1);                      // adding an object, grow if need be
	    insert_noresize_setMask(x,y,z,obj,id);
		validateNums();
	}
	
	public int erase(final int x,final int y,final int z) 
	{
		validateNums();
	    int pos = find_existing_position(x,y,z);   // shrug: shouldn't need to be const
	    if ( pos != -1 ) 
	    {
	      set_deleted(pos);
	      ++num_deleted;
	      consider_shrink = true;      // will think about shrink after next insert
	      validateNums();
	      return 1;                    // because we deleted one thing
	    } 
	    else 
	    {
	      return 0;                    // because we deleted nothing
	    }
	}

	public boolean exists(final int x,final int y,final int z)
	{
	    int pos = find_existing_position(x,y,z);
	    return (pos!=-1);
	}
	
	public float get(final int x,final int y,final int z)
	{
	    int pos = find_existing_position(x,y,z);
	    if(pos==-1)
	    	return Float.MAX_VALUE;
		return values[pos];
	}
	
	public void set(final int x,final int y,final int z, final float value, final long id)
	{
	    int pos = find_existing_position(x,y,z);
	    if(pos==-1)
	    {
	    	insert(x,y,z, value,id);
	    }
	    else
	    {
	    	values[pos] = value;
	    	int bitshift = (int)(id%64);
	    	long bit = 1L<<bitshift;
	    	idMasks[pos] |= bit;
	    }		
	}
	public void accumulate(final int x,final int y,final int z, final float value, final long id)
	{
	    int pos = find_existing_position(x,y,z);
	    if(pos==-1)
	    {
	    	insert(x,y,z, value,id);
	    }
	    else
	    {
	    	values[pos] += value;
	    	int bitshift = (int)(id%64);
	    	long bit = 1L<<bitshift;
	    	idMasks[pos] |= bit;
	    }		
	}

	  // If you know *this is big enough to hold obj, use this routine
	  void insert_noresize(final int x,final int y,final int z, final float value, final long id) 
	  {
	    int pos = find_insert_position(x,y,z);
	    if ( pos == -1 ) 
	    {   
	    	// object was already there
	    } 
	    else 
	    { 
	    	if ( table[pos*3]==delval ) 
	    	{
	    		--num_deleted;                       // used to be, now it isn't
	    	} 
	    	else 
	    	{
	    		++num_elements;                      // replacing an empty bucket
	    	}
    		table[pos*3] = x;
    		table[pos*3+1] = y;
    		table[pos*3+2] = z;
	    	values[pos] = value;
	    	int bitshift = (int)(id%64);
	    	long bit = 1L<<bitshift;
	    	idMasks[pos] |= bit;
	    }
	  }
	  
	  void insert_noresize_setMask(final int x,final int y,final int z, final float value, final long id) 
	  {
	    int pos = find_insert_position(x,y,z);
	    if ( pos == -1 ) 
	    {   
	    	// object was already there
	    } 
	    else 
	    { 
	    	if ( table[pos*3]==delval ) 
	    	{
	    		--num_deleted;                       // used to be, now it isn't
	    	} 
	    	else 
	    	{
	    		++num_elements;                      // replacing an empty bucket
	    	}
    		table[pos*3] = x;
    		table[pos*3+1] = y;
    		table[pos*3+2] = z;
	    	values[pos] = value;
	    	idMasks[pos] = id;
	    }
	  }

	  boolean set_deleted(int pos)
	  {
		  if(table[pos*3] == delval)
			 return false; 
		 table[pos*3] = delval;
		 values[pos] = 0.0f;
		 idMasks[pos] = 0;
		 return true;
	  }
	  
	int hash(int x,int y, int z)
	{		
		return ((x&0xFF)<<16)|((y&0xFF)<<8)|((z&0xFF));
	}
	
	  // Returns a pair of positions: 1st where the object is, 2nd where
	  // it would go if you wanted to insert it.  1st is ILLEGAL_BUCKET
	  // if object is not found; 2nd is ILLEGAL_BUCKET if it is.
	  // Note: because of deletions where-to-insert is not trivial: it's the
	  // first deleted bucket we see, as long as we don't find the key later
	  final int find_insert_position(int x,int y, int z)
	  {
	    int num_probes = 0;              // how many times we've probed
	    final int bucket_count_minus_one = num_buckets - 1;
	    int bucknum = hash(x,y,z) & bucket_count_minus_one;
	    while ( true ) 
	    {                          // probe until something happens
	      if ( table[bucknum*3]==emptyval ) 
	      {         // bucket is empty
	    	  return bucknum;
	      } 
	      else if ( table[bucknum*3]==delval ) 
	      {
	          return bucknum;
	      } 
	      else if ((x == table[bucknum*3])&&(y == table[bucknum*3+1])&&(z == table[bucknum*3+2])) 
	      {
	    	  return -1;
	      }
	      ++num_probes;                        // we're doing another probe
	      bucknum = (bucknum + num_probes) & bucket_count_minus_one;
	      //assert(num_probes < bucket_count()); // don't probe too many times!
	    }
	  }

	  final int find_existing_position(int x,int y, int z)
	  {
	    int num_probes = 0;              // how many times we've probed
	    final int bucket_count_minus_one = num_buckets - 1;
	    int bucknum = hash(x,y,z) & bucket_count_minus_one;
	    while ( true ) 
	    {                          // probe until something happens
	      if ( table[bucknum*3]==emptyval ) 
	      {         // bucket is empty
	    	  return -1;
	      } 
	      else if ( table[bucknum*3]==delval ) 
	      {
	          //return bucknum;
	      } 
	      else if ((x == table[bucknum*3])&&(y == table[bucknum*3+1])&&(z == table[bucknum*3+2])) 
	      {
	    	  return bucknum;
	      }
	      ++num_probes;                        // we're doing another probe
	      bucknum = (bucknum + num_probes) & bucket_count_minus_one;
	      //assert(num_probes < bucket_count()); // don't probe too many times!
	    }
	  }
	  
	  public void clearToEmpty()
	  {
			int size=table.length-1;
			int cleared=1;
			int index=1;
			table[0]=emptyval;
			num_deleted = 0;
			num_elements = 0;
			for(int i=0;i<idMasks.length;i++)
			{
				idMasks[i] = 0;
				values[i] = 0.0f;
			}
			while (cleared<size)
			{
				System.arraycopy(table,0,table,index,cleared);
				size-=cleared;
				index+=cleared;
				cleared<<=1;
			}
			System.arraycopy(table,0,table,index,size);
	  }
	  
	  void resize(int numberBuckets)
	  {
		  num_deleted = 0;

		  int[] oldTable = table;
		  float[] oldValues = values;
		  long[] oldIdMasks = idMasks;
		  
		  values = new float[numberBuckets];
		  table = new int[numberBuckets*3];
		  idMasks = new long[numberBuckets];
		  
		  clearToEmpty();
		  
		  num_buckets = numberBuckets;

		  for(int i=0;i<(oldTable.length/3);i++)
		  {
			  if((oldTable[i*3]!=emptyval)&&(oldTable[i*3]!=delval))
			  {
				  //Need to handle the masks properly
				  //aaa
				  insert_setMask(oldTable[i*3],oldTable[i*3+1],oldTable[i*3+2], oldValues[i], oldIdMasks[i]);
			  }
		  }
		  
		  reset_thresholds();
	  }

	  void resize_delta(int delta)
	  {
		  resize_delta(delta, 0);
	  }
	  
	  
	  // We'll let you resize a hashtable -- though this makes us copy all!
	  // When you resize, you say, "make it big enough for this many more elements"
	  void resize_delta(int delta, int min_buckets_wanted) 
	  {
	    if ( consider_shrink )                   // see if lots of deletes happened
	      maybe_shrink();
	    
	    if ( num_buckets > min_buckets_wanted &&
	         (num_elements + delta) <= enlarge_threshold )
	      return;                                // we're ok as we are

	    // Sometimes, we need to resize just to get rid of all the
	    // "deleted" buckets that are clogging up the hashtable.  So when
	    // deciding whether to resize, count the deleted buckets (which
	    // are currently taking up room).  But later, when we decide what
	    // size to resize to, *don't* count deleted buckets, since they
	    // get discarded during the resize.
	    final int needed_size = min_size(num_elements + delta,min_buckets_wanted);
	    if ( needed_size > num_buckets ) {      // we don't have enough buckets
	      int resize_to = min_size(num_elements - num_deleted + delta, min_buckets_wanted);
	      resize(resize_to);
	    }
	  }

	  // Used after a string of deletes
	  void maybe_shrink() 
	  {
	    //assert(num_elements >= num_deleted);
	    //assert((bucket_count() & (bucket_count()-1)) == 0); // is a power of two
	    //assert(bucket_count() >= HT_MIN_BUCKETS);

	    if (shrink_threshold > 0 &&
	        (num_elements-num_deleted) < shrink_threshold &&
	        num_buckets > HT_MIN_BUCKETS ) 
	    {
	      int sz = num_buckets / 2;    // find how much we should shrink
	      while ( 
	    		  (sz > HT_MIN_BUCKETS) 
	    		  &&
	              ((num_elements - num_deleted) < (sz * shrink_resize_percent)) 
	            )
	      {
	        sz /= 2;                            // stay a power of 2
	      }
	      
	      resize(sz);
	    }
	    consider_shrink = false;                // because we just considered it
	  }
	  	  
	   // This is the smallest size a hashtable can be without being too crowded
	  // If you like, you can give a min #buckets as well as a min #elts
	  int min_size(int num_elts, int min_buckets_wanted) 
	  {
	    int sz = HT_MIN_BUCKETS;             // min buckets allowed
	    while ( (sz < min_buckets_wanted) || (num_elts >= (sz * enlarge_resize_percent)) )
	      sz *= 2;
	    return sz;
	  }

	  void reset_thresholds() 
	{
		enlarge_threshold = (int)(num_buckets*enlarge_resize_percent);
		shrink_threshold = (int)(num_buckets*shrink_resize_percent);
		consider_shrink = false;   // whatever caused us to reset already considered
	}

}
