package com.muketer.PotalWebCrawler.DTO;

public class SearchInformationDTO {
	private String searchKeywords;
	private Integer age;
	private String sex;
	private Double googleScore;
	private Double naverScore;
	private Double daumScore;
	
	public String getSearchKeywords() {
		return searchKeywords;
	}
	public void setSearchKeywords(String searchKeywords) {
		this.searchKeywords = searchKeywords;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public Double getGoogleScore() {
		return googleScore;
	}
	public void setGoogleScore(Double googleScore) {
		this.googleScore = googleScore;
	}
	public Double getNaverScore() {
		return naverScore;
	}
	public void setNaverScore(Double naverScore) {
		this.naverScore = naverScore;
	}
	public Double getDaumScore() {
		return daumScore;
	}
	public void setDaumScore(Double daumScore) {
		this.daumScore = daumScore;
	}
	
	
}
