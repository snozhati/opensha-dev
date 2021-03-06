package scratch.kevin.simulators.plots;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.jfree.data.Range;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

public class MagAreaScalingPlot extends AbstractPlot {
	
	private DefaultXY_DataSet scatter;
	
	private static final int max_scatter_points = 1000000;
	
	public MagAreaScalingPlot() {
		scatter = new DefaultXY_DataSet();
	}

	@Override
	protected void doProcessEvent(SimulatorEvent e) {
		double mag = e.getMagnitude();
		if (!Doubles.isFinite(mag))
			return;
		double area = e.getArea(); // m^2
		area /= 1e6;
		
		scatter.set(area, mag);
	}

	@Override
	public void finalizePlot() throws IOException {
		writeScatterPlots(scatter, getCatalogName(), getOutputDir(), getOutputPrefix(), getPlotWidth(), getPlotHeight());
	}
	
	public static void writeScatterPlots(XY_DataSet scatter, String name, File outputDir, String prefix,
			int plotWidth, int plotHeight) throws IOException {
		// scatter plot
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		XY_DataSet plotScatter = scatter;
		if (scatter.size() > max_scatter_points) {
			System.out.println("Filtering M-A scatter from "+scatter.size()+" to ~"+max_scatter_points+" points");
//			plotScatter = new DefaultXY_DataSet();
//			Random r = new Random();
//			for (int i=0; i<max_scatter_points; i++)
//				plotScatter.set(scatter.get(r.nextInt(scatter.size())));
			plotScatter = downsampleByMag(scatter, false, max_scatter_points);
			System.out.println("Filter done (random mag-dependent sample): "+plotScatter.size());
		}
		funcs.add(plotScatter);
		plotScatter.setName(name);
		chars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.BLACK));
		
		System.out.println("Scatter mag range: "+scatter.getMinY()+" "+scatter.getMaxY());
		
		WC1994_MagAreaRelationship wc = new WC1994_MagAreaRelationship();
		EvenlyDiscretizedFunc wcFunc = new EvenlyDiscretizedFunc(scatter.getMinX(), scatter.getMaxX(), 1000);
		for (int i=0; i<wcFunc.size(); i++)
			wcFunc.set(i, wc.getMedianMag(wcFunc.getX(i)));
		wcFunc.setName("W-C 1994");
		funcs.add(wcFunc);
		PlotCurveCharacterstics wcChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.RED.darker());
		chars.add(wcChar);
		
		Ellsworth_B_WG02_MagAreaRel elb = new Ellsworth_B_WG02_MagAreaRel();
		EvenlyDiscretizedFunc elbFunc = new EvenlyDiscretizedFunc(scatter.getMinX(), scatter.getMaxX(), 1000);
		for (int i=0; i<elbFunc.size(); i++)
			elbFunc.set(i, elb.getMedianMag(elbFunc.getX(i)));
		elbFunc.setName("EllsworthB");
		funcs.add(elbFunc);
		PlotCurveCharacterstics elbChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLUE.darker());
		chars.add(elbChar);
		
		HanksBakun2002_MagAreaRel hb = new HanksBakun2002_MagAreaRel();
		EvenlyDiscretizedFunc hbFunc = new EvenlyDiscretizedFunc(scatter.getMinX(), scatter.getMaxX(), 1000);
		for (int i=0; i<hbFunc.size(); i++)
			hbFunc.set(i, hb.getMedianMag(hbFunc.getX(i)));
		hbFunc.setName("H-B 2002");
		funcs.add(hbFunc);
		PlotCurveCharacterstics hbChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.GREEN.darker());
		chars.add(hbChar);
		
		String title = "Mag-Area Scaling";
		String xAxisLabel = "Area (km^2)";
		String yAxisLabel = "Magnitude";
		
		PlotSpec plot = new PlotSpec(funcs, chars, title, xAxisLabel, yAxisLabel);
		plot.setLegendVisible(true);
		
		double minY = Math.floor(Math.min(scatter.getMinY(), wcFunc.getMinY()));
		double maxY = Math.ceil(Math.max(scatter.getMaxY(), wcFunc.getMaxY()));
		Range yRange = new Range(minY, maxY);
		Range xRange = calcEncompassingLog10Range(Math.min(scatter.getMinX(), wcFunc.getMinX()),
				Math.max(scatter.getMaxX(), wcFunc.getMaxX()));
		
		HeadlessGraphPanel gp = buildGraphPanel();
		gp.drawGraphPanel(plot, true, false, null, yRange);
		gp.getChartPanel().setSize(plotWidth, plotHeight);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		
		int nx = 51;
		int ny = 51;
		
		double logMinX = Math.log10(xRange.getLowerBound());
		double logMaxX = Math.log10(xRange.getUpperBound());
		double gridSpacingX = (logMaxX - logMinX)/(nx-1);
		
		System.out.println("XYZ logMinX="+logMinX+", logMaxX="+logMaxX+", spacing="+gridSpacingX);
		
		double gridSpacingY = (maxY - minY)/(ny-1);
		
		// XYZ plot (2D hist)
		EvenlyDiscrXYZ_DataSet xyz = new EvenlyDiscrXYZ_DataSet(nx, ny, logMinX, minY, gridSpacingX, gridSpacingY);
		
		for (Point2D pt : scatter) {
			int index = xyz.indexOf(Math.log10(pt.getX()), pt.getY());
			if (index < 0 || index >= xyz.size())
				throw new IllegalStateException("Scatter point not in XYZ range. x: "
							+pt.getX()+" ["+xyz.getMinX()+" "+xyz.getMaxX()
						+"], y: "+pt.getY()+" ["+xyz.getMinY()+" "+xyz.getMaxY()+"]");
			xyz.set(index, xyz.get(index)+1);
		}
		// convert to density
		for (int i=0; i<xyz.size(); i++) {
			// convert to density
			Point2D pt = xyz.getPoint(i);
			double x = pt.getX();
			double binWidth = Math.pow(10, x + 0.5*gridSpacingX) - Math.pow(10, x - 0.5*gridSpacingX);
			double binHeight = gridSpacingY;
			double area = binWidth * binHeight;
			xyz.set(i, xyz.get(i)*area);
		}
		xyz.scale(1d/xyz.getSumZ());
		
		// set all zero to NaN so that it will plot white
		for (int i=0; i<xyz.size(); i++) {
			if (xyz.get(i) == 0)
				xyz.set(i, Double.NaN);
		}
		xyz.log10();
		
		double minZ = Double.POSITIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;
		for (int i=0; i<xyz.size(); i++) {
			double val = xyz.get(i);
			if (!Doubles.isFinite(val))
				continue;
			if (val < minZ)
				minZ = val;
			if (val > maxZ)
				maxZ = val;
		}
		
		System.out.println("MinZ: "+minZ);
		System.out.println("MaxZ: "+maxZ);
		
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
		if ((float)minZ == (float)maxZ)
			cpt = cpt.rescale(minZ, minZ*2);
		else if (!Doubles.isFinite(minZ))
			cpt = cpt.rescale(0d, 1d);
		else
			cpt = cpt.rescale(minZ, maxZ);
		cpt.setNanColor(Color.WHITE);
		
		String zAxisLabel = "Log10(Density)";
		
		XYZPlotSpec xyzSpec = new XYZPlotSpec(xyz, cpt, title, "Log10 "+xAxisLabel, yAxisLabel, zAxisLabel);
		// add W-C
		funcs = Lists.newArrayList();
		chars = Lists.newArrayList();
		ArbitrarilyDiscretizedFunc logWCFunc = new ArbitrarilyDiscretizedFunc();
		for (Point2D pt : wcFunc)
			logWCFunc.set(Math.log10(pt.getX()), pt.getY());
		funcs.add(logWCFunc);
		chars.add(wcChar);
		// add EllB
		ArbitrarilyDiscretizedFunc logEllBFunc = new ArbitrarilyDiscretizedFunc();
		for (Point2D pt : elbFunc)
			logEllBFunc.set(Math.log10(pt.getX()), pt.getY());
		funcs.add(logEllBFunc);
		chars.add(elbChar);
		// add HB
		ArbitrarilyDiscretizedFunc logHBFunc = new ArbitrarilyDiscretizedFunc();
		for (Point2D pt : hbFunc)
			logHBFunc.set(Math.log10(pt.getX()), pt.getY());
		funcs.add(logHBFunc);
		chars.add(hbChar);
		xyzSpec.setXYElems(funcs);
		xyzSpec.setXYChars(chars);
		
		XYZGraphPanel xyzGP = buildXYZGraphPanel();
		xyzGP.drawPlot(xyzSpec, false, false, new Range(logMinX, logMaxX), yRange);
		// write plot
		xyzGP.getChartPanel().setSize(plotWidth, plotHeight);
		xyzGP.saveAsPNG(new File(outputDir, prefix+"_hist2D.png").getAbsolutePath());
		xyzGP.saveAsPDF(new File(outputDir, prefix+"_hist2D.pdf").getAbsolutePath());
	}

	@Override
	public Collection<SimulatorElement> getApplicableElements() {
		return null;
	}

}
