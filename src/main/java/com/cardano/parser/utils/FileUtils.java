package com.cardano.parser.utils;


import java.io.*;
import java.util.stream.Collectors;

import static com.cardano.parser.utils.GenerateUtils.capitalize;

public class FileUtils {

    public static String createFile(String folder, String fileName) {
        try {
            File dir = new File(folder);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.out.println("Create folder failed!");
                }
            }
            File myObj = new File(dir.getAbsolutePath(), capitalize(fileName));
            if (!myObj.createNewFile()) {
                System.out.println("File already exists.");
            }
            return myObj.getAbsolutePath();

        } catch (IOException e) {
            System.out.println("An error occurred." + e);
            return null;
        }
    }

    public static boolean writeFile(String filePath, String content) {
        try {
            File file = new File(filePath);
            boolean fileExisted = file.exists();
            if (!fileExisted) {
                fileExisted = file.createNewFile();
            }
            if (fileExisted) {
                file.setWritable(true);
                FileWriter myWriter = new FileWriter(file);
                myWriter.write(content);
                myWriter.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Write file error" + e);
            return false;
        }
    }

    public static String readFile(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));

        } catch (Exception e) {
            System.out.println("readFile error" + e);
            return null;
        }
    }

    /**
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */
    public static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

}
