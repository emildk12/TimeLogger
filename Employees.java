import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;


public class Employees {

    private static ArrayList<String> employees = new ArrayList<String>();
    private static ArrayList<String> fnr = new ArrayList<String>();

    public static int readEmployees() {

        Scanner scanner = null;
        try {
            File myFile = new File("ansatte.txt");
            scanner = new Scanner(myFile);
            String[] temp = new String[2];
            while (scanner.hasNextLine()) {
                temp = scanner.nextLine().strip().split(";");
                employees.add(temp[0]);
                fnr.add(temp[1]);
            }
        }
        catch (FileNotFoundException e) {
            // Gui.createAndDrawErrorMessageClosing("ansatte.txt ikke funnet, vennligst lag en fil med format [ansatte];[Fodselsnummer]");
            System.err.println("ansatte.txt ikke funnet, avslutter program");
            return -1;
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return 0;
    }

    public static String[] getEmployees() {
        String[] arr = new String[employees.size()];
        arr = employees.toArray(arr);
        return arr;
    }
    public static String[] getFnr() {
        String[] arr = new String[fnr.size()];
        arr = fnr.toArray(arr);
        return arr;
    }

}
