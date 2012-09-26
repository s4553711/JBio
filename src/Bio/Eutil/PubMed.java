/** 
  *PubMed E-utility
  *lib required: dom4j, jaxen
  *@author s4553711 
  *@see javax.swing.Japplet 
  */ 
package Bio.Eutil;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class PubMed {
	
	//private Hashtable<String,String> store;
	private ArrayList<Hashtable<String,String>> store_ar;
	private Hashtable<String,String> efetch_schema;
	private int retmax;
	private String max_return;
	
	public PubMed (){
		//store = new Hashtable<String,String>();
		retmax = 30000;
		max_return = "";
		
		store_ar = new ArrayList<Hashtable<String,String>>();
		efetch_schema = new Hashtable<String,String>(){{
			put("./MedlineCitation/PMID","PMID");
			put("//ISSN","ISSN");
			put(".//Volume","Volume");
		}};
	}

	public void set_retmax(int set_retmax){
		retmax = set_retmax;
	}
	
	public String get_return_max(){
		return max_return;
	}
	
	private void asign_store_val(Element ele, Hashtable<String,String> store ,String tar, String to){
		if (ele.selectSingleNode(tar) != null){
			store.put(to, ele.selectSingleNode(tar).getText());
		} else {
			store.put(to,"");
		}
	}
	
	public ArrayList<Hashtable<String,String>> return_data(){
		return store_ar;
	}
	
	/**
	 * Using E-search to get all the search results (PMID)
	 * 
	 * @param query String the query text
	 * @return tmp_store PMID list in ArrayList<String>
	 */
	public ArrayList<String> esearch(String query){
		
		ArrayList<String> tmp_store = new ArrayList<String>();
		
		try{
			
			URI uri = new URI("http","eutils.ncbi.nlm.nih.gov","/entrez/eutils/esearch.fcgi",
								"db=pubmed&term="+query+"&retmax="+retmax,null);
			URL url = uri.toURL();
			
			SAXReader readerx = new SAXReader();
			Document document = readerx.read(url.openStream());
			
			max_return = document.selectSingleNode(".//Count").getText();
			
			Iterator PMID_List = document.selectNodes("//Id").iterator();
			while(PMID_List.hasNext()){
				Element ele = (Element) PMID_List.next();
				tmp_store.add(ele.getData().toString());
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return tmp_store;
	}
	
	/**
	 * Using E-fetch to find the detail data about each PMID.
	 * Notice that there is no return value, the result store 
	 * in the sotre_ar and you can use return_data() to retrieve 
	 * the final results. 
	 * 
	 * @param query
	 */
	public void efetch(String query){
		
		try {
			String urlStr = "https://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="+query+"&retmode=xml";
			SAXReader readerx = new SAXReader();
			Document document = readerx.read(new URL(urlStr).openStream());
	
			Iterator i1 = document.selectNodes("//PubmedArticle").iterator();
			while(i1.hasNext()){
				
				Hashtable<String,String> store = new Hashtable<String,String>();
				Element ele = (Element) i1.next(); 
	        	
				//String thisPMID = ele.selectSingleNode(".//PMID").getText();
				
				asign_store_val(ele,store,"./MedlineCitation/PMID","PMID");
				asign_store_val(ele,store,".//ISSN","ISSN");
				asign_store_val(ele,store,".//Volume","Volume");
				asign_store_val(ele,store,".//Issue","Issue");
				asign_store_val(ele,store,"./MedlineCitation/Article/Journal/JournalIssue/PubDate/Year","PYear");
				asign_store_val(ele,store,"./MedlineCitation/Article/Journal/JournalIssue/PubDate/Month","PMonth");
				asign_store_val(ele,store,"./MedlineCitation/Article/Journal/Title","JTitle");
				asign_store_val(ele,store,"./MedlineCitation/Article/Journal/ISOAbbreviation","JTitleAbv");
				asign_store_val(ele,store,"./MedlineCitation/Article/Pagination/MedlinePgn","Page");
				asign_store_val(ele,store,"./MedlineCitation/Article/Abstract/AbstractText","Abs");
				asign_store_val(ele,store,"./MedlineCitation/Article/Affiliation","Aff");
				asign_store_val(ele,store,"./PubmedData/ArticleIdList/ArticleId[@IdType='doi']","DOI");
				
				//Iterator i2 = ele.selectNodes(".//JournalIssue/PubDate/Year").iterator();	            	
				//while(i2.hasNext()){
				//	Element ele2 = (Element) i2.next();
				//	System.out.println(thisPMID+"\t"+ele2.getStringValue());
				//}
				
				// Finding the Author data
				Iterator i3 = ele.selectNodes(".//Author").iterator();
				
				// store all author information
				String tmp_authors_str = new String("");
				
				while(i3.hasNext()){
					Element ele_author = (Element) i3.next();
					
					String fore_name = new String("");
					String last_name = new String("");
					String initials_name = new String("");
					
					if (ele_author.selectSingleNode(".//ForeName") != null) 
						fore_name = ele_author.selectSingleNode(".//ForeName").getText();
					
					if (ele_author.selectSingleNode(".//LastName") != null) 
						last_name = ele_author.selectSingleNode(".//LastName").getText();

					if (ele_author.selectSingleNode(".//Initials") != null) 
						initials_name = ele_author.selectSingleNode(".//Initials").getText();
					
					if (initials_name.equals("") && fore_name.equals("") && last_name.equals("")) continue;
					
					if (tmp_authors_str.equals("")){
						tmp_authors_str = fore_name+"::"+last_name+"::"+initials_name;
					} else {
						tmp_authors_str += "||"+fore_name+"::"+last_name+"::"+initials_name;
					}
				}
				
				store.put("authors",tmp_authors_str);
				
				store_ar.add(store);			
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void clear(){
		store_ar.clear();
	}
}
