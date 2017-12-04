package com.muketer.PotalWebCrawler.ServiceInterface;

import java.io.IOException;

import org.jsoup.nodes.Document;

public interface I_DocumentParser {

	public String documentParse(Document linkPage) throws IOException;

}
