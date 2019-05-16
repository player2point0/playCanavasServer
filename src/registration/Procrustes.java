package registration;

import java.util.Random;

import sort.QuickSort;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.linearalgebra.SVD;

public class Procrustes 
{
	//Working store
	GeneralMatrixFloat aShift; 
	GeneralMatrixFloat bShift; 
	GeneralMatrixFloat aTranspose; 
	GeneralMatrixFloat bTranspose; 
	GeneralMatrixFloat svd_U; 
	GeneralMatrixFloat svd_W; 
	GeneralMatrixFloat svd_V; 
	GeneralMatrixFloat svd_VT; 
	GeneralMatrixFloat aRotated; 
	GeneralMatrixFloat bRotated; 
	GeneralMatrixFloat rotation; 
	GeneralMatrixFloat product; 
	
	static GeneralMatrixFloat rotation_origin = new GeneralMatrixFloat(3,1);
	static GeneralMatrixFloat rotation_transform = new GeneralMatrixFloat(3,3);
	static GeneralMatrixFloat translation = new GeneralMatrixFloat(3,1);
	static GeneralMatrixFloat scale = new GeneralMatrixFloat(1,1);

	static GeneralMatrixFloat temppTransform1 = new GeneralMatrixFloat(4,4);
	static GeneralMatrixFloat temppTransform2 = new GeneralMatrixFloat(4,4);
	
	SVD svd;
	
	public Procrustes()
	{
		int dim = 0;
		int numPoints = 3;
		aShift = new GeneralMatrixFloat(dim,numPoints);
		bShift = new GeneralMatrixFloat(dim,numPoints);
		aTranspose = new GeneralMatrixFloat(numPoints,dim);
		bTranspose = new GeneralMatrixFloat(numPoints,dim);
		aRotated = new GeneralMatrixFloat(dim,numPoints); 
		bRotated = new GeneralMatrixFloat(dim,numPoints); 
		svd_U = new GeneralMatrixFloat(dim,dim);
		svd_W = new GeneralMatrixFloat(dim,1);
		svd_V = new GeneralMatrixFloat(dim,dim);
		svd_VT = new GeneralMatrixFloat(dim,dim);
		rotation = new GeneralMatrixFloat(dim,dim); 
		product = new GeneralMatrixFloat(numPoints,numPoints); 

		svd = new SVD(dim,dim);
	}
	public Procrustes(int numPoints,int dim)
	{
		aShift = new GeneralMatrixFloat(dim,numPoints);
		bShift = new GeneralMatrixFloat(dim,numPoints);
		aTranspose = new GeneralMatrixFloat(numPoints,dim);
		bTranspose = new GeneralMatrixFloat(numPoints,dim);
		aRotated = new GeneralMatrixFloat(dim,numPoints); 
		bRotated = new GeneralMatrixFloat(dim,numPoints); 
		svd_U = new GeneralMatrixFloat(dim,dim);
		svd_W = new GeneralMatrixFloat(dim,1);
		svd_V = new GeneralMatrixFloat(dim,dim);
		svd_VT = new GeneralMatrixFloat(dim,dim);
		rotation = new GeneralMatrixFloat(dim,dim); 
		product = new GeneralMatrixFloat(numPoints,numPoints); 

		svd = new SVD(dim,dim);
	}
	public void init(int numPoints,int dim)
	{
		aShift.setDimensions(dim, numPoints);
		bShift.setDimensions(dim, numPoints);

		aTranspose.setDimensions(numPoints, dim);
		bTranspose.setDimensions(numPoints, dim);

		aRotated.setDimensions(dim, numPoints);
		bRotated.setDimensions(dim, numPoints);

		svd_U.setDimensions(dim, dim);
		svd_W.setDimensions(dim, 1);
		svd_V.setDimensions(dim, dim);
		svd_VT.setDimensions(dim, dim);

		rotation.setDimensions(dim, dim);
		product.setDimensions(numPoints, numPoints);
	}
//	public void init(int numPoints,int dim)
//	{
//		aShift.width = dim;
//		bShift.width = dim;
//		aTranspose.width = numPoints;
//		bTranspose.width = numPoints;
//		aRotated.width = dim;
//		bRotated.width = dim;
//		svd_U.width = dim;
//		svd_W.width = dim;
//		svd_V.width = dim;
//		svd_VT.width = dim;
//		rotation.width = dim;
//		product.width = numPoints;
//
//		aShift.height = numPoints;
//		bShift.height = numPoints;
//		aTranspose.height = dim;
//		bTranspose.height = dim;
//		aRotated.height = numPoints;
//		bRotated.height = numPoints;
//		svd_U.height = dim;
//		svd_W.height = 1;
//		svd_V.height = dim;
//		svd_VT.height = dim;
//		rotation.height = dim;
//		product.height = numPoints;
//	}
	
	public void cleanup()
	{
		aShift = null;
		bShift = null;
		aTranspose = null;
		bTranspose = null;
		aRotated = null; 
		bRotated = null; 
		svd_U = null;
		svd_W = null;
		svd_V = null;
		svd_VT = null;
		rotation = null; 
		product = null; 
	}

	public void applyBoundedRobustNoScaleWeighted(
			GeneralMatrixInt loc,
			float samplesPerMeter,
			float samplesPerEuler,
			int maxLocDifference,
			float perc,
			GeneralMatrixFloat a,
			GeneralMatrixFloat b,
			GeneralMatrixFloat w,
			GeneralMatrixFloat dists,
			GeneralMatrixFloat procrustesTransform)
	{
		float totalWeight = 0.0f;
		
		GeneralMatrixFloat tempweights = new GeneralMatrixFloat(1,w.height);
		
		for(int i=0;i<w.height;i++)
		{
			totalWeight += 1.0f;//w.value[i];
		}

		int numIterations = 10;
		int procrustessubset = 4;
		long startseed = 0;
		
		Random r = new Random(startseed);
		GeneralMatrixFloat usubset = new GeneralMatrixFloat(3,procrustessubset);
		GeneralMatrixFloat xsubset = new GeneralMatrixFloat(3,procrustessubset);
		GeneralMatrixInt subset = new GeneralMatrixInt(1,procrustessubset);

		GeneralMatrixFloat tempTransform = new GeneralMatrixFloat(4,4);
		float bestMedianMatchError = Float.MAX_VALUE;
		GeneralMatrixFloat matchErrors = new GeneralMatrixFloat(1,w.height);
		GeneralMatrixInt matchErrorInd = new GeneralMatrixInt(1,w.height);

		GeneralMatrixFloat bestmatchErrors = new GeneralMatrixFloat(1,w.height);
		GeneralMatrixInt   bestmatchIndex = new GeneralMatrixInt(1,w.height);

		GeneralMatrixFloat euler = new GeneralMatrixFloat(3,1);
		
		for(int i=0;i<numIterations;i++)
		{		
			float runningTotal = totalWeight;
			//tempweights.set(w);
			tempweights.clear(1.0f);
			
			//System.out.println("itr="+i);
			for(int hi=0;hi<procrustessubset;hi++)
			{
				float f = r.nextFloat()*runningTotal;
				for(int pi=0;pi<a.height;pi++)
				{
					f -= tempweights.value[pi];
					if(f<=0.0f)
					{
						//System.out.println(""+pi);
						subset.value[hi] = pi;
						usubset.value[3*hi+0] = a.value[3*pi+0];
						usubset.value[3*hi+1] = a.value[3*pi+1];
						usubset.value[3*hi+2] = a.value[3*pi+2];
						xsubset.value[3*hi+0] = b.value[3*pi+0];
						xsubset.value[3*hi+1] = b.value[3*pi+1];
						xsubset.value[3*hi+2] = b.value[3*pi+2];
						runningTotal -= tempweights.value[pi];
						tempweights.value[pi] = 0.0f;
						break;
					}
				}
			}
			applyNoScale(usubset, xsubset, tempTransform);		
			
			//Now calc the point errors for this match
			for(int j=0;j<matchErrors.height;j++)
			{
				float ox = a.value[j*3+0];
				float oy = a.value[j*3+1];
				float oz = a.value[j*3+2];
				
				float wx =ox * tempTransform.value[0*4+0] +
				oy * tempTransform.value[1*4+0] +
				oz * tempTransform.value[2*4+0] +
				1.0f * tempTransform.value[3*4+0];
				float wy =ox * tempTransform.value[0*4+1] +
				oy * tempTransform.value[1*4+1] +
				oz * tempTransform.value[2*4+1] +
				1.0f * tempTransform.value[3*4+1];
				float wz =ox * tempTransform.value[0*4+2] +
				oy * tempTransform.value[1*4+2] +
				oz * tempTransform.value[2*4+2] +
				1.0f * tempTransform.value[3*4+2];			
				
				float dx = wx-b.value[j*3+0];
				float dy = wy-b.value[j*3+1];
				float dz = wz-b.value[j*3+2];
				
				matchErrors.value[j] = dx*dx+dy*dy+dz*dz;
				matchErrorInd.value[j] = j;
			}

			QuickSort.quicksort(matchErrors.value,matchErrorInd.value,matchErrors.height);
			
			//The measure to minimise (i.e. half are within a minimal error range)
			int medi = (int)(matchErrors.height*perc);
			float median = matchErrors.value[medi];
			
			if(median<bestMedianMatchError)
			{
				//Check to see if this is valid
				GeneralMatrixFloat.getEuler(tempTransform, euler);
				
				float fx = tempTransform.value[4*3+0];
				float fy = tempTransform.value[4*3+1];
				float fz = tempTransform.value[4*3+2];
				
				int locerror = 0;
				locerror += Math.abs(loc.value[0] - (int)(fx*samplesPerMeter));
				locerror += Math.abs(loc.value[1] - (int)(fy*samplesPerMeter));
				locerror += Math.abs(loc.value[2] - (int)(fz*samplesPerMeter));
				locerror += Math.abs(loc.value[3] - (int)(euler.value[0]*samplesPerEuler)); 
				locerror += Math.abs(loc.value[4] - (int)(euler.value[1]*samplesPerEuler)); 
				locerror += Math.abs(loc.value[5] - (int)(euler.value[2]*samplesPerEuler)); 

				if(locerror<=maxLocDifference)
				{
					bestMedianMatchError = median;
					bestmatchErrors.set(matchErrors);
					
					float maxError = (median*1.5f);
					for(int j=0;j<matchErrors.height;j++)
					{
						float error = matchErrors.value[j]; 
						
						if(error>maxError)
							dists.value[matchErrorInd.value[j]]=Float.MAX_VALUE;
						else
							dists.value[matchErrorInd.value[j]]=error;
					}
					
					bestmatchIndex.set(matchErrorInd);
					procrustesTransform.set(tempTransform);
				}
			}
		}
		
		//Now identify the points which are outliers from the best subset match
		//And simultaneously fit to all the inliers
//		for(int pi=0;pi<bestmatchIndex.height;pi++)
//		{
//		}
	}	
	public void applyRobustNoScaleWeighted(
			int numValid,
			GeneralMatrixFloat a,
			GeneralMatrixFloat b,
			GeneralMatrixFloat w,
			GeneralMatrixFloat dists,
			GeneralMatrixFloat procrustesTransform)
	{
		float totalWeight = 0.0f;
		
		GeneralMatrixFloat tempweights = new GeneralMatrixFloat(1,w.height);
		
		for(int i=0;i<w.height;i++)
		{
			totalWeight += w.value[i];
		}

		int numIterations = 30;//a.height*2;
		int procrustessubset = 4;
		long startseed = 0;
		
		Random r = new Random(startseed);
		GeneralMatrixFloat usubset = new GeneralMatrixFloat(3,procrustessubset);
		GeneralMatrixFloat xsubset = new GeneralMatrixFloat(3,procrustessubset);
		GeneralMatrixInt subset = new GeneralMatrixInt(1,procrustessubset);

		GeneralMatrixFloat tempTransform = new GeneralMatrixFloat(4,4);
		float bestMedianMatchError = Float.MAX_VALUE;
		GeneralMatrixFloat matchErrors = new GeneralMatrixFloat(1,w.height);
		GeneralMatrixInt matchErrorInd = new GeneralMatrixInt(1,w.height);

		GeneralMatrixFloat bestmatchErrors = new GeneralMatrixFloat(1,w.height);
		GeneralMatrixInt   bestmatchIndex = new GeneralMatrixInt(1,w.height);

		int lastperc = -1;
		
		for(int i=0;i<numIterations;i++)
		{		
			float runningTotal = totalWeight;
			tempweights.set(w);
			//tempweights.clear(1.0f);
			
			int percent = (int)(100*(i/(float)(numIterations-1)));
			if(percent!=lastperc)
			{
				lastperc = percent;
				System.out.println("Procrustes "+percent+"%");
			}
			
			//System.out.println("itr="+i);
			for(int hi=0;hi<procrustessubset;hi++)
			{
				float f = r.nextFloat()*runningTotal;
				for(int pi=0;pi<a.height;pi++)
				{
					f -= tempweights.value[pi];
					if(f<=0.0f)
					{
						//System.out.println(""+pi);
						subset.value[hi] = pi;
						usubset.value[3*hi+0] = a.value[3*pi+0];
						usubset.value[3*hi+1] = a.value[3*pi+1];
						usubset.value[3*hi+2] = a.value[3*pi+2];
						xsubset.value[3*hi+0] = b.value[3*pi+0];
						xsubset.value[3*hi+1] = b.value[3*pi+1];
						xsubset.value[3*hi+2] = b.value[3*pi+2];
						runningTotal -= tempweights.value[pi];
						tempweights.value[pi] = 0.0f;
						break;
					}
				}
			}
			applyNoScale(usubset, xsubset, tempTransform);		
			
			//Now calc the point errors for this match
			for(int j=0;j<matchErrors.height;j++)
			{
				float ox = a.value[j*3+0];
				float oy = a.value[j*3+1];
				float oz = a.value[j*3+2];
				
				float wx =ox * tempTransform.value[0*4+0] +
				oy * tempTransform.value[1*4+0] +
				oz * tempTransform.value[2*4+0] +
				1.0f * tempTransform.value[3*4+0];
				float wy =ox * tempTransform.value[0*4+1] +
				oy * tempTransform.value[1*4+1] +
				oz * tempTransform.value[2*4+1] +
				1.0f * tempTransform.value[3*4+1];
				float wz =ox * tempTransform.value[0*4+2] +
				oy * tempTransform.value[1*4+2] +
				oz * tempTransform.value[2*4+2] +
				1.0f * tempTransform.value[3*4+2];			
				
				float dx = wx-b.value[j*3+0];
				float dy = wy-b.value[j*3+1];
				float dz = wz-b.value[j*3+2];
				
				matchErrors.value[j] = dx*dx+dy*dy+dz*dz;
				matchErrorInd.value[j] = j;
			}

			QuickSort.quicksort(matchErrors.value,matchErrorInd.value,matchErrors.height);
			
			//The measure to minimise (i.e. half are within a minimal error range)
			int medi = (int)(numValid);
			float median = matchErrors.value[medi];
			
			if(median<bestMedianMatchError)
			{
				bestMedianMatchError = median;
				bestmatchErrors.set(matchErrors);
				bestmatchIndex.set(matchErrorInd);
				procrustesTransform.set(tempTransform);
			}
		}
		
		//Now identify the points which are outliers from the best subset match
		//And simultaneously fit to all the inliers
		for(int pi=0;pi<bestmatchIndex.height;pi++)
		{
			float maxError = (bestMedianMatchError*1.5f);
			for(int j=0;j<bestmatchErrors.height;j++)
			{
				float error = bestmatchErrors.value[j]; 
				
				if(error>maxError)
					dists.value[bestmatchIndex.value[j]]=Float.MAX_VALUE;
				else
					dists.value[bestmatchIndex.value[j]]=error;
			}
		}
	}
	
	public void applyNoScaleWeighted(GeneralMatrixFloat a,
			GeneralMatrixFloat b,
			GeneralMatrixFloat w,
			GeneralMatrixFloat procrustesTransform)
	{
		applyWeighted(a, b, w, rotation_origin,rotation_transform,translation,scale);
		//Build procrustes as a 4x4
		temppTransform1.setIdentity();
		temppTransform2.setIdentity();
		//Translate to rotation origin
		temppTransform1.set3DTransformPosition(-rotation_origin.value[0], -rotation_origin.value[1], -rotation_origin.value[2]);
		
		temppTransform2.setSubset(rotation_transform, 0, 0);
		//temppTransform2.setSubset(rotation_transform, 0, 0);
		//Fix up the homogenous value
		temppTransform2.set(3,3,1.0f);
		temppTransform2.set3DTransformPosition(rotation_origin.value[0]+translation.value[0], rotation_origin.value[1]+translation.value[1], rotation_origin.value[2]+translation.value[2]);
		//Apply rotation
		GeneralMatrixFloat.mult(temppTransform1,temppTransform2, procrustesTransform);

	}
	
	public void applyNoScale(GeneralMatrixFloat a,
			GeneralMatrixFloat b,
			GeneralMatrixFloat procrustesTransform)
	{
		apply(a, b,rotation_origin,rotation_transform,translation,scale);
		//Build procrustes as a 4x4
		temppTransform1.setIdentity();
		temppTransform2.setIdentity();
		//Translate to rotation origin
		temppTransform1.set3DTransformPosition(-rotation_origin.value[0], -rotation_origin.value[1], -rotation_origin.value[2]);
		
		temppTransform2.setSubset(rotation_transform, 0, 0);
		//temppTransform2.setSubset(rotation_transform, 0, 0);
		//Fix up the homogenous value
		temppTransform2.set(3,3,1.0f);
		temppTransform2.set3DTransformPosition(rotation_origin.value[0]+translation.value[0], rotation_origin.value[1]+translation.value[1], rotation_origin.value[2]+translation.value[2]);
		//Apply rotation
		GeneralMatrixFloat.mult(temppTransform1,temppTransform2, procrustesTransform);

		/*
		GeneralMatrixFloat transformedOrigin = new GeneralMatrixFloat(rotation_origin);
		for(int mi=0;mi<3;mi++)
		{
			transformedOrigin.value[mi] = 
				rotation_origin.value[0] * procrustesTransform.value[0*4+mi] +
				rotation_origin.value[1] * procrustesTransform.value[1*4+mi] +
				rotation_origin.value[2] * procrustesTransform.value[2*4+mi] +
				1.0f * procrustesTransform.value[3*4+mi];
		}

		GeneralMatrixFloat.mult(temppTransform1,temppTransform2, procrustesTransform);

		GeneralMatrixFloat transformedOrigin2 = new GeneralMatrixFloat(rotation_origin);
		for(int mi=0;mi<3;mi++)
		{
			transformedOrigin2.value[mi] = 
				rotation_origin.value[0] * procrustesTransform.value[0*4+mi] +
				rotation_origin.value[1] * procrustesTransform.value[1*4+mi] +
				rotation_origin.value[2] * procrustesTransform.value[2*4+mi] +
				1.0f * procrustesTransform.value[3*4+mi];
		}
		System.out.println("Debug");
		*/
	}
	
	public void apply(GeneralMatrixFloat a,
			GeneralMatrixFloat b,
			GeneralMatrixFloat procrustesTransform)
	{
		apply(a, b,rotation_origin,rotation_transform,translation,scale);
		//Build procrustes as a 4x4
		temppTransform1.setIdentity();
		temppTransform2.setIdentity();
		//Translate to rotation origin
		temppTransform1.set3DTransformPosition(-rotation_origin.value[0], -rotation_origin.value[1], -rotation_origin.value[2]);
		
		temppTransform2.setSubset(rotation_transform, 0, 0);
		//temppTransform2.setSubset(rotation_transform, 0, 0);
		temppTransform2.scale(scale.value[0]);
		//Fix up the homogenous value
		temppTransform2.set(3,3,1.0f);
		temppTransform2.set3DTransformPosition(rotation_origin.value[0]+translation.value[0], rotation_origin.value[1]+translation.value[1], rotation_origin.value[2]+translation.value[2]);
		//Apply rotation
		GeneralMatrixFloat.mult(temppTransform1,temppTransform2, procrustesTransform);
	}

	//Points as two matrixes 
	public void apply(GeneralMatrixFloat a,GeneralMatrixFloat b,
			GeneralMatrixFloat rotation_origin,GeneralMatrixFloat rotation_transform,GeneralMatrixFloat translation,GeneralMatrixFloat scale)
	{
		scale.value[0] = 1.0f;
		rotation_origin.clear(0.0f);
		translation.clear(0.0f);
		rotation_transform.setIdentity();

		calculateCentroid(a, rotation_origin);
		GeneralMatrixFloat.rowsub(a, rotation_origin,aShift);
		calculateCentroid(b, translation);
		GeneralMatrixFloat.rowsub(b, translation,bShift);
		GeneralMatrixFloat.sub(translation, rotation_origin,translation);
		GeneralMatrixFloat.transpose(bShift, bTranspose);

		GeneralMatrixFloat.mult(bTranspose,aShift,svd_U);

		svd.init(svd_U);
		//GeneralMatrixFloat.svd( svd_U, svd_W, svd_V, svd_U );
		GeneralMatrixFloat.transpose(svd.v, svd_VT);
		GeneralMatrixFloat.mult( svd.u, svd_VT, rotation );

		if(a.width==2)
		{
			
		}
		else
		{
		
		//Calculate the determinant to prevent reflections
		float det1 = rotation.value[0]*rotation.value[4]*rotation.value[8];
		det1 += rotation.value[1]*rotation.value[5]*rotation.value[6];
		det1 += rotation.value[2]*rotation.value[3]*rotation.value[7];
		
		float det2 = rotation.value[6]*rotation.value[4]*rotation.value[2];
		det2 += rotation.value[7]*rotation.value[5]*rotation.value[0];
		det2 += rotation.value[8]*rotation.value[3]*rotation.value[1];

		float det = det1-det2;
		
		if(det<0.0f)
		{
			svd.u.value[2] = -svd.u.value[2];
			svd.u.value[5] = -svd.u.value[5];
			svd.u.value[8] = -svd.u.value[8];
//			svd.u.value[6] = -svd.u.value[6];
//			svd.u.value[7] = -svd.u.value[7];
//			svd.u.value[8] = -svd.u.value[8];

//			svd.v.value[6] = -svd.v.value[6];
//			svd.v.value[7] = -svd.v.value[7];
//			svd.v.value[8] = -svd.v.value[8];
			
//			GeneralMatrixFloat.transpose(svd.v, svd_VT);
			GeneralMatrixFloat.mult( svd.u, svd_VT, rotation );

			det1 = rotation.value[0]*rotation.value[4]*rotation.value[8];
			det1 += rotation.value[1]*rotation.value[5]*rotation.value[6];
			det1 += rotation.value[2]*rotation.value[3]*rotation.value[7];
			
			det2 = rotation.value[6]*rotation.value[4]*rotation.value[2];
			det2 += rotation.value[7]*rotation.value[5]*rotation.value[0];
			det2 += rotation.value[8]*rotation.value[3]*rotation.value[1];
		}
		}
		// Calculate the scale

		GeneralMatrixFloat.mult(bShift, rotation, bRotated );
		GeneralMatrixFloat.transpose(aShift, aTranspose);
		GeneralMatrixFloat.mult( bRotated, aTranspose, product );
	    float trace1 = product.trace();
	    GeneralMatrixFloat.mult( bShift, bTranspose, product );
	    float trace2 = product.trace();

	    if (trace2 != 0.0f)
	        scale.value[0] = trace2 / trace1;
	    else
	    	scale.value[0] = 0.0f;

	    GeneralMatrixFloat.transpose(rotation,rotation_transform);
	}
	
	void calculateCentroid(GeneralMatrixFloat a,GeneralMatrixFloat centroid)
	{
		int ai = 0;
		for(int i = 0;i<a.height;i++)
		{
			for(int j=0;j<a.width;j++)
			{
				centroid.value[j] += a.value[ai];
				ai++;
			}
		}
		for(int j=0;j<a.width;j++)
		{
			centroid.value[j] /= a.height;
		}
	}

	public void applyWeighted(GeneralMatrixFloat a,GeneralMatrixFloat b,GeneralMatrixFloat weight,
			GeneralMatrixFloat rotation_origin,GeneralMatrixFloat rotation_transform,GeneralMatrixFloat translation,GeneralMatrixFloat scale)
	{
		scale.value[0] = 1.0f;
		rotation_origin.clear(0.0f);
		translation.clear(0.0f);
		rotation_transform.setIdentity();

		calculateCentroidWeighted(a, weight, rotation_origin);
		GeneralMatrixFloat.rowsub(a, rotation_origin,aShift);
		calculateCentroidWeighted(b, weight, translation);
		GeneralMatrixFloat.rowsub(b, translation, bShift);
		
		//Scale ashift and bshift to reduce or increase their influence
		for(int i=0;i<a.height;i++)
		{
			float w = weight.value[i];
			for(int j=0;j<3;j++)
			{
				aShift.value[i*3+j] *= w;
				bShift.value[i*3+j] *= w;
			}
		}
		
		GeneralMatrixFloat.sub(translation, rotation_origin,translation);
		GeneralMatrixFloat.transpose(bShift, bTranspose);

		GeneralMatrixFloat.mult(bTranspose,aShift,svd_U);

		svd.init(svd_U);
		//GeneralMatrixFloat.svd( svd_U, svd_W, svd_V, svd_U );
		GeneralMatrixFloat.transpose(svd.v, svd_VT);
		GeneralMatrixFloat.mult( svd.u, svd_VT, rotation );

		//Calculate the determinant to prevent reflections
		float det1 = rotation.value[0]*rotation.value[4]*rotation.value[8];
		det1 += rotation.value[1]*rotation.value[5]*rotation.value[6];
		det1 += rotation.value[2]*rotation.value[3]*rotation.value[7];
		
		float det2 = rotation.value[6]*rotation.value[4]*rotation.value[2];
		det2 += rotation.value[7]*rotation.value[5]*rotation.value[0];
		det2 += rotation.value[8]*rotation.value[3]*rotation.value[1];

		float det = det1-det2;
		
		if(det<0.0f)
		{
			svd.u.value[2] = -svd.u.value[2];
			svd.u.value[5] = -svd.u.value[5];
			svd.u.value[8] = -svd.u.value[8];
//			svd.u.value[6] = -svd.u.value[6];
//			svd.u.value[7] = -svd.u.value[7];
//			svd.u.value[8] = -svd.u.value[8];

//			svd.v.value[6] = -svd.v.value[6];
//			svd.v.value[7] = -svd.v.value[7];
//			svd.v.value[8] = -svd.v.value[8];
			
//			GeneralMatrixFloat.transpose(svd.v, svd_VT);
			GeneralMatrixFloat.mult( svd.u, svd_VT, rotation );

			det1 = rotation.value[0]*rotation.value[4]*rotation.value[8];
			det1 += rotation.value[1]*rotation.value[5]*rotation.value[6];
			det1 += rotation.value[2]*rotation.value[3]*rotation.value[7];
			
			det2 = rotation.value[6]*rotation.value[4]*rotation.value[2];
			det2 += rotation.value[7]*rotation.value[5]*rotation.value[0];
			det2 += rotation.value[8]*rotation.value[3]*rotation.value[1];
		}
		
		// Calculate the scale

		GeneralMatrixFloat.mult(bShift, rotation, bRotated );
		GeneralMatrixFloat.transpose(aShift, aTranspose);
		GeneralMatrixFloat.mult( bRotated, aTranspose, product );
	    float trace1 = product.trace();
	    GeneralMatrixFloat.mult( bShift, bTranspose, product );
	    float trace2 = product.trace();

	    if (trace2 != 0.0f)
	        scale.value[0] = trace2 / trace1;
	    else
	    	scale.value[0] = 0.0f;

	    GeneralMatrixFloat.transpose(rotation,rotation_transform);
	}
	
	void calculateCentroidWeighted(GeneralMatrixFloat a,GeneralMatrixFloat weight,GeneralMatrixFloat centroid)
	{
		int ai = 0;
		float tweight = 0.0f;
		for(int i = 0;i<a.height;i++)
		{
			float w = weight.value[i];
			for(int j=0;j<a.width;j++)
			{
				centroid.value[j] += a.value[ai]*w;
				ai++;
			}
			tweight += w;
		}
		for(int j=0;j<a.width;j++)
		{
			centroid.value[j] /= tweight;
		}
	}
}
