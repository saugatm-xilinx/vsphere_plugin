package com.solarflare.firmware;

import java.io.File;
import java.util.regex.Pattern;

import com.solarflare.firmware.util.MetadataGeneratorHelper;

public class Runner
{
    static int basePathLastIndex;

    public static void main(String args[])
    {
        try
        {
            String path = "";
            File file = null;
            int lastIndex = 0;
            // First check in argument
            if (args.length != 0)
            {
                path = args[0];
                file = new File(path);
                lastIndex = getLastIndex(file.getAbsolutePath());
            }
            else if (System.getenv("FIRMWARE_PATH") != null)// Second check on environment variable
            {
                path = System.getenv("FIRMWARE_PATH");
                file = new File(path);
                lastIndex = getLastIndex(file.getAbsolutePath());
            }
            else
            {
                throw new RuntimeException("Firmware binary file path not found !!!");
            }
            if (lastIndex > 0)
            {
                MetadataGeneratorHelper.generateMetadataJSON(file, lastIndex);
                System.out.println("Firmware metadata file generated successfully at location :" + file.getAbsolutePath());
            }
            else
            {
                throw new Exception("Invalid path");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /**
     * Get last index of a string path;
     * @param path
     * @return
     * @throws Exception
     */
    private static int getLastIndex(String path) throws Exception
    {
        int lastIndex = -1;
        String defaultPathSeparator = "/";
        if (path != null && !path.isEmpty())
        {
            String lastChar = path.substring(path.length() - 1, path.length());
            if (path.contains("\\"))
            {
                defaultPathSeparator = "\\";
            }
            if (Pattern.matches("[a-z A-Z]", lastChar))
            {
                path = path + defaultPathSeparator;
            }
            lastIndex = path.lastIndexOf(defaultPathSeparator);
        }
        else
        {
            throw new Exception("Invalid path, found null or empty value!!!");
        }
        return lastIndex;
    }
}
