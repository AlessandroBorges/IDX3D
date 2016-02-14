package idx3d.tests;

import java.util.Arrays;

import idx3d.idx3d_Math;

public class LUT90 {

    /**
     * Number of steps
     */
    protected static final int SIZE = 128;
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
    private static final float STEP_INV = 1.0f / STEP;
    /**
     * the sin table to look up
     */
    private static final float[] LUT_SIN = new float[SIZE + 2]; // extra room
                                                                // for roundings
    
    private static final int INDEX_MIN = (int) (0.5f + MIN_ANGLE * STEP_INV) % SIZE;
    public  static final float PI = (float) Math.PI;
    private static final float PI_2 = (float) Math.PI / 2.0f;

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

    
//    private static int index(float rad){
//        float ang = rad < 0 ? -rad : rad;              
//        // mod 4 to expand range from 90 to 360
//        int pos = (int) (0.5f + ang * STEP_INV) % SIZE_x4;
//        int index = pos;
//        if (index > SIZE * 2) index -= SIZE*2; // reduce angles>180
//        if (index > SIZE)     index  = SIZE*2 - index; // reduce angles>90
//        return index;
//    }
//    
    
    /**
     * Sine of angle.
     * @param radAngle - angle in Radians
     * @return sine of radAngle
     */
    public static final float sin(final float radAngle) {
        float ang = radAngle < 0 ? -radAngle : radAngle;
        // low angles 
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
    
    
    
//    public static final float cos2(float rad) {
//        rad = rad < 0 ? -rad : rad;
//        int pos = (int)(0.5f + rad * STEP_INV) & (4*SIZE-1); //redux to 360        
//        int index = pos;
//        if (index > SIZE*2) index -= SIZE*2; // reduce angles>180
//        if (index > SIZE)   index  = SIZE*2 - index; // reduce angles>90
//                
//        float v = LUT_SIN[SIZE - index];        
//        v = (pos < SIZE || pos > 3*SIZE  ) ? v : -v; // cos(x) is negative in range [90..270];        
//        return v; 
//    }

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
