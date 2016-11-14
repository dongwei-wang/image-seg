import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// interface Parametrization{
//     public void initialize(int img[][], int height, int width, int bg[][], int fg[][]);
//     public double penaltyP(int x1, int y1, int x2, int y2);
//     public double penaltyF(int x, int y);
//     public double penaltyB(int x, int y);
// }

// class SomeParametrization implements Parametrization{
//public class ImageSeg implements Parametrization{
public class ImageSeg{

	int img[][]=null;
	double h=-1;
	double w=-1;
	int bg[][]=null;
	int fg[][]=null;

	int fgHist[]=new int[256];
	int bgHist[]=new int[256];

	double fgConst=10;
    double bgConst=10;
    double fgHintBump=1000;
    double bgHintBump=1000;
    double graphConst=1000;
	double dThreshold=3;
	double distExpConst=100.0;

	public void initialize(int img[][], int height, int width, int bg[][], int fg[][]){
	// img[0...height-1][0...width-1], same for fg and bg
	// i.e. first index is Y, second index is X
		this.img=img;
		this.bg=bg;
		this.fg=fg;
		this.h=height;
		this.w=width;
		int x,y;
		int c;

		for(c=0;c<256;c++){
			fgHist[c]=1;
			bgHist[c]=1; //initialize psedocounts
		}

		for (x=0;x<w;x++){
			for (y=0;y<h;y++){
				if (fg[y][x]>0)
					fgHist[img[y][x]]++;
				if (bg[y][x]>0)
					bgHist[img[y][x]]++;
			}
		}
	}

	public double penaltyP(int x1, int y1, int x2, int y2){
		int c1=img[y1][x1];
		int c2=img[y2][x2];
		double dist = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
		double cDiff=Math.sqrt((c1-c2)*(c1-c2))/255;
		double penalty=0;
		if (dist>dThreshold)
			penalty=0;
		else if (dist==0) // this shouldn't happen!
			penalty=0;
		else{
			penalty = graphConst * (1.0/dist) * (Math.exp(-distExpConst*cDiff));
		}
		return penalty;
	}

	public  double penaltyF(int x, int y){
		int c=img[y][x];
		int isInHint=bg[y][x];
		double bgProb=((double)bgHist[c])/((double)fgHist[c]+(double)bgHist[c]);
		double penalty= bgConst*(bgProb +bgHintBump*isInHint);
		return penalty;
	}

	public double penaltyB(int x, int y){
		int c=img[y][x];
		int isInHint=fg[y][x];
		double fgProb=((double)fgHist[c])/((double)fgHist[c]+(double)bgHist[c]);
		double penalty= fgConst*(fgProb +fgHintBump*isInHint);
		return penalty;
	}

	public void solver(){

	}

	public int[][] getImagePixels(String filename) throws Exception{
		//BufferedImage image = ImageIO.read(getClass().getResourceAsStream(filename));
		BufferedImage image = ImageIO.read(new File(filename));

		// variable to pass to raster getPixels() function as a parameter
		int [] dummy = null;

		// store the one dimension array from getPixels()
		int [] one_d_pixels = null;

		int width, height;
		width = image.getWidth();
		height = image.getHeight();

		// this is the return two dimension array for the image
		int [][] two_d_pixels = new int[height][width];

		// start getting the pixels
		Raster rasterData;
		rasterData = image.getData();
		one_d_pixels = rasterData.getPixels(0, 0, width, height, dummy);

		// convert the array from one dimension to two dimensions
		for( int i=0; i<height; i++ ){
			for(int j=0; j<width; j++){
				two_d_pixels[i][j] = one_d_pixels[i*width + j];
			}
		}
		return two_d_pixels;
	}

	// display the value of each pixel in the image
	public void displayImagePixels(int [][] pixels, String filename) {
		System.out.println(filename);

		// get the height of the two dimension array
		int height = pixels.length;
		// get the width of the two dimension array
		int width = pixels[0].length;

		System.out.format("The height is: %d, width is %d\n",height, width );

		for( int i=0; i<height; i++ ){
			for( int j=0; j<width; j++ )
				System.out.format("%4d", pixels[i][j]);
			System.out.format("%n");
		}
	}

	public static void main(String[] args) throws IOException, Exception{
		System.out.println("This is the program to process image");

		ImageSeg image_seg = new ImageSeg();
		int[][] img_pixel, fgin_pixel, bgin_pixel, fgout_pixel, bgout_pixel;
		img_pixel	= image_seg.getImagePixels(args[0]);
		fgin_pixel	= image_seg.getImagePixels(args[1]);
		bgin_pixel	= image_seg.getImagePixels(args[2]);

		image_seg.displayImagePixels(img_pixel,	args[0]);
		image_seg.displayImagePixels(fgin_pixel, args[1]);
		image_seg.displayImagePixels(bgin_pixel, args[2]);
	}

}


/* main() skeleton:

usage: java segment img.png fgIn.png bgIn.png fgOut.png bgOut.png

1) read images into img[][], fgIn[][] bgIn[][] from .png files;
1a) all three images should have the same dimensions, imH * imW (determine what imH, imW is)
1b) img[y][x] should be in range from 0 (black) to 255 (white)
1c) fgIn[y][x] should have 255 (white) for "likely" foreground, 0 (black) for the rest (unknown status)
1d) bgIn[y][x] should have 255 (white) for "likely" background, 0 (black) for the rest (unknown status)

2) create a parameterization object based on the image, foreground, and background, and img dimensions, e.g.
myParam=new SomeParametrization();myParam.initialize(img, imH, imW, fgIn, bgIn);

3) use the solver (which is the main objective of this homework project) to obtain fgOut and bgOut, e.g.
3a) allocate fgOut, bgOut 2D arrays (h x w)
3b) call  solve(myParam,fgOut,bgOut,imH,imW);

4) write fgOut bgOut to .png files


*/

/* solver skeleton:
// designing and implementing the solver is the main objective of this project assignment!
void solve(Parametrization params, int fgOut[][],int bgOut[][],int h, int w)
1) use the input arguments (parametrization, h, w), to form a flow network (i.e. use penaltyF(), penaltyB(), penaltyP() as source of penalty values for each pixel or pixel pair
2) solve the max flow/min cut problem by implementing e.g. Edmonds-Karp algorithm we covered in class
3) recover the min cut from max flow solution, fill out the output arguments (fgOut and bgOut) based on it
3a) fgOut[y][x] = 255 (white) if pixel (x,y) is in foreground, fgOut[y][x]=0 otherwise
3b) bgOut[y][x] = 255 (white) if pixel (x,y) is in background, bgOut[y][x]=0 otherwise
3c) in the output, you should always have: fgOut[y][x]+bgOut[y][x]==255  (every pixel is classified as either foreground or background)
*/
