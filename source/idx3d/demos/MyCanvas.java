/**
 * 
 */
package idx3d.demos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Livia
 *
 */
public class MyCanvas extends JPanel implements Runnable {
  
    public int pixel[];
    public int width;
    public int height;
    private Image imageBuffer;   
    private MemoryImageSource mImageProducer;   
    private ColorModel cm;    
    private Thread thread;
    
    /**
     * 
     */
    public MyCanvas() {
        super(true);
        thread = new Thread(this, "MyCanvas Thread");
    }

    /**
     * Call it after been visible and after resizes.
     */
    public void init(){        
        cm = getCompatibleColorModel();
        width = getWidth();
        height = getHeight();
        int screenSize = width * height;
        if(pixel == null || pixel.length < screenSize){
            pixel = new int[screenSize];
        }
        
        mImageProducer =  new MemoryImageSource(width, height, cm, pixel,0, width);
        mImageProducer.setAnimated(true);
        mImageProducer.setFullBufferUpdates(true);
       
        imageBuffer = Toolkit.getDefaultToolkit().createImage(mImageProducer);
        
        if(thread.isInterrupted() || !thread.isAlive()){
            thread.start();
        }
    }
    
   /**
    * Do your draws in here !!
    */
    public /*abstract*/ void render(){
        // rubisch draw
        int[] p = pixel; // this avoid crash when resizing
        if(p.length != width * height) return;
        
        for(int x=0; x < width; x++){
            for(int y=0; y<height; y++){
                int color =  (((x + i) % 255) & 0xFF) << 16; //red
                    color |= (((y + j) % 255) & 0xFF) <<  8; //green
                    color |= (((y/2 + x/2 - j) % 255) & 0xFF) ;   //blue         
                p[ x + y * width] = color;
            }
        }        
        i += 1;
        j += 1;   
        
    }
    
    private int i=1,j=256;

    @Override
    public void run() {
        while (true) {
            // request a drawing
            repaint();            
            if(count > 128){
               printFPS();
            }            
            try {Thread.sleep(5);} catch (InterruptedException e) {}
        }
    }
    
    private int count = 0;
    private long time = System.currentTimeMillis();
    private void printFPS(){        
            long t2 = System.currentTimeMillis(); 
            t2 = t2 - time;
            float fps = (float)(count * 1000) / t2;
            System.out.println("FPS: " + fps);
            time = System.currentTimeMillis();        
        count = 0;
    }

     
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
       // perform draws on pixels
        render();
        // ask ImageProducer to update image
        mImageProducer.newPixels();            
        // draw it on panel          
        g.drawImage(this.imageBuffer, 0, 0, this);  
        count++;
    }
    
    
    
    /**
     * Overrides ImageObserver.imageUpdate.
     * Always return true, assuming that imageBuffer is ready to go when called
     */
    @Override
    public boolean imageUpdate(Image image, int a, int b, int c, int d, int e) {
        return true;
    }
    
    /**
     * Get Best Color model available for current screen.
     * @return color model
     */
    protected static ColorModel getCompatibleColorModel(){
        // obtain the current system graphical settings
        GraphicsConfiguration gfx_config = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();        
        return gfx_config.getColorModel();
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        //frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        
        MyCanvas canvas = new MyCanvas();
        canvas.setPreferredSize(new Dimension(800,600));
        frame.getContentPane().add(canvas, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        canvas.init();
        
        
    }

}
