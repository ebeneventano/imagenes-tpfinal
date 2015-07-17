package ar.edu.untref.imagenes.tpfinal;

import java.io.File;

import javax.swing.JOptionPane;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.model.fit.RANSAC;

public class Sift {

	public static void aplicar(File img1, File img2) throws Exception {
		MBFImage query = ImageUtilities.readMBF(img1);
		MBFImage target = ImageUtilities.readMBF(img2);

		DoGSIFTEngine engine = new DoGSIFTEngine();

		LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
		LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

		LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<Keypoint>(75);
		matcher.setModelFeatures(queryKeypoints);
		matcher.findMatches(targetKeypoints);

		RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(
				5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5));
		matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				new FastBasicKeypointMatcher<Keypoint>(8), modelFitter);

		matcher.setModelFeatures(queryKeypoints);
		matcher.findMatches(targetKeypoints);

		MBFImage consistentMatches = MatchingUtilities.drawMatches(query,
				target, matcher.getMatches(), RGBColour.BLUE);

		JOptionPane.showMessageDialog(null,
				" Coincidencias entre descriptores: " + String.valueOf(matcher.getMatches().size()));

		DisplayUtilities.display(consistentMatches);
	}

}