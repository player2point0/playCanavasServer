/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;

import rendering.RenderBuffer;

public class WebDisplay implements ImageProducer
{
	private RenderBuffer rb;
	boolean consumerDirty = false;
	private ImageConsumer consumer;
	final private Image image;
	final private ColorModel cm=new DirectColorModel(32,0xFF0000,0xFF00,0xFF);
	final private int hints,sfd;

	public WebDisplay(final RenderBuffer rb)
	{
		this.rb = rb;
		image=Toolkit.getDefaultToolkit().createImage(this);	
		hints=ImageConsumer.TOPDOWNLEFTRIGHT
		|ImageConsumer.COMPLETESCANLINES
		|ImageConsumer.SINGLEPASS
		|ImageConsumer.SINGLEFRAME;
		sfd=ImageConsumer.SINGLEFRAMEDONE;
	}
	
	public Image getImage()
	{
		update();
		return image;
	}

	public synchronized void resize(int w,int h)
	{
		rb.ensureCapacity(w*h);
		rb.width = w;
		rb.height = h;
		consumerDirty = true;
	}
	
	public synchronized void addConsumer(final ImageConsumer consumer)
	{
		this.consumer=consumer;
	}

	public synchronized void startProduction(final ImageConsumer imageconsumer)
	{
		final int w = rb.width;
		final int h = rb.height;
		final int[] pixel = rb.pixel;
		if ((!imageconsumer.equals(consumer))||consumerDirty)
		{
			consumer=imageconsumer;
			consumer.setDimensions(w,h);
			consumer.setProperties(null);
			consumer.setColorModel(cm);
			consumer.setHints(hints);
			consumerDirty = false;
		}
		consumer.setPixels(0, 0, w, h, cm, pixel, 0, w);
		consumer.imageComplete(sfd);
	}
    	
    		
	public void update()
	{
		if (consumer!=null) startProduction(consumer);
	}
	
	public final boolean isConsumer(final ImageConsumer imageconsumer)
	{
		return consumer.equals(imageconsumer);
	}
	
	public synchronized void requestTopDownLeftRightResend(final ImageConsumer imageconsumer)
	{
	}
	
	public synchronized void removeConsumer(ImageConsumer imageconsumer)
	{
	}	
}
