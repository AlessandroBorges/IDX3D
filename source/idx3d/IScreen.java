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

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageProducer;

public class IScreen
{
    // F I E L D S

    public int pixel[];
    public int width;
    public int height;

    int p[]; // pixel array before antialiasing
    int w, h; // dimensions before antialiasing

    private Image image;
    private IImageProducer producer;    
    private ColorModel cm;// = new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF);
    boolean antialias = false;
    /**
     * if true, it uses native color model to create a compatible image.<br> 
     */
    boolean useNativeColorModel = true;
   

    // BENCHMARK STUFF
    private long timestamp = 0;
    private long time = 0;
    public static final int sampleLength = 120;
    private int probes = sampleLength;
    
    float FPS = 0;

	// C O N S T R U C T O R S
    public IScreen(int w, int h) {
        width = w;
        height = h;
        this.w = width;
        this.h = height;
        pixel = new int[w * h];
        p = pixel;
        cm = useNativeColorModel ? getCompatibleColorModel() : new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF);        
        producer = new IImageProducer(width, height, cm, pixel);
        image = Toolkit.getDefaultToolkit().createImage(producer);
        
        
        System.out.println("ColorModel " + cm + " bitness: " + cm.getPixelSize());
        System.out.println("Image: " + image);
        
    }

    private BufferedImage toCompatibleImage(Image img) {
        BufferedImage image=null;
        if(img instanceof BufferedImage){
            image = (BufferedImage)img;
        }else{            
            image = new BufferedImage(img.getWidth(null), img.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
        }
        
        // obtain the current system graphical settings
        GraphicsConfiguration gfx_config = GraphicsEnvironment
                                           .getLocalGraphicsEnvironment()
                                           .getDefaultScreenDevice()
                                           .getDefaultConfiguration();

        /*
         * if image is already compatible and optimized for current system 
         * settings, simply return it
         */
        if (image.getColorModel().equals(gfx_config.getColorModel())) {
            return image;
        }

        // image is not optimized, so create a new image that is
        BufferedImage new_image = gfx_config.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency());

        // get the graphics context of the new image to draw the old image on
        Graphics2D g2d = (Graphics2D) new_image.getGraphics();

        // actually draw the image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // return the new optimized image
        return new_image;
    }
    
    /**
     * Get Best Color model available for current screen.
     * @return color model
     */
    protected ColorModel getCompatibleColorModel(){
        // obtain the current system graphical settings
        GraphicsConfiguration gfx_config = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();
        /*
         * if image is already compatible and optimized for current system 
         * settings, simply return it
         */
        return gfx_config.getColorModel();
    }

	// P U B L I C   M E T H O D S
    public void render() {
        if (antialias) {
            performAntialiasing();
        }
    }

    public Image getImage() {
        producer.update();
        performBench();
        return image;
    }

    public void setAntialias(boolean active) {
        if (antialias == active) {
            return;
        }
        antialias = active;
        if (antialias) {
            w = width * 2;
            h = height * 2;
            p = new int[w * h];

        } else {
            w = width;
            h = height;
            p = pixel;
        }
    }

    public ITexture asTexture() {
        return new ITexture(width, height, pixel);
    }

    public final void clear(int bgcolor) {
        IMath.clearBuffer(p, bgcolor);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        if (antialias) {
            w = width * 2;
            h = height * 2;
            pixel = new int[width * height];
            p = new int[w * h];

        } else {
            w = width;
            h = height;
            pixel = new int[width * height];
            p = pixel;
        }
        producer =  new IImageProducer(width, height, cm, pixel);
        image = Toolkit.getDefaultToolkit().createImage(producer);
    }

    public boolean antialias() {
        return antialias;
    }

	// P R I V A T E   M E T H O D S
    private void performBench() {        
        if (++probes > sampleLength) {
            time = System.currentTimeMillis();
            FPS = (float)sampleLength / ((float) (time - timestamp) / 1000f);
            timestamp = time;
            probes = 0;
        }
    }

    private void performAntialiasing() {
        int offset;
        int pos = 0;
        for (int y = 0; y < (h >> 1); y++) {
            offset = (y << 1) * w;
            for (int x = 0; x < (w >> 1); x++) {
                pixel[pos] = ((p[offset] & 0xFCFCFC) >> 2)
                        + ((p[offset + 1] & 0xFCFCFC) >> 2)
                        + ((p[offset + w] & 0xFCFCFC) >> 2)
                        + ((p[offset + w + 1] & 0xFCFCFC) >> 2);
                pos += 1;
                offset += 2;
            }
        }
    }

	// IMAGE OVERLAYING
    public void draw(ITexture texture, int posx, int posy, int xsize, int ysize) {
        draw(pixel, width, height, texture, posx, posy, xsize, ysize);
    }

    public void add(ITexture texture, int posx, int posy, int xsize, int ysize) {
        add(pixel, width, height, texture, posx, posy, xsize, ysize);
    }

    public void drawBackground(ITexture texture, int posx, int posy, int xsize, int ysize) {
        draw(p, w, h, texture, posx, posy, xsize, ysize);
    }

	// Private part of image overlaying
    private void draw(int[] buffer, int width, int height, ITexture texture, int posx, int posy, int xsize, int ysize) {
        if (texture == null) {
            return;
        }
        int w = xsize;
        int h = ysize;
        int xBase = posx;
        int yBase = posy;
        int tx = texture.width * 255;
        int ty = texture.height * 255;
        int tw = texture.width;
        int dtx = tx / w;
        int dty = ty / h;
        int txBase = IMath.crop(-xBase * dtx, 0, 255 * tx);
        int tyBase = IMath.crop(-yBase * dty, 0, 255 * ty);
        int xend = IMath.crop(xBase + w, 0, width);
        int yend = IMath.crop(yBase + h, 0, height);
        int pos, offset1, offset2;
        xBase = IMath.crop(xBase, 0, width);
        yBase = IMath.crop(yBase, 0, height);

        ty = tyBase;
        for (int j = yBase; j < yend; j++) {
            tx = txBase;
            offset1 = j * width;
            offset2 = (ty >> 8) * tw;
            for (int i = xBase; i < xend; i++) {
                buffer[i + offset1] = texture.pixel[(tx >> 8) + offset2];
                tx += dtx;
            }
            ty += dty;
        }
    }

    private void add(int[] buffer, int width, int height, ITexture texture, int posx, int posy, int xsize, int ysize) {
        int w = xsize;
        int h = ysize;
        int xBase = posx;
        int yBase = posy;
        int tx = texture.width * 255;
        int ty = texture.height * 255;
        int tw = texture.width;
        int dtx = tx / w;
        int dty = ty / h;
        int txBase = IMath.crop(-xBase * dtx, 0, 255 * tx);
        int tyBase = IMath.crop(-yBase * dty, 0, 255 * ty);
        int xend = IMath.crop(xBase + w, 0, width);
        int yend = IMath.crop(yBase + h, 0, height);
        int pos, offset1, offset2;
        xBase = IMath.crop(xBase, 0, width);
        yBase = IMath.crop(yBase, 0, height);

        ty = tyBase;
        for (int j = yBase; j < yend; j++) {
            tx = txBase;
            offset1 = j * width;
            offset2 = (ty >> 8) * tw;
            for (int i = xBase; i < xend; i++) {
                buffer[i + offset1] = IColor.add(texture.pixel[(tx >> 8) + offset2], pixel[i + offset1]);
                tx += dtx;
            }
            ty += dty;
        }
    }

    private static final int crop(int num, int min, int max) {
        return (num < min) ? min : (num > max) ? max : num;
    }

    private static final float crop(float num, float min, float max) {
        return (num < min) ? min : (num > max) ? max : num;
    }

    private static final boolean inrange(int num, int min, int max) {
        return ((num >= min) && (num < max));
    }

}
