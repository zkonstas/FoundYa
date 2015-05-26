package com.alchemyapi.api;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class TaxonomyTestLocally {

	public static void main(String[] args) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {
		// TODO Auto-generated method stub

		// Create an AlchemyAPI object.
				AlchemyAPI alchemyObj = AlchemyAPI
						.GetInstanceFromString("");

				// Extract a ranked list of relations for a web URL.
				Document doc = alchemyObj
						.URLGetTaxonomy("http://www.bbc.com/");
				System.out.println (getStringFromDocument(doc));
				
	}
	
	private static String getFileContents(String filename) throws IOException, FileNotFoundException
	    {
	        File file = new File(filename);
	        StringBuilder contents = new StringBuilder();

	        BufferedReader input = new BufferedReader(new FileReader(file));

	        try {
	            String line = null;

	            while ((line = input.readLine()) != null) {
	                contents.append(line);
	                contents.append(System.getProperty("line.separator"));
	            }
	        } finally {
	            input.close();
	        }

	        return contents.toString();
	    }
	// utility method
	private static String getStringFromDocument(Document doc) {
		try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            String[] sentiment = writer.toString().split("\\n");
            String taxon = "";
            for (int i=0;i<sentiment.length;i++){
            	if (sentiment[i].contains("<label>")){
            		taxon = sentiment[i].replace("<label>", "").replace("</label>", "").trim();
            		break;
            	}
            }
            System.out.println(writer.toString());
            return taxon;
            //return sentiment[7].replace("<label>", "").replace("</label>", "").trim();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
	}

}
