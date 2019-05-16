package registration;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;

public class GeneralisedProcrustes 
{
	public static void align(GeneralMatrixFloat pointsets,
			GeneralMatrixFloat pointsetTransforms,
			GeneralMatrixFloat meanPointSet,
			GeneralMatrixInt bestSet)
	{
		float thresh = 0.0001f;
		GeneralMatrixFloat a = new GeneralMatrixFloat(3);
		GeneralMatrixFloat b = new GeneralMatrixFloat(3);
		GeneralMatrixFloat procrustesTransform = new GeneralMatrixFloat(4,4);
		
		GeneralMatrixFloat newmean = new GeneralMatrixFloat(meanPointSet.width,meanPointSet.height);
		GeneralMatrixInt meancount = new GeneralMatrixInt(1,meanPointSet.height);
		GeneralMatrixFloat bestcost = new GeneralMatrixFloat(1,meanPointSet.height);
		
		Procrustes proc = new Procrustes();
		
		//pick a starting meanpointset
		for(int i=0;i<pointsets.width;i++)
			meanPointSet.value[i] = pointsets.value[i];
		
		int itr = 0;
		while(itr<100)
		{
			itr++;
			newmean.clear(0.0f);
			meancount.clear(0);
			//Calc procrustes alignment from the each pointsets to the mean set
			int ind = 0;
			for(int i=0;i<pointsets.height;i++)
			{
				a.height = 0;
				b.height = 0;
				int sind = ind;
				for(int pi=0;pi<pointsets.width;pi+=3)
				{
					float v = pointsets.value[ind];
					float mv = meanPointSet.value[pi];
					if((v!=Float.MAX_VALUE)&&(mv!=Float.MAX_VALUE))
					{
						a.push_back(pointsets.value[ind+0]);
						a.push_back(pointsets.value[ind+1]);
						a.push_back(pointsets.value[ind+2]);
						
						b.push_back(meanPointSet.value[pi+0]);
						b.push_back(meanPointSet.value[pi+1]);
						b.push_back(meanPointSet.value[pi+2]);
					}
					ind+=3;
				}
				
				if(a.height==0)
				{
					
				}
				else
				{
					proc.init(a.height, 3);
					proc.applyNoScale(a, b, procrustesTransform);
					
					int pind = 0;
					
					for(int pi=0;pi<16;pi+=3)
					{
						pointsetTransforms.value[16*i+pi] = procrustesTransform.value[pi];
					}
					
					for(int pi=0;pi<pointsets.width;pi+=3)
					{
						float v = pointsets.value[sind];
						if(v!=Float.MAX_VALUE)
						{
							meancount.value[pi/3]++;
							
							for (int vi=0; vi<3; vi++) 
							{
								newmean.value[pi+vi] += 
										a.value[pind+0] * procrustesTransform.value[0*4+vi] +
										a.value[pind+1] * procrustesTransform.value[1*4+vi] +
										a.value[pind+2] * procrustesTransform.value[2*4+vi] +
										1.0f * procrustesTransform.value[3*4+vi];
							}
	
							pind += 3;
						}
						sind+=3;
					}
				}
			}
			
			float meansqrdelta = 0.0f;
			//Update the mean using the transformed version of each pointset
			for(int pi=0;pi<newmean.height;pi++)
			{
				if(meancount.value[pi]==0)
				{
					meanPointSet.value[pi*3+0] = Float.MAX_VALUE;
					meanPointSet.value[pi*3+1] = Float.MAX_VALUE;
					meanPointSet.value[pi*3+2] = Float.MAX_VALUE;
				}
				else
				{
					int pi3 = pi*3;
					
					float nm0 = newmean.value[pi3+0] / meancount.value[pi];
					float nm1 = newmean.value[pi3+0] / meancount.value[pi];
					float nm2 = newmean.value[pi3+0] / meancount.value[pi];
					
					if(meanPointSet.value[pi3+0]==Float.MAX_VALUE)
					{
						
					}
					else
					{
						float d0 = nm0-meanPointSet.value[pi3+0];
						float d1 = nm1-meanPointSet.value[pi3+1];
						float d2 = nm2-meanPointSet.value[pi3+2];
						meansqrdelta += d0*d0;
						meansqrdelta += d1*d1;
						meansqrdelta += d2*d2;
						meanPointSet.value[pi3+0] = nm0;
						meanPointSet.value[pi3+1] = nm1;
						meanPointSet.value[pi3+2] = nm2;
					}				
				}
			}
			if(meansqrdelta<thresh)
			{
				break;
			}
		}

		bestcost.clear(Float.MAX_VALUE);
		int sind = 0;
		for(int i=0;i<pointsets.height;i++)
		{
			for(int pi=0;pi<pointsets.width;pi+=3)
			{
				float v = pointsets.value[sind];
				if(v!=Float.MAX_VALUE)
				{
					float err = 0.0f;
					for (int vi=0; vi<3; vi++) 
					{
						float e = meanPointSet.value[pi+0] - 
								pointsets.value[sind+0] * pointsetTransforms.value[16*i+0*4+vi] +
								pointsets.value[sind+1] * pointsetTransforms.value[16*i+1*4+vi] +
								pointsets.value[sind+2] * pointsetTransforms.value[16*i+2*4+vi] +
								1.0f * pointsetTransforms.value[16*i+3*4+vi];
						err += e*e;
					}
					int pi_3 = pi/3;
					if(err<bestcost.value[pi_3])
					{
						bestSet.value[pi_3] = i;
						bestcost.value[pi_3] = err;
					}
				}
				sind+=3;
			}
		}				
	}
}
