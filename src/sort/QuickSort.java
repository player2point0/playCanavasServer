/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package sort;

public class QuickSort 
{	
	   public static void quicksort(int[] a,int in[], int len) 
	   {
	        //shuffle(a,in);                        // to guard against worst-case
	        quicksort(a,in, 0, len-1);
	    }
	   public static void quicksort(long[] a,int in[], int len) 
	   {
	        //shuffle(a,in);                        // to guard against worst-case
	        quicksort(a,in, 0, len-1);
	    }
	   public static void quicksort(String[] a, int len) 
	   {
	        //shuffle(a,in);                        // to guard against worst-case
	        quicksort(a, 0, len-1);
	    }
	   public static void quicksort(String[] a,int in[], int len) 
	   {
	        //shuffle(a,in);                        // to guard against worst-case
	        quicksort(a,in, 0, len-1);
	    }
	   public static void quicksort(int[] a, int len) 
	   {
	        //shuffle(a,in);                        // to guard against worst-case
	        quicksort(a, 0, len-1);
	    }
	   public static void quicksort(double[] a,long in[]) 
	   {
	        shuffle(a,in);                        // to guard against worst-case
	        quicksort(a,in, 0, a.length - 1);
	    }
	   public static void quicksort(double[] a,int in[]) 
	   {
	        shuffle(a,in);                        // to guard against worst-case
	        quicksort(a,in, 0, a.length - 1);
	    }
	   public static void quicksort(double[] a,int in[],int len) 
	   {
	        //shuffle(a,in);                        // to guard against worst-case
	        quicksort(a,in, 0, len-1);
	    }
	   public static void quicksort(float[] a,int in[],int len) 
	   {
	        //shuffle(a,in);                        // to guard against worst-case
	        quicksort(a,in, 0, len-1);
	    }
	   public static void quicksort(float[] a,int len) 
	   {
	        //shuffle(a,in);                        // to guard against worst-case
	        quicksort(a, 0, len-1);
	    }
	   public static void quicksort(double[] a,int len) 
	   {
	        //shuffle(a,in);                        // to guard against worst-case
	        quicksort(a, 0, len-1);
	    }


	    // quicksort a[left] to a[right]
	    public static void quicksort(double[] a,long in[], int left, int right) 
	    {
	        if (right <= left) return;
	        int i = partition(a,in, left, right);
	        quicksort(a,in, left, i-1);
	        quicksort(a,in, i+1, right);
	    }
	    public static void quicksort(double[] a,int in[], int left, int right) {
	        if (right <= left) return;
	        int i = partition(a,in, left, right);
	        quicksort(a,in, left, i-1);
	        quicksort(a,in, i+1, right);
	    }
	    public static void quicksort(int[] a,int in[], int left, int right) {
	        if (right <= left) return;
	        int i = partition(a,in, left, right);
	        quicksort(a,in, left, i-1);
	        quicksort(a,in, i+1, right);
	    }
	    public static void quicksort(long[] a,int in[], int left, int right) {
	        if (right <= left) return;
	        int i = partition(a,in, left, right);
	        quicksort(a,in, left, i-1);
	        quicksort(a,in, i+1, right);
	    }
	    public static void quicksort(String[] a,int in[],int left, int right) 
	    {
	        if (right <= left) return;
	        int i = partition(a,in, left, right);
	        quicksort(a,in, left, i-1);
	        quicksort(a,in, i+1, right);
	    }
	    public static void quicksort(String[] a, int left, int right) 
	    {
	        if (right <= left) return;
	        int i = partition(a, left, right);
	        quicksort(a, left, i-1);
	        quicksort(a, i+1, right);
	    }
	    public static void quicksort(int[] a, int left, int right) 
	    {
	        if (right <= left) return;
	        int i = partition(a, left, right);
	        quicksort(a, left, i-1);
	        quicksort(a, i+1, right);
	    }
	    public static void quicksort(float[] a,int in[], int left, int right) {
	        if (right <= left) return;
	        int i = partition(a,in, left, right);
	        quicksort(a,in, left, i-1);
	        quicksort(a,in, i+1, right);
	    }

	    public static void quicksort(float[] a, int left, int right) {
	        if (right <= left) return;
	        int i = partition(a, left, right);
	        quicksort(a, left, i-1);
	        quicksort(a, i+1, right);
	    }
	    public static void quicksort(double[] a, int left, int right) {
	        if (right <= left) return;
	        int i = partition(a, left, right);
	        quicksort(a, left, i-1);
	        quicksort(a, i+1, right);
	    }

	    // partition a[left] to a[right], assumes left < right
	    private static int partition(double[] a,long in[], int left, int right) {
	        int i = left - 1;
	        int j = right;
	        while (true) {
	            while (a[++i]<a[right])      // find item on left to swap
	                ;                               // a[right] acts as sentinel
	            while (a[right]<a[--j])      // find item on right to swap
	                if (j == left) break;           // don't go out-of-bounds
	            if (i >= j) break;                  // check if pointers cross
	            exch(a,in, i, j);                      // swap two elements into place
	        }
	        exch(a,in, i, right);                      // swap with partition element
	        return i;
	    }
	    // partition a[left] to a[right], assumes left < right
	    private static int partition(double[] a,int in[], int left, int right) {
	        int i = left - 1;
	        int j = right;
	        while (true) {
	            while (a[++i]<a[right])      // find item on left to swap
	                ;                               // a[right] acts as sentinel
	            while (a[right]<a[--j])      // find item on right to swap
	                if (j == left) break;           // don't go out-of-bounds
	            if (i >= j) break;                  // check if pointers cross
		        if(a[i]!=a[j])
		        	exch(a,in, i, j);                      // swap two elements into place
	        }
	        if(a[i]!=a[right])
	        	exch(a,in, i, right);                      // swap with partition element
	        return i;
	    }
	    // partition a[left] to a[right], assumes left < right
	    private static int partition(long[] a,int in[], int left, int right) {
	        int i = left - 1;
	        int j = right;
	        while (true) {
	            while (a[++i]<a[right])      // find item on left to swap
	                ;                               // a[right] acts as sentinel
	            while (a[right]<a[--j])      // find item on right to swap
	                if (j == left) break;           // don't go out-of-bounds
	            if (i >= j) break;                  // check if pointers cross
	            exch(a,in, i, j);                      // swap two elements into place
	        }
	        exch(a,in, i, right);                      // swap with partition element
	        return i;
	    }
	    private static int partition(int[] a,int in[], int left, int right) {
	        int i = left - 1;
	        int j = right;
	        while (true) {
	            while (a[++i]<a[right])      // find item on left to swap
	                ;                               // a[right] acts as sentinel
	            while (a[right]<a[--j])      // find item on right to swap
	                if (j == left) break;           // don't go out-of-bounds
	            if (i >= j) break;                  // check if pointers cross
	            exch(a,in, i, j);                      // swap two elements into place
	        }
	        exch(a,in, i, right);                      // swap with partition element
	        return i;
	    }
	    private static int partition(String[] a,int in[], int left, int right) 
	    {
	        int i = left - 1;
	        int j = right;
	        while (true) {
	            while (a[++i].compareToIgnoreCase(a[right])<0)      // find item on left to swap
	                ;                               // a[right] acts as sentinel
	            while (a[right].compareToIgnoreCase(a[--j])<0)      // find item on right to swap
	                if (j == left) break;           // don't go out-of-bounds
	            if (i >= j) break;                  // check if pointers cross
	            exch(a,in, i, j);                      // swap two elements into place
	        }
	        exch(a,in, i, right);                      // swap with partition element
	        return i;
	    }
	    private static int partition(String[] a, int left, int right) 
	    {
	        int i = left - 1;
	        int j = right;
	        while (true) {
	            while (a[++i].compareToIgnoreCase(a[right])<0)      // find item on left to swap
	                ;                               // a[right] acts as sentinel
	            while (a[right].compareToIgnoreCase(a[--j])<0)      // find item on right to swap
	                if (j == left) break;           // don't go out-of-bounds
	            if (i >= j) break;                  // check if pointers cross
	            exch(a, i, j);                      // swap two elements into place
	        }
	        exch(a, i, right);                      // swap with partition element
	        return i;
	    }
	    private static int partition(int[] a, int left, int right) 
	    {
	        int i = left - 1;
	        int j = right;
	        while (true) {
	            while (a[++i]<a[right])      // find item on left to swap
	                ;                               // a[right] acts as sentinel
	            while (a[right]<a[--j])      // find item on right to swap
	                if (j == left) break;           // don't go out-of-bounds
	            if (i >= j) break;                  // check if pointers cross
	            exch(a, i, j);                      // swap two elements into place
	        }
	        exch(a, i, right);                      // swap with partition element
	        return i;
	    }
	    private static int partition(float[] a,int in[], int left, int right) {
	        int i = left - 1;
	        int j = right;
	        while (true) {
	            while (a[++i]<a[right])      // find item on left to swap
	                ;                               // a[right] acts as sentinel
	            while (a[right]<a[--j])      // find item on right to swap
	                if (j == left) break;           // don't go out-of-bounds
	            if (i >= j) break;                  // check if pointers cross
	            exch(a,in, i, j);                      // swap two elements into place
	        }
	        exch(a,in, i, right);                      // swap with partition element
	        return i;
	    }
	    private static int partition(float[] a, int left, int right) {
	        int i = left - 1;
	        int j = right;
	        while (true) {
	            while (a[++i]<a[right])      // find item on left to swap
	                ;                               // a[right] acts as sentinel
	            while (a[right]<a[--j])      // find item on right to swap
	                if (j == left) break;           // don't go out-of-bounds
	            if (i >= j) break;                  // check if pointers cross
	            exch(a, i, j);                      // swap two elements into place
	        }
	        exch(a, i, right);                      // swap with partition element
	        return i;
	    }
	    private static int partition(double[] a, int left, int right) {
	        int i = left - 1;
	        int j = right;
	        while (true) {
	            while (a[++i]<a[right])      // find item on left to swap
	                ;                               // a[right] acts as sentinel
	            while (a[right]<a[--j])      // find item on right to swap
	                if (j == left) break;           // don't go out-of-bounds
	            if (i >= j) break;                  // check if pointers cross
	            exch(a, i, j);                      // swap two elements into place
	        }
	        exch(a, i, right);                      // swap with partition element
	        return i;
	    }

	    // exchange a[i] and a[j]
	    private static void exch(double[] a,long in[], int i, int j) {
	    	double T;
	    	long ti;
	    	ti = in[i];
	    	in[i] = in[j];
	    	in[j] = ti;
            T = a[i]; 
            a[i] = a[j];
            a[j] = T;
	    }
	    // exchange a[i] and a[j]
	    private static void exch(double[] a,int in[], int i, int j) {
	    	double T;
	    	int ti;
	    	ti = in[i];
	    	in[i] = in[j];
	    	in[j] = ti;
            T = a[i]; 
            a[i] = a[j];
            a[j] = T;
	    }
	    // exchange a[i] and a[j]
	    private static void exch(long[] a,int in[], int i, int j) {
	    	long T;
	    	int ti;
	    	ti = in[i];
	    	in[i] = in[j];
	    	in[j] = ti;
            T = a[i]; 
            a[i] = a[j];
            a[j] = T;
	    }

	    // exchange a[i] and a[j]
	    private static void exch(int[] a,int in[], int i, int j) {
	    	int T;
	    	int ti;
	    	ti = in[i];
	    	in[i] = in[j];
	    	in[j] = ti;
            T = a[i]; 
            a[i] = a[j];
            a[j] = T;
	    }

	    // exchange a[i] and a[j]
	    private static void exch(String[] a,int in[], int i, int j) 
	    {
	    	String T;
	    	int ti;
	    	ti = in[i];
	    	in[i] = in[j];
	    	in[j] = ti;
            T = a[i]; 
            a[i] = a[j];
            a[j] = T;
	    }
	    private static void exch(String[] a, int i, int j) 
	    {
	    	String T;
            T = a[i]; 
            a[i] = a[j];
            a[j] = T;
	    }
	    private static void exch(int[] a, int i, int j) 
	    {
	    	int T;
            T = a[i]; 
            a[i] = a[j];
            a[j] = T;
	    }

	    // exchange a[i] and a[j]
	    private static void exch(float[] a,int in[], int i, int j) {
	    	float T;
	    	int ti;
	    	ti = in[i];
	    	in[i] = in[j];
	    	in[j] = ti;
            T = a[i]; 
            a[i] = a[j];
            a[j] = T;
	    }
	    
	    // exchange a[i] and a[j]
	    private static void exch(float[] a, int i, int j) 
	    {
	    	float T;
            T = a[i]; 
            a[i] = a[j];
            a[j] = T;
	    }
	    // exchange a[i] and a[j]
	    private static void exch(double[] a, int i, int j) 
	    {
	    	double T;
            T = a[i]; 
            a[i] = a[j];
            a[j] = T;
	    }
	    
	    // shuffle the array a[]
	    private static void shuffle(double[] a,long in[]) {
	        int N = a.length;
	        for (int i = 0; i < N; i++) {
	            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
	            exch(a,in, i, r);
	        }
	    }
	    // shuffle the array a[]
	    private static void shuffle(double[] a,int in[]) {
	        int N = a.length;
	        for (int i = 0; i < N; i++) {
	            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
	            exch(a,in, i, r);
	        }
	    }

	    // shuffle the array a[]
	    private static void shuffle(int[] a,int in[]) {
	        int N = a.length;
	        for (int i = 0; i < N; i++) {
	            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
	            exch(a,in, i, r);
	        }
	    }

	    // shuffle the array a[]
	    private static void shuffle(float[] a,int in[]) {
	        int N = a.length;
	        for (int i = 0; i < N; i++) {
	            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
	            exch(a,in, i, r);
	        }
	    }
	    

	public static void sort(double a[],long in[]) 
	{ 
		quicksort(a,in);
	}

	public static void sort(int a[],int len) 
	{ 
		quicksort(a,len);
	}

	public static void sort(float a[],int in[]) 
	{ 
		quicksort(a,in,a.length);
	}
	public static void sort(float a[],int in[],int len) 
	{ 
		quicksort(a,in,len);
	}
}
