package animation;

import procedural.human.Human;
import sparsedatabase.PropertyHashtable;
import sparsedatabase.PropertyMatrixFloat;
import mathematics.GeneralMatrixFloat;

public class Pose 
{
	public GeneralMatrixFloat vertexPositions;// = new GeneralMatrixFloat(3,15);
	public GeneralMatrixFloat boneMatrices;// = new GeneralMatrixFloat(9,14);
	public GeneralMatrixFloat boneWidths;// = new GeneralMatrixFloat(3,14);

	public GeneralMatrixFloat volumeEstimates = new GeneralMatrixFloat(15,6);
	public GeneralMatrixFloat volumeEstimates_detailed = new GeneralMatrixFloat(15,10);
	public GeneralMatrixFloat volumeEstimates_detailed2 = new GeneralMatrixFloat(15,12);

	public Pose()
	{
	}
	
	public Pose(int vs,int bs)
	{
		vertexPositions = new GeneralMatrixFloat(3,vs);
		boneMatrices = new GeneralMatrixFloat(9,bs);

		boneWidths = new GeneralMatrixFloat(3,bs);		
	}
	public Pose(Pose op)
	{
		vertexPositions = new GeneralMatrixFloat(op.vertexPositions);
		boneMatrices = new GeneralMatrixFloat(op.boneMatrices);
		boneWidths = new GeneralMatrixFloat(op.boneWidths);
		volumeEstimates.set(op.volumeEstimates);
		volumeEstimates_detailed.set(op.volumeEstimates_detailed);
		volumeEstimates_detailed2.set(op.volumeEstimates_detailed2);
	}
	public void setSkeleton(Human skeleton)
	{
		skeleton.pose.vertexPositions.set(vertexPositions);
		skeleton.pose.boneMatrices.set(boneMatrices);
		skeleton.pose.boneWidths.set(boneWidths);

		skeleton.pose.volumeEstimates.set(volumeEstimates);
		skeleton.pose.volumeEstimates_detailed.set(volumeEstimates_detailed);
		skeleton.pose.volumeEstimates_detailed2.set(volumeEstimates_detailed2);
	}
	
	public void set(Pose p)
	{
		vertexPositions.set(p.vertexPositions);
		boneMatrices.set(p.boneMatrices);
		boneWidths.set(p.boneWidths);

		volumeEstimates.set(p.volumeEstimates);
		volumeEstimates_detailed.set(p.volumeEstimates_detailed);
		volumeEstimates_detailed2.set(p.volumeEstimates_detailed2);
	}
	
	public void saveAsProperty(PropertyHashtable root)
	{
		PropertyMatrixFloat pvpos = new PropertyMatrixFloat(root, vertexPositions, "vpos");
		PropertyMatrixFloat pbmat = new PropertyMatrixFloat(root, boneMatrices, "bmats");
		PropertyMatrixFloat pbws = new PropertyMatrixFloat(root, boneWidths, "bws");
		PropertyMatrixFloat pves = new PropertyMatrixFloat(root, volumeEstimates, "ves");
		//PropertyMatrixFloat pvesd = new PropertyMatrixFloat(root, volumeEstimates_detailed, "vesd");
		PropertyMatrixFloat pvesd = new PropertyMatrixFloat(root, volumeEstimates_detailed2, "vesd");
	}
	
	public void loadFromProperty(PropertyHashtable root)
	{
		PropertyMatrixFloat pvpos = (PropertyMatrixFloat)root.GetProperty("vpos");
		PropertyMatrixFloat pbmat = (PropertyMatrixFloat)root.GetProperty("bmats");
		PropertyMatrixFloat pbws = (PropertyMatrixFloat)root.GetProperty("bws");
		PropertyMatrixFloat pves = (PropertyMatrixFloat)root.GetProperty("ves");
		PropertyMatrixFloat pvesd = (PropertyMatrixFloat)root.GetProperty("vesd");
		PropertyMatrixFloat pvesd2 = (PropertyMatrixFloat)root.GetProperty("vesd2");
		
		vertexPositions = pvpos.matrix;
		boneMatrices = pbmat.matrix;
		boneWidths = pbws.matrix;
		volumeEstimates = pves.matrix;
		volumeEstimates_detailed = pvesd.matrix;
		volumeEstimates_detailed2 = pvesd2.matrix;
	}
	
	public void setVolumeEstimate(int v,float x,float y,float z,float r)
	{
		volumeEstimates.value[v*volumeEstimates.width+0] = x;
		volumeEstimates.value[v*volumeEstimates.width+1] = y;
		volumeEstimates.value[v*volumeEstimates.width+2] = z;

		volumeEstimates.value[v*volumeEstimates.width+3] = 1.0f;
		volumeEstimates.value[v*volumeEstimates.width+4] = 0.0f;
		volumeEstimates.value[v*volumeEstimates.width+5] = 0.0f;
		volumeEstimates.value[v*volumeEstimates.width+6] = r;

		volumeEstimates.value[v*volumeEstimates.width+7] = 0.0f;
		volumeEstimates.value[v*volumeEstimates.width+8] = 1.0f;
		volumeEstimates.value[v*volumeEstimates.width+9] = 0.0f;
		volumeEstimates.value[v*volumeEstimates.width+10] = r;

		volumeEstimates.value[v*volumeEstimates.width+11] = 0.0f;
		volumeEstimates.value[v*volumeEstimates.width+12] = 0.0f;
		volumeEstimates.value[v*volumeEstimates.width+13] = 1.0f;
		volumeEstimates.value[v*volumeEstimates.width+14] = r;
	}	
}
