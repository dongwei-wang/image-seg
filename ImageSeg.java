/*
 main() skeleton:

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

 solver skeleton:
// designing and implementing the solver is the main objective of this project assignment!
void solve(Parametrization params, int fgOut[][],int bgOut[][],int h, int w)
1) use the input arguments (parametrization, h, w), to form a flow network (i.e. use penaltyF(), penaltyB(), penaltyP() as source of penalty values for each pixel or pixel pair
2) solve the max flow/min cut problem by implementing e.g. Edmonds-Karp algorithm we covered in class
3) recover the min cut from max flow solution, fill out the output arguments (fgOut and bgOut) based on it
3a) fgOut[y][x] = 255 (white) if pixel (x,y) is in foreground, fgOut[y][x]=0 otherwise
3b) bgOut[y][x] = 255 (white) if pixel (x,y) is in background, bgOut[y][x]=0 otherwise
3c) in the output, you should always have: fgOut[y][x]+bgOut[y][x]==255  (every pixel is classified as either foreground or background)

*/

/*
 * Autho: Dongwei Wang
 * Email: wdw828@gmail.com
 * Date: Nov 16th, 2016
 */

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.LinkedList;
import java.util.Queue;

class Node{
	public Node(double p, int idx){
		penalty = p;
		nodeidx = idx;
	}

	double penalty;
	int nodeidx;
};

// class SomeParametrization implements Parametrization{
//public class ImageSeg implements Parametrization{
public class ImageSeg{
	private static final int INFINITY = 9999;
	private static final double D_INF = 9999.00;
	private static final int MAX_VAL_BYTE = 255;

	int img[][]=null;
	int h = 0;
	int w = 0;

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

	public void initialize(int img[][], int height, int width, int fg[][], int bg[][]){
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
		double cDiff=Math.sqrt((c1-c2)*(c1-c2))/MAX_VAL_BYTE;
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

	public LinkedList<Node>[] createAdjLink(){
		int nodecnt = h * w;
		// add start and sink node +2
		LinkedList<Node>[] link_array = new LinkedList[nodecnt+2];
		for( int i = 0; i<nodecnt+2; i++ )
			link_array[i] = new LinkedList<Node>();

		// initialize the start and sink link list
		// check the foreground and background to initizlize the value of penalty

		// for the penalty of the network
		double penalty = 0.0;
		for( int i=0; i<h; i++ ){
			for( int j=0; j<w; j++ ){
				for( int m=i-3; m<i+3; m++ ){
					for (int n=j-3; n<j+3;n++){
						if(n>=0&&m>=0&&n<w&&m<h&&(m!=i||n!=j)){
							penalty = penaltyP(j,i,n,m);
							if( penalty >1e-6 ){
								Node node = new Node(penalty, m*w+n+1);
								link_array[i*w + j+1].add(node);
							}
						}
					}
				}

				// for foreground
				if(fg[i][j] == MAX_VAL_BYTE){
					Node node = new Node(INFINITY,i*w+j+1);
					link_array[0].add(node);
				}

				// if current node belongs to background, it connect to the sink
				if( bg[i][j] == MAX_VAL_BYTE ){
					// current should connect to the sink
					Node node = new Node(INFINITY,nodecnt+1);
					link_array[i*w+j+1].add(node);
				}
			}
		}
		return link_array;
	}

	public double [][] createMatrixPenaltyF(){
		double [][] matrix = new double[h][w];
		for( int i= 0; i<h; i++ ){
			for( int j=0; j<w; j++ ){
				matrix[i][j] = penaltyF(j,i);
			}
		}
		return matrix;
	}

	public double [][] createMatrixPenaltyB(){
		double [][] matrix = new double[h][w];
		for( int i=0; i<h; i++ ){
			for( int j=0; j<w; j++ ){
				matrix[i][j] = penaltyB(j,i);
			}
		}
		return matrix;
	}

	// find the source and sink for bfs searching
	public int[] findSrcAndSnkOfBFS(LinkedList<Node>[] residualnet){
		int length = residualnet.length;
		int [] array = new int[2];
		double[] inflow = new double[length];

		for( int i = 0; i<residualnet.length; i++ ){
			// for a adjecent list
			// the size is 0: there is no out flow
			// it should be the sink
			//System.out.format("%d", i);
			// if(residualnet[i].size() == 0)
			//     array[1] = i;
			for( int j=0; j<residualnet[i].size();j++ ){
				Node node = residualnet[i].get(j);
				inflow[node.nodeidx] += node.penalty;
			}
		}

		// for the node that there is no any inflow
		// it should be the start point for bfs
		for( int i=0; i<length; i++ ){
			if((inflow[i] == 0)&&(residualnet[i].size()!=0))
				array[0] = i;

			if( (inflow[i] != 0 )&& (residualnet[i].size() == 0 ))
				array[1] = i;
		}
		return array;
	}

	public LinkedList<Integer> BFS(LinkedList<Node>[] residualnet, int src, int sink, double min_capacity){
		// number of nodes in the flow network
		int nodecnt = residualnet.length;

		// variables to store the information of shortest path
		double [] weight	= new double[nodecnt]; // store the weight of the weight
		int [] predecessor	= new int[nodecnt]; // store the predecessor of current node
		boolean [] visited	= new boolean[nodecnt]; // flag: if current node visited or not

		// initialize the variables
		for( int i= 0; i<nodecnt; i++ ){
			weight[i] = D_INF;
			predecessor[i] = -1;
			visited[i] = false;
		}

		Queue<Integer> queue = new LinkedList<Integer>();
		queue.offer(src);

		while(queue.peek() != null){
			int source = queue.remove();
			LinkedList<Node> neighbours = residualnet[source];
			if(neighbours.size() > 0){
				for(int j = 0; j<residualnet[source].size(); j++){
					Node node = residualnet[source].get(j);
					int nodeidx = node.nodeidx;
					if( !visited[nodeidx] ){
						visited[nodeidx] = true;
						weight[nodeidx] = node.penalty;
						predecessor[nodeidx] = source;
						queue.offer(nodeidx);
					}
				}
			}
			// set the source as visited
			visited[source] = true;
		}

		LinkedList<Integer> reverse_sp = new LinkedList<Integer>();
		reverse_sp.add(sink);
		int pred = sink;
		while( predecessor[pred] != -1){
			reverse_sp.add(predecessor[pred]);
			pred = predecessor[pred];
		}

		LinkedList<Integer> sp = new LinkedList<Integer>();
		for(int i = reverse_sp.size()-1; i>=0; i--)
			sp.add(reverse_sp.get(i));

		return sp;
	}

	public double Find_Max_Flow(LinkedList<Node>[] residualnet, LinkedList<Integer> sp){
		double max_flow = D_INF;
		int v1 = 0;
		int v2 = 0;
		for( int i= 0; i<sp.size()-1; i++ ){
			v1 = sp.get(i);
			v2 = sp.get(i+1);
			for( int j = 0; j<residualnet[v1].size();j++ ){
				if(residualnet[v1].get(j).nodeidx == v2 && residualnet[v1].get(j).penalty<max_flow){
					max_flow = residualnet[v1].get(j).penalty;
				}
			}
		}
		return max_flow;
	}

	// adjust the value of each edges in residual network
	public void adjust_residual_network(LinkedList<Node>[] residualnet, LinkedList<Integer> sp, double min_capacity){
		int v1 = -1;
		int v2 = -1;
		for( int i=0; i<sp.size()-1; i++ ){
			v1 = sp.get(i);
			v2 = sp.get(i+1);
			for( int j = 0; j<residualnet[v1].size(); j++ ){
				if( residualnet[v1].get(j).nodeidx == v2 ){
					System.out.format("%4d--->%4d: %10.4f", v1, v2, residualnet[v1].get(j).penalty);
					System.out.format("%n");
					residualnet[v1].get(j).penalty -= min_capacity;

					// if the capacity of current node become 0
					// remove it
					if(residualnet[v1].get(j).penalty == 0)
						residualnet[v1].remove(j);
				}
			}

			for( int k = 0; k<residualnet[v2].size(); k++ ){
				if(residualnet[v2].get(k).nodeidx == v1 ){
					residualnet[v2].get(k).penalty += min_capacity;
				}
			}
		}
	}

	// set the pixesl of foreground and background
	// write the value to foreground and background files
	public void Set_Pixels(LinkedList<Node>[] residualnet, int[][] fgout, int [][] bgout){
		int nodecnt = residualnet.length;

		// flag: if current node visited or not
		boolean [] visited	= new boolean[nodecnt];

		// initialize the variables
		for( int i= 0; i<nodecnt; i++ ){
			visited[i] = false;
		}

		LinkedList<Integer> foreGrdPxl = new LinkedList<Integer>();
		Queue<Integer> queue = new LinkedList<Integer>();
		foreGrdPxl.add(0);
		queue.offer(0);

		while(queue.peek() != null){
			int source = queue.remove();
			LinkedList<Node> neighbours = residualnet[source];
			if(neighbours.size() > 0){
				for(int j = 0; j<residualnet[source].size(); j++){
					Node node = residualnet[source].get(j);
					int nodeidx = node.nodeidx;
					if( !visited[nodeidx] ){
						visited[nodeidx] = true;
						queue.offer(nodeidx);
						foreGrdPxl.add(nodeidx);
					}
				}
			}
			// set the source as visited
			visited[source] = true;
		}

		int m=0;
		int n=0;
		for( int i=0; i<foreGrdPxl.size(); i++ ){
			if(foreGrdPxl.get(i)>0){
				m = ((foreGrdPxl.get(i)-1)/w)%w;
				n = (foreGrdPxl.get(i)-1)%w;
				fgout[m][n] = 1;
				bgout[m][n] = 0;
			}
		}
	}

	// output the shortest path information
	public void displaySP(LinkedList<Integer> sp){
		System.out.format("Shortest path: ");
		for( int i=0; i<sp.size(); i++){
			System.out.format("%4d--->", sp.get(i));
		}
		System.out.format("%n");
	}

	// Edmonds Karp algorithm
	public void Edmonds_Karp_Solve(int[][] fgout, int[][] bgout, int imH, int imW, LinkedList<Node>[] residualnet ){
		// source and sink for each bfs searching
		int [] src_sink = new int[2];
		LinkedList<Integer> shortestPath = new LinkedList<Integer>();

		// node count
		int nodecnt = residualnet.length;

		// minimum capacity in each shortest path
		double sp_capacity = D_INF;

		// find the source and sink
		double maxflow = 0.0;
		while(true){
			src_sink[0] = -1;
			src_sink[1] = -1;
			src_sink = findSrcAndSnkOfBFS(residualnet);

			// start the bfs to find a path
			shortestPath.clear();
			shortestPath = BFS(residualnet, src_sink[0], src_sink[1], sp_capacity);
			displaySP(shortestPath);

			// do not find any shortest path by bfs
			if( shortestPath.size() <= 1){
				System.out.println("No any augmenting path");
				break;
			}

			// find the minimum penalty in each shortest path
			sp_capacity = Find_Max_Flow(residualnet, shortestPath);
			// acculumate maxflow
			maxflow += sp_capacity;
			System.out.format("Min capacity is %10.4f\n",sp_capacity);
			// adjust residual network
			adjust_residual_network(residualnet, shortestPath, sp_capacity);
		}

		System.out.format("The Maximum Flow is %10.4f", maxflow);
		System.out.format("%n");
		Set_Pixels(residualnet, fgout, bgout);
	}

	public int[][] Read_Image_Pixels(String filename) throws Exception{
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

	public void Write_Image_Pixels(String filename, int [][] pixels) throws Exception{
		BufferedImage img_buf = new BufferedImage( pixels[0].length, pixels.length, BufferedImage.TYPE_BYTE_GRAY);
		for( int i = 0; i<pixels[0].length; i++){
			for(int j = 0; j<pixels.length; j++){
				if(pixels[j][i] == 1)
					img_buf.setRGB(i,j, 0xffffff);
			}
		}
		ImageIO.write(img_buf, "png", new File(filename));
	}

	// display the value of each pixel in the image
	public void displayImagePixels(int [][] pixels, String filename) {
		System.out.println(filename);

		// get the height of the two dimension array
		int height = pixels.length;
		// get the width of the two dimension array int width = pixels[0].length;
		int width = pixels[0].length;

		System.out.format("The height is: %d, width is %d\n",height, width );

		for( int i=0; i<height; i++ ){
			for( int j=0; j<width; j++ )
				System.out.format("%4d", pixels[i][j]);
			System.out.format("%n");
		}
	}

	public void displayArray(){
		System.out.println("fgHist value");
		for( int i=0; i<256; i++ ){
			System.out.format("%4d", fgHist[i]);
			if( (i+1)%32 == 0 )
				System.out.format("%n");
		}

		System.out.println("bgHist value");
		for( int i=0; i<256; i++ ){
			System.out.format("%4d", bgHist[i]);
			if( (i+1)%32 == 0 )
				System.out.format("%n");
		}
	}

	// display the penalty matrix of F and P
	public void displayMatrix(double [][] matrix){
		for( int i = 0; i<matrix.length; i++ ){
			for ( int j = 0; j<matrix[0].length; j++ ){
				System.out.format("%4.2f", matrix[i][j]);
			}
			System.out.format("%n");
		}
	}

	// display the linked list
	public void displayLinkedList(LinkedList<Node>[] linklist){
		for( int i = 0; i<linklist.length; i++ ){
			for( int j=0; j<linklist[i].size();j++ ){
				Node linklist_node = linklist[i].get(j);
				System.out.format("%10.4f-->%4d", linklist_node.penalty, linklist_node.nodeidx);
			}
			System.out.format("%n");
		}
	}

	public static void main(String[] args) throws IOException, Exception{
		System.out.println("******************");
		System.out.println("This is the program to process image " + args[0]);

		ImageSeg image_seg = new ImageSeg();
		int [][] img_pixel, fgin_pixel, bgin_pixel;
		img_pixel	= image_seg.Read_Image_Pixels(args[0]);
		fgin_pixel	= image_seg.Read_Image_Pixels(args[1]);
		bgin_pixel	= image_seg.Read_Image_Pixels(args[2]);

		int height = img_pixel.length;
		int width = img_pixel[0].length;

		int [][] fgout_pixel = new int[height][width];
		int [][] bgout_pixel = new int[height][width];

		// set all the values in bgout_pixel to 1
		// so that to write to file
		for( int i= 0; i<height; i++ ){
			for( int j= 0; j<width; j++ ){
				bgout_pixel[i][j] = 1;
			}
		}

		// initialize
		image_seg.initialize(img_pixel, img_pixel.length, img_pixel[0].length, fgin_pixel, bgin_pixel);

		// create adjenct list for penalty (Pij)
		LinkedList<Node>[] adj_link = image_seg.createAdjLink();

		// create penalty of F and B
		double [][] matrixF = image_seg.createMatrixPenaltyF();
		double [][] matrixB = image_seg.createMatrixPenaltyB();

		// this is the edmonds karp solver
		image_seg.Edmonds_Karp_Solve(fgout_pixel, bgout_pixel, img_pixel.length, img_pixel[0].length, adj_link );

		// write the picture into file
		image_seg.Write_Image_Pixels(args[3], fgout_pixel);
		image_seg.Write_Image_Pixels(args[4], bgout_pixel);
		System.out.println("******************");
	}
}
