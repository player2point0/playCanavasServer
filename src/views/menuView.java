package views;

import org.json.JSONArray;
import org.json.JSONObject;

import storage.DatabaseInterface;
import storage.FileStoreInterface;
import web.WebRequest;
import web.WebResponse;

public class menuView extends DynamicWebPage {
		
    public menuView(DatabaseInterface db, FileStoreInterface fs) {
		super(db, fs);
		// TODO Auto-generated constructor stub
	} 
    
    public boolean process(WebRequest toProcess)
    {
        if(toProcess.path.equalsIgnoreCase("menu"))
        {
            String stringToSendToWebBrowser = "<html>\n" + 
        			"<head>"+
    					"<script src=\"./js/playcanvas-latest.js\"></script>\n" + 
            			"<script src=\"./js/JavaEntity.js\"></script>\n" + 
            			"<script src=\"./js/FirstPersonCam.js\"></script>\n" + 
                        "<link href=\"css/stats.css\" rel=\"stylesheet\" type=\"text/css\">" +
            		"</head>"+
            		"<body>\n" + 
                    "<canvas id=\"application-canvas\"></canvas>\n" + 
            		"<script src=\"js/playCanvas.js\"></script>\n"+
            		"  </body>\n" + 
            		"</html>";

            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_HTML, stringToSendToWebBrowser );
            return true;
        }
        
        else if(toProcess.path.equalsIgnoreCase("menu/playCanvasStart"))
        { 	  	
            JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            String sceneDetectIcon = "./img/Scene Detect.png";
            String bodyDetectIcon = "./img/Body Detect.png";
            String faceDetectIcon = "./img/Face Detect.png";
            String voiceDetectIcon = "./img/Voice Detect.png";
            String autoIcon = "./img/AUTO.png";
            String virtualniIcon = "./img/Virtual NI.png";
            String virtualniStatsIcon = "./img/Virtual NI stats.png";   
            
            double yHeight = 8;
            
            JSONObject topLeft = menuTile(-1.5, (yHeight + 1.5), -5, "scenedetect", sceneDetectIcon);
            entities.put(0, topLeft);
            JSONObject top = menuTile(0, (yHeight + 1.5), -5, "bodydetect", bodyDetectIcon);
            entities.put(1, top);
            JSONObject topRight = menuTile(1.5, (yHeight + 1.5), -5, "facedetect", faceDetectIcon);
            entities.put(2, topRight);
            JSONObject centerLeft = menuTile(-1.5, yHeight, -5, "voicedetect", voiceDetectIcon);
            entities.put(3, centerLeft);
            JSONObject center = menuTile(0, yHeight, -5, "auto", autoIcon);
            entities.put(4, center);
            JSONObject centerRight = menuTile(1.5, yHeight, -5, "virtualni", virtualniIcon);
            entities.put(5, centerRight);
            JSONObject bottomLeft = menuTile(-1.5, (yHeight - 1.5), -5, "virtualnistats", virtualniStatsIcon);
            entities.put(6, bottomLeft);
            
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
        
        else if(toProcess.path.equalsIgnoreCase("menu/playCanvasUpdate"))
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
    
    
    public JSONObject menuTile(double x, double y, double z, String link, String texture)
    {
        JSONObject entity = new JSONObject();
        entity.put("model", "plane");
        entity.put("x", x);
        entity.put("y", y);
        entity.put("z", z);
        entity.put("xRotate", 90);
        entity.put("clickLink", link);
        entity.put("boundingBoxX", 0.5);
        entity.put("boundingBoxY", 0.5);
        entity.put("boundingBoxZ", 0.5);
        entity.put("texture", texture);
        
        return entity;
    }
    
}
    