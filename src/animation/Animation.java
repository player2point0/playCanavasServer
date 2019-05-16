package animation;

import sparsedatabase.PropertyHashtable;
import sparsedatabase.PropertyList;
import sparsedatabase.PropertyMatrixFloat;
import sparsedatabase.SparseDatabase;
import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixObject;

public class Animation 
{
	public float starttime = 0.0f;
	public float endtime = 100.0f;
	public GeneralMatrixFloat frames = new GeneralMatrixFloat(1);
	public GeneralMatrixObject poses = new GeneralMatrixObject(1);
	
	//No interpolation just find the closest recorded frame
	public void getPose(float frame,Pose p)
	{
		float pFrame = 0.0f;
		float nFrame = 0.0f;
		Pose pPose = null;
		Pose nPose = null;
		for(int i=0;i<frames.height;i++)
		{
			float f = frames.value[i];
			if(f<frame)
			{
				pFrame = f;
				pPose = (Pose)poses.value[i];				
			}
			else
			{
				nFrame = f;
				nPose = (Pose)poses.value[i];
				break;
			}
		}
		if(pPose==null)
		{
			p.set(nPose);
		}
		else
		if(nPose==null)
		{
			p.set(pPose);			
		}
		else
		{
			float dp = frame-pFrame;
			float dn = nFrame-frame;
			if(dp<dn)
				p.set(pPose);
			else
				p.set(nPose);
		}
	}

	public void append(Animation anim,float startTime)
	{
		for(int i=0;i<anim.frames.height;i++)
		{
			frames.push_back(anim.frames.value[i]+startTime);
			poses.push_back(anim.poses.value[i]);
		}
	}
	
	public void save(String toPath)
	{
		PropertyHashtable proot = new PropertyHashtable();
		PropertyMatrixFloat pf = new PropertyMatrixFloat(proot,frames,"frames");
		PropertyList pl = new PropertyList(proot,poses.height,"poses");
		for(int i=0;i<poses.height;i++)
		{
			PropertyHashtable poser = new PropertyHashtable();
			Pose p = (Pose)poses.value[i];
			p.saveAsProperty(poser);
			pl.entries.value[i] = poser;
		}
		SparseDatabase.SaveEntryVerbose(proot, toPath);
	}

	public void load(String fromPath)
	{
		PropertyHashtable proot = SparseDatabase.LoadEntryFromFileOutsideOfDatabase(fromPath);
		PropertyMatrixFloat pf = (PropertyMatrixFloat)proot.GetProperty("frames");
		frames = pf.matrix;
		PropertyList pl = (PropertyList)proot.GetProperty("poses");
		for(int i=0;i<pl.entries.height;i++)
		{
			Pose p = new Pose();
			PropertyHashtable ph = (PropertyHashtable)pl.entries.value[i];
			p.loadFromProperty(ph);
			poses.push_back(p);
		}
	}
}
