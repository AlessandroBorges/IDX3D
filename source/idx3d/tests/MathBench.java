package idx3d.tests;



import java.util.Arrays;

import idx3d.idx3d_Math;

public class MathBench {

    ////////////////////////////////////////////////////////////////////////////

    private static float[] sample = null;
    private static float[] result = null;

    public static void main(String[] args) {
       
        
        int reps = 10000;
        int sets = 100;
        sample = random(reps,Math.toRadians(0), Math.toRadians(20));
        result = sample(reps);

        System.out.println("reps: " + reps);
        System.out.println("  Trial  Math.sin()  idx3d.sin()  LUT90.sin()  Math.cos()");
        for (int i = 0; i < sets; i++) {
            System.out.printf("%4d \t %5.4f \t %5.4f \t %5.4f \t %5.4f\n", 
                    i, testSinLib(reps), testSinTab(reps), testSinLut(reps), testCosLib(reps));
        }

        boolean doPrecisionTest = true;
        if (doPrecisionTest) {
            System.out.println("Precision Test");
            System.out.println("\n\nAngle \t JavaMath \t idxSin \t lutSin \t errIdx  \t errLut \t Lut_Win");
            for (int i = 0; i < 361; i++) {
                float angle = (float) i / 2f;
                float rad = (float) Math.toRadians(angle);
                float jsin = (float) Math.sin(rad);
                float isin = idx3d_Math.sin(rad);
                float lutSin = LUT90.sin(rad);
                float arc = (float) Math.abs(Math.toDegrees(Math.asin(jsin - isin)));
                float arcLut = (float) Math.abs(Math.toDegrees(Math.asin(jsin - lutSin)));

                System.out.printf("%7.2f\t %7.7f\t %7.7f\t %7.7f \t %7.7f \t %7.7f \t %b\n",
                        angle, jsin, isin, lutSin, arc, arcLut, (arcLut <= arc));
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
    
    private static float[] random(int size, double min, double max){
        float[] v = new float[size];
        max = max-min;
        for (int i = 0; i < v.length; i++) {
            v[i] = (float)(min + max * Math.random());
        }
        Arrays.sort(v);
        return v;
    }

    private static float testSinTab(int n) {
        float v = 0;
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            //result[i] = 
                v +=    idx3d_Math.sin(sample[i]);
        }
        time += System.nanoTime();
       //System.out.println("vidx = "+v);
        return (time / 1e6f);
    }

    private static float testSinLut(int n) {
        float v = 0;
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            //result[i] = 
                v += LUT90.sin(sample[i]);
        }
        time += System.nanoTime();
       // System.out.println("slut = "+v);
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
     //   System.out.println("clut = "+v);
        return (time / 1e6f);
    }
    
    private static float testSinNaive(int n) {
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
    
    private static float testCosTab(int n) {
        float v = 0;
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            result[i] = idx3d_Math.cos(sample[i]);
        }
        time += System.nanoTime();
        return time / 1e6f;
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
    
    private static float testCosLib(int n) {
        long time = -System.nanoTime();
        float v = 0;
        for (int i = 0; i < n; i++) {
            //result[i] = 
               v +=   (float) Math.cos(sample[i]);
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
          float lut = (float) idx3d_Math.sin(angle);
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
