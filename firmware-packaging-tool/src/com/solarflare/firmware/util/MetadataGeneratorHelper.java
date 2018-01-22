package com.solarflare.firmware.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.solarflare.firmware.model.BinaryFiles;
import com.solarflare.firmware.model.BootROM;
import com.solarflare.firmware.model.Controller;
import com.solarflare.firmware.model.FileHeader;
import com.solarflare.firmware.model.FirmwareType;
import com.solarflare.firmware.model.SfFirmware;

public class MetadataGeneratorHelper
{
    static BinaryFiles metaModel = new BinaryFiles();
    static Controller controller = new Controller();
    static BootROM bootROM = new BootROM();
    static String FILE_SEPARATOR = "/";
    static int basePathLastIndex;
    static String METADATA_JSON = "FirmwareMetadata.json";
    static String BINARY_FILE_EXTENTION = "dat"; 

    /**
     * Generate JSON file for firmware metadata information
     * 
     * @param filePath
     * @param lastIndex
     * @throws Exception
     */
    public static void generateMetadataJSON(File filePath, int lastIndex) throws Exception
    {
        basePathLastIndex = lastIndex;
        searchBinaryFileAndCreateMetadataJSON(BINARY_FILE_EXTENTION, filePath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(metaModel);
        writeJSON(json, filePath.getPath());
    }

    /**
     * Search a binary file in given path and generate metadata JSON
     * @param filename
     * @param file
     * @throws Exception 
     */
    public static void searchBinaryFileAndCreateMetadataJSON(String filename, File file) throws Exception
    {
        File[] list = file.listFiles();
        if (list != null)
        {
            for (File fil : list)
            {
                if (fil.isDirectory())
                {
                    searchBinaryFileAndCreateMetadataJSON(filename, fil);
                }
                else if (filename.equalsIgnoreCase(getFileExtension(fil)))
                {

                    String path = fil.getParent();
                    String relativePath = FILE_SEPARATOR + "firmware" + FILE_SEPARATOR
                            + path.substring(basePathLastIndex + 1, path.length());
                    FileHeader header = getFileHeader(readData(fil, false));
                    if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType())
                    {
                        // For controller binary file
                        SfFirmware sfFirmware = new SfFirmware();
                        sfFirmware.setName(fil.getName());
                        sfFirmware.setType("" + header.getType());
                        sfFirmware.setSubtype("" + header.getSubtype());
                        sfFirmware.setVersionString(header.getVersionString());

                        sfFirmware.setPath(relativePath + FILE_SEPARATOR + fil.getName());
                        if (metaModel != null && metaModel.getController() != null)
                        {
                            metaModel.getController().getFiles().add(sfFirmware);
                        }
                        else
                        {
                            List<SfFirmware> sfFirmwareList = new ArrayList<>();
                            sfFirmwareList.add(sfFirmware);
                            controller.setFiles(sfFirmwareList);
                            metaModel.setController(controller);
                        }
                    }
                    else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType())
                    {
                        // For bootrom binary file
                        SfFirmware sfFirmware = new SfFirmware();
                        sfFirmware.setName(fil.getName());
                        sfFirmware.setType("" + header.getType());
                        sfFirmware.setSubtype("" + header.getSubtype());
                        sfFirmware.setVersionString(header.getVersionString());
                        sfFirmware.setPath(relativePath + FILE_SEPARATOR + fil.getName());

                        if (metaModel != null && metaModel.getBootROM() != null)
                        {
                            metaModel.getBootROM().getFiles().add(sfFirmware);
                        }
                        else
                        {
                            List<SfFirmware> sfFirmwareList = new ArrayList<>();
                            sfFirmwareList.add(sfFirmware);
                            bootROM.setFiles(sfFirmwareList);
                            metaModel.setBootROM(bootROM);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get file extension
     * @param file
     * @return
     */
    private static String getFileExtension(File file) throws Exception
    {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else
            return "";
    }

    /**
     * Get header of a binary file
     * @param bytes
     * @return
     */
    public static FileHeader getFileHeader(byte[] bytes)throws Exception
    {
        FileHeader header = new FileHeader();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int ih_magic = buffer.getInt();
        header.setMagic(ih_magic);

        int ih_version = buffer.getInt();
        header.setVersion(ih_version);

        int ih_type = buffer.getInt();
        header.setType(ih_type);

        int ih_subtype = buffer.getInt();
        header.setSubtype(ih_subtype);

        int ih_code_size = buffer.getInt();
        header.setCodeSize(ih_code_size);

        int ih_size = buffer.getInt();
        header.setSize(ih_size);

        int ih_controller_version_min = buffer.getInt();
        header.setControllerVersionMin(ih_controller_version_min);

        int ih_controller_version_max = buffer.getInt();
        header.setControllerVersionMax(ih_controller_version_max);

        short ih_code_version_a = buffer.getShort();
        header.setCodeVersion_a(ih_code_version_a);

        short ih_code_version_b = buffer.getShort();
        header.setCodeVersion_b(ih_code_version_b);

        short ih_code_version_c = buffer.getShort();
        header.setCodeVersion_c(ih_code_version_c);

        short ih_code_version_d = buffer.getShort();
        header.setCodeVersion_d(ih_code_version_d);
        return header;
    }

    /**
     * Read chunk data from a file where chunk size is 1000 byte
     * @param file
     * @param isCompleted
     * @return
     */
    public static byte[] readData(File file, boolean isCompleted) throws Exception
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream stream = null;
        try
        {
            byte[] chunk = new byte[1000];
            int bytesRead;
            stream = new FileInputStream(file);

            if (isCompleted)
            {
                while ((bytesRead = stream.read(chunk)) > 0)
                {
                    outputStream.write(chunk, 0, bytesRead);
                }
            }
            else
            {
                bytesRead = stream.read(chunk);
                outputStream.write(chunk, 0, bytesRead);
            }
            stream.close();
        }
        catch (Exception e)
        {
            throw e;
        }
        return outputStream.toByteArray();
    }

    /**
     * Write string json into a given file 
     * @param data
     * @param path
     */
    private static void writeJSON(String data, String path) throws Exception
    {

        File file = new File(path + FILE_SEPARATOR + METADATA_JSON);
        try (FileWriter fr = new FileWriter(file))
        {
            fr.write(data);
        }
        catch (IOException e)
        {
            throw e;
        }
    }

}
