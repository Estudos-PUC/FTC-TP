import java.io.File;
import java.io.FileNotFoundException;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            
            String arquivo = "code/model/GLC.txt";
            File file = new File(arquivo);
            Scanner sc = new Scanner(file);
            System.out.println(sc.nextLine());

        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }
}
