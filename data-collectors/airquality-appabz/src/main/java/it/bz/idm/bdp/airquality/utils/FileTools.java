package it.bz.idm.bdp.airquality.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileTools {

	private static final Logger log = LogManager.getLogger(FileTools.class.getName());

	public static void createWriteableFolderIfNotExists(final String folderName) throws IOException {
		File folder = new File(folderName);
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				if (!folder.setWritable(true, true)) {
					throw new IOException("Unable to create folder: " + folder.getAbsolutePath());
				}
			}
		}
	}

	public static String expandPath(String path) {
		if (path.startsWith("~" + File.separator)) {
			return System.getProperty("user.home") + path.substring(1);
		}
		if (!path.startsWith(File.separator)) {
			ClassLoader classLoader = FileTools.class.getClassLoader();
			URL resource = classLoader.getResource(path);
			File file = new File(resource.getFile());
			return file.getAbsolutePath();
		}
		return path;
	}

	public static void purgeDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				purgeDirectory(file);
			file.delete();
		}
	}

	public static void purgeDirectory(String path) {
		File dir = new File(path);
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				purgeDirectory(file);
			file.delete();
		}
	}

	/**
	 * Unzip it
	 *
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 * @throws IOException
	 */
	public static void unZip(String zipFile, String outputFolder) throws IOException {
		byte[] buffer = new byte[1024];

		FileTools.createWriteableFolderIfNotExists(outputFolder);
		ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry zipEntry = zipStream.getNextEntry();

		while (zipEntry != null) {

			String fileName = zipEntry.getName();
			File newFile = new File(outputFolder + File.separator + fileName);

			createWriteableFolderIfNotExists(newFile.getParent());
			FileOutputStream fos = new FileOutputStream(newFile);

			int len;
			while ((len = zipStream.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

			fos.close();
			zipEntry = zipStream.getNextEntry();
			log.debug("File {} unzipped.", newFile.getAbsoluteFile());
		}

		zipStream.closeEntry();
		zipStream.close();
	}

}
