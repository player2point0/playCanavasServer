package rendering;

public class VolumeBufferFloat 
{
	//A generalisation of a render buffer float to a volume
	public int width;
	public int height;
	public int depth;
	public float[] voxels;

	public VolumeBufferFloat(int w,int h,int d)
	{
		width = w;
		height = h;
		depth = d;
		voxels = new float[w*h*d];
	}

}
