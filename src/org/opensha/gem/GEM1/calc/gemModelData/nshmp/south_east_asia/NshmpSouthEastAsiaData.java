package org.opensha.gem.GEM1.calc.gemModelData.nshmp.south_east_asia;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.gem.GEM1.calc.gemModelParsers.GemFileParser;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;

public class NshmpSouthEastAsiaData extends GemFileParser{
	
	public NshmpSouthEastAsiaData(double latmin, double latmax, double lonmin, double lonmax) throws IOException{
		
		srcDataList = new ArrayList<GEMSourceData>();
		
		NshmpSouthEastAsiaFaultData fault = new NshmpSouthEastAsiaFaultData(latmin, latmax, lonmin, lonmax);
		
		NshmpSouthEastAsiaGridData grid = new NshmpSouthEastAsiaGridData(latmin, latmax, lonmin, lonmax);
		
		NshmpSouthEastAsiaSubductionData sub = new NshmpSouthEastAsiaSubductionData(latmin, latmax, lonmin, lonmax);
		
		srcDataList.addAll(fault.getList());
		srcDataList.addAll(grid.getList());
		srcDataList.addAll(sub.getList());
		
	}
	
	// for testing
	public static void main(String[] args) throws IOException{
		
		NshmpSouthEastAsiaData model = new NshmpSouthEastAsiaData(-90.0,+90.0,-180.0,+180.0);
		
	}

}
