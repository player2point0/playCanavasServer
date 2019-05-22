package views;

import org.json.JSONObject;

import org.json.JSONArray;
import storage.DatabaseInterface;
import storage.FileStoreInterface;
import web.WebRequest;
import web.WebResponse;

public class brainUI extends DynamicWebPage {
	
	private double offset = 0;
	
    public brainUI(DatabaseInterface db, FileStoreInterface fs) {
		super(db, fs);
		// TODO Auto-generated constructor stub
	} 
    
    public boolean process(WebRequest toProcess)
    {
        if(toProcess.path.equalsIgnoreCase("playCanvas"))
        {
            String stringToSendToWebBrowser = "<html>\n" + 
        			"<head>"+
            			"<script src=\"https://code.playcanvas.com/playcanvas-latest.js\"></script>\n" + 
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
        
        else if(toProcess.path.equalsIgnoreCase("playCanvasStart"))
        { 	  	
            JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
                   
            double cubeNum = 12;
            double angleAmt = (2*Math.PI) / cubeNum;
            double dis = 5;
            
            for(int i = 0;i<cubeNum;i++)
            {
            	double angle = angleAmt * i;
            	double x = Math.sin(angle) * dis;
            	double z = Math.cos(angle) * dis;
            	
            	JSONObject entity = new JSONObject();
            	entity.put("model", "box");
            	entity.put("name", i);
            	entity.put("realtimeModel", true);
            	entity.put("x", x);
            	entity.put("z", z);
            	entity.put("yRotate", (angle * (180/Math.PI)));
            	
            	entities.put(i, entity);
            }  
            
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
        
        else if(toProcess.path.equalsIgnoreCase("playCanvasUpdate"))
        {  	
        	JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            double cubeNum = 12;
            double angleAmt = (2*Math.PI) / cubeNum;
            double dis = 5;
            offset += 0.0375;
            
            for(int i = 0;i<cubeNum;i++)
            {
            	double angle = angleAmt * (i + offset);
            	double x = Math.sin(angle) * dis;
            	double z = Math.cos(angle) * dis;
            	
            	JSONObject entity = new JSONObject();
            	entity.put("model", "box");
            	entity.put("name", i);
            	entity.put("x", x);
            	entity.put("z", z);
            	entity.put("yRotate", (angle * (180/Math.PI)));
            	
            	entities.put(i, entity);
            }  
            
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
         
        return false;
    }
}