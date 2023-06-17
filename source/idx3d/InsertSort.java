/**
 * 
 */
package idx3d;

/**
 * InsertSort <br>
 * 
 * from http://www.algolist.net/Algorithms/Sorting/Insertion_sort
 * 
 * @author Livia
 *
 */
public class InsertSort {

   public static void insertionSort(int[] arr) {
        int i, j, newValue;
        for (i = 1; i < arr.length; i++) {
            newValue = arr[i];
            j = i;
            while (j > 0 && arr[j - 1] > newValue) {
                arr[j] = arr[j - 1];
                j--;
            }
            arr[j] = newValue;
        }
    }
   
   /**
    * partial insertionSort
    * @param arr - array to be sorted
    * @param left - left most index, zero based
    * @param right - rightmost index. Must be < arr.length
    */
   public static void insertionSort(int[] arr, int left, int right) {
       int i, j;
       int newValue; 
       
       for (i = left + 1; i < right; i++) {
           newValue = arr[i];
           j = i;
           while (j > left && arr[j - 1] > newValue) {
               arr[j] = arr[j - 1];
               j--;
           }
           arr[j] = newValue;
       }
   }
   
   public static void insertionSort(ITriangle[] arr) {
       int j;
       ITriangle newValue; 
       
       for (int i = 1; i < arr.length; i++) {
           newValue = arr[i];
           j = i;
           while (j > 0 && arr[j - 1].dist > newValue.dist) {
               arr[j] = arr[j - 1];
               j--;
           }
           arr[j] = newValue;
       }
   }
   
   /**
    * partial insertionSort
    * @param arr - array to be sorted
    * @param left - left most index, zero based
    * @param right - rightmost index. Must be < arr.length
    */
   public static void insertionSort(ITriangle[] arr, int left, int right) {
       int i, j;
       ITriangle newValue; 
       
       for (i = left + 1; i < right; i++) {
           newValue = arr[i];
           j = i;
           while (j > left && arr[j - 1].dist > newValue.dist) {
               arr[j] = arr[j - 1];
               j--;
           }
           arr[j] = newValue;
       }
   }
   
   
   
   
   

}
