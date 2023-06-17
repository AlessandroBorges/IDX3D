package idx3d.tests;



import java.util.Arrays;

import idx3d.IMath;
/**
 * Testing sin/cos
 * 
 * @author Alessandro
 *
 */
public class MathBench {

    ////////////////////////////////////////////////////////////////////////////

    private static float[] sample = null;
  //  private static float[] result = null;
    private static float sum = 0;

    public static void main(String[] args) {
       
        
        int size_arr = 10*1000*1000;
        int sets = 10;
        float min = (float) Math.toRadians(0);
        float max = (float)Math.toRadians(45);
        //create a float array with size_arr, with values between min and max
        sample = random(size_arr,min, max);       

        System.out.println("Size test: " + size_arr);
        System.out.println("Angles range: ["+ (float)Math.toDegrees(min) +"..."+ (float)Math.toDegrees(max) +"]");
        System.out.println("Time in ms");
        System.out.println("Trial java.lang.Math.sin() | Math2.sin() | LUT90.sin() | Math2.sin2() [interpolation]");
        for (int i = 0; i < sets; i++) {
            System.out.printf("%4d \t %5.4f \t     %5.4f \t    %5.4f \t %5.4f\n", 
                    i, testSinLib(size_arr), testSinMath2(size_arr), testSinLut90(size_arr), testSin2Math2(size_arr));
        }

        boolean doPrecisionTest = true;
        if (doPrecisionTest) {
            System.out.println("\n\nPrecision Test");
            System.out.println("Error is given in angles, given by arcSin(diff)");
            System.out.println("\n\nAngle \t java.Math \t Math2.sin2() \t LUT90.Sin() \t error.Sin2  \t error.LUT90 \t Sin2 win LUT?");
            for (int i = -360; i < 722; i++) {
                float angle = (float) i ;
                float rad = (float) Math.toRadians(angle);
                float jsin = (float) Math.sin(rad);
                float sin2 = Math2.sin2(rad); // interpolet LUT sin()
                float lut90Sin = LUT90.sin(rad); // LUT 90
                float arc = (float) Math.abs(Math.toDegrees(Math.asin(jsin - sin2)));
                float arcLut = (float) Math.abs(Math.toDegrees(Math.asin(jsin - lut90Sin)));

                System.out.printf("%7.2f\t %7.7f\t %7.7f\t %7.7f \t %7.7f \t %7.7f \t %b\n",
                        angle, jsin, sin2, lut90Sin, arc, arcLut, (arc < arcLut));
            }
        }

    }

    private static float[] sample(int n) {
        float[] values = new float[n];
        float step = 180f / (float) n;
        for (int i = 0; i < n; i++) {
            values[i] = (float) Math.toRadians(i * step);
        }
        return values;
    }
    
    /**
     * Creates a sorted float array with psedo random values between min and max
     * @param size size of array
     * @param min - minimal value
     * @param max - mas value
     * @return float array with values
     */
    private static float[] random(int size, double min, double max){
        float[] v = new float[size];
        max = max-min;
        for (int i = 0; i < v.length; i++) {
            v[i] = (float)(min + max * Math.random());
        }
        Arrays.sort(v);
        return v;
    }

    private static float testSinMath2(int n) {
        float v = 0;
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {            
                v +=    Math2.sin(sample[i]);
        }
        time += System.nanoTime();
        // this is to avoid JVM skip optimizations
        sum = v;
        return (time / 1e6f);
    }

    private static float testSinLut90(int n) {
        float v = 0;
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            //result[i] = 
                v += LUT90.sin(sample[i]);
        }
        time += System.nanoTime();
       // this is to avoid JVM skip optimizations
       sum = v;
        return (time / 1e6f);
    }
    
    private static float testCosLut(int n) {
        float v = 0;
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            //result[i] = 
                v += LUT90.cos(sample[i]);
        }
        time += System.nanoTime();
        // this is to avoid JVM skip optimizations
        sum = v;
        return (time / 1e6f);
    }
    
    private static float testSin2Math2(int n) {
        float v = 0;
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            //result[i] = 
                v += Math2.sin2(sample[i]);
        }
        time += System.nanoTime();
     //   System.out.println("vnav = "+v);
        return (time / 1e6f);
    }
    
      
    private static float testSinLib(int n) {
        long time = -System.nanoTime();
        float v = 0;
        for (int i = 0; i < n; i++) {
            //result[i] = 
               v +=   (float) Math.sin(sample[i]);
        }        
        time += System.nanoTime();
       // System.out.println("vjav = " + v);
        return time / 1e6f;
    }
    
    
    
    /**
     * Test LUT90 class
     */
  public static void testLUT90(){
      System.out.println("Testing LUT sin(x) Precision");
      System.out.println("LUT size: " + LUT90.SIZE);
      System.out.println("index \t Deg     \t  Rad \t      Math.sin(x)   \t Lut.Sin() \t Dif      \t erro(DEG)");
      
      
      System.out.println("Testing LUT sin(x) Classic values");
      double[] classic = {0,30,45,60,90,120,135,180,270,360};
      for (int i = 0; i < classic.length; i++) {
          float angle = (float)Math.toRadians(classic[i]); 
          int idx = i;// index(angle);
          float degree = (float) Math.toDegrees(angle);
          float sin = (float) Math.sin(angle);
          float lut = (float) LUT90.sin(angle);
          float erro = (float) Math.toDegrees(Math.asin(Math.abs(lut-sin)));
          System.out.printf("%3d \t %7.4f \t%7.4f \t%7.5f \t %7.5f \t%7.6f \t%7.6f\n", 
                  idx, degree, angle, sin, lut, Math.abs(sin - lut), erro);
          
      }
      
      System.out.println();
      
      float[] radians = random(120, -6*Math.PI, 6*Math.PI);
      boolean testSin = true;
      if(testSin){
          System.out.println("Test sin");
      for (int i = 0; i < radians.length; i++) {
          float angle = -( radians[i]);
          int idx = i;// index(angle);
          float degree = (float) Math.toDegrees(angle);
          float sin = (float) Math.sin(angle);
          float lut = (float) IMath.sin(angle);
          float erro = (float) Math.toDegrees(Math.asin(Math.abs(lut-sin)));
          System.out.printf("%3d \t %7.4f \t%7.4f \t%7.5f \t %7.5f \t%7.6f \t%7.6f\n", 
                  idx, degree, angle, sin, lut, Math.abs(sin - lut), erro);
      }
      }
      
      boolean testCos = false;
      if(testCos){
          System.out.println("Test cos2");
      for (int i = 0; i < 361 /* radians.length*/; i++) {
          float angle =  (float)Math.toRadians(i);//radians[i];
          int idx = i;//index(angle);
          float degree = (float) Math.toDegrees(angle);
          float cos = (float) Math.cos(angle);
          float lut = (float) LUT90.cos(angle);
          float erro = (float) Math.toDegrees(Math.asin(Math.abs(lut-cos)));
          System.out.printf("%3d \t %7.4f \t%7.4f \t%7.5f \t %7.5f \t%7.6f \t%7.6f\n", 
                  idx, degree, angle, cos, lut, (float)Math.abs(cos - lut), erro);
      }
      }
      
  }
}
