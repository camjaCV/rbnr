package com.camjacv.rbnr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.web.multipart.MultipartFile;

import com.camjacv.rbnr.adapter.RbnrAdapter;
import com.camjacv.rbnr.adapter.Settings;

public class HIServiceImpl implements HIService {

	private static final Logger logger = LoggerFactory.getLogger(HIServiceImpl.class);
	
	//private static final String INPUT_IMAGE_DIR = "data/input-images";
	//private static final String OUTPUT_IMAGE_DIR = "data/output-images";
	private static final String PATH_TO_WEBAPP = "webapps/rbnr";
	private static Integer thresholdForAccess;
	private static final int defaultThreshold = 10;
	
	static
	{
		//Get the threshold for access
		Properties properties = new Properties();
		InputStream istream;
		try {
			istream = new FileInputStream(PATH_TO_WEBAPP + "/data/tracking/tracking.properties");
			properties.load(istream);
			thresholdForAccess = Integer.valueOf(properties.getProperty("visitThresholdForAccess"));
		} catch (FileNotFoundException e) {
			logger.warn("The tracking.properties file was not found, using default value of " + defaultThreshold, e);
		} catch (IOException e) {
			logger.warn("There was an issue with reading the tracking.properties, using default value of " + defaultThreshold, e);
		} catch (NumberFormatException e) {
			logger.warn("The property \"visitThresholdForAccess\" inside the tracking.properties file cannot be coverted to an Integer, using default value of " + defaultThreshold, e);
		} finally {
			if (null == thresholdForAccess)
			{
				thresholdForAccess = defaultThreshold;
			}
		}
	}
	
	@Override
	public HIResult getBinarizedImage(Settings settings, String inputImagePath) {
		String relativePathToInputImages = "data/input-images";
		String fullPathToInputImages = PATH_TO_WEBAPP + "/" + relativePathToInputImages;
		String relativePathToOutputImages = "data/output-images";
		String fullPathToOutputImages = PATH_TO_WEBAPP + "/" + relativePathToOutputImages;
		//TODO add more intelligence to the process (i.e. mlp's)
//		String pathToExecutable = System.getenv("HeadstoneIndexerPath");
		
		logger.info("image storage path: " + fullPathToInputImages);
		
		File image = new File(fullPathToInputImages + "/" + inputImagePath);
		if (!image.exists() || !image.isFile())
		{
			throw new IllegalArgumentException("The image specified does not exist or is not a file (" + image.getAbsolutePath() + ")");
		}
		String fullImagePath = image.getAbsolutePath();
		
		File destinationDir = new File(fullPathToOutputImages);
		destinationDir.mkdirs();
		
		HIResult result = new HIResult();
		result.setOriginalPath(relativePathToInputImages + "/" + inputImagePath);
		
		RbnrAdapter adapter = new RbnrAdapter();
		Date timerStart = new Date();
		String[] imagesToProcess = {fullImagePath};
		Map<String, String[]> ocrResults = adapter.recognizeRbns(settings, imagesToProcess);
		Date timerEnd = new Date();
		result.setDuration(timerEnd.getTime() - timerStart.getTime());
		logger.info("Finished process for image at path: {}", fullImagePath);
		result.setOcrResult(ocrResults);
		
		return result;
	}

	@Override
	public String storeUploadedImage(MultipartFile uploadedFile) throws IllegalStateException, IOException {
		String relativePathToInputImages = "data/input-images";
		String inputImagePath = PATH_TO_WEBAPP + "/" + relativePathToInputImages;
		File inputImageDir = new File(inputImagePath);
		inputImageDir.mkdirs();

		String imageId = UUID.randomUUID().toString();

		String originalFileName = uploadedFile.getOriginalFilename();
		String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
		File newFile = new File(inputImagePath + "/" + imageId + fileExtension);
	
		uploadedFile.transferTo(newFile);
		return relativePathToInputImages + "/" + newFile.getName();
	}

	@Override
	public List<String> getInputImages() {
		String relativePathToInputImages = "data/input-images";
		String fullPathToInputImages = PATH_TO_WEBAPP + "/" + relativePathToInputImages;
		
		List<String> inputImages = new ArrayList<String>();
		File inputImagesDir = new File(fullPathToInputImages);
		logger.info("Using input files found at: " + inputImagesDir.getAbsolutePath());
		File[] files = inputImagesDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				logger.debug("Checking against dir: " + dir + ", name: " + name);
				if (name.matches("\\d+\\.(JPG|jpg)"))
				{
					return true;
				}
				return false;
			}
		});
		
		if (null == files)
		{
			logger.warn("There were no preloaded input images found, return an empty list");
			return inputImages;
		}
		
		for (File file : files)
		{
			inputImages.add(file.getName());
		}
		
		Collections.sort(inputImages);
		return inputImages;
	}

	@Override
	public boolean trackUser(String ipAddress) throws IOException, ParseException {
		DateFormatter dateFormatter = new DateFormatter("yy-MM-dd kk:mm:ss:SS");
		
		//Make sure the file exists
		File trackFile = new File(PATH_TO_WEBAPP + "/data/tracking/" + ipAddress);
		trackFile.createNewFile();
		Date newDate = new Date();

		int count = 0;
		String fullText = "";
		try(BufferedReader reader = new BufferedReader(new FileReader(trackFile)))
		{
			//Remove all +1 day old entries and count lines as we go
			String line;
			while (null != (line = reader.readLine()))
			{
				if (line.trim().isEmpty()) continue;
				//check if date is more than a day old, if so, skip it
				Date oldDate = dateFormatter.parse(line, new Locale("en"));
				long diffInMillis = newDate.getTime() - oldDate.getTime();
				if (diffInMillis <= 86400000)
				{
					//Add the timestamp if it has been less than a day
					count++;
					fullText += line + "\n";
				}
			}
		}
		
		//Add the current vist
		fullText += dateFormatter.print(new Date(), new Locale("en")) + "\n";
		count++;
		
		try(PrintWriter printWriter = new PrintWriter(trackFile))
		{
			//write the entries back to the file
			printWriter.println(fullText);
		}
		
		//if there are [x] visits in the last day, revoke access
		if (count > thresholdForAccess)
		{
			logger.error("Request rejected for ipaddress " + ipAddress);
			return false;
		}
		
		return true;
	}
}
