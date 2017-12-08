package com.muketer.PotalWebCrawler.ServiceClass;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.muketer.PotalWebCrawler.ServiceInterface.I_DocumentParser;

@Service
public class DocumentParser implements I_DocumentParser{
	
	public DocumentParser(){}
	
	@Override
	public String documentParse(Document linkPage) throws IOException{		
		String bodyContents = pickHtmlBodyContents(linkPage);
		
		if(bodyContents==null) {
			
			// 테스트
			System.out.println("DocumentParser - documentParse / bodyContents==null");
			
			return null;
		}
		
/*		// 테스트
		printDoc(bodyContents, pageNo, "코드 정제 전의  bodyContents");
		System.out.println("--- 코드 정제 전의 bodyContents print 완료");*/
		
		bodyContents.trim();
		bodyContents = removeNedlessCode(bodyContents, "<!--", ">", new InnerParser());
		
/*		// 테스트
		printDoc(bodyContents, pageNo, "코멘트 삭제 후의 bodyContents");
		System.out.println("--- 코멘트 삭제 완료");*/
		
		bodyContents = removeNedlessCode(bodyContents, "<script", "</script>", new InnerParser(){
			@Override
			public int backCutting(String bodyContents, String backString, int frontCuttingPoint){
				int backCuttingPoint = bodyContents.indexOf(backString, frontCuttingPoint);
				return bodyContents.indexOf(">", backCuttingPoint)+1;
			}
		});
		
/*		// 테스트
		printDoc(bodyContents, pageNo, "scipt 코드 삭제 후의 bodyContents");
		System.out.println("--- script 코드 삭제 완료");*/
		
		bodyContents = removeNedlessCode(bodyContents, "<", ">", new InnerParser(){
			@Override
			protected String nextCutting(String bodyContents, String frontString, String backString, int frontCuttingPoint,
					int backCuttingPoint){
				return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(backCuttingPoint);
			}
		});
		
/*		// 테스트
		printDoc(bodyContents, pageNo, "나머지 태그 삭제 후의 bodyContents");
		System.out.println("--- 나머지 태그 삭제 완료");*/
		
		return bodyContents;
	}
	
	private String pickHtmlBodyContents(Document linkPage){
		String contents = linkPage.toString();
		int frontCuttingPoint = htmlBodyStartingTag_IndexNo(contents)+1;
		
		if(frontCuttingPoint<=0) {
		
			// 테스트
			System.out.println("DocumentParser - pickHtmlBodyContents / frontCuttingPoint<=0");
			
			return null;
		}

		int backCuttingPoint = contents.indexOf("</body>");
		return contents.substring(frontCuttingPoint, backCuttingPoint);
	}
	
	private int htmlBodyStartingTag_IndexNo(String contents){
		int cuttingPoint = contents.indexOf("<body");
		
		if(cuttingPoint<0) {
			
			// 테스트
			System.out.println("DocumentParser - htmlBodyStartingTag_IndexNo / cuttingPoint<0");
			
			return -1;
		}
		
		return contents.indexOf(">", cuttingPoint);
	}
	
	private String removeNedlessCode(String bodyContents, String frontString, String backString,
			InnerParser innerParser){
		while(bodyContents.contains(frontString))
			bodyContents = innerParser.removeText(bodyContents, frontString, backString);
		return bodyContents.trim();
	}
	
	private class InnerParser{
		protected int backCutting(String bodyContents, String backString, int frontCuttingPoint){
			
/*			// 테스트
			System.out.println("DocumentParser - backCutting");*/
			
			 return bodyContents.indexOf(backString, frontCuttingPoint)+1;
		}
		
		protected String nextCutting(String bodyContents, String frontString, String backString, int frontCuttingPoint,
				int backCuttingPoint){
			
/*			// 테스트
			System.out.println("DocumentParser - nextCutting");*/
			
			int nextCuttingPoint = bodyContents.indexOf("<", backCuttingPoint);
			if(nextCuttingPoint<0)
				return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(backCuttingPoint);
			return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(nextCuttingPoint);
		}
		
		protected String removeText(String bodyContents, String frontString, String backString){
			
/*			// 테스트
			System.out.println("DocumentParser - removeText");*/
			
			int frontCuttingPoint = bodyContents.indexOf(frontString);
			if(frontCuttingPoint<0)
				return bodyContents;
			int	backCuttingPoint = backCutting(bodyContents, backString, frontCuttingPoint);
			return nextCutting(bodyContents, frontString, backString, frontCuttingPoint, backCuttingPoint);
		}
	}
	
	
	
	// ----------------------------------------------- 출력 관련 ------------------------------------------------
	
	private void printDoc(Document doc, int pageNum, String fileName) throws IOException{
		if(doc==null)
			return;
		PrintWriter printer = makeFile(pageNum, fileName);
		String translatedDoc = doc.toString();
		String[] splitedDoc = translatedDoc.split("\n");
		for(String docLine : splitedDoc){
			printer.println(docLine);
		}
		//printer.flush();
		printer.close();
	}
	
	private void printDoc(String str, int pageNum, String fileName) throws IOException {
		if(str==null)
			return;
		PrintWriter printer = makeFile(pageNum, fileName);
		// "&amp;nbsp;" 나 "&amp;&nbsp;"는 안 됨 / " "로는 가능
		/*String[] splitedStr = str.split("\u0020");*/
		/*char[] splitedChar = str.toCharArray();*/
		/*for(String strLine : splitedStr){
			printer.println(strLine);
		}*/
		/*for(char chr : splitedChar){
			printer.println(chr);
		}*/
		//printer.flush();
		
		printer.println(str);
		
		printer.close();
	}
	
	private void printDoc(Elements elements, int pageNum, String fileName) throws IOException{
		if(elements == null)
			return;
		PrintWriter printer = makeFile(pageNum, fileName);
		for(Element element : elements)
			printer.println(element.text());
		printer.close();
	}
	
	private PrintWriter makeFile(int pageNum, String fileName) throws IOException{
		String pageNumString = String.valueOf(pageNum);
		File target = new File("E:/java&bigData_KimDongwook/PrintDoc",
				fileName+"_"+pageNumString+".txt");
		FileWriter writer = new FileWriter(target);
		BufferedWriter buffer = new BufferedWriter(writer);	//외장버퍼
		PrintWriter printer = new PrintWriter(buffer);		//엔터생성기
		return printer;
	}
	
}
