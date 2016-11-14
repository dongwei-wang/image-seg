import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageSeg{
	BufferedImage image;
	void load(String filename) throws Exception {
		image = ImageIO.read(getClass().getResourceAsStream(filename));
	}

	public int[] getImagePixels() {
		int [] dummy = null;
		int width, height;
		width = image.getWidth();
		height = image.getHeight();

		// start getting the pixels
		Raster pixelData;
		pixelData = image.getData();

		// System.out.println("width:"+ width);
		// System.out.println("height:"+ height);
		// System.out.println("Channels:"+pixelData.getNumDataElements());
		return pixelData.getPixels(0, 0, width, height, dummy);
	}

	public static void main(String[] args) throws IOException, Exception {
		ImageSeg l = new ImageSeg();

		l.load(args[0]);
		int[] pixel;
		pixel= l.getImagePixels();
		//System.out.println("length:"+pixel.length);

		for(int i=0; i<pixel.length; i++){
			System.out.println(pixel[i]);
		}
	}
}
