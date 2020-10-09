package com.yitu.txwl.pojo;

import java.util.List;

public class CenterMaskSearch {
	
	private String allLabel;
	
	private String allBindCameras;
	
	private List<CenterMaskPojo> query;

	public String getAllLabel() {
		return allLabel;
	}

	public void setAllLabel(String allLabel) {
		this.allLabel = allLabel;
	}

	public String getAllBindCameras() {
		return allBindCameras;
	}

	public void setAllBindCameras(String allBindCameras) {
		this.allBindCameras = allBindCameras;
	}

	public List<CenterMaskPojo> getQuery() {
		return query;
	}

	public void setQuery(List<CenterMaskPojo> query) {
		this.query = query;
	}
	
	

}
