package com.camjacv.rbnr;

import java.util.Map;

public class HIResult {
	private Long duration;
	private String originalPath;
	private Map<String, String[]> ocrResult;
	
	public Long getDuration() {
		return duration;
	}
	public void setDuration(Long duration) {
		this.duration = duration;
	}
	public String getOriginalPath() {
		return originalPath;
	}
	public void setOriginalPath(String originalPath) {
		this.originalPath = originalPath;
	}
	public Map<String, String[]> getOcrResult() {
		return ocrResult;
	}
	public void setOcrResult(Map<String, String[]> ocrResult) {
		this.ocrResult = ocrResult;
	}
}
