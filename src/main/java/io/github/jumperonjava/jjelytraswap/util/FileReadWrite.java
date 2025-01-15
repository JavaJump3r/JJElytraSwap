package io.github.jumperonjava.jjelytraswap.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class FileReadWrite {

    /**
     * Writes text to file, throws runtime exception if something goes wrong
     * @param file
     * @param text
     */
    public static void write(File file, String text) {
        try{
            file.getParentFile().mkdirs();
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] strToBytes = text.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to read file, creates empty file if it does not exist and returns empty string
     * @param file
     * @return
     */
    public static String read(File file) {
        try{
            return new String(Files.readAllBytes(file.toPath()));
        }
        catch(IOException e)
        {
            write(file,"");
            return read(file);
        }
    }
}
