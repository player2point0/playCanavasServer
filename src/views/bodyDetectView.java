package views;

import org.json.JSONArray;
import org.json.JSONObject;

import storage.DatabaseInterface;
import storage.FileStoreInterface;
import web.WebRequest;
import web.WebResponse;

public class bodyDetectView extends DynamicWebPage {
		
    public bodyDetectView(DatabaseInterface db, FileStoreInterface fs) {
		super(db, fs);
		// TODO Auto-generated constructor stub
	} 
    
    public boolean process(WebRequest toProcess)
    {
        if(toProcess.path.equalsIgnoreCase("bodydetect/playCanvasStart"))
        { 	  	
            JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            String bodyDetectIcon = "./img/Scenes/Body Detect.png";
                      
            double yHeight = 8;   
            
            JSONObject center = imgTile(0, yHeight, -5, bodyDetectIcon);
            entities.put(0, center);
             
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
        
        else if(toProcess.path.equalsIgnoreCase("bodydetect/playCanvasUpdate"))
        {  	
        	JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
         
        return false;
    }
    
    
    public JSONObject imgTile(double x, double y, double z, String texture)
    {
        JSONObject entity = new JSONObject();
        entity.put("model", "plane");
        entity.put("x", x);
        entity.put("y", y);
        entity.put("z", z);
        entity.put("xRotate", 90);
        entity.put("texture", texture);
        
        entity.put("xScale", 5.9*2.5);
        entity.put("yScale", 1.5*2.5);
        entity.put("zScale", 1.5*2.5);
        
        entity.put("clickLink", "menu");
        entity.put("boundingBoxX", 1);
        entity.put("boundingBoxY", 1);
        entity.put("boundingBoxZ", 1);
        
        return entity;
    }
    
}