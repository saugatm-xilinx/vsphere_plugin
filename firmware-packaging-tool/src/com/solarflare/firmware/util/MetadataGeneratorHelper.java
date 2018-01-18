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
import com.solarflare.firmware.model.BinaryFiles;
import com.solarflare.firmware.model.BootROM;
import com.solarflare.firmware.model.Controller;
import com.solarflare.firmware.model.FileHeader;
import com.solarflare.firmware.model.SfFirmware;

public class MetadataGeneratorHelper {
	static BinaryFiles meta = new BinaryFiles();
	static Controller controller = new Controller();
	static BootROM bootRom = new BootROM();
	static int basePathLastIndex ; 
	static String FILE_SEPARATOR = File.separator;
	
	public static void generateMetadataJSON(File filePath) throws Exception
	{
		basePathLastIndex = filePath.getAbsolutePath().lastIndexOf(FILE_SEPARATOR);
		searchAndCreateMetae("dat", filePath);
		Gson gson = new Gson();
		String json = gson.toJson(meta);
		writeUsingFileWriter(json, filePath.getPath());
	}
	
	public static void searchAndCreateMetae(String filename, File file)
	{
		File[] list = file.listFiles();
		if (list != null)
		{
			for (File fil : list) 
			{
				if (fil.isDirectory()) 
				{
					searchAndCreateMetae(filename, fil);
				} 
				else if (filename.equalsIgnoreCase(getFileExtension(fil))) 
				{
					
					String path = fil.getParent();
					String relativePath = FILE_SEPARATOR + path.substring(basePathLastIndex+1, path.length());
					String parantDir = path.substring(path.lastIndexOf(FILE_SEPARATOR)+1,path.length());
					
					if(parantDir.equalsIgnoreCase("bootrom"))
					{
						System.out.println(fil.getName());
						FileHeader header = getFileHeader(readData(fil, false));
						SfFirmware sfFirmware = new SfFirmware();
						sfFirmware.setName(fil.getName());
						sfFirmware.setType(""+header.getType());
						sfFirmware.setSubtype(""+header.getSubtype());
						sfFirmware.setVersionString(header.getVersionString());
						sfFirmware.setPath(relativePath+FILE_SEPARATOR+fil.getName());
						
						if(meta != null && meta.getBootROM() != null)
						{
							meta.getBootROM().getFiles().add(sfFirmware);
						}
						else
						{
							List<SfFirmware> sfFirmwareList = new ArrayList<>();
							sfFirmwareList.add(sfFirmware);
							bootRom.setFiles(sfFirmwareList);
							meta.setBootROM(bootRom);
						}
						
					}
					else if(parantDir.equalsIgnoreCase("controller"))
					{
						System.out.println(fil.getName());
						FileHeader header = getFileHeader(readData(fil, false));
						SfFirmware sfFirmware = new SfFirmware();
						sfFirmware.setName(fil.getName());
						sfFirmware.setType(""+header.getType());
						sfFirmware.setSubtype(""+header.getSubtype());
						sfFirmware.setVersionString(header.getVersionString());
						
						sfFirmware.setPath(relativePath+FILE_SEPARATOR+fil.getName());
						if(meta != null && meta.getController() != null)
						{
							meta.getController().getFiles().add(sfFirmware);
						}
						else
						{
							List<SfFirmware> sfFirmwareList = new ArrayList<>();
							sfFirmwareList.add(sfFirmware);
							controller.setFiles(sfFirmwareList);
							meta.setController(controller);
						}
					}
				}
			}
		}
	}
	private static String getFileExtension(File file) 
	{
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
	
	public static FileHeader getFileHeader(byte[] bytes) {
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
	
	public static byte[] readData(File file, boolean isCompleted) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream stream  = null;
		try {
			byte[] chunk = new byte[1000];
			int bytesRead;
			stream = new FileInputStream(file);
			
			if (isCompleted) {
				while ((bytesRead = stream.read(chunk)) > 0) {
					outputStream.write(chunk, 0, bytesRead);
				}
			} else {
				bytesRead = stream.read(chunk);
				outputStream.write(chunk, 0, bytesRead);
			}
			stream.close();
		} catch (IOException e) {
			return null;
		}
		return outputStream.toByteArray();
	}
	
	 private static void writeUsingFileWriter(String data, String path) 
	 {
	        File file = new File(path+FILE_SEPARATOR+ "firmware-metadata.json");
	        try ( FileWriter fr = new FileWriter(file))
	        {
	            fr.write(data);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

}
