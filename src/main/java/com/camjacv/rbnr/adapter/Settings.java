package com.camjacv.rbnr.adapter;

import java.util.List;

public class Settings {
	private int numThreadsToUse;
	private int textDarkFlag;
	private int confidenceThreshold;

	public int getNumThreadsToUse() {
		return numThreadsToUse;
	}

	public void setNumThreadsToUse(int numThreadsToUse) {
		this.numThreadsToUse = numThreadsToUse;
	}

	public int getTextDarkFlag() {
		return textDarkFlag;
	}

	public void setTextDarkFlag(int textDarkFlag) {
		this.textDarkFlag = textDarkFlag;
	}

	public int getConfidenceThreshold() {
		return confidenceThreshold;
	}

	public void setConfidenceThreshold(int confidenceThreshold) {
		this.confidenceThreshold = confidenceThreshold;
	}
}
