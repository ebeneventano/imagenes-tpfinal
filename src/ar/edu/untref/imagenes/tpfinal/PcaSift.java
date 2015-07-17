package ar.edu.untref.imagenes.tpfinal;

import java.io.File;

import javax.swing.JOptionPane;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.FloatKeypoint;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.array.ArrayUtils;

public class PcaSift {

	public static void aplicar(File img1, File img2) throws Exception {
		MBFImage query = ImageUtilities.readMBF(img1);
		MBFImage target = ImageUtilities.readMBF(img2);

		DoGSIFTEngine engine = new DoGSIFTEngine();

		LocalFeatureList<FloatKeypoint> queryKeypoints = FloatKeypoint.convert(engine.findFeatures(query.flatten()));
		LocalFeatureList<FloatKeypoint> targetKeypoints = FloatKeypoint.convert(engine.findFeatures(target.flatten()));
		
		final double[][] queryReduce128 = new double[queryKeypoints.size() -1][];
		for (int i = 0; i < queryReduce128.length; i++) {
		    queryReduce128[i] = ArrayUtils.convertToDouble(queryKeypoints.get(i).vector);
		}
		
		final double[][] targetReduce128 = new double[targetKeypoints.size() -1][];
		for (int i = 0; i < targetReduce128.length; i++) {
			targetReduce128[i] = ArrayUtils.convertToDouble(targetKeypoints.get(i).vector);
		}
		
		final ThinSvdPrincipalComponentAnalysis pca = new ThinSvdPrincipalComponentAnalysis(64);
	    pca.learnBasis(queryReduce128);

	    for (final FloatKeypoint kp : queryKeypoints) {
	         kp.vector = ArrayUtils.convertToFloat(pca.project(ArrayUtils.convertToDouble(kp.vector)));
	    }
	    
	    pca.learnBasis(targetReduce128);

	    for (final FloatKeypoint kp : targetKeypoints) {
	         kp.vector = ArrayUtils.convertToFloat(pca.project(ArrayUtils.convertToDouble(kp.vector)));
	    }
	    
		LocalFeatureMatcher<FloatKeypoint> matcher = new BasicMatcher<FloatKeypoint>(70);
		matcher.setModelFeatures(queryKeypoints);
		matcher.findMatches(targetKeypoints);

		RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(
				5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5));
		matcher = new ConsistentLocalFeatureMatcher2d<FloatKeypoint>(
				new BasicMatcher<FloatKeypoint>(70), modelFitter);
		
		matcher.setModelFeatures(queryKeypoints);
		matcher.findMatches(targetKeypoints);

		MBFImage consistentMatches = MatchingUtilities.drawMatches(query,
				target, matcher.getMatches(), RGBColour.BLUE);

		JOptionPane.showMessageDialog(null,
				" Coincidencias entre descriptores: " + String.valueOf(matcher.getMatches().size()));

		DisplayUtilities.display(consistentMatches);
	}
	
}
