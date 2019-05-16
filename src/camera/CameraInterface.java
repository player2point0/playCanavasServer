package camera;

import mathematics.GeneralMatrixFloat;
import ui.input.InputEvents;

public class CameraInterface 
{
	
	public static void defineCamera(int w,int h,GeneralMatrixFloat intrinsicCameraProperties)
	{
		int i = 0;
		//fov, cx, cy, kappa1, width, height
		intrinsicCameraProperties.value[i*6+0] = 80.0f;
		intrinsicCameraProperties.value[i*6+1] = 0.0f;
		intrinsicCameraProperties.value[i*6+2] = 0.0f;
		intrinsicCameraProperties.value[i*6+3] = 0.0f;
		intrinsicCameraProperties.value[i*6+4] = w;
		intrinsicCameraProperties.value[i*6+5] = h;				
	}

	public static void initDefault(int w,int h,GeneralMatrixFloat extrinsicCameraProperties,GeneralMatrixFloat intrinsicCameraProperties)
	{
		defineCamera(w, h, intrinsicCameraProperties);
		
		extrinsicCameraProperties.value[0] = 0.0f;
		extrinsicCameraProperties.value[1] = 0.0f;
		extrinsicCameraProperties.value[2] = 0.0f;

		extrinsicCameraProperties.value[3] = 3.5f;

		extrinsicCameraProperties.value[4] = 0.0f;
		extrinsicCameraProperties.value[5] = 0.0f;
		extrinsicCameraProperties.value[6] = 0.0f;
	}

    public static boolean updateCamera(int i,Camera c,InputEvents input,GeneralMatrixFloat extrinsicCameraProperties)
    {
    	boolean cameradirty = false;
		if(input.dragging)
		{
			//System.out.println("Draggin should be re rendering");
			if(!input.altDown)//input.metaDown)
			{	
//				if(input.letterDown('r'))
//				{
//					extrinsicCameraProperties.value[7*i+7] += input.mousedX;
//					if(extrinsicCameraProperties.value[7*i+7]<-180.0f)
//						extrinsicCameraProperties.value[7*i+7] = extrinsicCameraProperties.value[7*i+7]+360.0f;
//					if(extrinsicCameraProperties.value[7*i+7]>180.0f)
//						extrinsicCameraProperties.value[7*i+7] = extrinsicCameraProperties.value[7*i+7]-360.0f;
//				}
//				else
				if(input.shiftDown)
				{
					{
						float scale = 0.1f;//extrinsicCameraProperties.value[7*i+3]/100.0f;
						float d = -scale*input.mousedY;

						if(d>0.8f)
							d=0.8f;
						if(d<-0.8f)
							d=-0.8f;
						
						float tzscale = 1.0f/1.0f+d;
						if(d<0.0f)
							tzscale = 1.0f+d;

						//Translate in axes of camera
						//extrinsicCameraProperties.value[7*i+3] -= input.mousedY*scale;
						//extrinsicCameraProperties.value[7*i+3] -= input.mousedY*scale;
						extrinsicCameraProperties.value[7*i+3] *= tzscale;
						if(extrinsicCameraProperties.value[7*i+3]<0.00001f)
							extrinsicCameraProperties.value[7*i+3]=0.00001f;
						
						cameradirty = true;
					}
				}
				else
				{
					float scale = extrinsicCameraProperties.value[7*i+3]/400.0f;
					float dx = -input.mousedX*scale;
					//Negative y is positive world y
					float dy = input.mousedY*scale;
					boolean moved = false;
					
					if(input.letterDown('x'))
					{
						extrinsicCameraProperties.value[7*i+4] += c.cameraTransformMatrix.value[0*4+0]*dx;
						extrinsicCameraProperties.value[7*i+4] += c.cameraTransformMatrix.value[1*4+0]*dy;
						moved = true;
					}
					if(input.letterDown('y'))
					{
						extrinsicCameraProperties.value[7*i+5] += c.cameraTransformMatrix.value[0*4+1]*dx;
						extrinsicCameraProperties.value[7*i+5] += c.cameraTransformMatrix.value[1*4+1]*dy;
						moved = true;
					}
					if(input.letterDown('z'))
					{
						extrinsicCameraProperties.value[7*i+6] += c.cameraTransformMatrix.value[0*4+2]*dx;
						extrinsicCameraProperties.value[7*i+6] += c.cameraTransformMatrix.value[1*4+2]*dy;
						moved = true;
					}
					if(input.letterDown('s'))
					{
						extrinsicCameraProperties.value[7*i+4] += c.cameraTransformMatrix.value[0*4+0]*dx;
						extrinsicCameraProperties.value[7*i+4] += c.cameraTransformMatrix.value[1*4+0]*dy;

						extrinsicCameraProperties.value[7*i+5] += c.cameraTransformMatrix.value[0*4+1]*dx;
						extrinsicCameraProperties.value[7*i+5] += c.cameraTransformMatrix.value[1*4+1]*dy;

						extrinsicCameraProperties.value[7*i+6] += c.cameraTransformMatrix.value[0*4+2]*dx;
						extrinsicCameraProperties.value[7*i+6] += c.cameraTransformMatrix.value[1*4+2]*dy;
						moved = true;
					}
					if(!moved)
					{
						extrinsicCameraProperties.value[7*i+0] += input.mousedY;
						if(extrinsicCameraProperties.value[7*i+0]>89.5f)
							extrinsicCameraProperties.value[7*i+0] = 89.5f;
						if(extrinsicCameraProperties.value[7*i+0]<-89.5f)
							extrinsicCameraProperties.value[7*i+0] = -89.5f;
						extrinsicCameraProperties.value[7*i+1] += input.mousedX;
						if(extrinsicCameraProperties.value[7*i+1]<-180.0f)
							extrinsicCameraProperties.value[7*i+1] = extrinsicCameraProperties.value[7*i+1]+360.0f;
						if(extrinsicCameraProperties.value[7*i+1]>180.0f)
							extrinsicCameraProperties.value[7*i+1] = extrinsicCameraProperties.value[7*i+1]-360.0f;
						cameradirty = true;							
					}
				}
			}
			else if(input.altDown)
			{
				float scale = extrinsicCameraProperties.value[7*i+3]/400.0f;

				float dx = -input.mousedX*scale;
				//Negative y is positive world y
				float dy = input.mousedY*scale;
				//Translate in axes of camera
				extrinsicCameraProperties.value[7*i+4] += c.cameraTransformMatrix.value[0*4+0]*dx;
				extrinsicCameraProperties.value[7*i+4] += c.cameraTransformMatrix.value[1*4+0]*dy;

				extrinsicCameraProperties.value[7*i+5] += c.cameraTransformMatrix.value[0*4+1]*dx;
				extrinsicCameraProperties.value[7*i+5] += c.cameraTransformMatrix.value[1*4+1]*dy;

				extrinsicCameraProperties.value[7*i+6] += c.cameraTransformMatrix.value[0*4+2]*dx;
				extrinsicCameraProperties.value[7*i+6] += c.cameraTransformMatrix.value[1*4+2]*dy;
					cameradirty = true;
			}
		}
		{
//			if(input.pressed[InputEvents.K_EQUALS]||input.down[InputEvents.K_EQUALS])
//			{
//				extrinsicCameraProperties.value[7*i+3] /= 2.0f;
//			}
//			else
//			if(input.pressed[InputEvents.K_MINUS]||input.down[InputEvents.K_MINUS])
//			{
//				extrinsicCameraProperties.value[7*i+3] *= 2.0f;
//			}				
		}
		return cameradirty;
    }

    public static void setCamera(int i,Camera c,GeneralMatrixFloat intrinsicCameraProperties,GeneralMatrixFloat extrinsicCameraProperties,boolean rotateArroundPoint,float znear,float zfar)
    {
		c.setFromFOVY(intrinsicCameraProperties.value[6*i+0], znear, zfar,  
				(int)intrinsicCameraProperties.value[6*i+4], (int)intrinsicCameraProperties.value[6*i+5]);
		c.centrePixelOffsetx = intrinsicCameraProperties.value[6*i+1];
		c.centrePixelOffsety = intrinsicCameraProperties.value[6*i+2];
		c.kappa1 = intrinsicCameraProperties.value[6*i+3];

		if(rotateArroundPoint)
		{
			c.setFromRotateAroundPoint(extrinsicCameraProperties.value[7*i+4], 
					extrinsicCameraProperties.value[7*i+5], 
					extrinsicCameraProperties.value[7*i+6], 
					extrinsicCameraProperties.value[7*i+0], 
					extrinsicCameraProperties.value[7*i+1], 
					(extrinsicCameraProperties.value[7*i+2]/(float)Math.PI)*180.0f, 
					extrinsicCameraProperties.value[7*i+3]);
		}
		else
		{
			c.setModelFromLookat(extrinsicCameraProperties.value[7*i+0], 
					extrinsicCameraProperties.value[7*i+1], 
					extrinsicCameraProperties.value[7*i+2], 
					extrinsicCameraProperties.value[7*i+3], 
					extrinsicCameraProperties.value[7*i+4], 
					extrinsicCameraProperties.value[7*i+5], 
					(extrinsicCameraProperties.value[7*i+6]/(float)Math.PI)*180.0f);
		}
		Camera.buildModelMatrix(c.cameraTransformMatrix,c.modelMatrix);
		c.buildClipPlanes();
    }

}
