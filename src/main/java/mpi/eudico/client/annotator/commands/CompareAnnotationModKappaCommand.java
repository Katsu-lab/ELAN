package mpi.eudico.client.annotator.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import mpi.eudico.client.annotator.interannotator.AnnotatorCompareUtil2;
import mpi.eudico.client.annotator.interannotator.CompareCombi;
import mpi.eudico.client.annotator.interannotator.CompareConstants;
import mpi.eudico.client.annotator.interannotator.modkappa.AgreementTable;
import mpi.eudico.client.annotator.interannotator.modkappa.CKappa;
import mpi.eudico.client.annotator.interannotator.modkappa.GlobalCKappa;
import mpi.eudico.client.annotator.interannotator.modkappa.KappaCompareCombi;
import mpi.eudico.client.annotator.interannotator.modkappa.TwoSquareTable;
import mpi.eudico.server.corpora.clom.AnnotationCore;

/**
 * A command that compares the annotations of two tiers by applying the 
 * Holle/Rein modified kappa algorithm.  
 * {@code
 * Holle, H., & Rein, R. (2014). EasyDIAg: A tool for easy determination of interrater agreement. 
 * Behavior Research Methods, published online August 2014. 
 * doi:10.3758/s13428-014-0506-7
 * }
 * 
 * @version 2 Sept 2015, writing of NaN instead of 0 where applicable, 
 * for negative kappa values write 0 instead of 0.0000
 */
public class CompareAnnotationModKappaCommand extends AbstractCompareCommand {
	List<KappaCompareCombi> modCompareCombis;
	private Map<String, KappaCompareCombi> tierCombisGlobal;
	private KappaCompareCombi globalCombi;
	// should be 51% or more, default is 60%
	private double minimalOverlapPercentage = 0.6d;
	private boolean perTierPairOutput;
	private final String NAN = "NaN";
	private final String EMPTY = "\"empty\"";
	private final String ZERO = "0";
    private final String NL = "\n";
    private final String NL2 = "\n\n";
    private final String TAB = "\t";
    private final DecimalFormat decFormat = new DecimalFormat("#0.0000",
            new DecimalFormatSymbols(Locale.US));
    
	/**
	 * Constructor.
	 * 
	 * @param theName name of the command
	 */
	public CompareAnnotationModKappaCommand(String theName) {
		super(theName);
	}

	/**
	 * Override by returning a list of the extended KappaCompareCombi objects
	 * 
	 */
	@Override
	public List<CompareCombi> getCompareSegments() {
		if (modCompareCombis != null) {
			return new ArrayList<CompareCombi>(modCompareCombis);
		}
		return super.getCompareSegments();
	}

	/**
	 * The actual calculation, applies the algorithm to the compare combinations and
	 * stores intermediate results per annotation (= coding) value.
	 */
	@Override
	protected void calculateAgreement() {
		// in the super class checks have been performed and combinations of tiers have been
		// created. Start the calculations right away.
		if (compareSegments.size() == 0) {
			logErrorAndInterrupt("There are no tier pairs, nothing to calculate.");
			return;
		}

		Object prefOPObject = compareProperties.get(CompareConstants.OVERLAP_PERCENTAGE);
		if (prefOPObject instanceof Double) {
			minimalOverlapPercentage = (Double) prefOPObject;
		} else if (prefOPObject instanceof Integer) {
			// value as an integer between 51 and 100
			minimalOverlapPercentage = ((Integer) prefOPObject) / 100d;
		}
		perTierPairOutput = false;
		Object prefPerTPObject = compareProperties.get(CompareConstants.OUTPUT_PER_TIER_PAIR);
		if (prefPerTPObject instanceof Boolean) {
			perTierPairOutput = (Boolean) prefPerTPObject;
		}
		
		//int combiCount = 0;
		modCompareCombis = new ArrayList<KappaCompareCombi>();
		// starting at an arbitrary 30%
		float perCombi = 50f / compareSegments.size();
		AnnotatorCompareUtil2 compareUtil = new AnnotatorCompareUtil2();
		progressUpdate((int) curProgress, "Building agreement tables, per file...");
		
		Map<AnnotationCore, AnnotationCore> curMatchedAnnotations;
		KappaCompareCombi curKappaCC;
		
		for (CompareCombi cc : compareSegments) {
			curMatchedAnnotations = compareUtil.matchAnnotations(cc, minimalOverlapPercentage);
			curKappaCC = calculateOverallMatrix(cc, curMatchedAnnotations);
			// if all matched values should be counted twice than multiply by two here. 
			// follows example of Holle&Rein and GSEQ-DP
			curKappaCC.doubleMatchedValues();
			// create the per value agreement tables
			calculatePerValueAgreementTables(curKappaCC);
			
			modCompareCombis.add(curKappaCC);
			
			curProgress += perCombi;
			progressUpdate((int) curProgress, null);
		}
		
		progressUpdate((int) curProgress, "Starting global agreement calculations...");
		
		// collect all labels/codings (also per tier set, if required) 
		SortedSet<String> allValuesGlobal = new TreeSet<String>();
		SortedMap<String, TreeSet<String>> perTierCombiValuesGlobal = new TreeMap<String, TreeSet<String>>();
		
		for (KappaCompareCombi kcc : modCompareCombis) {
			allValuesGlobal.addAll(kcc.getAllValues());// includes UNMATCHED
			String tierCombi = kcc.getFirstUnit().tierName + ":" + kcc.getSecondUnit().tierName;
			TreeSet<String> combiValues = perTierCombiValuesGlobal.get(tierCombi);
			if (combiValues == null) {
				combiValues = new TreeSet<String>();
				perTierCombiValuesGlobal.put(tierCombi, combiValues);
			}
			combiValues.addAll(kcc.getAllValues());
		}
		
		List<String> globalValuesList = new ArrayList<String>(allValuesGlobal);
		if (globalValuesList.remove(CompareConstants.UNMATCHED)) {
			globalValuesList.add(CompareConstants.UNMATCHED);// move to the end of the list
		}
		
		AgreementTable globalTable = new AgreementTable("Multi file global", globalValuesList);
		
		// re-iterate the kappa combis
		for (KappaCompareCombi kcc : modCompareCombis) {
			mergeGlobalAgreementTables(globalTable, kcc.getOverallTable());
		}

		// calculate kappa agreement, raw agreement, ..., ipf kappa?
		CKappa ckappa = new CKappa();
		globalCombi = new KappaCompareCombi(null, null);
		globalCombi.setAllValues(globalValuesList);
		globalCombi.setOverallTable(globalTable);
		// create global per value agreement table based on global matrix
		calculatePerValueAgreementTables(globalCombi);
		
		Map<String, TwoSquareTable> globalPerValueTables = globalCombi.getPerValueTables();		
		Iterator<String> perValueIter = globalPerValueTables.keySet().iterator();
		while (perValueIter.hasNext()) {
			String keyIter = perValueIter.next();
			TwoSquareTable iterTable = globalPerValueTables.get(keyIter);

			globalCombi.getPerValueAgreement().put(keyIter, ckappa.calcKappa(iterTable.getTable()));
		}
		// repeat the steps for the global agreement for global per tier combination agreement
		if (perTierPairOutput) {
			tierCombisGlobal = new TreeMap<String, KappaCompareCombi>();
			SortedMap<String, AgreementTable> perTierAgreeTableGlobal = new TreeMap<String, AgreementTable>();
			
			Iterator<String> tcIter = perTierCombiValuesGlobal.keySet().iterator();
			while (tcIter.hasNext()) {
				String key = tcIter.next();
				List<String> valList = new ArrayList<String>(perTierCombiValuesGlobal.get(key));
				if(valList.remove(CompareConstants.UNMATCHED)) {
					valList.add(CompareConstants.UNMATCHED);
				}
				
				perTierAgreeTableGlobal.put(key, new AgreementTable(key, valList));
			}
			
			// re-iterate the kappa combis
			for (KappaCompareCombi kcc : modCompareCombis) {
				String tierCombi = kcc.getFirstUnit().tierName + ":" + kcc.getSecondUnit().tierName;
				AgreementTable at = perTierAgreeTableGlobal.get(tierCombi);
				
				mergeGlobalAgreementTables(at, kcc.getOverallTable());
			}
			
			Iterator<String> taIter = perTierAgreeTableGlobal.keySet().iterator();
			while (taIter.hasNext()) {
				String key = taIter.next();
				AgreementTable at = perTierAgreeTableGlobal.get(key);
				
				KappaCompareCombi perTierKCCombi = new KappaCompareCombi(null, null);
				perTierKCCombi.setAllValues(at.getLabels());
				perTierKCCombi.setOverallTable(at);
				
				calculatePerValueAgreementTables(perTierKCCombi);
				// calculate per value kappa
				Map<String, TwoSquareTable> perTierPerValueTables = perTierKCCombi.getPerValueTables();		
				Iterator<String> ptpvIter = perTierPerValueTables.keySet().iterator();
				while (ptpvIter.hasNext()) {
					String keyIter = ptpvIter.next();
					TwoSquareTable iterTable = perTierPerValueTables.get(keyIter);

					perTierKCCombi.getPerValueAgreement().put(keyIter, ckappa.calcKappa(iterTable.getTable()));
				}
				
				tierCombisGlobal.put(key, perTierKCCombi);
			}
			
		} // endPerTierOutput

		progressComplete(String.format("Completed calculations of %d pairs of tiers.", modCompareCombis.size()));
	}


	@Override
	public void writeResultsAsText(File toFile, String encoding)
			throws IOException {
		if (globalCombi == null || globalCombi.getAllValues().isEmpty()){
			throw new IOException("There are no results to save.");// throw other type of exception
		}
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
            writer.write(NL2); // or writer.newLine();//??
            // some explanation
            writer.write(String.format("%s\t: a kappa value of \"%s\" usually indicates a division by zero", NAN, NAN));
            writer.write(NL);
            writer.write("0\t: a kappa value of \"0\" replaces a value < 0 (negative kappa), as opposed to the value \"0.000\"");
            writer.write(NL2);
            
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
            writer.write("Number of pairs of tiers in the comparison: " + modCompareCombis.size());
            writer.write(NL);
            writer.write("Required minimal overlap percentage: " + String.valueOf(Math.round(minimalOverlapPercentage * 100)) + "%");
            writer.write(NL2);
            
            writer.write("Global results of all files and all tiers:");
            writer.write(NL);
            writeValuesTable(writer, globalCombi);
            writer.write(NL);
            // write the agreement table             
            writer.write("Global kappa values and agreement matrix:");
            writer.write(NL);
            writeAgreementMatrix(writer, globalCombi.getOverallTable());

            writer.write(NL2);
            writer.flush();
            // write per value agreement table
            writer.write("Global per value agreement table:");
            writer.write(NL);
            for (String value : globalCombi.getAllValues()) {
            	if (value.equals(CompareConstants.UNMATCHED)) {
            		continue;
            	}
            	writer.write(value.isEmpty() ? EMPTY : value);
            	writer.write(NL);
            	TwoSquareTable tst = globalCombi.getAgreementTable(value);
            	if (tst != null) {
                	writer.write(String.valueOf(tst.getTable()[0][0]));
                	writer.write(TAB);
                	writer.write(String.valueOf(tst.getTable()[0][1]));
                	writer.write(NL);
                	writer.write(String.valueOf(tst.getTable()[1][0]));
                	writer.write(TAB);
                	writer.write(String.valueOf(tst.getTable()[1][1]));
                	writer.write(NL);
            	} else {
                	writer.write("-");
            	}
            	writer.write(NL);
            	writer.flush();
            }
            // write global per tier pair results
            if (perTierPairOutput) {
            	writeGlobalPerTier(writer);
            } 
            
            writer.write("End of global results.");
            writer.write(NL);
            writer.write("################################################");
            if (perTierPairOutput) {
            	writer.write(NL2);
            	writer.write("Results per individual tier combination: kappa,\tkappa_max,\traw agreement");
            	writer.write(NL);
            	CKappa cKappa = new CKappa();
            	
            	for (KappaCompareCombi kcc : modCompareCombis) {
            		writer.write("File 1: " + kcc.getFirstUnit().fileName + " Tier 1: " + kcc.getFirstUnit().tierName);
            		writer.write(NL);
            		writer.write("File 2: " + kcc.getSecondUnit().fileName + " Tier 2: " + kcc.getSecondUnit().tierName);
            		writer.write(NL);

            		List<String> locLabels = kcc.getAllValues();
            		TwoSquareTable curTable;
            		for (String value : locLabels) {
                     	if (value.equals(CompareConstants.UNMATCHED)) {
                    		continue;
                    	}
            			curTable = kcc.getAgreementTable(value);
                    	writer.write(value.isEmpty() ? EMPTY : value);
                    	writer.write(TAB);
                    	//writer.write(decFormat.format(Math.max(0, cKappa.calcKappa(curTable.getTable()))));
                    	double ck = cKappa.calcKappa(curTable.getTable());
                    	writer.write(Double.isNaN(ck) ? NAN : (ck < 0 ? ZERO : decFormat.format(ck)));
                    	writer.write(TAB);
                    	//writer.write(decFormat.format(Math.max(0, cKappa.calcMaxKappa(curTable.getTable()))));
                    	double mk = cKappa.calcMaxKappa(curTable.getTable());
                    	writer.write(Double.isNaN(mk) ? NAN : (mk < 0 ? ZERO : decFormat.format(mk)));
                    	writer.write(TAB);
                    	//writer.write(decFormat.format(Math.max(0, cKappa.calcRawAgreement(curTable.getTable()))));
                    	double ra = cKappa.calcRawAgreement(curTable.getTable());
                    	writer.write(Double.isNaN(ra) ? NAN : (ra < 0 ? ZERO : decFormat.format(ra)));
                    	writer.write(NL);
            		}
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
	 * If per tier output is requested, this creates compare combinations over
	 * per tier pair over all files. The combinations are stored in a map in
	 * which concatenated tier names serve as the keys (e.g. tier_r1:tier_r2).
	 * 
	 * @param writer the output writer
	 * @throws IOException any IO exception
	 */
	private void writeGlobalPerTier(Writer writer) throws IOException {
		writer.write("======================================================");
		writer.write(NL);
    	writer.write("Overall results per tier combination");
    	writer.write(NL2);
    	
    	Iterator<String> tierCombiIter = tierCombisGlobal.keySet().iterator();
    	while (tierCombiIter.hasNext()) {
    		String tierCombiName = tierCombiIter.next();
    		KappaCompareCombi kcc = tierCombisGlobal.get(tierCombiName);
    		writer.write("Tier combination: " + tierCombiName);
    		writer.write(NL);
    		writer.write("Per value:");
    		writer.write(NL);
    		
    		writeValuesTable(writer, kcc);
    		writer.write(NL);
            // write the agreement table             
            writer.write("Overall kappa values and agreement matrix:");
            writer.write(NL);
            writeAgreementMatrix(writer, kcc.getOverallTable());
            writer.write("------------------------------------------------------");
            writer.write(NL2);
    	}
        writer.flush();
	}
	
	/**
	 * Writes several kappa values per annotation value (i.e. per label, or per
	 * category).
	 * 
	 * @param writer the output writer
	 * @param kcc the compare combination containing the data
	 * @throws IOException any IO exception
	 */
	private void writeValuesTable(Writer writer, KappaCompareCombi kcc) throws IOException {
        List<String> allValues = kcc.getAllValues();
        CKappa cKappa = new CKappa();
        TwoSquareTable curTst;
        
        writer.write("value");
        writer.write(TAB);
        writer.write("kappa");
        writer.write(TAB);
        writer.write("kappa_max");
        writer.write(TAB);
        writer.write("raw agreement");
        writer.write(NL);
        
        for (String value : allValues) {
        	if (value.equals(CompareConstants.UNMATCHED)) {
        		continue;
        	}
        	writer.write(value.isEmpty() ? EMPTY : value);
        	writer.write(TAB);
        	double ck = kcc.getAgreementForValue(value);
        	writer.write(Double.isNaN(ck) ? NAN : (ck < 0 ? ZERO : decFormat.format(ck)));// visually distinguish <0 from 0.0000
        	curTst = kcc.getAgreementTable(value);// if value != null, there will be a table
        	writer.write(TAB);
        	double mk = cKappa.calcMaxKappa(curTst.getTable());
        	writer.write(Double.isNaN(mk) ? NAN : (mk < 0 ? ZERO : decFormat.format(mk)));
        	writer.write(TAB);
        	double ra = cKappa.calcRawAgreement(curTst.getTable());
        	writer.write(Double.isNaN(ra) ? NAN : (ra < 0 ? ZERO : decFormat.format(ra)));
        	writer.write(NL);
        }
	}
	
	/**
	 * Writes overall kappa values (including and excluding unmatched annotations)
	 * and the overall agreement matrix these are based on.
	 * 
	 * @param writer the output writer
	 * @param overallTable the table or matrix containing the data
	 * @throws IOException ant IO exception
	 */
	private void writeAgreementMatrix(Writer writer, AgreementTable overallTable) throws IOException {
		 // write the agreement table 
        //AgreementTable overallTable = kcc.getOverallTable();
        GlobalCKappa gcKappa = new GlobalCKappa(overallTable.getTable(), true);
        writer.write("Overall results (incl. unlinked/unmatched annotations):");
        writer.write(NL);
        writer.write("kappa_ipf");
        writer.write(TAB);
        writer.write("kappa_max");
        writer.write(TAB);
        writer.write("raw agreement");
        writer.write(NL);
        //writer.write(!Double.isNaN(gcKappa.kappaAllIPF) ? decFormat.format(Math.max(0, gcKappa.kappaAllIPF)) : NAN);
        writer.write(Double.isNaN(gcKappa.kappaAllIPF) ? NAN : 
        	(gcKappa.kappaAllIPF < 0 ? ZERO : decFormat.format(gcKappa.kappaAllIPF)));
        writer.write(TAB);
        writer.write(Double.isNaN(gcKappa.maxKappaAll) ? NAN :
        	(gcKappa.maxKappaAll < 0 ? ZERO : decFormat.format(gcKappa.maxKappaAll)));
        writer.write(TAB);
        writer.write(Double.isNaN(gcKappa.rawAgreementAll) ? NAN :
        	(gcKappa.rawAgreementAll < 0 ? ZERO : decFormat.format(gcKappa.rawAgreementAll)));
        writer.write(NL2);
        writer.write("Overall results (excl. unlinked/unmatched annotations):");
        writer.write(NL);
        // if there is only one value or category (beside UNMATCHED), don't write statistics
        if (overallTable.getLabels().size() > 2) {
            writer.write("kappa (excl.)");
            writer.write(TAB);
            writer.write("kappa_max (excl.)");
            writer.write(TAB);
            writer.write("raw agreement (excl.)");
            writer.write(NL); 
            writer.write(Double.isNaN(gcKappa.kappaMatched) ? NAN :
            	(gcKappa.kappaMatched < 0 ? ZERO : decFormat.format(gcKappa.kappaMatched)));
            writer.write(TAB);
            writer.write(Double.isNaN(gcKappa.maxKappaMatched) ? NAN :
            	(gcKappa.maxKappaMatched < 0 ? ZERO : decFormat.format(gcKappa.maxKappaMatched)));
            writer.write(TAB);
            writer.write(Double.isNaN(gcKappa.rawAgreementMatched) ? NAN :
            	(gcKappa.rawAgreementMatched < 0 ? ZERO : decFormat.format(gcKappa.rawAgreementMatched)));
        } else {
        	writer.write("\tNot available because there is only one value in the matrix apart from the Unmatched category");
        }

        // more global values
        writer.write(NL2);
        writer.write("Overall Agreement Matrix:");
        writer.write(NL);
        writer.write("First annotator in the rows, second annotator in the columns");
        writer.write(NL2);
        
        List<String> labels = overallTable.getLabels();
        // write labels horizontal, the column headers, first cell is empty
        writer.write(TAB);
        for (int i = 0; i < labels.size(); i++) {
        	String label = labels.get(i);
        	writer.write(label.isEmpty() ? EMPTY : label);
        	writer.write(TAB);
        }
        writer.write(NL);
        // write table
        for (int i = 0; i < labels.size(); i++) {
        	String label = labels.get(i);
        	writer.write(label.isEmpty() ? EMPTY : label);
        	writer.write(TAB);
        	//int[] row = overallTable.getTable()[i];
        	for (int j = 0; j < overallTable.getTable()[i].length; j++) {
        		writer.write(String.valueOf(overallTable.getTable()[i][j]));
        		writer.write(TAB);
        	}
        	writer.write(NL);
        }
	}

	/**
	 * Creates the overall, global matrix containing the values found in the two segment sets.
	 * @param cc the compare combination (i.e. two tiers)
	 * @param matchedAnnotations the previously found matching annotations
	 * @return a new compare combi object
	 */
	private KappaCompareCombi calculateOverallMatrix(CompareCombi cc, 
			Map<AnnotationCore, AnnotationCore> matchedAnnotations) {
		KappaCompareCombi curKappaCC = new KappaCompareCombi(cc.getFirstUnit(), cc.getSecondUnit());
		
	// step 1: extract all values that are in the two lists of annotations
		SortedSet<String> allValues = new TreeSet<String>();
		
		List<AnnotationCore> ac1List = cc.getFirstUnit().annotations;
		for (AnnotationCore ac : ac1List) {
			allValues.add(ac.getValue());
		}
		List<AnnotationCore> ac2List = cc.getSecondUnit().annotations;
		for (AnnotationCore ac : ac2List) {
			allValues.add(ac.getValue());
		}
		List<String> allValuesList = new ArrayList<String>(allValues);
		allValuesList.add(CompareConstants.UNMATCHED);
		curKappaCC.setAllValues(allValuesList);
		
		// step 2: process the matched annotations, add them to the proper agreement matrices
		// handle situation where there are no values? or will there always be the empty string?
		// creation and calculation of per value 2x2 tables is performed at a later stage 
		// (allowing multiplication of "matched" cells by 2 in a separate step) 
		AgreementTable globalTable = new AgreementTable("File global", allValuesList);
		
		AnnotationCore ac1, ac2;
		String s1, s2;
		
		if (matchedAnnotations != null) {
			Iterator<AnnotationCore> acIter = matchedAnnotations.keySet().iterator();
			
			while (acIter.hasNext()) {
				ac1 = acIter.next();
				ac2 = matchedAnnotations.get(ac1);
				s1 = ac1.getValue();
				s2 = ac2.getValue();

				// update tables
				if (s1.equals(s2)) {
					globalTable.increment(s1);
				} else {
					globalTable.increment(s1, s2);// -> Note: this way R1 along the vertical axis, R2 along horizontal
				}
			}
		}
		
	// step 3: process unmatched annotations from the first set of annotations, the first "unit"
		for (AnnotationCore aco : ac1List) {
			if (!matchedAnnotations.containsKey(aco)) {// not processed yet
				s1 = aco.getValue();
				globalTable.increment(s1, CompareConstants.UNMATCHED);// ->
			}
		}
		
	// step 4: process unmatched annotations from the second set of annotations, the second "unit"
		for (AnnotationCore aco : ac2List) {
			if (!matchedAnnotations.containsValue(aco)) {// not processed yet
				s2 = aco.getValue();
				globalTable.increment(CompareConstants.UNMATCHED, s2);// ->
			}
		}
		
	// step 5 add table to the combi
		curKappaCC.setOverallTable(globalTable);
		
		return curKappaCC;
	}
	
	/**
	 * Generates per value agreement tables based on the global matrix.
	 * This method partly overlaps functionality in {@link GlobalCKappa}, some restructuring
	 * might be considered.
	 * 
	 * @param kcc the compare combination
	 */
	private void calculatePerValueAgreementTables(KappaCompareCombi kcc) {
		List<String> allValuesList = kcc.getAllValues();
		AgreementTable globalTable = kcc.getOverallTable();
		int totalSum = globalTable.getTotalSum();
		
		Map<String, TwoSquareTable> perValueTable = new HashMap<String, TwoSquareTable>();
		kcc.setPerValueTables(perValueTable);
		
		for (int i = 0; i < allValuesList.size() - 1; i++) {// don't create a 2x2 table for unmatched
			String code = allValuesList.get(i);
			perValueTable.put(code, new TwoSquareTable(code));
		}
		
		for (int i = 0; i < allValuesList.size(); i++) {// i is also used to loop over all rows
			String code = allValuesList.get(i);
			TwoSquareTable tst = perValueTable.get(code); // null in case of UNMATCHED 
			 
			int cell1 = globalTable.getTable()[i][i];
			int cell2 = 0, cell3 = 0;
			
			int[] row = globalTable.getTable()[i];
			for (int j = 0; j < row.length; j++) {
				if (j != i) {
					cell2 += row[j];
				}
			}
			
			for (int j = 0; j < globalTable.getTable().length; j++) {
				if (j != i) {
					cell3 += globalTable.getTable()[j][i];
				}
			}
			
			if (tst != null) {// if i == allValuesList.size() - 1 (UNMATCHED) there is no 2x2 table
				tst.addToCell(0, 0, cell1);
				tst.addToCell(1, 0, cell2);// R1 in the rows
				tst.addToCell(0, 1, cell3);// R2 in the columns
				tst.addToCell(1, 1, totalSum - cell1 - cell2 - cell3);
			}
		}
	}
	
	/**
	 * Adds contents from the second table to the first.
	 * 
	 * @param intoTable receiver, not null
	 * @param fromTable supplier, not null
	 */
	private void mergeGlobalAgreementTables(AgreementTable intoTable, AgreementTable fromTable) {
		int numRows = fromTable.getLabels().size();
		
		// nested loops
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numRows; j++) {// columns
				intoTable.addCount(fromTable.getLabels().get(i), fromTable.getLabels().get(j), 
						fromTable.getTable()[i][j]);
			}
		}
	}
	
}
