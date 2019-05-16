package animation;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixString;
import mathematics.distance.Ellipsoid;

public class Skeleton 
{
	//The definition of a skeleton
	public GeneralMatrixString jointNames;// = new GeneralMatrixString(1,15,15);
	public GeneralMatrixString boneNames;// = new GeneralMatrixString(1,14,15);

	//Bones are links between vertexes in the skeleton (a single real bone, like the pelvis, can be made of multiple model bones)
	public GeneralMatrixInt bones;// = new GeneralMatrixInt(2,14);

	//Useful connection information
	//The bone that this vertex 
	public GeneralMatrixInt boneParents;// = new GeneralMatrixInt(1,14);
	//The order to calculate the bones so that the parent is always calculated before the child
	public GeneralMatrixInt boneUpdateOrder;// = new GeneralMatrixInt(1,14);

	//To a large extent this defines a particular skeletons shape (i.e. where all the joints are relative to one another)
	//How to get from the parent vertex to the child vertex of a bone (in the space of the bone)
	public GeneralMatrixFloat boneUntransformedChildVertexFromParentBone;// = new GeneralMatrixFloat(3,14);

	
	//A given pose, or in this case the bind pose
	//The relative orientation of the bones of the skeleton (rotation order is Z, X,  then Y)
	public GeneralMatrixFloat boneRotationFromParentBone;// = new GeneralMatrixFloat(3,14);
	//Some joints have translational capability
	//public GeneralMatrixFloat boneTranslationFromNormalChildPosition;// = new GeneralMatrixFloat(3,14);
	
	public Skeleton(int vs,int bs)
	{
		//The definition of a skeleton
		jointNames = new GeneralMatrixString(1,vs,vs);
		boneNames = new GeneralMatrixString(1,bs,bs);

		//Bones are links between vertexes in the skeleton (a single real bone, like the pelvis, can be made of multiple model bones)
		bones = new GeneralMatrixInt(2,bs);

		//Useful connection information
		//The bone that this vertex 
		boneParents = new GeneralMatrixInt(1,bs);
		//The order to calculate the bones so that the parent is always calculated before the child
		boneUpdateOrder = new GeneralMatrixInt(1,bs);

		//To a large extent this defines a particular skeletons shape (i.e. where all the joints are relative to one another)
		//How to get from the parent vertex to the child vertex of a bone (in the space of the bone)
		boneUntransformedChildVertexFromParentBone = new GeneralMatrixFloat(3,bs);

		
		//A given pose, or in this case the bind pose
		//The relative orientation of the bones of the skeleton (rotation order is Z, X,  then Y)
		boneRotationFromParentBone = new GeneralMatrixFloat(3,bs);
		//Some joints have translational capability
		//public GeneralMatrixFloat boneTranslationFromNormalChildPosition = new GeneralMatrixFloat(3,bs);		
	}

	public static void calculateWorldPoseFromLocalSkeleton(Skeleton s,Pose p)
	{		
		GeneralMatrixFloat euler = new GeneralMatrixFloat(3,1);
		GeneralMatrixFloat rot = new GeneralMatrixFloat(3,3);
		GeneralMatrixFloat prot = new GeneralMatrixFloat(3,3);
		GeneralMatrixFloat wrot = new GeneralMatrixFloat(3,3);

		for(int i=0;i<s.boneUpdateOrder.height;i++)
		{
			int b = s.boneUpdateOrder.value[i];
			int parent = s.boneParents.value[i];
			
			if(parent==-1)
			{
				euler.setFromSubset(s.boneRotationFromParentBone, b);
				rot.width = 3;
				rot.height = 3;
				rot.set3DTransformRotation(euler.value[0], euler.value[1], euler.value[2]);
				rot.width = 9;
				rot.height = 1;
				p.boneMatrices.setRow(b,rot);

				//vertexPositions remains unchanged
			}
			else
			{
				euler.setFromSubset(s.boneRotationFromParentBone, b);
				rot.width = 3;
				rot.height = 3;
				rot.set3DTransformRotation(euler.value[0], euler.value[1], euler.value[2]);
				
				euler.setFromSubset(s.boneRotationFromParentBone, parent);
				prot.width = 3;
				prot.height = 3;
				prot.set3DTransformRotation(euler.value[0], euler.value[1], euler.value[2]);
				
				wrot.width = 3;
				wrot.height = 3;
				GeneralMatrixFloat.mult(prot, rot, wrot);
				
				int v0 = s.bones.value[b*2+0];
				int v1 = s.bones.value[b*2+1];
				
				wrot.width = 9;
				wrot.height = 1;
				p.boneMatrices.setRow(b,wrot);
				
				p.vertexPositions.value[v1*3+0] = p.vertexPositions.value[v0*3+0]
				            +wrot.value[3*0+0]*s.boneUntransformedChildVertexFromParentBone.value[3*b+0]
				            +wrot.value[3*1+0]*s.boneUntransformedChildVertexFromParentBone.value[3*b+1]
				            +wrot.value[3*2+0]*s.boneUntransformedChildVertexFromParentBone.value[3*b+2];
				p.vertexPositions.value[v1*3+1] = p.vertexPositions.value[v0*3+1]
				    				            +wrot.value[3*0+1]*s.boneUntransformedChildVertexFromParentBone.value[3*b+0]
				    				            +wrot.value[3*1+1]*s.boneUntransformedChildVertexFromParentBone.value[3*b+1]
				    				            +wrot.value[3*2+1]*s.boneUntransformedChildVertexFromParentBone.value[3*b+2];
				p.vertexPositions.value[v1*3+2] = p.vertexPositions.value[v0*3+2]
				    				            +wrot.value[3*0+2]*s.boneUntransformedChildVertexFromParentBone.value[3*b+0]
				    				            +wrot.value[3*1+2]*s.boneUntransformedChildVertexFromParentBone.value[3*b+1]
				    				            +wrot.value[3*2+2]*s.boneUntransformedChildVertexFromParentBone.value[3*b+2];
			}
		}
	}

	public static void calculateLocalSkeletonFromWorldPose(Skeleton s,Pose p)
	{
		GeneralMatrixFloat euler = new GeneralMatrixFloat(3,1);
		GeneralMatrixFloat rot = new GeneralMatrixFloat(3,3);
		GeneralMatrixFloat prot = new GeneralMatrixFloat(3,3);
		GeneralMatrixFloat iprot = new GeneralMatrixFloat(3,3);
		GeneralMatrixFloat lrot = new GeneralMatrixFloat(3,3);
		for(int i=0;i<s.bones.height;i++)
		{
			int parent = s.boneParents.value[i];
			if(parent==-1)
			{
				lrot.width = 9;
				lrot.height = 1;
				lrot.setFromSubset(p.boneMatrices, i);
				lrot.width = 3;
				lrot.height = 3;
			}
			else
			{
				rot.width = 9;
				rot.height = 1;
				rot.setFromSubset(p.boneMatrices, i);
				rot.width = 3;
				rot.height = 3;

				prot.width = 9;
				prot.height = 1;
				prot.setFromSubset(p.boneMatrices, parent);
				prot.width = 3;
				prot.height = 3;
				GeneralMatrixFloat.invert(prot, iprot);
				
				GeneralMatrixFloat.mult(iprot, rot, lrot);
			}
			
			GeneralMatrixFloat.getEuler(lrot, euler);
			s.boneRotationFromParentBone.value[i*3+0] = euler.value[0];
			s.boneRotationFromParentBone.value[i*3+1] = euler.value[1];
			s.boneRotationFromParentBone.value[i*3+2] = euler.value[2];			
		}
		
		for(int i=0;i<s.bones.height;i++)
		{
			s.boneUntransformedChildVertexFromParentBone.clear(0.0f);
			s.boneUntransformedChildVertexFromParentBone.value[1] = p.boneWidths.value[1]*2.0f;
		}
	}

	public static float distanceFromVolume(int v,float x,float y,float z,GeneralMatrixFloat volumeEstimates)
	{
		int o = v*volumeEstimates.width;
		return Ellipsoid.distanceFrom(
				x,y,z,
				volumeEstimates.value[o+0], volumeEstimates.value[o+1], volumeEstimates.value[o+2], 
				volumeEstimates.value[o+3], volumeEstimates.value[o+4], volumeEstimates.value[o+5], 
				volumeEstimates.value[o+7], volumeEstimates.value[o+8], volumeEstimates.value[o+9], 
				volumeEstimates.value[o+11], volumeEstimates.value[o+12], volumeEstimates.value[o+13], 
				volumeEstimates.value[o+6], volumeEstimates.value[o+10], volumeEstimates.value[o+14]);
		
	}
	
	public float distanceFromBone(int b,float x,float y,float z, Pose p)
	{
		int v0 = bones.value[b*2+0];
		int v1 = bones.value[b*2+1];
		float bx = (p.vertexPositions.value[v0*3+0]+p.vertexPositions.value[v1*3+0])*0.5f;
		float by = (p.vertexPositions.value[v0*3+1]+p.vertexPositions.value[v1*3+1])*0.5f;
		float bz = (p.vertexPositions.value[v0*3+2]+p.vertexPositions.value[v1*3+2])*0.5f;
		return Ellipsoid.distanceFrom(
				x,y,z,
				bx, by, bz, 
				p.boneMatrices.value[b*9+3*0+0], p.boneMatrices.value[b*9+3*0+1], p.boneMatrices.value[b*9+3*0+2], 
				p.boneMatrices.value[b*9+3*1+0], p.boneMatrices.value[b*9+3*1+1], p.boneMatrices.value[b*9+3*1+2], 
				p.boneMatrices.value[b*9+3*2+0], p.boneMatrices.value[b*9+3*2+1], p.boneMatrices.value[b*9+3*2+2], 
				p.boneWidths.value[b*3+0], p.boneWidths.value[b*3+1], p.boneWidths.value[b*3+2]);
	}
	
	public float getBoneLength(int b, Pose p)
	{
		int v0 = bones.value[b*2+0];
		int v1 = bones.value[b*2+1];
		float dx = p.vertexPositions.value[v0*3+0]-p.vertexPositions.value[v1*3+0];
		float dy = p.vertexPositions.value[v0*3+1]-p.vertexPositions.value[v1*3+1];
		float dz = p.vertexPositions.value[v0*3+2]-p.vertexPositions.value[v1*3+2];
		return (float)Math.sqrt(dx*dx+dy*dy+dz*dz);
	}
	
	public void getBoneCente(int b,GeneralMatrixFloat bonePos, Pose p)
	{
		int v0 = bones.value[b*2+0];
		int v1 = bones.value[b*2+1];
		bonePos.value[0] = (p.vertexPositions.value[v0*3+0]+p.vertexPositions.value[v1*3+0])*0.5f;
		bonePos.value[1] = (p.vertexPositions.value[v0*3+1]+p.vertexPositions.value[v1*3+1])*0.5f;
		bonePos.value[2] = (p.vertexPositions.value[v0*3+2]+p.vertexPositions.value[v1*3+2])*0.5f;
		
	}
}
