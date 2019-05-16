package registration;

import camera.Camera;
import mathematics.GeneralMatrixFloat;
import mathematics.linearalgebra.SVD;
import rendering.rasteriser.MorphableModel;
import sort.QuickSort;

public class MorphableModelPointRegistration2D 
{
	public Camera camera;
	//public MorphableModel model;

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

	//float imageQualityFactor = 100.0f;

	
	public GeneralMatrixFloat currentLambda;
	public GeneralMatrixFloat currentTransform = new GeneralMatrixFloat(6,1);
	//Jacobian

	public void init(int numpcs)
	{
		currentLambda = new GeneralMatrixFloat(numpcs,1);
		currentLambda.clear(0.0f);
	}
	
	//Camera calibration
	public void solve(GeneralMatrixFloat pixelPositions,GeneralMatrixFloat worldPositions,Camera camera,int w,int h)
	{
		init(0);
		this.camera = camera;
		camera.width = w;
		camera.height = h;
		featurePointMean = worldPositions;
		numberOfFeaturePoints = featurePointMean.height;
		featurePointDeltas = new GeneralMatrixFloat(featurePointMean.height*3);
		featurePointDetections = new GeneralMatrixFloat[featurePointMean.height];
		for(int i=0;i<featurePointMean.height;i++)
		{
			featurePointDetections[i] = new GeneralMatrixFloat(3,1);
			featurePointDetections[i].value[0] = pixelPositions.value[i*2+0];
			featurePointDetections[i].value[1] = pixelPositions.value[i*2+1];
			featurePointDetections[i].value[2] = 1.0f;
		}
		register(false);
	}
	
	public void register(boolean modelNotCamera)
	{
		
		//Runtime values		
		GeneralMatrixFloat worldFPPosition = new GeneralMatrixFloat(featurePointMean.width,featurePointMean.height);
		GeneralMatrixFloat imageFPPosition = new GeneralMatrixFloat(featurePointMean.width,featurePointMean.height);
		GeneralMatrixFloat transformedWorldFPPosition = new GeneralMatrixFloat(featurePointMean.width,featurePointMean.height);

		GeneralMatrixFloat errorInPositions = new GeneralMatrixFloat(2,featurePointMean.height);

		GeneralMatrixFloat jacobian = new GeneralMatrixFloat(numberOfFeaturePoints*2,featurePointDeltas.height+7);
		GeneralMatrixFloat hessian = new GeneralMatrixFloat(featurePointDeltas.height+7,featurePointDeltas.height+7);
		//GeneralMatrixFloat invhessian = new GeneralMatrixFloat(featurePointDeltas.height+6,featurePointDeltas.height+6);

		//Steepest descent deltas that need normalising and uncorrelating through the inverse hessian
		GeneralMatrixFloat jdeltaParams = new GeneralMatrixFloat(featurePointDeltas.height+7,1);
		GeneralMatrixFloat deltaParams = new GeneralMatrixFloat(featurePointDeltas.height+7,1);
		
		GeneralMatrixFloat objToCameraTransform = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat point2D = new GeneralMatrixFloat(2,1);
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
		
		GeneralMatrixFloat objToWorldTransform = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat ictransform = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat tempcamTransform = new GeneralMatrixFloat(4,4);

		GeneralMatrixFloat tempTransform = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat worldFPZdeltaPosition = new GeneralMatrixFloat(worldFPPosition);
		GeneralMatrixFloat imageFPZdeltaPosition = new GeneralMatrixFloat(imageFPPosition);
		GeneralMatrixFloat imageFPZdeltaPosition2 = new GeneralMatrixFloat(imageFPPosition);
		
		tempTransform.setIdentity();
		
		float rollbackfov = 0.0f;
		GeneralMatrixFloat rollbacktransform = new GeneralMatrixFloat(currentTransform);
		GeneralMatrixFloat rollbackcamera = new GeneralMatrixFloat(camera.cameraTransformMatrix);
		
		GeneralMatrixFloat deltaScales = new GeneralMatrixFloat(deltaParams.width,1);
		deltaScales.clear(1.0f);
		deltaScales.value[featurePointDeltas.height+6] = 1.0f/100.0f;
		
		int niters = 30;
		int rotFrom = 19;
		int fovFrom = 29;
		for(int f=0;f<niters;f++)
		{
			rollbacktransform.set(currentTransform);
			rollbackcamera.set(camera.cameraTransformMatrix);
			rollbackfov = camera.calculateFOVYFromFocalLength();

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
			objToWorldTransform.setIdentity();
			//objToWorldTransform.set3DTransformRotation(currentTransform.value[0],currentTransform.value[1],currentTransform.value[2]);
			//objToWorldTransform.set3DTransformPosition(currentTransform.value[3],currentTransform.value[4],currentTransform.value[5]);
			
			GeneralMatrixFloat.invert(camera.cameraTransformMatrix, ictransform);
			GeneralMatrixFloat.mult(ictransform, objToWorldTransform, objToCameraTransform);
			
			//Project each point of the current model (assuming a non transformed camera)
			//tempcamTransform.set(camera.cameraTransformMatrix);
			//GeneralMatrixFloat.setIdentity(camera.cameraTransformMatrix);
			//camera.buildModelMatrix();
			//camera.cameraTransformMatrix.printTransform();
			//camera.projectionMatrix.printProjection();
			
			camera.projectAndCalcScale(objToWorldTransform, worldFPPosition, imageFPPosition);
			//camera.cameraTransformMatrix.set(tempcamTransform);
			//camera.buildModelMatrix();
			
			//Debug set the detected positions from debug movement of points
			/*
			if(f==0)
			{
				GeneralMatrixFloat testworldFPPosition = new GeneralMatrixFloat(worldFPPosition);
				for(int i=0;i<numberOfFeaturePoints;i++)
				{
					testworldFPPosition.value[i*3+1] -= 0.1f;
				}			
				GeneralMatrixFloat testimageFPPosition = new GeneralMatrixFloat(imageFPPosition);
				tempTransform.set3DTransformRotation(0.0f, 0.0f, 0.0f);
				camera.projectAndCalcScale(tempTransform, testworldFPPosition, testimageFPPosition);
				//featurePointDetections
				for(int i=0;i<numberOfFeaturePoints;i++)
				{
					featurePointDetections[i].value[0] = testimageFPPosition.value[i*3+0];
					featurePointDetections[i].value[1] = testimageFPPosition.value[i*3+1];
					featurePointDetections[i].value[2] = 1.0f;
				}
			}
			//*/
			//Calculate the best point match for the current configuration
			//This is a combination of the quality of the point match and the distance of the point to the current model
			//Like an enhanced icp with feature descriptors
			//Ideally the best point would take into account the differing eigen values of feature points from the mean given the current orientation
			//Although that will be handled by the regularisation term of the fitting process
			float totalcost = 0.0f;
			for(int i=0;i<imageFPPosition.height;i++)
			{
				float cost = Float.MAX_VALUE;
				int bestPoint = 0;
				for(int j = 0;j<featurePointDetections[i].height;j++)
				{
					//A weighted sum of the euclidean distance and the matching quality
					float dx =  featurePointDetections[i].value[j*3+0]-imageFPPosition.value[i*3+0];
					float dy =  featurePointDetections[i].value[j*3+1]-imageFPPosition.value[i*3+1];
					float thisCost = dx*dx+dy*dy;
					//thisCost += imageQualityFactor*featurePointDetections[i].value[j*3+2];
					if(thisCost<cost)
					{
						bestPoint = j;
						cost = thisCost;
					}
					
				}
				errorInPositions.value[i*2+0] = featurePointDetections[i].value[bestPoint*3+0]-imageFPPosition.value[i*3+0];
				errorInPositions.value[i*2+1] = featurePointDetections[i].value[bestPoint*3+1]-imageFPPosition.value[i*3+1];
				//errorInPositions.value[i*3+2] = cost;
			
				totalcost += cost;
				
				sortCost[i] = cost;
				sortIndex[i] = i;
			}

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
					for (int k=0; k<2; k++) 
					{
						point2D.value[k] = 
							featurePointDeltas.value[ind+j*3+0] * objToCameraTransform.value[0*4+k] +
							featurePointDeltas.value[ind+j*3+1] * -objToCameraTransform.value[1*4+k] +
							featurePointDeltas.value[ind+j*3+2] * -objToCameraTransform.value[2*4+k];
					}
					jacobian.value[jind+0] = point2D.value[0]*imageFPPosition.value[3*j+2];
					jacobian.value[jind+1] = point2D.value[1]*imageFPPosition.value[3*j+2];
					jind += 2;
				}
				ind += featurePointDeltas.width;
			}
			//*/
			
			//Add entries for rotation and translation and their effects on feature positions
			//Calculate the absolute rotation delta by removing the later rotations, 
			//approximating by sin(a) = a  cos(a) = 1
			//restore with later rotation and scale by point z
			
			float delta = 0.1f;
			//Rotate x
			//*
			//z translation needs to appear to have an effect otherwise it will never be used
			float xdelta = delta;
			tempTransform.set3DTransformRotation(xdelta, 0.0f, 0.0f);
			camera.projectAndCalcScale(tempTransform, worldFPPosition, imageFPZdeltaPosition);

			ind = 0;
			for(int j=0;j<numberOfFeaturePoints;j++)
			{
				/*
				//Equivalent to a simplified rotation matrix as a delta
				//I.e. world fp * objtoworld * rotmatrix * modeltoworld * imagefp
				for (int k=0; k<3; k++) 
				{
					transformedWorldFPPosition.value[k+j*3] = 
						worldFPPosition.value[ind+j*3+0] * objToCameraTransform.value[0*4+k] +
						worldFPPosition.value[ind+j*3+1] * objToCameraTransform.value[1*4+k] +
						worldFPPosition.value[ind+j*3+2] * objToCameraTransform.value[2*4+k];
				}

				
				{
					point2D.value[0] = 0.0f;
					point2D.value[1] = 
						transformedWorldFPPosition.value[2+j*3] * 1.0f * -1.0f;
				}
				
					jacobian.value[jind+0] = point2D.value[0]*imageFPPosition.value[3*j+2];
					jacobian.value[jind+1] = point2D.value[1]*imageFPPosition.value[3*j+2];
				}
				*/
				if(f>=rotFrom)
				{
				jacobian.value[jind+0] = (imageFPZdeltaPosition.value[3*j+0]-imageFPPosition.value[3*j+0])/xdelta;
				jacobian.value[jind+1] = (imageFPZdeltaPosition.value[3*j+1]-imageFPPosition.value[3*j+1])/xdelta;
				}
				jind += 2;
			}
			
			//Rotate y
			float ydelta = delta;
			tempTransform.setIdentity();
			tempTransform.set3DTransformRotation(0.0f, ydelta, 0.0f);
			camera.projectAndCalcScale(tempTransform, worldFPPosition, imageFPZdeltaPosition);

			for(int j=0;j<numberOfFeaturePoints;j++)
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

				/*
				{
					point2D.value[0] = transformedWorldFPPosition.value[2+j*3] * -1.0f * -1.0f;
					//point2D.value[0] = point3D.value[2] * -1.0f * -1.0f;
					point2D.value[1] = 0.0f;
				}
				
				if(f>=rotFrom)
				{
					jacobian.value[jind+0] = point2D.value[0]*imageFPPosition.value[3*j+2];
					jacobian.value[jind+1] = point2D.value[1]*imageFPPosition.value[3*j+2];
				}
				*/
				if(f>=rotFrom)
				{
				jacobian.value[jind+0] = (imageFPZdeltaPosition.value[3*j+0]-imageFPPosition.value[3*j+0])/ydelta;
				jacobian.value[jind+1] = (imageFPZdeltaPosition.value[3*j+1]-imageFPPosition.value[3*j+1])/ydelta;				
				}
				jind += 2;
			}
			//Rotate z
			float zdelta = delta;
			tempTransform.set3DTransformRotation(0.0f, 0.0f, zdelta);
			camera.projectAndCalcScale(tempTransform, worldFPPosition, imageFPZdeltaPosition);
			
			for(int j=0;j<numberOfFeaturePoints;j++)
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

				/*
				{
					
					point2D.value[0] = transformedWorldFPPosition.value[1+j*3] * 1.0f * 1.0f;
					point2D.value[1] = transformedWorldFPPosition.value[0+j*3] * -1.0f * -1.0f;
//					point2D.value[0] = point3D.value[1] * 1.0f * 1.0f;
//					point2D.value[1] = point3D.value[0] * -1.0f * -1.0f;
				}
				
				if(f>=rotFrom)
				{
					jacobian.value[jind+0] = point2D.value[0]*imageFPPosition.value[3*j+2];
					jacobian.value[jind+1] = point2D.value[1]*imageFPPosition.value[3*j+2];
				}
				*/
				if(f>=rotFrom)
				{
				jacobian.value[jind+0] = (imageFPZdeltaPosition.value[3*j+0]-imageFPPosition.value[3*j+0])/zdelta;
				jacobian.value[jind+1] = (imageFPZdeltaPosition.value[3*j+1]-imageFPPosition.value[3*j+1])/zdelta;
				}
				jind += 2;
			}

			//Translate x
			for(int j=0;j<numberOfFeaturePoints;j++)
			{
				jacobian.value[jind+0] = 1.0f*imageFPPosition.value[3*j+2];
				jacobian.value[jind+1] = 0.0f*imageFPPosition.value[3*j+2];
				jind += 2;
			}			
			//Translate y
			for(int j=0;j<numberOfFeaturePoints;j++)
			{
				jacobian.value[jind+0] = 0.0f*imageFPPosition.value[3*j+2];
				jacobian.value[jind+1] = 1.0f*imageFPPosition.value[3*j+2];
				jind += 2;
			}			
			//*/
			
			zdelta = 0.01f;
			//z translation needs to appear to have an effect otherwise it will never be used
			for(int j=0;j<numberOfFeaturePoints;j++)
			{
				worldFPZdeltaPosition.value[j*3+0] = worldFPPosition.value[j*3+0]+camera.cameraTransformMatrix.value[4*2+0]*zdelta;
				worldFPZdeltaPosition.value[j*3+1] = worldFPPosition.value[j*3+1]+camera.cameraTransformMatrix.value[4*2+1]*zdelta;
				worldFPZdeltaPosition.value[j*3+2] = worldFPPosition.value[j*3+2]+camera.cameraTransformMatrix.value[4*2+2]*zdelta;
			}
			camera.projectAndCalcScale(objToWorldTransform, worldFPZdeltaPosition, imageFPZdeltaPosition);
			
			for(int j=0;j<numberOfFeaturePoints;j++)
			{
				worldFPZdeltaPosition.value[j*3+0] = worldFPPosition.value[j*3+0]+camera.cameraTransformMatrix.value[4*2+0]*-zdelta;
				worldFPZdeltaPosition.value[j*3+1] = worldFPPosition.value[j*3+1]+camera.cameraTransformMatrix.value[4*2+1]*-zdelta;
				worldFPZdeltaPosition.value[j*3+2] = worldFPPosition.value[j*3+2]+camera.cameraTransformMatrix.value[4*2+2]*-zdelta;
			}
			camera.projectAndCalcScale(objToWorldTransform, worldFPZdeltaPosition, imageFPZdeltaPosition2);
			
			//Translate z
			for(int j=0;j<numberOfFeaturePoints;j++)
			{
				//float z = transformedWorldFPPosition.value[2+j*3]+objToCameraTransform.value[3*4+2];
				//float s = -z/((z+1)*(z+1));
				//This change 
				
				jacobian.value[jind+0] = ((imageFPZdeltaPosition.value[3*j+0]-imageFPPosition.value[3*j+0])/zdelta)*0.5f;
				jacobian.value[jind+1] = ((imageFPZdeltaPosition.value[3*j+1]-imageFPPosition.value[3*j+1])/zdelta)*0.5f;
				
				jacobian.value[jind+0] += ((imageFPZdeltaPosition2.value[3*j+0]-imageFPPosition.value[3*j+0])/-zdelta)*0.5f;
				jacobian.value[jind+1] += ((imageFPZdeltaPosition2.value[3*j+1]-imageFPPosition.value[3*j+1])/-zdelta)*0.5f;
//				jacobian.value[jind+0] = (imageFPPosition.value[3*j+0]-(camera.width*0.5f))*(s);
//				jacobian.value[jind+1] = (imageFPPosition.value[3*j+1]-(camera.height*0.5f))*(s);
				jind += 2;
			}			
			
			//FOV change
			float fovdelta = 1.0f;
			camera.setFOVY(rollbackfov+fovdelta);
			camera.projectAndCalcScale(objToWorldTransform, worldFPPosition, imageFPZdeltaPosition);
			camera.setFOVY(rollbackfov-fovdelta);
			camera.projectAndCalcScale(objToWorldTransform, worldFPPosition, imageFPZdeltaPosition2);
			camera.setFOVY(rollbackfov);
			fovdelta *= deltaScales.value[featurePointDeltas.height+6];
			for(int j=0;j<numberOfFeaturePoints;j++)
			{
				//float z = transformedWorldFPPosition.value[2+j*3]+objToCameraTransform.value[3*4+2];
				//float s = -z/((z+1)*(z+1));
				//This change 
				
				if(f>=fovFrom)
				{

				jacobian.value[jind+0] = ((imageFPZdeltaPosition.value[3*j+0]-imageFPPosition.value[3*j+0])/fovdelta)*0.5f;
				jacobian.value[jind+1] = ((imageFPZdeltaPosition.value[3*j+1]-imageFPPosition.value[3*j+1])/fovdelta)*0.5f;
				
				jacobian.value[jind+0] += ((imageFPZdeltaPosition2.value[3*j+0]-imageFPPosition.value[3*j+0])/-fovdelta)*0.5f;
				jacobian.value[jind+1] += ((imageFPZdeltaPosition2.value[3*j+1]-imageFPPosition.value[3*j+1])/-fovdelta)*0.5f;
//				jacobian.value[jind+0] = (imageFPPosition.value[3*j+0]-(camera.width*0.5f))*(s);
//				jacobian.value[jind+1] = (imageFPPosition.value[3*j+1]-(camera.height*0.5f))*(s);
				}
				
				jind += 2;
			}			
			
			//Scale the jacobian by eigenvalues to minimise error
			//I.e. parameters of small variance properties must be large to achieve change
			//then have the SVD minimise the sum of squared parameters
			
			
			if((niters<10)||((f%(niters/10))==0))
			{
				//System.out.println(""+f+"="+totalcost);
			}
			
			//These are sorted by cost and a percentage dropped, to achieve a base level of occlusion robustness
			QuickSort.quicksort(sortCost, sortIndex, 0, sortIndex.length-1);

			//Determine the occluding features
			float perc_out = 0.0f;
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
						}
						else
							ind+=2;
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
			
			float min = 0.0f;
			float max = 1.0f;
			float current = max;
			int numiterations = 5;
			boolean success = false;
			for(int k=0;k<numiterations;k++)
			{
				//current = 1.0f-((k)/(float)numiterations);
				current = 2.0f/(1<<k);
				//The deltas are in camera space (need to be restored to world space)
				if(modelNotCamera)
				{
					//Apply the translations
					currentTransform.value[3] += deltaParams.value[currentLambda.width+3]*current;
					currentTransform.value[4] += deltaParams.value[currentLambda.width+4]*current;
					currentTransform.value[5] += deltaParams.value[currentLambda.width+5]*current;
					
					//To calculate the absolute rotations, build a delta rotation using the deltas
					deltaRotation.setIdentity();
					deltaRotation.set3DTransformRotation(deltaParams.value[currentLambda.width+0]*current, deltaParams.value[currentLambda.width+1]*current, deltaParams.value[currentLambda.width+2]*current);
		
					//Then compose with the existing rotation
					objToCameraTransform.set3DTransformPosition(0.0f, 0.0f, 0.0f);
					GeneralMatrixFloat.transpose(deltaRotation, deltaRotationt);
					GeneralMatrixFloat.mult(deltaRotationt, objToCameraTransform, deltaRotation);
					//Then infer the new euler angles
					GeneralMatrixFloat.getEuler(deltaRotation,point3D);
					currentTransform.value[0] = point3D.value[0];
					currentTransform.value[1] = point3D.value[1];
					currentTransform.value[2] = point3D.value[2];
				}
				else
				{
					deltaRotation.setIdentity();
					deltaRotation.set3DTransformRotation(deltaParams.value[currentLambda.width+0]*current, deltaParams.value[currentLambda.width+1]*current, deltaParams.value[currentLambda.width+2]*current);
					deltaRotation.set3DTransformPosition(deltaParams.value[currentLambda.width+3]*current*camera.cameraTransformMatrix.value[4*0+0]+
							 deltaParams.value[currentLambda.width+4]*current*camera.cameraTransformMatrix.value[4*0+1]+
							 deltaParams.value[currentLambda.width+5]*current*camera.cameraTransformMatrix.value[4*0+2], 
							 deltaParams.value[currentLambda.width+3]*current*camera.cameraTransformMatrix.value[4*1+0]+
							 deltaParams.value[currentLambda.width+4]*current*camera.cameraTransformMatrix.value[4*1+1]+
							 deltaParams.value[currentLambda.width+5]*current*camera.cameraTransformMatrix.value[4*1+2], 
							 deltaParams.value[currentLambda.width+3]*current*camera.cameraTransformMatrix.value[4*2+0]+
							 deltaParams.value[currentLambda.width+4]*current*camera.cameraTransformMatrix.value[4*2+1]+
							 deltaParams.value[currentLambda.width+5]*current*camera.cameraTransformMatrix.value[4*2+2]);
	
					GeneralMatrixFloat.invert(deltaRotation,ictransform);
					//GeneralMatrixFloat.mult(deltaRotation,camera.cameraTransformMatrix, tempcamTransform);
					GeneralMatrixFloat.mult(camera.cameraTransformMatrix,deltaRotation, tempcamTransform);
					camera.cameraTransformMatrix.set(tempcamTransform);
					camera.setFOVY(rollbackfov+deltaParams.value[currentLambda.width+6]*current*deltaScales.value[featurePointDeltas.height+6]);
					
					Camera.buildModelMatrix(camera.cameraTransformMatrix,camera.modelMatrix);
				}
				
				camera.projectAndCalcScale(objToWorldTransform, worldFPPosition, imageFPPosition);
				float posttotalcost = 0.0f;
				for(int i=0;i<imageFPPosition.height;i++)
				{
					float cost = Float.MAX_VALUE;
					int bestPoint = 0;
					for(int j = 0;j<featurePointDetections[i].height;j++)
					{
						//A weighted sum of the euclidean distance and the matching quality
						float dx =  featurePointDetections[i].value[j*3+0]-imageFPPosition.value[i*3+0];
						float dy =  featurePointDetections[i].value[j*3+1]-imageFPPosition.value[i*3+1];
						float thisCost = dx*dx+dy*dy;
						//thisCost += imageQualityFactor*featurePointDetections[i].value[j*3+2];
						if(thisCost<cost)
						{
							bestPoint = j;
							cost = thisCost;
						}
					}			
					posttotalcost += cost;
				}
				if(posttotalcost>totalcost)
				{
					System.out.println("Cost is worse old:"+totalcost+" new:"+posttotalcost);
					currentTransform.set(rollbacktransform);
					camera.cameraTransformMatrix.set(rollbackcamera);
					camera.setFOVY(rollbackfov+deltaParams.value[currentLambda.width+6]*current);
					Camera.buildModelMatrix(camera.cameraTransformMatrix,camera.modelMatrix);
				}
				else
				{
					success = true;
					break;
				}
			}
			if(!success)
			{
				if(f<rotFrom)
					f = rotFrom-1;
				if(f<fovFrom)
					f = fovFrom-1;
				else				
					return;
			}
		}
	}
}
