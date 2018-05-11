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
                System.out.println("Default path get from environment variable 'FIRMWARE_PATH', where path is set : "+path);
                file = new File(path);
                lastIndex = getLastIndex(file.getAbsolutePath());
            }
            else
            {
                System.out.println("Error: Environment variable 'FIRMWARE_PATH' not found !!!");
            }
            if (lastIndex > 0)
            {
                MetadataGeneratorHelper.generateMetadataJSON(file, lastIndex);
                System.out.println("Firmware metadata file generated successfully at location :" + file.getAbsolutePath());
            }
            else
            {
               System.out.println("Error: Invalid path");
            }
        }
        catch (Exception e)
        {
            System.out.println("Error: "+e.getMessage());
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
            System.out.println("Error: Invalid path, not found firmware binary file !!!");
        }
        return lastIndex;
    }
}
