package io.araujo.androidhttp.util;

import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlUtil {

	static GsonXml gsonXml;

	public static GsonXml getXmlParser() {
		if (gsonXml == null) {
			XmlParserCreator parserCreator = new XmlParserCreator() {
				@Override
				public XmlPullParser createParser() {
					try {
						return XmlPullParserFactory.newInstance().newPullParser();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
			gsonXml = new GsonXmlBuilder().setXmlParserCreator(parserCreator).create();
		}
		return gsonXml;
	}

}
