package mpi.eudico.client.annotator.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import mpi.eudico.client.annotator.interannotator.CompareConstants;
import mpi.eudico.client.annotator.interannotator.CompareUnit;
import mpi.eudico.client.annotator.interannotator.multi.AnnotatorCompareUtilMulti;
import mpi.eudico.client.annotator.interannotator.multi.CompareCombiMulti;
import mpi.eudico.client.annotator.interannotator.multi.MatchCluster;
import mpi.eudico.client.annotator.interannotator.multi.MultiMatrix;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * An implementation of interrater reliability calculation for two or more 
 * annotators, based on the formula for Fleiss' kappa.
 * As with the implementation of the modified Cohen's kappa this is a modified
 * version of the algorithm; in case the raters have to identify and create the
 * segments as well as to apply a category or code, a matching algorithm 
 * determines which annotations of different annotators "match" and are 
 * considered to be the same item or "subject". Consequently some "subjects"
 * may not be rated by all raters but the Fleiss algorithm does not permit that.
 * To overcome this problem, the incomplete subjects are discarded (removed from
 * the set) or an {@code Unmatched} or {@code Void} category are added.
 * 
 * @author Han Sloetjes
 *
 * @see <a href="https://en.wikipedia.org/wiki/Fleiss'_kappa">https://en.wikipedia.org/wiki/Fleiss'_kappa</a>  
 */
public class CompareAnnotationMultiRaterCommand extends AbstractCompareCommand {
	// the average overlap percentage, default is 60%
	private double minimalOverlapPercentage = 0.6d;
	//private boolean perTierPairOutput;
	//private boolean groupTiersWithSharedCV = true;
	private boolean outputTables = true;
	
    private final String NL = "\n";
    private final String NL2 = "\n\n";
    //private final String TAB = "\t";
    private final DecimalFormat decFormat = new DecimalFormat("#0.0000",
            new DecimalFormatSymbols(Locale.US));
    
    private List<List<MatchCluster>> clusterLists;
    private List<MultiMatrix> matrixList;
	
    /**
     * Constructor.
     * 
     * @param theName the name of the command
     */
	public CompareAnnotationMultiRaterCommand(String theName) {
		super(theName);
	}


	/**
	 * Creates clusters of matching annotations and creates matrices for these
	 * clusters.
	 * If export of matching tiers has been requested, this is done immediately
	 * after creation of the compare combi's.
	 */
	@Override
	protected void calculateAgreement() {
		// in the super class checks have been performed and combinations of tiers have been
		// created. Start the calculations right away.
		if (compareSegments.size() == 0) {
			logErrorAndInterrupt("There are no tier groups, nothing to calculate.");
			return;
		}
		
		Object prefObject = compareProperties.get(CompareConstants.OVERLAP_AVERAGE);
		if (prefObject instanceof Double) {
			minimalOverlapPercentage = (Double) prefObject;
		} else if (prefObject instanceof Integer) {
			// value as an integer between 1 and 100
			minimalOverlapPercentage = ((Integer) prefObject) / 100d;
		}
		
		Object prefTablesObject = compareProperties.get(CompareConstants.OUTPUT_TABLES_VALUES);
		if (prefTablesObject instanceof Boolean) {
			outputTables = (Boolean) prefTablesObject;
		}
		// additional and optional tier output
		boolean exportMatchingTiers = false;
		//boolean exportTiersPerTierSet = true;// only option at the moment
		String exportTierFolder = null;
		Object prefTierExport = compareProperties.get(CompareConstants.EXPORT_MATCHING_TIERS_KEY);
		if (prefTierExport instanceof Boolean) {
			exportMatchingTiers = (Boolean) prefTierExport;
			if (exportMatchingTiers) {
				Object prefFolder = compareProperties.get(CompareConstants.EXPORT_FOLDER_KEY);
				if (prefFolder instanceof String) {
					exportTierFolder = (String) prefFolder;
					exportTierFolder = exportTierFolder.replace('\\', '/');
					if (!ensureBaseFolder(exportTierFolder)) {
						exportMatchingTiers = false;
						LOG.warning("Cannot export matching tiers, the output folder does not exist and cannot be created");
					}
				} else {
					exportMatchingTiers = false;
					LOG.warning("Cannot export matching tiers, no output folder specified");
				}
			}
		}
		
		// starting at an arbitrary 30%
		float perCombi = (50f / 2) / compareSegments.size();
		clusterLists = new ArrayList<List<MatchCluster>>(compareSegments.size());
		AnnotatorCompareUtilMulti compareUtil = new AnnotatorCompareUtilMulti();
		compareUtil.setAvgRatioThreshold(minimalOverlapPercentage);
		progressUpdate((int) curProgress, "Matching annotations from multiple raters...");
		
		for (int i = 0; i < compareSegments.size(); i++) {
			clusterLists.add(compareUtil.matchAnnotationsMulti(
					(CompareCombiMulti) compareSegments.get(i)));
			// at this stage or later?
			if (exportMatchingTiers) {
				writeCCtoEAF((CompareCombiMulti) compareSegments.get(i), i, exportTierFolder);
			}
			
			curProgress += perCombi;
			progressUpdate((int) curProgress, null);			
		}
		progressUpdate((int) curProgress, "Creating matrices for multiple raters...");
		// create matrices, per combination and for grouped combinations based on shared CV
		matrixList = new ArrayList<MultiMatrix>(compareSegments.size());
		for (int i = 0; i < compareSegments.size(); i++) {
			matrixList.add(new MultiMatrix((CompareCombiMulti) compareSegments.get(i), clusterLists.get(i)));
			
			curProgress += perCombi;
			progressUpdate((int) curProgress, null);
		}
		// ready to print, set progress
		
		progressComplete(String.format("Completed calculations of %d groups of tiers.", matrixList.size()));
	}

	/**
	 * Writes the results to a text file.
	 * 
	 * @param toFile the file to write to
	 * @param encoding the encoding to use for the file
	 * 
	 * @throws IOException any IO related exception
	 */
	@Override
	public void writeResultsAsText(File toFile, String encoding) throws IOException {
		if (toFile == null) {
			throw new IOException("There is no file location specified.");
		}
		BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(toFile);
            OutputStreamWriter osw = null;

            try {
                osw = new OutputStreamWriter(out, encoding);
            } catch (UnsupportedCharsetException uce) {
                osw = new OutputStreamWriter(out, "UTF-8");
            }

            writer = new BufferedWriter(osw);
            
            // write "header" date and time
            writer.write(String.format("Output created: %tD %<tT",  Calendar.getInstance()));
            writer.write(NL2);
            // some explanation
            // something about the matching and about Fleiss' kappa
            writer.write("Calculating Fleiss' kappa per group of matching tiers.");
        	writer.write(NL);
        	writer.write("k = (P - Pe) / (1 - Pe), see https://en.wikipedia.org/wiki/Fleiss'_kappa");
        	writer.write(NL);
        	
            writer.write("Number of files involved: ");
            if (transcription != null && compareProperties.get(CompareConstants.TIER_SOURCE_KEY) == CompareConstants.FILE_MATCHING.CURRENT_DOC) {
            	writer.write("1 (current transcription)");
            } else {
            	writer.write(String.valueOf(numFiles));
            }
            writer.write(NL);
            // number of tiers pairs
            writer.write("Number of selected tiers: " + numSelTiers);
            writer.write(NL);
//            writer.write("Number of pairs of tiers in the comparison: " + clusterLists.size());
//            writer.write(NL);
//            
            writer.write("Preferred average overlap percentage: " + String.valueOf(Math.round(minimalOverlapPercentage * 100)) + "%");
            writer.write(NL2);
            
            // per tier group output
            
            for (int i = 0; i < matrixList.size() && i < clusterLists.size(); i++) {
            	writer.write(String.format("Comparison cluster: ", i));
            	writer.write(NL);
            	CompareCombiMulti ccm = (CompareCombiMulti) compareSegments.get(i);
            	
            	int numEmptyTiers = 0;
            	for (CompareUnit cu : ccm.getCompareUnits()) {
            		if (cu.annotations.size() == 0) {
            			numEmptyTiers++;
            		}
            	}

            	writer.write(String.format("Number of tiers in this cluster: %d (%d without annotations)", 
            			ccm.getCompareUnits().size(), numEmptyTiers));
            	writer.write(NL);
            	
            	for (int j = 0; j < ccm.getCompareUnits().size(); j++) {
            		CompareUnit cu = ccm.getCompareUnit(j);
            		writer.write(String.format("File: %s, Tier %d: %s, #Annotations: %d", 
            				cu.fileName, j, cu.tierName, cu.annotations.size()));
            		writer.write(NL);
            	}
            	
            	List<MatchCluster> mcList = clusterLists.get(i);
            	MultiMatrix mm = matrixList.get(i);
            	if (mcList == null) {
            		writer.write("There are no clusters of matching annotations for this combination.");
            		writer.write(NL2);
            		continue;
            	}
            	
            	if (mm == null) {
            		writer.write("There's no matrix as the basis for the calculation of the kappa value.");
            		writer.write(NL2);
            		continue;
            	}
            	
            	writer.write(String.format("Kappa including \"Unmatched\" value: k = (%s - %s) / (1.0 - %s) = %s", 
            			decFormat.format(mm.getAvgPIU()), decFormat.format(mm.getPeIU()), 
            			decFormat.format(mm.getPeIU()), decFormat.format(mm.getKappaIU())));
            	if (mm.getPeIU() == 1) {
            		writer.write(" (k = 1.0)");
            	}
            	writer.write(NL);
            	writer.write(String.format("Kappa excluding \"Unmatched\" value:  k = (%s - %s) / (1.0 - %s) = %s", 
            			decFormat.format(mm.getAvgPEU()), decFormat.format(mm.getPeEU()), 
            			decFormat.format(mm.getPeEU()), decFormat.format(mm.getKappaEU())));
            	if (mm.getPeEU() == 1) {
            		writer.write(" (k = 1.0)");
            	}
            	writer.write(NL);
            	if (outputTables) {
            		mm.printMatrix(writer, decFormat, true);
            		writer.write(NL);
            		mm.printMatrix(writer, decFormat, false);
            		writer.write(NL);
            	}
            }
            writer.flush();
        } catch (Throwable tr) {
            // FileNotFound, Security or UnsupportedEncoding exceptions
            throw new IOException("Cannot write to file: " + tr.getMessage());
        } finally {
        	if (writer != null) {
	        	try {
	        		writer.close();
	        	} catch (Throwable t){
	        		
	        	}
        	}
        }
	}

	/** 
	 * Temporary implementation of saving combinations of matching tiers
	 * from different files in a new transcription. 
	 * Currently uses the first tier name and first file name to construct a 
	 * destination path.
	 * Possible improvement would be to remove affixes from tier and 
	 * transcription names and use that as the basis for export tier and file
	 * names.
	 */
	private void writeCCtoEAF(CompareCombiMulti ccm, int index, String folderPath) {
		try {
			TranscriptionImpl tr = new TranscriptionImpl();
			LinguisticType lt = new LinguisticType("compare");
			tr.addLinguisticType(lt);
			String trName = null;
			String tierName = null;
			
			for (int i = 0; i < ccm.getCompareUnits().size(); i++) {
				CompareUnit cu = ccm.getCompareUnit(i);
				if (i == 0) {
					trName = cu.fileName;
					tierName = cu.tierName;
				}
				TierImpl nt = null;
				if (tr.getTierWithId(cu.tierName) == null) {
					nt = new TierImpl(cu.tierName, cu.annotator, tr, lt);
				} else {
					nt = new TierImpl(cu.tierName + "-" + i, cu.annotator, tr, lt);
				}
				tr.addTier(nt);
				
				for (AnnotationCore ac : cu.annotations) {
					AbstractAnnotation aa = (AbstractAnnotation) 
							nt.createAnnotation(ac.getBeginTimeBoundary(), ac.getEndTimeBoundary());
					if (aa != null) {
						aa.setValue(ac.getValue());
						nt.addAnnotation(aa);
					}			
				}
			}
			
			if (trName != null) {
				if (trName.indexOf("/") > 0) {
					trName = trName.substring(trName.lastIndexOf("/") + 1);
				}
			}
			String path = folderPath + "/ccm-" + index + ".eaf";
			if (trName != null) {
				String dir = folderPath + "/" + tierName;
				File dirFile = new File(dir);
				if (!dirFile.exists()) {
					dirFile.mkdir();
				}
				path = dir + "/" + trName;
			}
			tr.setPathName(path);
				ACMTranscriptionStore.getCurrentTranscriptionStore().storeTranscriptionIn(
					tr, null, null, path, TranscriptionStore.EAF);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * Checks if a folder exists and tries to create it if not.
	 * 
	 * @param exportFolderString the folder path
	 * 
	 * @return {@code true} if it existed or could be created
	 */
	private boolean ensureBaseFolder(String exportFolderString) {
		try {
			File f = new File(exportFolderString);
			if (f.exists()) {
				return f.canWrite();
			} else {
				// try to create
				return f.mkdirs();
			}
			
		} catch (Throwable t) {// e.g. security exception
			return false;
		}
	}
}
