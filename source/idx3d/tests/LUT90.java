package idx3d.tests;

/**
 * Look Up Table for trigonometric functions.<br>
 *  Instead of using a circular 360 deg table, 
 *  it uses a circular 90 degrees table, reduncing memory impact.
 *  
 * @author Alessandro
 *
 */
public class LUT90 {

    /**
     * Size of sin(x) table, with values for [0..90] degrees.<br>
     * Size MUST be power of 2: 128, 256, 512, 1024, etc.<br> 
     * Higher values improve precision, but this implementation 
     * acts as this size is 4 times bigger.
     */
    protected static final int SIZE = 256;
    /** Mimics a circular **/ 
    private static final int SIZE_x4 = SIZE*4;
    /**
     * Max angle is 90 or PI/4
     */
    private static final float MAX_ANGLE = (float) (Math.PI / 2.0);
    
    /**
     * Angles lower than MIN_ANGLE will return sin(x) == x
     * It results on smoother sin(x) transitions in range between [-MIN_ANGLE .. +MIN_ANGLE]  
     * Default value is 
     */
    private static final float MIN_ANGLE = (float) (Math.toRadians(12f));
    /** steps to angles */
    private static final float STEP = (float) ((MAX_ANGLE) / SIZE);
    /** Inverse of STEP*/
    private static final float STEP_INV = 1.0f / STEP;
    /**
     * the circular sin table, for angles [0..90]. <br>
     * Angles in range [90..360] are calculated using this table, 
     */
    private static final float[] LUT_SIN = new float[SIZE + 2]; // extra room
                                                                // for roundings
    /** PI value as float **/    
    public  static final float PI = (float) Math.PI;
    /** PI/2 value, as float **/
    private static final float PI_2 = (float) Math.PI / 2.0f;

    /**
     * Build look up table
     */
    static {
        buildLUT();
    }

    /**
     * Build the Look Up Table for sin(x) LUT
     */
    private static final void buildLUT() {
        for (int i = 0; i < LUT_SIN.length; i++) {
            float angle = i * STEP;
            LUT_SIN[i] = (float) Math.sin(angle);
        }
    }

     
    /**
     * Sine of angle.
     * @param radAngle - angle in Radians
     * @return sine of radAngle
     */
    public static final float sin(final float radAngle) {
        float ang = radAngle < 0 ? -radAngle : radAngle;
        // low angles has sin(x) = x
        if(ang < MIN_ANGLE){
            return radAngle;
        } 
        
        // mod (4xSize) to expand range from 90 to 360          
        //     a % b == a - (b * a/b) 
        //     a % b ==  x & (2^n - 1) 
        // ex: a % 256 == x & 255
        //int pos = (int) (0.5f + ang * STEP_INV) % (SIZE * 4);        
        int pos = (int)(0.5f + ang * STEP_INV) & (SIZE_x4 - 1);
        int index = pos;
        if (index > SIZE*2) index -= SIZE*2; // reduce angles>180
        if (index > SIZE)   index  = SIZE*2 - index; // reduce angles>90
        
        float v = LUT_SIN[index]; // on reduced to low angles
        
        v = (pos > SIZE*2) ? -v : v; // sin(x) is negative in range [180..360];        
        return (radAngle < 0) ? -v : v; // sin(-x) == -sin(x)
    }
    
   
    /**
     * Cosine of angle in radians
     * @param radAngle - angle in radians
     * @return cosine of rad
     */
    public static final float cos(final float radAngle) {
        return sin(radAngle + PI_2); // cos(x) = sin(x + PI)
    }
    
    /**
     * Tangent of radAngle.
     * 
     * @param radAngle - angle in Radians
     */
    public static final float tan(float radAngle){
        // return sin(rad) / cos(PI_2);
        // use same function to improve cache L1 access
        return sin(radAngle) / sin(radAngle + PI_2);
    }
}
