package registration;

import mathematics.GeneralMatrixFloat;
import mathematics.linearalgebra.SVD;
import rendering.rasteriser.MorphableModel;
import sort.QuickSort;

public class MorphableModelPointRegistration3D 
{
	public MorphableModel model;

	public int numberOfFeaturePoints;

	//The mean feature points of the morphable model
	public GeneralMatrixFloat featurePointMean;
	
	//This matrix represents how each vertex of the model is altered (in world space) by 
	//Altering an appearance parameter
	//width is 3*feature points
	//height is number of pcs in model
	public GeneralMatrixFloat featurePointDeltas;
	
	//The feature detection process produces a range of possible feature matches
	//Values are positions on screen and the distance measure of their match
	public GeneralMatrixFloat[] featurePointDetections;

	float imageQualityFactor = 100.0f;

	
	public GeneralMatrixFloat currentLambda;
	public GeneralMatrixFloat currentTransform = new GeneralMatrixFloat(6,1);
	//Jacobian

	public void init(int numpcs)
	{
		currentLambda = new GeneralMatrixFloat(numpcs,1);
		currentLambda.clear(0.0f);
	}
	
	public void register()
	{
		
		//Runtime values		
		GeneralMatrixFloat worldFPPosition = new GeneralMatrixFloat(featurePointMean.width,featurePointMean.height);
		//GeneralMatrixFloat imageFPPosition = new GeneralMatrixFloat(featurePointMean.width,featurePointMean.height);
		GeneralMatrixFloat transformedWorldFPPosition = new GeneralMatrixFloat(featurePointMean.width,featurePointMean.height);

		GeneralMatrixFloat errorInPositions = new GeneralMatrixFloat(3,featurePointMean.height);

		GeneralMatrixFloat jacobian = new GeneralMatrixFloat(featurePointDeltas.width,featurePointDeltas.height+6);
		GeneralMatrixFloat hessian = new GeneralMatrixFloat(featurePointDeltas.height+6,featurePointDeltas.height+6);
		//GeneralMatrixFloat invhessian = new GeneralMatrixFloat(featurePointDeltas.height+6,featurePointDeltas.height+6);

		//Steepest descent deltas that need normalising and uncorrelating through the inverse hessian
		GeneralMatrixFloat jdeltaParams = new GeneralMatrixFloat(featurePointDeltas.height+6,1);
		GeneralMatrixFloat deltaParams = new GeneralMatrixFloat(featurePointDeltas.height+6,1);
		
		GeneralMatrixFloat objToWorldTransform = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat point3D = new GeneralMatrixFloat(3,1);
		//Get current estimate of position (could use epnp with best feature detections and mean feature position)
		
		GeneralMatrixFloat occludedFeatures = new GeneralMatrixFloat(numberOfFeaturePoints,1);
		
		GeneralMatrixFloat deltaRotation = new GeneralMatrixFloat(4,4);
		//GeneralMatrixFloat compositeRotation = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat deltaRotationt = new GeneralMatrixFloat(4,4);
		
		SVD svd = new SVD(hessian.width,hessian.height);
		
		//Basic approach, the pose and appearance values are initialised
		//either through hough clustering estimates from feature matches
		//or from previous step

		int sortIndex[] = new int[numberOfFeaturePoints]; 
		float sortCost[] = new float[numberOfFeaturePoints]; 
		
		int niters = 1;
		for(int f=0;f<niters;f++)
		{
			//Using the current model parameters
			//Determine the jacobian in image space
			//Effect on error of each point by changing each parameter
			//Make work like inverse compositional:
			//Assuming small change estimate effect of pose change on current position of point
			//Taking into account depth scale effect
			//theta*r is world distance moved, screen distance altered by z depth
			//rotated morph direction is world change, scaled by z depth
			//hessian is the correlation of point changes to one another
			
			//Evaluate the world position of feature points
			worldFPPosition.set(featurePointMean);
			int ind = 0;
			for(int i=0;i<featurePointDeltas.height;i++)
			{
				for(int j=0;j<featurePointDeltas.width;j++)
				{
					if(featurePointDeltas.value[ind+j]!=Float.MAX_VALUE)
						worldFPPosition.value[j] += featurePointDeltas.value[ind+j]*currentLambda.value[i];
				}
				ind += featurePointDeltas.width;
			}
			
			//Build the current transformation matrix
			objToWorldTransform.set3DTransformRotation(currentTransform.value[0],currentTransform.value[1],currentTransform.value[2]);
			objToWorldTransform.set3DTransformPosition(currentTransform.value[3],currentTransform.value[4],currentTransform.value[5]);


			//Build the jacobian
			//DEBUG
			jacobian.clear(0.0f);
			//Take each delta (x and y) and rotate it
			//Scale it by the z based scale (this approximates local points as having no z cost in movement)
			//Translations are also scaled by z scale
			//*
			ind = 0;
			int jind = 0;
			for(int i=0;i<featurePointDeltas.height;i++)
			{
				for(int j=0;j<(featurePointDeltas.width/3);j++)
				{
					for (int k=0; k<3; k++) 
					{
						jacobian.value[jind+k] = 
							featurePointDeltas.value[ind+j*3+0] * objToWorldTransform.value[0*4+k] +
							featurePointDeltas.value[ind+j*3+1] * objToWorldTransform.value[1*4+k] +
							featurePointDeltas.value[ind+j*3+2] * objToWorldTransform.value[2*4+k];
					}
					jind += 3;
				}
				ind += featurePointDeltas.width;
			}
			//*/
			
			//Add entries for rotation and translation and their effects on feature positions
			//Calculate the absolute rotation delta by removing the later rotations, 
			//approximating by sin(a) = a  cos(a) = 1
			//restore with later rotation and scale by point z
			
			//Rotate x
			ind = 0;
			for(int j=0;j<(featurePointDeltas.width/3);j++)
			{
				//Equivalent to a simplified rotation matrix as a delta
				//I.e. world fp * objtoworld * rotmatrix * modeltoworld * imagefp
				for (int k=0; k<3; k++) 
				{
					transformedWorldFPPosition.value[k+j*3] = 
						worldFPPosition.value[ind+j*3+0] * objToWorldTransform.value[0*4+k] +
						worldFPPosition.value[ind+j*3+1] * objToWorldTransform.value[1*4+k] +
						worldFPPosition.value[ind+j*3+2] * objToWorldTransform.value[2*4+k];
				}

				
				{
					jacobian.value[jind+0] = 0.0f;
					jacobian.value[jind+1] = transformedWorldFPPosition.value[2] * -1.0f;
					jacobian.value[jind+2] = transformedWorldFPPosition.value[1] * 1.0f;
				}
				
				jind += 3;
			}
			//Rotate y
			for(int j=0;j<(featurePointDeltas.width/3);j++)
			{
				//Equivalent to a simplified rotation matrix as a delta
				//I.e. world fp * objtoworld * rotmatrix * modeltoworld * imagefp
//				for (int k=0; k<3; k++) 
//				{
//					point3D.value[k] = 
//						worldFPPosition.value[ind+j*3+0] * objToWorldTransform.value[0*4+k] +
//						worldFPPosition.value[ind+j*3+1] * objToWorldTransform.value[1*4+k] +
//						worldFPPosition.value[ind+j*3+2] * objToWorldTransform.value[2*4+k];
//				}

				
				{
					jacobian.value[jind+0] = transformedWorldFPPosition.value[2] * 1.0f;
					jacobian.value[jind+1] = 0.0f;
					jacobian.value[jind+2] = transformedWorldFPPosition.value[0] * -1.0f;
				}

				jind += 3;
			}
			//Rotate z
			for(int j=0;j<(featurePointDeltas.width/3);j++)
			{
				//Equivalent to a simplified rotation matrix as a delta
				//I.e. world fp * objtoworld * rotmatrix * modeltoworld * imagefp
//				for (int k=0; k<3; k++) 
//				{
//					point3D.value[k] = 
//						worldFPPosition.value[ind+j*3+0] * objToWorldTransform.value[0*4+k] +
//						worldFPPosition.value[ind+j*3+1] * objToWorldTransform.value[1*4+k] +
//						worldFPPosition.value[ind+j*3+2] * objToWorldTransform.value[2*4+k];
//				}

				
				{
					jacobian.value[jind+0] = transformedWorldFPPosition.value[1+j*3] * 1.0f;
					jacobian.value[jind+1] = transformedWorldFPPosition.value[0+j*3] * -1.0f;
					jacobian.value[jind+2] = 0.0f;
				}
				
				jind += 3;
			}
			
			//Translate x
			for(int j=0;j<(featurePointDeltas.width/3);j++)
			{
				jacobian.value[jind+0] = 1.0f;
				jacobian.value[jind+1] = 0.0f;
				jacobian.value[jind+2] = 0.0f;
				jind += 3;
			}			
			//Translate y
			for(int j=0;j<(featurePointDeltas.width/3);j++)
			{
				jacobian.value[jind+0] = 0.0f;
				jacobian.value[jind+1] = 1.0f;
				jacobian.value[jind+2] = 0.0f;
				jind += 3;
			}			
			//z translation needs to appear to have an effect otherwise it will never be used
			//Translate z
			for(int j=0;j<(featurePointDeltas.width/3);j++)
			{
				jacobian.value[jind+0] = 0.0f;
				jacobian.value[jind+1] = 0.0f;
				jacobian.value[jind+2] = 1.0f;
				jind += 3;
			}			
			
			//Scale the jacobian by eigenvalues to minimise error
			//I.e. parameters of small variance properties must be large to achieve change
			//then have the SVD minimise the sum of squared parameters
			
			//Calculate the best point match for the current configuration
			//This is a combination of the quality of the point match and the distance of the point to the current model
			//Like an enhanced icp with feature descriptors
			//Ideally the best point would take into account the differing eigen values of feature points from the mean given the current orientation
			//Although that will be handled by the regularisation term of the fitting process
			for(int i=0;i<transformedWorldFPPosition.height;i++)
			{
				float cost = Float.MAX_VALUE;
				int bestPoint = 0;
				for(int j = 0;j<featurePointDetections[i].height;j++)
				{
					//A weighted sum of the euclidean distance and the matching quality
					float dx =  featurePointDetections[i].value[j*3+0]-transformedWorldFPPosition.value[i*3+0];
					float dy =  featurePointDetections[i].value[j*3+1]-transformedWorldFPPosition.value[i*3+1];
					float dz =  featurePointDetections[i].value[j*3+2]-transformedWorldFPPosition.value[i*3+2];
					float thisCost = dx*dx+dy*dy;//+dz*dz;
					thisCost += imageQualityFactor*featurePointDetections[i].value[j*3+2];
					if(thisCost<cost)
					{
						bestPoint = j;
						cost = thisCost;
					}
					
				}
				errorInPositions.value[i*3+0] = featurePointDetections[i].value[bestPoint*3+0]-transformedWorldFPPosition.value[i*3+0];
				errorInPositions.value[i*3+1] = featurePointDetections[i].value[bestPoint*3+1]-transformedWorldFPPosition.value[i*3+1];
				errorInPositions.value[i*3+2] = featurePointDetections[i].value[bestPoint*3+2]-transformedWorldFPPosition.value[i*3+2];
				//errorInPositions.value[i*3+2] = cost;
				
				sortCost[i] = cost;
				sortIndex[i] = i;
			}
						
			//These are sorted by cost and a percentage dropped, to achieve a base level of occlusion robustness
			QuickSort.quicksort(sortCost, sortIndex, 0, sortIndex.length-1);

			//Determine the occluding features
			float perc_out = 0.1f;
			occludedFeatures.clear(1.0f);
			int numOutliers = (int)(sortIndex.length*perc_out);
			for(int i=0;i<numOutliers;i++)
			{
				occludedFeatures.value[sortIndex[sortIndex.length-(i+1)]] = 0.0f;
			}
			
			//Calculate the hessian
			//GeneralMatrixFloat hessian = new GeneralMatrixFloat(featurePointDeltas.height+6,featurePointDeltas.height+6);
			for(int i=0;i<hessian.height;i++)
			{
				for(int j=0;j<hessian.width;j++)
				{
					float sum = 0.0f;
					ind = 0;
					int iOffset = jacobian.width*i;
					int jOffset = jacobian.width*j;
					for(int y=0;y<numberOfFeaturePoints;y++)
					{
						//jacobian
						if(occludedFeatures.value[y]>0)
						{
							sum += jacobian.value[ind+iOffset]*jacobian.value[ind+jOffset];
							ind++;
							sum += jacobian.value[ind+iOffset]*jacobian.value[ind+jOffset];
							ind++;
							sum += jacobian.value[ind+iOffset]*jacobian.value[ind+jOffset];
							ind++;
						}
						else
							ind+=3;
						//sum += sdImages[i].pixel[ind]*sdImages[j].pixel[ind]*weights.pixel[ind];
					}
					hessian.value[i+j*hessian.width] = sum;
				}
			}
			
			//Make the hessian use absolute values so the SVD will minimise the absolute deltas
			//Ax = b -> A(x+c) = b+A(c)
			//Scale the current params by 1/eigenvalues
			
			//Determine the current error due to each feature position
			//GeneralMatrixFloat.invert(hessian, invhessian);
			svd.init(hessian);
			
			//If there are too few feature points, more could be obtained by performing 
			//ICAlignment of feature matches with current position estimate
			for(int j=0;j<jacobian.height;j++)
			{
				float sum = 0.0f;
				//Error in positions
				for(int i=0;i<jacobian.width;i++)
				{
					sum += errorInPositions.value[i]*jacobian.value[j*jacobian.width+i]*occludedFeatures.value[i/2];
				}
				jdeltaParams.value[j] = sum;
			}
			
			//GeneralMatrixFloat.mult(invhessian, jdeltaParams, deltaParams);
			svd.solve(jdeltaParams, deltaParams, -1.0f);

			//scale the parameters by eigenvalues to remove weighting
			
			//Use the new params directly
			
			//Apply the delta
			for(int j=0;j<currentLambda.width;j++)
			{
				currentLambda.value[j] += deltaParams.value[j];
			}	
			//Apply the translations
			currentTransform.value[3] += deltaParams.value[currentLambda.width+3];
			currentTransform.value[4] += deltaParams.value[currentLambda.width+4];
			currentTransform.value[5] += deltaParams.value[currentLambda.width+5];
			
			//To calculate the absolute rotations, build a delta rotation using the deltas
			deltaRotation.setIdentity();
			deltaRotation.set3DTransformRotation(deltaParams.value[currentLambda.width+0], deltaParams.value[currentLambda.width+1], deltaParams.value[currentLambda.width+2]);

			//Then compose with the existing rotation
			objToWorldTransform.set3DTransformPosition(0.0f, 0.0f, 0.0f);
			GeneralMatrixFloat.transpose(deltaRotation, deltaRotationt);
			GeneralMatrixFloat.mult(deltaRotationt, objToWorldTransform, deltaRotation);
			//Then infer the new euler angles
			GeneralMatrixFloat.getEuler(deltaRotation,point3D);
			currentTransform.value[0] = point3D.value[0];
			currentTransform.value[1] = point3D.value[1];
			currentTransform.value[2] = point3D.value[2];
		}
	}

}
