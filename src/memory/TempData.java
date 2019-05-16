package memory;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import rendering.RenderBuffer;
import rendering.RenderBufferFloat;
import camera.Camera;

public class TempData 
{
	public Camera camera = new Camera();

	//Wrappers for one of the lower arrays
	public RenderBuffer tempBuffer = new RenderBuffer();
	public RenderBuffer tempBuffer2 = new RenderBuffer();
	public RenderBuffer tempBuffer3 = new RenderBuffer();
	public RenderBuffer tempBuffer4 = new RenderBuffer();
	public RenderBufferFloat tempBufferFloat = new RenderBufferFloat();
	public RenderBufferFloat tempBufferFloat2 = new RenderBufferFloat();
	public RenderBufferFloat tempBufferFloat3 = new RenderBufferFloat();
	
	//Utility variables
	public GeneralMatrixFloat worldPoint = new GeneralMatrixFloat(3,1); 
	public GeneralMatrixFloat imagePoint = new GeneralMatrixFloat(3,1); 
	public GeneralMatrixFloat imagePointWithScale = new GeneralMatrixFloat(4,1); 
	public GeneralMatrixFloat uiPoint = new GeneralMatrixFloat(2,1); 
	public GeneralMatrixFloat ray = new GeneralMatrixFloat(6,1);
	//Debug tris that overlap each other in the uv map
	public float[] postModel = new float[3];
	public float[] postProj = new float[4];
	public GeneralMatrixFloat centre = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat bounds = new GeneralMatrixFloat(3,3);

	public GeneralMatrixFloat tempPoint = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint2 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint3 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint4 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint5 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint6 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint7 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint8 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint9 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint10 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint11 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint12 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint13 = new GeneralMatrixFloat(3,1);
	public GeneralMatrixFloat tempPoint14 = new GeneralMatrixFloat(3,1);

	public GeneralMatrixInt tempIntPoint = new GeneralMatrixInt(4,1);
	public GeneralMatrixInt tempIntPoint2 = new GeneralMatrixInt(4,1);
	public GeneralMatrixInt tempIntPoint3 = new GeneralMatrixInt(4,1);
	public GeneralMatrixInt tempIntPoint4 = new GeneralMatrixInt(4,1);
	
	//Matrices
	public GeneralMatrixFloat localMatrix = new GeneralMatrixFloat(4,4);
	public GeneralMatrixFloat worldMatrix = new GeneralMatrixFloat(4,4);
	public GeneralMatrixFloat tempMatrix = new GeneralMatrixFloat(4,4);
	
	//Buffers of varying length
	public GeneralMatrixInt tempInt = new GeneralMatrixInt(1);
	public GeneralMatrixInt tempInt2 = new GeneralMatrixInt(1);
	public GeneralMatrixInt tempInt3 = new GeneralMatrixInt(1);
	public GeneralMatrixInt tempInt4 = new GeneralMatrixInt(1);
	public GeneralMatrixInt tempInt5 = new GeneralMatrixInt(1);
	public GeneralMatrixInt tempInt6 = new GeneralMatrixInt(1);
	public GeneralMatrixInt tempInt7 = new GeneralMatrixInt(1);
	public GeneralMatrixInt tempInt8 = new GeneralMatrixInt(1);
	public GeneralMatrixInt tempInt9 = new GeneralMatrixInt(1);
	public GeneralMatrixInt tempInt10 = new GeneralMatrixInt(1);
	public GeneralMatrixInt tempInt11 = new GeneralMatrixInt(1);
	//public GeneralMatrixInt tempV = new GeneralMatrixInt(1);
	//public GeneralMatrixInt loadTempData = new GeneralMatrixInt(7,1);

	public GeneralMatrixFloat tempFloat = new GeneralMatrixFloat(3);
	public GeneralMatrixFloat tempFloat2 = new GeneralMatrixFloat(3);
	//public GeneralMatrixFloat tempPolygonUVs = new GeneralMatrixFloat(2);
	public GeneralMatrixFloat tempFloat3 = new GeneralMatrixFloat(2);
	//public GeneralMatrixFloat debugClipPlanes = new GeneralMatrixFloat(4,6);	
	public GeneralMatrixFloat tempFloat4 = new GeneralMatrixFloat(3,8);
	public GeneralMatrixFloat tempFloat5 = new GeneralMatrixFloat(1);
	public GeneralMatrixFloat tempFloat6 = new GeneralMatrixFloat(1);
}
