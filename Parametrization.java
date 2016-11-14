interface Parametrization{
	public void initialize(int img[][], int height, int width, int bg[][], int fg[][]);
	public double penaltyP(int x1, int y1, int x2, int y2);
	public double penaltyF(int x, int y);
	public double penaltyB(int x, int y);

}

// class SomeParametrization implements Parametrization{
class ImageSeg implements Parametrization{
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
		for (c=0;c<256;c++){
			fgHist[c]=1;bgHist[c]=1; //initialize psedocounts
		}
		for (x=0;x<w;x++) for (y=0;y<h;y++){
			if (fg[y][x]>0) fgHist[img[y][x]]++;
			if (bg[y][x]>0) bgHist[img[y][x]]++;
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


	public static void main(String[] args){
		System.out.println("This is the program to process image");
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
