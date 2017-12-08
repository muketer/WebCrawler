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
			
			// �׽�Ʈ
			System.out.println("DocumentParser - documentParse / bodyContents==null");
			
			return null;
		}
		
/*		// �׽�Ʈ
		printDoc(bodyContents, pageNo, "�ڵ� ���� ����  bodyContents");
		System.out.println("--- �ڵ� ���� ���� bodyContents print �Ϸ�");*/
		
		bodyContents.trim();
		bodyContents = removeNedlessCode(bodyContents, "<!--", ">", new InnerParser());
		
/*		// �׽�Ʈ
		printDoc(bodyContents, pageNo, "�ڸ�Ʈ ���� ���� bodyContents");
		System.out.println("--- �ڸ�Ʈ ���� �Ϸ�");*/
		
		bodyContents = removeNedlessCode(bodyContents, "<script", "</script>", new InnerParser(){
			@Override
			public int backCutting(String bodyContents, String backString, int frontCuttingPoint){
				int backCuttingPoint = bodyContents.indexOf(backString, frontCuttingPoint);
				return bodyContents.indexOf(">", backCuttingPoint)+1;
			}
		});
		
/*		// �׽�Ʈ
		printDoc(bodyContents, pageNo, "scipt �ڵ� ���� ���� bodyContents");
		System.out.println("--- script �ڵ� ���� �Ϸ�");*/
		
		bodyContents = removeNedlessCode(bodyContents, "<", ">", new InnerParser(){
			@Override
			protected String nextCutting(String bodyContents, String frontString, String backString, int frontCuttingPoint,
					int backCuttingPoint){
				return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(backCuttingPoint);
			}
		});
		
/*		// �׽�Ʈ
		printDoc(bodyContents, pageNo, "������ �±� ���� ���� bodyContents");
		System.out.println("--- ������ �±� ���� �Ϸ�");*/
		
		return bodyContents;
	}
	
	private String pickHtmlBodyContents(Document linkPage){
		String contents = linkPage.toString();
		int frontCuttingPoint = htmlBodyStartingTag_IndexNo(contents)+1;
		
		if(frontCuttingPoint<=0) {
		
			// �׽�Ʈ
			System.out.println("DocumentParser - pickHtmlBodyContents / frontCuttingPoint<=0");
			
			return null;
		}

		int backCuttingPoint = contents.indexOf("</body>");
		return contents.substring(frontCuttingPoint, backCuttingPoint);
	}
	
	private int htmlBodyStartingTag_IndexNo(String contents){
		int cuttingPoint = contents.indexOf("<body");
		
		if(cuttingPoint<0) {
			
			// �׽�Ʈ
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
			
/*			// �׽�Ʈ
			System.out.println("DocumentParser - backCutting");*/
			
			 return bodyContents.indexOf(backString, frontCuttingPoint)+1;
		}
		
		protected String nextCutting(String bodyContents, String frontString, String backString, int frontCuttingPoint,
				int backCuttingPoint){
			
/*			// �׽�Ʈ
			System.out.println("DocumentParser - nextCutting");*/
			
			int nextCuttingPoint = bodyContents.indexOf("<", backCuttingPoint);
			if(nextCuttingPoint<0)
				return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(backCuttingPoint);
			return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(nextCuttingPoint);
		}
		
		protected String removeText(String bodyContents, String frontString, String backString){
			
/*			// �׽�Ʈ
			System.out.println("DocumentParser - removeText");*/
			
			int frontCuttingPoint = bodyContents.indexOf(frontString);
			if(frontCuttingPoint<0)
				return bodyContents;
			int	backCuttingPoint = backCutting(bodyContents, backString, frontCuttingPoint);
			return nextCutting(bodyContents, frontString, backString, frontCuttingPoint, backCuttingPoint);
		}
	}
	
	
	
	// ----------------------------------------------- ��� ���� ------------------------------------------------
	
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
		// "&amp;nbsp;" �� "&amp;&nbsp;"�� �� �� / " "�δ� ����
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
		BufferedWriter buffer = new BufferedWriter(writer);	//�������
		PrintWriter printer = new PrintWriter(buffer);		//���ͻ�����
		return printer;
	}
	
}
