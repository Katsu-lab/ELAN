package mpi.eudico.client.annotator.gui;

import static mpi.eudico.server.corpora.util.ServerLogger.LOG;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.json.HSQLAnnotationMapperDB;
import nl.mpi.util.FileExtension;

/**
 * A Dialog which lists all the tiers and their associated collection IDs 
 * and created date from the hsql db
 * 
 *
 */
@SuppressWarnings("serial")
public class ListCollectionIDsDialog extends ClosableDialog implements ActionListener {
	
	private Transcription transcription;
	
	private JLabel messageLabel;
    private JTable resultTable;
    private JScrollPane scrollPane;
    private DefaultTableModel model;
    private JButton buttonSave;
    private JButton buttonClose;
    private final int tableW = 380;

    /**
     * constructor to initialize the owner frame and transcription
     * @param owner
     * @param transcription
     */
    
    public ListCollectionIDsDialog(Frame owner, Transcription transcription) {
    	 super(owner, true);
         
         this.transcription = transcription;
         
         initComponents();
         setTitle (ElanLocale.getString("ListCollectionID.Title"));
         
         pack ();
         setLocationRelativeTo(getParent());
         
         
    }
    
    /**
     * method that makes connections to database to fetch the list
     * @return
     */
    public Boolean populateList() {
    	 HSQLAnnotationMapperDB mapperDB = new HSQLAnnotationMapperDB();
         Boolean isListPresent = mapperDB.getCollectionIDsFromDB(this.transcription,model);
         
         return isListPresent;
    }
    
    /**
     * Initilazes the dialog
     */
    protected void initComponents() {
    	getContentPane().setLayout(new GridBagLayout());
    	getContentPane().setPreferredSize(new Dimension(650, 400));

		GridBagConstraints gbc = new GridBagConstraints();
		
		messageLabel = new JLabel(ElanLocale.getString("ListCollectionID.Message"));
	
		model = new DefaultTableModel() {
            @Override
				public boolean isCellEditable(int row, int column) {
                if (column > 0) {
                    return false;
                }

                return true;
            }
        };
        
        model.setColumnCount(3);
        
        resultTable = new JTable(model);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JTableHeader tableHeader= resultTable.getTableHeader();
        TableColumnModel colMod = tableHeader.getColumnModel();
        TableColumn tabCol0 = colMod.getColumn(0);
        tabCol0.setHeaderValue("Tier");
        TableColumn tabCol1 = colMod.getColumn(1);
        tabCol1.setHeaderValue("Collection ID");
        TableColumn tabCol2 = colMod.getColumn(2);
        tabCol2.setHeaderValue("Created Date");
        tableHeader.repaint();
        tableHeader.setFont(new Font("Serif", Font.BOLD, 14));
        
        resultTable.setTableHeader(tableHeader);

        
        scrollPane = new JScrollPane(resultTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(tableW, 100));

        setLayout(new GridBagLayout());
        
        buttonSave = new JButton(ElanLocale.getString("ListCollectionID.Save"));
		buttonSave.addActionListener(this);
		
		buttonClose = new JButton(ElanLocale.getString("Button.Close"));
		buttonClose.addActionListener(this);
        
       
        gbc.insets = new Insets(3, 6, 10, 6);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        getContentPane().add(messageLabel, gbc);
        
        
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        getContentPane().add(scrollPane,gbc);
        
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        buttonPanel.add(buttonSave);
        buttonPanel.add(buttonClose);
	   
	    gbc.gridy = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.SOUTH;
		getContentPane().add(buttonPanel, gbc);


    }
    
    
    /**
     * method to receive action events 
     */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == buttonSave) {
			exportTableToFile();
			
		}else if (e.getSource() == buttonClose) {
			setVisible(false);
	        dispose();
        }
	}
	
	
	/**
	 * Exports the table which consists of list to a file
	 */
	public void exportTableToFile() {
		FileChooser chooser = new FileChooser (this);

	      chooser.createAndShowFileDialog(ElanLocale.getString("ExportJSONDialog.Export.Location"),
	        FileChooser.SAVE_DIALOG, FileExtension.TEXT_EXT, "ExportJSONDialog.Current.Directory");
	      
	      if (chooser.getSelectedFile() != null) {
	    	  
	    	  FileOutputStream out;
			try {
				out = new FileOutputStream(chooser.getSelectedFile());
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
				StringBuffer sbTableData = new StringBuffer();
				for(int row = 0; row < resultTable.getRowCount(); row ++){
				    for(int column = 0; column < resultTable.getColumnCount(); column ++) {
				        sbTableData.append(resultTable.getValueAt(row, column)).append("\t");
				    }
				    sbTableData.append("\n");
				}
				writer.write(sbTableData + "\t");
				
				writer.close();
				
			}catch (FileNotFoundException e) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "FileNotFoundException occured " + e.getMessage());
				}
			}catch (IOException e) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "IOException occured " + e.getMessage());
				}
			}
	         
	      }
	}

}
