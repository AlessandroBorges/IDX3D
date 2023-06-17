/**
 * 
 */
package idx3d.tests;

import java.util.Arrays;

/**
 * Testing Look-Up Table (LUT), for trigonometric functions sin()/cos().
 * This versions uses cosine table.
 * @author Alessandro Borges
 *
 */
public class Math2 {
    
   /**
     * Look up table size.
     * MUST be power of 2.
     * If you plan to use only the interpolated versions, sin2() and cos2(),
     * You can reduce this to 64 ans still have nice precision when compared to 
     * java.lang.Math.sin() and java.lang.Math.cos();
     * 
     * But for simple version of sin()/cos(), consider 
     * increase SIZE to 512 or 1024.
     */
    private static final int SIZE = 256;
    
    /**
     * Angle step in table
     */
    private static final float STEP = (float)((2.0d*Math.PI)/SIZE);
    private static final float STEP_INV = 1.0f / STEP;
    
    /**
     * THE look up table !
     * It has extra room for positions used by interpolation, as [index+1],
     * as well bad index roundings
     */
    private static final float[] LUT_COS = new float[SIZE + 2]; // extra room for roundings
    
    public static final float PI = (float) Math.PI;
    public static final float PI_2 = (float) Math.PI / 2.0f;
    private static final int BIT_SIGN = ~(1<<32);
    
    /**
     * Angles lower than MIN_ANGLE will return sin(x) == x
     * It results on smoother sin(x) transitions in range between [-MIN_ANGLE .. +MIN_ANGLE]  
     * Default value is 
     */
    private static final float MIN_ANGLE = (float) (Math.toRadians(10f));
    
    /**
     * Build look up table
     */
    static{
        buildLUT();
    }
    
    /**
     * Build Look Up Table for sin(x)
     */
    private static final void buildLUT(){
        for(int i=0; i<LUT_COS.length; i++){
            float angle = i*STEP;
            LUT_COS[i] = (float)Math.cos(angle);
        }
    }
    
    /**
    * sin() using Look up table.
    * @param rad - angle in radians
    * @return sin of rad
    */
    public static final float sin(final float rad){          
        return LUT_COS[ ((int)(0.5F + (rad - PI/2)*STEP_INV) & (BIT_SIGN)) & (SIZE-1)];           
    }
    
    /**
     * cos() using Look up table    
     * @param rad - angle in radians
     * @return cosine of angle rad
     */
    public static final float cos(final float rad){ 
        return LUT_COS[((int)(0.5f + rad*STEP_INV) & (BIT_SIGN)) & (SIZE-1)];        
    }
    
    /**
     * Get  sin(x) and cos(x) in a single call.<br>
     * 
     * @param radAngle - angle in radians
     * @param sincos - float array 
     * @return float array with sin(x) and cos(x) at index [0] and [1], respect. 
     */
    public static float[] fsincos(float radAngle, float[] sincos){
        sincos[0] = sin(radAngle);
        sincos[1] = cos(radAngle);
        return sincos;
    }
    
    /**
     * cosine with improved precision, using interpolation.
     * @param rad - angle in radians
     * @return
     */
    public static final float cos2(final float rad){  
        float ang = rad < 0 ? -rad : rad;
        // ang = rad MOD 2*PI
        if(ang >= 2*PI){
             ang = ang - (2*PI * (int)(ang*(1.0f/(2*PI))));
         }
         int index = ((int)(ang*STEP_INV)) & (SIZE-1);        
         float y1 = LUT_COS[index];
         float y2 = LUT_COS[index + 1];
         float v =  y1 + (y2-y1)*STEP_INV*(ang - index*STEP);
         return v; 
    }
    
    /**
     * sin using interpolation. High precison.
     * @param rad angle in radians
     * @return sin
     */
    public static final float sin2(final float rad){
        return cos2(rad-PI/2);
  }
  
  
    ////////////////////////////////////////////////////////////////////
    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Testing LUT sin(x) Precision");       
        System.out.println("LUT size: " + SIZE);       
        System.out.println("Deg     \t  Rad \t      Math.sin(x)   \t Lut.Sin() \t Lut.Sin2()  \t  Dif");
    
        float[] radians = random(SIZE, Math.toRadians(0), Math.toRadians(45));
        for (int i=0; i<radians.length; i++){
            float angle = radians[i];// (float) (2*Math.PI * Math.random()); // i*STEP;
            float degree = (float)Math.toDegrees(angle);
            float sin = (float) Math.sin(angle);
            float lut = (float) sin(angle);
            float lut2 = (float) sin2(angle);
            float err = (float)Math.abs(Math.asin(lut2-sin));
            System.out.printf("%7.4f \t%7.4f \t%7.5f \t %7.5f \t %7.5f \t%7.6f\n",degree, angle, sin, lut, lut2, err);            
        }
        
    }//main
    
    
    /////////////////////////////////////////////////////////////////////////////////
    
    private static float[] random(int size, double min, double max){
        float[] v = new float[size];
        max = max-min;
        for (int i = 0; i < v.length; i++) {
            v[i] = (float)(min + max * Math.random());
        }
        Arrays.sort(v);
        return v;
    }

}
