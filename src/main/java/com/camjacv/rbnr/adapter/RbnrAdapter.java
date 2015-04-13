package com.camjacv.rbnr.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RbnrAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(RbnrAdapter.class);
	public native Map<String, String[]> recognizeRbns(Settings settings, String[] imagePaths);

	static {
		try{
			logger.warn("rbnr loading");
			System.loadLibrary("rbnr");
			logger.warn("rbnr loaded successfully");
		}
		catch (Exception e)
		{
			logger.error("in static block, error loading class", e);
		}
	}
}