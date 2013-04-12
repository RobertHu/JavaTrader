package Util;

import nu.xom.*;
import java.io.*;

public class DocumentHelper
{
	public static Document load(String path) throws FileNotFoundException, IOException, ParsingException
	{
		File file = new File(path);
		if(!file.exists()){
			return null;
		}
		FileInputStream stream = new FileInputStream(file);
		Builder builder = new Builder();
		Document doc = builder.build(stream);
		return doc;
	}
}
