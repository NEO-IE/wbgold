package iitb.worldBankDataParsing;

import gnu.trove.TFloatArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.xml.crypto.Data;

public class WorldBankQueries {

	public static String extractUnits(String header, String[] contextTokens) {
		String units = "";
		if (contextTokens != null)
			contextTokens[0] = header;
		if (header.contains("(") && header.contains(")")) {
			if (contextTokens != null)
				contextTokens[0] = "";
			while (header.contains("(") && header.contains(")")) {
				int index1 = header.indexOf("(");
				if (contextTokens != null)
					contextTokens[0] += header.substring(0, index1) + " ";
				int index2 = header.indexOf(")");
				if (index1 < 0 || index2 < 0)
					break;
				if (index2 > index1) {
					units = units + " "
							+ (header.substring(index1 + 1, index2));
					if (units.trim().startsWith("in ") && (units.length() > 4))
						units = units.substring(3);
				} else
					break;
				if (index2 < header.length() - 1)
					header = header.substring(index2 + 1, header.length());
				else
					break;
			}
		}
		return units;
	}
	/**
	 * Reads the country names and returns them in an Arraylist
	 * @return Arraylist of countries
	 */
	public static HashSet<String> readCountries() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(DatabaseMetadata.countriesFile));
		ArrayList<String> countries = new ArrayList<String>();
		String temp = null;
		while((temp = br.readLine()) != null) {
			countries.add(temp);
		}
		br.close();
		return new HashSet<String>(countries);
	}
	
	/**
	 * Reads the xml downloaded from data.worldbank.org
	 * @throws Exception
	 */
	public static void genericWorldBankDataParser() throws Exception {
		String store = DatabaseMetadata.store;
		String outFile = DatabaseMetadata.BaseDir + "worldbank-queries.xml";
		
		//Set the outstream
		System.setOut(new PrintStream(new File(outFile)));
		// HashSet<String> selectedAttrs=new
		// HashSet<String>(Arrays.asList(attrsA));
		
		HashSet<String> countries = WorldBankQueries.readCountries();
		java.util.Hashtable<String, String> unitMap = new java.util.Hashtable<String, String>();
		unitMap.put("sq. km", "square kilometre");
		unitMap.put("kt", "kiloton");
		// TObjectFloatHashMap<String> country2Area =
		// getCAFromWikiInfo(countries);
		
		File dir = new File(store);
	
		File files[] = dir.listFiles();
		boolean range = true;
		System.out.println("<worldbank-sample-queries>");
		for (int i = 0; i < files.length; i++) {
			String fname = files[i].getName();
			
			if (!fname.endsWith(".csv"))
				continue;
		
			BufferedReader in = new BufferedReader(new FileReader(files[i]));
			in.readLine();
			in.readLine(); // skip the first 2 lines.
			String line = in.readLine();
			
			int numCols = line.split(",").length;
			String cols[] = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			for (int cnt = 0; (line = in.readLine()) != null; cnt++) {
				
				String flds[] = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				if (flds.length != numCols) {
					throw new Exception("File corrupted at line " + cnt);
				}
				for (int j = 0; j < flds.length; j++) {
					if (flds[j].startsWith("\"")) {
						flds[j] = flds[j].substring(1, flds[j].length() - 1);
					}
				}
				String entityName = flds[DatabaseMetadata.ENTITY_NAME_INDEX];
				String entityCode = flds[DatabaseMetadata.ENTITY_CODE_INDEX];
				System.err.println("Entity : " + entityName);
				
				System.err.println(countries);
				if (!countries.contains(entityName))
					continue;
				String attrName = flds[DatabaseMetadata.INDICATOR_NAME_INDEX];
				String attrCode = flds[DatabaseMetadata.INDICATOR_CODE_INDEX];

				float minVal = Float.POSITIVE_INFINITY;
				float maxVal = Float.NEGATIVE_INFINITY;
				TFloatArrayList vals = new TFloatArrayList();
				ArrayList<String> timeVals = new ArrayList<String>();
				
				for (int col = 4; col < flds.length; col++) {
					if (flds[col] == null || flds[col].trim().length() == 0)
						continue;
					float val = Float.parseFloat(flds[col]);
					minVal = Math.min(minVal, val);
					maxVal = Math.max(maxVal, val);
					vals.add(val);
					timeVals.add(cols[col]);
				}
				if (Float.isInfinite(minVal) || Float.isInfinite(maxVal)) {
					continue;
				}

				String attrNameNU[] = new String[1];
				String unit = extractUnits(attrName, attrNameNU);
				// if (!selectedAttrs.contains(attrNameNU[0].toLowerCase()))
				// break;
				printRecord(null, entityName, attrNameNU[0],
						unitMap.get(unit.trim()), vals, minVal, maxVal, range,
						timeVals);
				// if (vals.size() > 2) qproc.testIntervals(vals,minVal,maxVal);
			}
		}
		System.out.println("</worldbank-sample-queries>");
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		WorldBankQueries.genericWorldBankDataParser();
	}

	static void printRecord(String id, String entityName, String attrName,
			String unit, TFloatArrayList vals, float minVal, float maxVal,
			boolean range) {
		printRecord(id, entityName, attrName, unit, vals, minVal, maxVal,
				range, null);
	}

	static void printRecord(String id, String entityName, String attrName,
			String unit, TFloatArrayList vals, float minVal, float maxVal,
			boolean range, ArrayList<String> timeVals) {

		System.out.println("<quantityquery"
				+ (id == null ? "" : " id=\"" + id + "\"") + "> <queryString>"
				+ entityName.trim() + ";" + attrName.trim()
				+ "</queryString><answerSet>");
		if (vals != null && timeVals == null) {
			vals.sort();
		}
		if (vals == null || (range && vals.size() > 2)) {
			System.out
					.println("<string>" + minVal + "-" + maxVal + "</string>");
		} else {
			for (int v = 0; v < vals.size(); v++) {
				if (v > 0 && vals.get(v) == vals.get(v - 1))
					continue;
				System.out.println("<string>" + vals.get(v) + "</string>");
			}
		}
		if (vals != null && vals.size() > 0) {
			assert (timeVals == null || timeVals.size() == vals.size());
			for (int v = 0; v < vals.size(); v++) {
				if (timeVals == null && v > 0 && vals.get(v) == vals.get(v - 1))
					continue;
				System.out.println("<value"
						+ (timeVals != null ? " time=\"" + timeVals.get(v)
								+ "\"" : "") + ">" + vals.get(v) + "</value>");
			}
		}
		System.out.println("</answerSet><unit>" + (unit == null ? "" : unit)
				+ "</unit></quantityquery>");
	}

	public static void printRecord(String entityName, String attrName,
			String unit, String val) {
		TFloatArrayList vals = new TFloatArrayList();
		vals.add(Float.parseFloat(val));
		printRecord(null, entityName, attrName, unit, vals, 0, 0, false);
	}

}
