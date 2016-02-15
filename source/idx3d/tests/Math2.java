/**
 * 
 */
package idx3d.tests;

import java.util.Arrays;

/**
 * Testing LUT table
 * @author Alessandro Borges
 *
 */
public class Math2 {
    
    private static final int SIZE = 256;
    private static final float STEP = (float)((2.0d*Math.PI)/SIZE);
    private static final float STEP_INV = 1.0f / STEP;
    private static final float[] LUT_SIN = new float[SIZE + 2]; // extra room for roundings
    private static final float PI = (float) Math.PI;
    private static final float PI_2 = (float) Math.PI / 2.0f;
    /**
     * Angles lower than MIN_ANGLE will return sin(x) == x
     * It results on smoother sin(x) transitions in range between [-MIN_ANGLE .. +MIN_ANGLE]  
     * Default value is 
     */
    private static final float MIN_ANGLE = (float) (Math.toRadians(12f));
    
    static{
        buildLUT();
    }
    
    private static final void buildLUT(){
        for(int i=0; i<LUT_SIN.length; i++){
            float angle = i*STEP;
            LUT_SIN[i] = (float)Math.sin(angle);
        }
    }
    
    /**
	* sin() using Look up table.
	* @param rad - angle in radians
	* @return sin of rad
	*/
    public static final float sin(final float rad){  
             float ang = rad < 0 ? -rad : rad;   
          // low angles 
             if(ang < MIN_ANGLE){
                 return rad;
             }   
             //int index = (int)(0.5f + ang*STEP_INV)  % SIZE;
             int index = (int)(0.5f + ang*STEP_INV)  & (SIZE-1);
             float v = LUT_SIN[index];
             return rad < 0 ? -v : v; // sin(-x) == -sin(x)
    }
    
        
    public static final float cos(final float rad){      
        return sin(rad + PI_2); // cos(x) = sin(x + PI)
    }
    
    public static final float cos2(final float rad){        
        return sin2(rad + PI_2); 
    }
    
	/**
	 * sin using interpolation. High precison
	 * @param rad angle in radians
	 * @return sin
	 */
    public static final float sin2(final float rad){
        float ang = rad < 0 ? -rad : rad;
       // while(ang > 2 * PI) ang -= 2 * PI; // reduce angle to [0..2PI]            
        
        if(ang > 2*PI){
            ang = ang - (2*PI * (int)(ang*(1.0f/(2*PI))));
        }
        
        int index = (int)(ang * STEP_INV) & (SIZE-1);// % SIZE;        
        float y1 = LUT_SIN[index];
        float y2 = LUT_SIN[index + 1];
        float v =  y1 + (y2-y1)*STEP_INV*(ang - index*STEP);
        return rad < 0 ? -v : v; // sin(-x) == -sin(x)
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
        
        
//        System.err.println("Testing LUT cos(x) Precision");       
//        System.err.println("LUT size: " + SIZE);       
//        System.err.println(" Degree   \t  Rad \t      Math.cos(x)   \t Lut.cos(x)  \t Lut.cos2(x)\t  Dif. cos2 - lut");        
//        for (int i=0; i<radians.length; i++){
//            float radian = -radians[i];// (float) (2*Math.PI * Math.random()); // i*STEP;
//            float degree = (float)Math.toDegrees(radian);
//            float cos = (float) Math.cos(radian);
//            float lut =  cos(radian);
//            float cos2 = cos2(radian); 
//            System.err.printf("%7.4f \t%7.4f \t%7.5f \t %7.5f \t %7.5f \t%7.6f\n",degree, radian, cos, lut, cos2, (float) (cos2-lut));            
//        }
        
        
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
