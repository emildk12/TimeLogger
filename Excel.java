import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.io.*;
import jxl.write.*;
import jxl.*;

import java.util.Calendar;
import java.util.Scanner;
import java.util.ArrayList;

import java.lang.Math;

// https://jexcelapi.sourceforge.net/resources/javadocs/2_6_10/docs/index.html

public class Excel {

    private static LocalDate date;
    private static String dateFormatted;
    private static String fileName;
    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String TOTALT = "TIMER TOTALT:";

    public static int writeTime(String employee, float from, float to) {

        setDate();
        String path = "";
        try {
            File myFile = new File("ut_mappe.txt");
            Scanner scanner = new Scanner(myFile);
            path = scanner.nextLine().strip();
            scanner.close();
        }
        catch (Exception e) {}
        fileName = path + date.getYear() + "_" + date.getMonth() + ".xls";
        WritableWorkbook workbook = null;

        // Skriver i en eksistreende excel fil, eller så lager den en ny hvis
        // det ikke finnes en for denne måneden
        try {
            File file = new File(fileName);
            Workbook temp = Workbook.getWorkbook(file);
            workbook = Workbook.createWorkbook(file, temp);
            temp.close();
        }
        catch (FileNotFoundException e) {
            workbook = createFile();
        }
        catch (Exception e) {
            System.err.println("writeTime: " + e);
            return -1;
        }
        try {
            WritableSheet sheet = (WritableSheet) workbook.getSheet(0);
            Cell employeeCell = sheet.findCell(employee);
            if (employeeCell == null) {
                workbook.close();
                return -1;
            }
            int column = employeeCell.getColumn();
            Cell dateCell = sheet.findCell(dateFormatted);
            int row = dateCell.getRow();
            sheet.addCell(new jxl.write.Number(column, row, (to - from)));
            workbook.write();
            workbook.close();
        }
        catch (Exception e) {
            System.err.println(e);
            return -1;
        }
        return 0;
    }

    private static void setDate() {
        if (Gui.arg.length > 0) {
            date = LocalDate.now().plusDays(Integer.parseInt(Gui.arg[0]));
        }
        else {
            date = LocalDate.now();
        }
        dateFormatted = date.format(format);
    }

    private static WritableWorkbook createFile() {
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(new File(fileName));
            WritableSheet sheet = workbook.createSheet("Sheet 1", 0);

            sheet.setColumnView(0, 14);
            int max = date.lengthOfMonth();
            LocalDate iterativeDate = LocalDate.of(date.getYear(), date.getMonthValue(), 1);
            for (int i = 0; i < max; i++) {
                sheet.addCell(new Label(0, i + 3, iterativeDate.format(format)));
                iterativeDate = iterativeDate.plusDays(1);
            }
            sheet.addCell(new Label(0, max + 3, TOTALT));
            sheet.addCell(new Label(0, 0, "Navn"));
            sheet.addCell(new Label(0, 1, "Fodselsnummer"));
            sheet.addCell(new Label(0, 2, "Dato"));

            String[] employees = Employees.getEmployees();
            String[] fnr = Employees.getFnr();
            for (int i = 0; i < employees.length; i++) {
                sheet.addCell(new Label(i + 1, 0, employees[i]));
                sheet.addCell(new Label(i + 1, 1, fnr[i]));
                sheet.setColumnView(i + 1, 20);
                String letter = translateIntToExcelColumnLetter(i + 2);
                sheet.addCell(new Formula(i + 1, max + 3, "SUM(" + letter + "4:" + letter + (max + 3) + ")"));

            }
        }
        catch (Exception e) {
            System.err.println("createFile: " + e);
        }
        return workbook;
    }

    private static String translateIntToExcelColumnLetter(int column) {
        int count = 0;
        ArrayList<Integer> indexList = new ArrayList<>();

        while (column > 26) {
            int index = column%26;
            index = index == 0 ? 26 : index;
            indexList.add(index - 1);
            column = column - index;
            count++;
            column = column/26;
        }
        column = column == 0 ? 26 : column;
        indexList.add(column - 1);
        count++;

        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                            "U", "V", "W", "X", "Y", "Z"};
        String outString = "";
        for (; count > 0; count--) {
            outString += letters[indexList.get(count-1)];
        }
        return outString;
    }
}
