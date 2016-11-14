import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageSeg{

	// read the image and put it value in an one dimension array
	public int[] getImagePixels(String filename) throws Exception{
		BufferedImage image = ImageIO.read(getClass().getResourceAsStream(filename));

		int [] dummy = null;
		int width, height;
		width = image.getWidth();
		height = image.getHeight();
		// start getting the pixels
		Raster pixelData;
		pixelData = image.getData();

		// System.out.println(filename);
		// System.out.println("width:"+ width);
		// System.out.println("height:"+ height);
		// System.out.println("Channels:"+pixelData.getNumDataElements());
		return pixelData.getPixels(0, 0, width, height, dummy);
	}

	// display the value of each pixel in the image
	public void displayImagePixels(int [] pixels, String filename) {
		System.out.println(filename);
		for(int i=0; i<pixels.length; i++){
			System.out.format("%4d",pixels[i]);
			if( (i+1)%69 == 0 )
				System.out.format("%n");
			//System.out.println(pixels[i]);
		}

	}

	public static void main(String[] args) throws IOException, Exception {
		ImageSeg image_seg = new ImageSeg();
		int[] img_pixel, fgin_pixel, bgin_pixel, fgout_pixel, bgout_pixel;
		img_pixel	= image_seg.getImagePixels(args[0]);
		fgin_pixel	= image_seg.getImagePixels(args[1]);
		bgin_pixel	= image_seg.getImagePixels(args[2]);

		image_seg.displayImagePixels(img_pixel,	args[0]);
		image_seg.displayImagePixels(fgin_pixel, args[1]);
		image_seg.displayImagePixels(bgin_pixel, args[2]);
	}
}
