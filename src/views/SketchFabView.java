package views;

import org.json.JSONObject;

import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.io.*;

import org.json.JSONArray;
import storage.DatabaseInterface;
import storage.FileStoreInterface;
import web.WebRequest;
import web.WebResponse;

public class SketchFabView extends DynamicWebPage {
		
    public SketchFabView(DatabaseInterface db, FileStoreInterface fs) {
		super(db, fs);
		// TODO Auto-generated constructor stub
	} 
    
    public boolean process(WebRequest toProcess)
    {
        if(toProcess.path.equalsIgnoreCase("sketchfab"))
        {
            String stringToSendToWebBrowser = "<html>\n" + 
        			"<head>"+
            			"<script src=\"./js/playcanvas-latest.js\"></script>\n" + 
            			"<script src=\"./js/playcanvas-anim.js\"></script>\n" + 
            			"<script src=\"./js/playcanvas-gltf.js\"></script>\n" +
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
        
        else if(toProcess.path.equalsIgnoreCase("sketchfab/playCanvasStart"))
        { 	  	
            JSONObject responseData = new JSONObject();
            JSONArray entities = new JSONArray();
            
            //String path = "C:\\Users\\makerspacestaff\\Documents\\playcanvas\\playCanvasServer\\httpdocs\\sketchFab\\b52a08120c059008.zip\\out\\b52a08120c059008\\"+"ad10226b4f7a451ea23920a556c72a90.zip";
    		//String path1 = "C:\\Users\\makerspacestaff\\Documents\\playcanvas\\playCanvasServer\\httpdocs\\sketchFab\\chicken";
            
            String query = "box";
            
            
            try {
				getSketchFabModel(query);
			} catch (Exception e) {
				query = "dog";
				e.printStackTrace();
			}
            
            JSONObject entity1 = new JSONObject();
            entity1.put("model", "box");
            entity1.put("sketchFabFolder", query);
            entity1.put("scriptName", "rotate1");
            entity1.put("script", "this.entity.rotate(0, 10 * dt, 0);");
            
            entities.put(0, entity1);
                            
            responseData.put("entities", entities);
                    
            responseData.put("time", System.currentTimeMillis()); 
            responseData.put("vr", true); 
            
                        
            toProcess.r = new WebResponse( WebResponse.HTTP_OK, WebResponse.MIME_PLAINTEXT, responseData.toString() );
            return true;
        }
        
        else if(toProcess.path.equalsIgnoreCase("sketchfab/playCanvasUpdate"))
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
    
    
    public void getSketchFabModel(String query) throws Exception
    {
    	String webURL = "http://169.254.100.38:5000/sfscraper/get?query="+query+"&max_items=1&wait=on";
    	URL oracle = new URL(webURL);
        URLConnection yc = oracle.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String page = in.readLine();
        String jobID = "";
        in.close();
        
        for(int i = 8;i<page.length();i++)
        {
        	if(page.charAt(i) == '<')break;
        	jobID += page.charAt(i);
        }
        
        String downloadURL = "http://169.254.100.38:5000/sfscraper/download/"+jobID;        
    	System.out.println("downloading");

        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(downloadURL).openStream()))  
		{
        	System.out.println("downloaded");  	
        	
        	//Open the file
            try(ZipInputStream stream = new ZipInputStream(inputStream))
            {
            
            	String outDir = "httpdocs/sketchFab/"+query+"/";
            	
    			ZipEntry entry;
	            while ((entry = stream.getNextEntry()) != null) {

    				String name = entry.getName();
    				long size = entry.getSize();
    				long compressedSize = entry.getCompressedSize();
    				System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n", 
    						name, size, compressedSize);

    				File file = new File(outDir+name);
    				if (name.endsWith("/")) {
    					file.mkdirs();
    					continue;
    				}

    				File parent = file.getParentFile();
    				if (parent != null) {
    					parent.mkdirs();
    				}

    		        
    				FileOutputStream fos = new FileOutputStream(file);
    				byte[] bytes = new byte[1024];
    				int length;
    				while ((length = stream.read(bytes)) >= 0) {
    					fos.write(bytes, 0, length);
    				}
    				
    				
    				if(name.endsWith(".zip"))
    				{	
    					ZipFile zipFile = new ZipFile(outDir+name);
    					Enumeration<?> enu = zipFile.entries();
    					while (enu.hasMoreElements()) {
    						ZipEntry zipEntry = (ZipEntry) enu.nextElement();

    						name = zipEntry.getName();
    						size = zipEntry.getSize();
    						compressedSize = zipEntry.getCompressedSize();
    						System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n", 
    								name, size, compressedSize);

    						file = new File(outDir+name);
    						if (name.endsWith("/")) {
    							file.mkdirs();
    							continue;
    						}

    						parent = file.getParentFile();
    						if (parent != null) {
    							parent.mkdirs();
    						}

    						InputStream is = zipFile.getInputStream(zipEntry);
    						fos = new FileOutputStream(file);
    						bytes = new byte[1024];
    						
    						while ((length = is.read(bytes)) >= 0) {
    							fos.write(bytes, 0, length);
    						}
    						is.close();
    						fos.close();

    					}
    					zipFile.close();
    				
    				}	
    			}
            }
		} 
    }

}