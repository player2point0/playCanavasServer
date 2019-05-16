package os;

import importexport.TextImportExport;

import java.io.File;

import mathematics.GeneralMatrixString;

public class Devices 
{
	public static void getVideo4LinuxDevices(GeneralMatrixString ids)
	{
		ids.height = 0;
	    File dir = new File("/sys/class/video4linux");
	    if(!dir.exists())
	    	return;
	    String[] gallery = dir.list();
	    for(int i=0;i<gallery.length;i++)
	    {
	    	ids.push_back(gallery[i]);
	    }
	}
	
	public static void getUSBDevices(String vendor,String product,GeneralMatrixString ids)
	{
		ids.height = 0;
	    File dir = new File("/sys/bus/usb/devices");
	    if(!dir.exists())
	    	return;
	    String[] gallery = dir.list();
	    for(int i=0;i<gallery.length;i++)
	    {
	    	if(!FileSystem.exists("/sys/bus/usb/devices/"+gallery[i]+"/idProduct"))
	    		continue;
	    	if(!FileSystem.exists("/sys/bus/usb/devices/"+gallery[i]+"/idVendor"))
	    		continue;
	    	String idVendor = TextImportExport.loadTextFileASString("/sys/bus/usb/devices/"+gallery[i]+"/idVendor");
	    	if(!idVendor.equalsIgnoreCase(vendor))
	    		continue;
	    	String idProduct = TextImportExport.loadTextFileASString("/sys/bus/usb/devices/"+gallery[i]+"/idProduct");
	    	if(!idProduct.equalsIgnoreCase(product))
	    		continue;
	    	ids.push_back(gallery[i]);
	    }
	}	
}
