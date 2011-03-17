package helpers;

public class ArrayUtilities {
    public static void printArray(Object[][] array) {
        if (array == null)
            return;
        
        for (int i = 0; i < array[0].length; i++) {
            for (int j = 0; j < array.length; j++) {
                System.out.print(array[j][i]);
            }
            System.out.println("");
        }
    }
}
