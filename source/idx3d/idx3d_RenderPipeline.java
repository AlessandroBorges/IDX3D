// | -----------------------------------------------------------------
// | idx3d III is (c)1999/2000 by Peter Walser
// | -----------------------------------------------------------------
// | idx3d is a 3d engine written in 100% pure Java (1.1 compatible)
// | and provides a fast and flexible API for software 3d rendering
// | on the Java platform.
// |
// | Feel free to use the idx3d API / classes / source code for
// | non-commercial purposes (of course on your own risk).
// | If you intend to use idx3d for commercial purposes, please
// | contact me with an e-mail [proxima@active.ch].
// |
// | Thanx & greetinx go to:
// | * Wilfred L. Guerin, 	for testing, bug report, and tons 
// |			of brilliant suggestions
// | * Sandy McArthur,	for reverse loops
// | * Dr. Douglas Lyons,	for mentioning idx3d1 in his book
// | * Hugo Elias,		for maintaining his great page
// | * the comp.graphics.algorithms people, 
// | 			for scientific concerns
// | * Tobias Hill,		for inspiration and awakening my
// |			interest in java gfx coding
// | * Kai Krause,		for inspiration and hope
// | * Incarom & Parisienne,	for keeping me awake during the 
// |			long coding nights
// | * Doris Langhard,	for being the sweetest girl on earth
// | * Etnica, Infinity Project, X-Dream and "Space Night"@BR3
// | 			for great sound while coding
// | and all coderz & scenerz out there (keep up the good work, ppl :)
// |
// | Peter Walser
// | proxima@active.ch
// | http://www2.active.ch/~proxima
// | "On the eigth day, God started debugging"
// | -----------------------------------------------------------------

package idx3d;

import java.awt.Image;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;

public class idx3d_RenderPipeline
{
	// F I E L D S

		public idx3d_Screen screen;
		idx3d_Scene scene;
		public idx3d_Lightmap lightmap;

		private boolean resizingRequested=false;
		private boolean antialiasChangeRequested=false;
		private int requestedWidth;
		private int requestedHeight;
		private boolean requestedAntialias;
		boolean useIdBuffer=false;
   
   /** temp matrixes **/ 
   private idx3d_Matrix m = new idx3d_Matrix();
   private idx3d_Matrix nm = new idx3d_Matrix();
   private idx3d_Matrix vertexProjection = new idx3d_Matrix();
   private idx3d_Matrix normalProjection = new idx3d_Matrix();

   private idx3d_Rasterizer      rasterizer;
   private List<idx3d_Triangle>  opaqueQueue              = new ArrayList<idx3d_Triangle>();
   private List<idx3d_Triangle>  transparentQueue         = new ArrayList<idx3d_Triangle>();
      

    // Q U I C K R E F E R E N C E S

    final int             zFar                     = 0xFFFFFFF;

    // B U F F E R S

    public int            zBuffer[];
    public int            idBuffer[];


	// C O N S T R U C T O R S

    public idx3d_RenderPipeline(idx3d_Scene scene, int w, int h) {
        this.scene = scene;
        screen = new idx3d_Screen(w, h);
        zBuffer = new int[screen.w * screen.h];
        rasterizer = new idx3d_Rasterizer(this);
    }	

	// P U B L I C   M E T H O D S
	
    public void setAntialias(boolean antialias) {
        antialiasChangeRequested = true;
        requestedAntialias = antialias;
    }

    public float getFPS() {
        return (float) ((int) (screen.FPS * 100)) / 100;
    }

    public void resize(int w, int h) {
        resizingRequested = true;
        requestedWidth = w;
        requestedHeight = h;
    }

    public void buildLightMap() {
        if (lightmap == null) lightmap = new idx3d_Lightmap(scene);
        else lightmap.rebuildLightmap();
        rasterizer.loadLightmap(lightmap);
    }
 
		
  public final void render(idx3d_Camera cam){
      // Resize if requested
      if (resizingRequested) performResizing();
      if (antialiasChangeRequested) performAntialiasChange();
      rasterizer.rebuildReferences(this);
      
      // Clear buffers
      idx3d_Math.clearBuffer2(zBuffer,zFar);
      if (useIdBuffer) idx3d_Math.clearBuffer2(idBuffer,-1);
      if (scene.environment.background!=null)
          screen.drawBackground(scene.environment.background,0,0,screen.w,screen.h);
      else 
          screen.clear(scene.environment.bgcolor);
      
      // Prepare
      cam.setScreensize(screen.w,screen.h);
      scene.prepareForRendering();
      emptyQueues();
     		
	//idx3d_Matrix m =idx3d_Matrix.multiply(cam.getMatrix(),scene.matrix);
	//idx3d_Matrix nm=idx3d_Matrix.multiply(cam.getNormalMatrix(),scene.normalmatrix);
	//idx3d_Matrix vertexProjection = null;
        // idx3d_Matrix normalProjection = null;        
        idx3d_Matrix.multiply(cam.getMatrix(),scene.matrix, m);
        idx3d_Matrix.multiply(cam.getNormalMatrix(),scene.normalmatrix,nm);

        idx3d_Object obj;
        idx3d_Triangle t;
        idx3d_Vertex v;
        int w = screen.w;
        int h = screen.h;
        for (int id = scene.objects - 1; id >= 0; id--) {
            obj = scene.object[id];
            if (obj.visible) {
	       //vertexProjection=obj.matrix.getClone();
               vertexProjection=obj.matrix.copyTo(vertexProjection);            
	      //normalProjection=obj.normalmatrix.getClone();
               normalProjection=obj.normalmatrix.copyTo(normalProjection);
            
	       vertexProjection.transform(m);
	       normalProjection.transform(nm);

                for (int i = obj.vertices - 1; i >= 0; i--) {
                    v = obj.vertex[i];
                    v.project(vertexProjection, normalProjection, cam);
                    v.clipFrustrum(w, h);
                }
                for (int i = obj.triangles - 1; i >= 0; i--) {
                    t = obj.triangle[i];
                    t.project(normalProjection);
                    t.clipFrustrum(w, h);
                    enqueueTriangle(t);
                }
               }
            }
				
	idx3d_Triangle[] tri;
	tri=getOpaqueQueue();
	if (tri!=null)
	    for (int i=topOpaque - 1;i>=0;i--){
	        rasterizer.loadMaterial(tri[i].parent.material);
	        rasterizer.render(tri[i]);
	        }
	tri=getTransparentQueue();
	if (tri!=null)
	    for (int i=0;i<topTrans-1;i++){
	        rasterizer.loadMaterial(tri[i].parent.material);
	        rasterizer.render(tri[i]);
	        }
	screen.render();
	}
		
	public void useIdBuffer(boolean useIdBuffer){
	    this.useIdBuffer=useIdBuffer;
	    if (useIdBuffer) idBuffer=new int[screen.w*screen.h];
	    else idBuffer=null;
	 }
		

	// P R I V A T E   M E T H O D S

		private void performResizing()
		{
			resizingRequested=false;
			screen.resize(requestedWidth,requestedHeight);
			zBuffer=new int[screen.w*screen.h];
			if (useIdBuffer) idBuffer=new int[screen.w*screen.h];
		}

		private void performAntialiasChange()
		{
			antialiasChangeRequested=false;
			screen.setAntialias(requestedAntialias);
			zBuffer=new int[screen.w*screen.h];
			if (useIdBuffer) idBuffer=new int[screen.w*screen.h];
		}
		
     // Triangle sorting
    private void emptyQueues() {
        opaqueQueue.clear();
        transparentQueue.clear();
    }

    private void enqueueTriangle(idx3d_Triangle tri) {
        if (tri.parent.material == null) return;
        if (tri.visible == false) return;
        if ((tri.parent.material.transparency == 255) && (tri.parent.material.reflectivity == 0)) return;

        if (tri.parent.material.transparency > 0) 
            transparentQueue.add(tri);
        else 
            opaqueQueue.add(tri);
    }

    //
    
    private idx3d_Triangle[] triTrans;
    private int topTrans = 0;
    private int topOpaque = 0;
    private idx3d_Triangle[] triOpaque;
    
    /**
   * 
   * @return 
   */
private idx3d_Triangle[] getOpaqueQueue(){
    if (opaqueQueue.isEmpty()) return null;    
        int sz = opaqueQueue.size();
        if (triOpaque == null || triOpaque.length < sz || topOpaque > 2 * sz) {
            triOpaque = new idx3d_Triangle[(int)(sz * 1.44f)];
            topOpaque = sz;
            // System.err.println("opaque size " +sz);
        } else topOpaque = sz;
        int id = 0;
        for (int i = 0; i < sz; i++){     
            triOpaque[id++] = (idx3d_Triangle) opaqueQueue.get(i);
        }   
       sortTriangles(triOpaque, 0, topOpaque - 1);       
        return triOpaque;
    }
		
    
    /**
   * 
   * @return 
   */
    private idx3d_Triangle[] getTransparentQueue() {
        if (transparentQueue.size() == 0) return null;        
        int sz = transparentQueue.size();
        if (triTrans == null || triTrans.length < sz || topTrans > 2 * sz) {
            triTrans = new idx3d_Triangle[(int)(sz * 1.44f) ];
            topTrans = sz;
        } else topTrans = sz;
        int id = 0;
        for (int i = 0; i < topTrans; i++) {
            triTrans[id++] = (idx3d_Triangle) transparentQueue.get(i);
        }
        //System.out.println("*******************");
        return sort(triTrans, 0, topTrans - 1);
    }
   

    private int depth = 0;
    private static final int KMAX = 20;
    
    /**
     * This sort method combines QuickSort and InsertionSort.<br>
     * It will use InsertionSort to sort internal portions up to KMAX length.
     *  
     * @param tri - list to sort
     * @param start - start index
     * @param end - end index
     * @return the same tri as a sorted list
     */
    private idx3d_Triangle[] sort(idx3d_Triangle[] tri, int start, int end){
        sortTriangles(tri, start, end);
        InsertSort.insertionSort(tri, start, end);
        return tri;
    }
    
    
    
    private idx3d_Triangle[] sortTriangles(idx3d_Triangle[] tri, int start, int end) {
       // System.out.print("QuickSort - ["+ (++depth) + "] \t" + start +" - " + end);
        if((end - start) < KMAX){
          //  depth--;
           // System.out.println(" ... quick!");
            return tri;
        } 
        
        float m = (tri[start].dist + tri[end].dist) / 2f;
        int i = start;
        int j = end;
        idx3d_Triangle temp;

        do {
            while (tri[i].dist > m)
                i++;
            while (tri[j].dist < m)
                j--;

            if (i <= j) {
                temp = tri[i];
                tri[i] = tri[j];
                tri[j] = temp;
                i++;
                j--;
              //  System.out.print("*");
            }
        } while (j >= i);
       // System.out.println("...");
        if (start < j)  sortTriangles(tri, start, j);
        if (end > i) sortTriangles(tri, i, end);
        //depth--;
        return tri;
    }
    
    ////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////
    private idx3d_Triangle[] tmpArr = null;
    
    private  void mergeSort(idx3d_Triangle[] a, int l, int r) {
        int len = r-l;
        if(tmpArr == null || tmpArr.length < len)
         tmpArr = new idx3d_Triangle[2*a.length];        
        //mergeSort(a, tmp, 0, a.length - 1);
        mergeSort(a, tmpArr, l, r);
    }

    private static void mergeSort(idx3d_Triangle[] a, idx3d_Triangle[] tmp, int left, int right) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSort(a, tmp, left, center);
            mergeSort(a, tmp, center + 1, right);
            merge(a, tmp, left, center + 1, right);
        }
    }

    private static void merge(idx3d_Triangle[] a, idx3d_Triangle[] tmp, int left, int right, int rightEnd) {
        int leftEnd = right - 1;
        int k = left;
        int num = rightEnd - left + 1;

        while (left <= leftEnd && right <= rightEnd) {
            if (a[left].dist <= a[right].dist) {
                tmp[k++] = a[left++];
            } else {
                tmp[k++] = a[right++];
            }
        }

        while (left <= leftEnd) // Copy rest of first half
        {
            tmp[k++] = a[left++];
        }

        while (right <= rightEnd) // Copy rest of right half
        {
            tmp[k++] = a[right++];
        }

        // Copy tmp back
        for (int i = 0; i < num; i++, rightEnd--) {
            a[rightEnd] = tmp[rightEnd];
        }
    }

		
}
