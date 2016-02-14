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
// | * Wilfred L. Guerin, for testing, bug report, and tons
// | of brilliant suggestions
// | * Sandy McArthur, for reverse loops
// | * Dr. Douglas Lyons, for mentioning idx3d1 in his book
// | * Hugo Elias, for maintaining his great page
// | * the comp.graphics.algorithms people,
// | for scientific concerns
// | * Tobias Hill, for inspiration and awakening my
// | interest in java gfx coding
// | * Kai Krause, for inspiration and hope
// | * Incarom & Parisienne, for keeping me awake during the
// | long coding nights
// | * Doris Langhard, for being the sweetest girl on earth
// | * Etnica, Infinity Project, X-Dream and "Space Night"@BR3
// | for great sound while coding
// | and all coderz & scenerz out there (keep up the good work, ppl :)
// |
// | Peter Walser
// | proxima@active.ch
// | http://www2.active.ch/~proxima
// | "On the eigth day, God started debugging"
// | -----------------------------------------------------------------

package idx3d;

import idx3d.tests.LUT90;

/**
 * 
 * Singleton class for accelerated mathematical operations
 *
 */
public final class idx3d_Math {
   
    public static float    PI  = (float) Math.PI;
    private static int[]   fastRandoms;
    private static int     fastRndPointer = 0;
    private static boolean fastRndInit    = false;

   
    // A L L O W NO I N S T A N C E S

    private idx3d_Math() {
    }

    // T R I G O N O M E T R Y

    public static final float deg2rad(float deg) {
        return deg * 0.0174532925194f;
    }

    public static final float rad2deg(float rad) {
        return rad * 57.295779514719f;
    }

    /**
     * Return sin(angleRad). <br>
     * This versions uses java.lang.Math.sin(x). <br>
     * It is very fast for angles in range [0..PI/4], and has zero memory
     * impact. The old LUT required 32kbytes, no good for CPU L1/L2 cache.
     * 
     * @param angleRad - angle in Radians
     * @return sin of angle
     */
    public static final float sin(float angleRad) {
        return (float) Math.sin(angleRad);
    }

    /**
     * Return cosine of angleRad. <br>
     * This versions uses java.lang.Math.cos(x). <br>
     * It is very fast for angles in range [0..PI/4], and has zero memory
     * impact. The old LUT required 32kbytes, no good for CPU L1/L2 cache.
     * 
     * @param angleRad - angle in Radians
     * @return sin of angle
     */
    public static final float cos(float angle) {
        return (float) Math.cos(angle);
    }

    // private static void buildTrig() {
    // System.out.println(">> Building idx3d_Math LUT");
    // sinus = new float[sizeTrig];
    // cosinus = new float[sizeTrig];
    //
    // for (int i = 0; i < sizeTrig; i++) {
    // sinus[i] = (float) Math.sin((float) i / rad2scale);
    // cosinus[i] = (float) Math.cos((float) i / rad2scale);
    // }
    // }

    public static final float pythagoras(float a, float b) {
        return (float) Math.sqrt(a * a + b * b);
    }

    public static final int pythagoras(int a, int b) {
        return (int) Math.sqrt(a * a + b * b);
    }

    // R A N G E T O O L S

    public static final int crop(int num, int min, int max) {
        return (num < min) ? min : (num > max) ? max : num;
    }

    public static final float crop(float num, float min, float max) {
        return (num < min) ? min : (num > max) ? max : num;
    }

    public static final boolean inrange(int num, int min, int max) {
        return ((num >= min) && (num < max));
    }

    // B U F F E R O P E R A T I O N S

    /**
     * clear a buffer with value
     * @param buffer - full to fill up
     * @param value - value to set on buffer
     */
    public static final void clearBuffer(int[] buffer, int value) {
        int size = buffer.length - 1;
        int cleared = 1;
        int index = 1;
        buffer[0] = value;

        while (cleared < size) {
            System.arraycopy(buffer, 0, buffer, index, cleared);
            size -= cleared;
            index += cleared;
            cleared <<= 1;
        }
        System.arraycopy(buffer, 0, buffer, index, size);
    }

    /**
     * Experimental
     * @param buffer
     * @param value
     */
    public static final void clearBuffer2(int[] buffer, int value) {
        int size = buffer.length - 1;
        int init = Math.min(32, buffer.length);
        int cleared = init;
        int index = init;

        for (int i = 0; i < init; i++)  buffer[i] = value;
        
        size -= cleared;

        while (cleared < size) {
            System.arraycopy(buffer, 0, buffer, index, cleared);
            size -= cleared;
            index += cleared;
            cleared <<= 1;
        }
        System.arraycopy(buffer, 0, buffer, index, size);
    }

    /**
     * Naive clear buffer - experimental
     * 
     * @param buffer
     * @param value
     */
    public static final void clearBuffer3(int[] buffer, int value) {
        int size = buffer.length;
        for (int i = 0; i < size; i++) {
            buffer[i] = value;
        }
    }

    public static final void cropBuffer(int[] buffer, int min, int max) {
        for (int i = buffer.length - 1; i >= 0; i--)
            buffer[i] = crop(buffer[i], min, max);
    }

    public static final void copyBuffer(int[] source, int[] target) {
        System.arraycopy(source, 0, target, 0, crop(source.length, 0, target.length));
    }

    // R A N D O M N U M B E R S

    public static final float random() {
        return (float) (Math.random() * 2 - 1);
    }

    public static final float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static final float randomWithDelta(float averidge, float delta) {
        return averidge + random() * delta;
    }

    public static final int fastRnd(int bits) {
        if (bits < 1) return 0;
        fastRndPointer = (fastRndPointer + 1) & 31;
        if (!fastRndInit) {
            fastRandoms = new int[32];
            for (int i = 0; i < 32; i++)
                fastRandoms[i] = (int) random(0, 0xFFFFFF);
            fastRndInit = true;
        }
        return fastRandoms[fastRndPointer] & (1 << (bits - 1));
    }

    public static final int fastRndBit() {
        return fastRnd(1);
    }

    public static final float interpolate(float a, float b, float d) {
        float f = (1 - cos(d * PI)) * 0.5f;
        return a + f * (b - a);
    }

}
