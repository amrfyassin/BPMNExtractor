package com.amr.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import com.amr.bpmextractor.alphaalgorithm.AlphaMatrix;
import com.amr.bpmextractor.bpmnbuilder.Process;
import com.amr.bpmextractor.engine.BpmnExtractorEngine;

public class BPMExtractorUI extends JPanel{

    private JFrame frmBpmExtractor;
    private JTextField txtOutputDir;
    private JTextField txtInputDir;
    private JFileChooser inputFileChooser;
    private JFileChooser outputFileChooser;
    
    File inputDirectory;
    File outputDirectory;
    File[] inputFiles;
    File[] outputFiles;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    BPMExtractorUI window = new BPMExtractorUI();
                    window.frmBpmExtractor.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public BPMExtractorUI() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        
        inputDirectory = new File("/Users/amr/Documents/Personal/GUC/ThesisWorkspaces/workspace2/BpmnExtractor/data");
        outputDirectory = new File("/Users/amr/Documents/Personal/GUC/ThesisWorkspaces/workspace2/BpmnExtractor/out");
        
        inputFileChooser = new JFileChooser();
        inputFileChooser.setDialogTitle("Select Input Directory");
        inputFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        inputFileChooser.setCurrentDirectory(inputDirectory);

        outputFileChooser = new JFileChooser();
        outputFileChooser.setDialogTitle("Select Input Directory");
        outputFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputFileChooser.setCurrentDirectory(outputDirectory);
        
        frmBpmExtractor = new JFrame();
        frmBpmExtractor.setTitle("BPM Extractor");
        frmBpmExtractor.setBounds(50, 50, 850, 400);
        frmBpmExtractor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        initInputPane();
        initOutputPane();
    }

    private void initInputPane() {
        JPanel panel_Input = new JPanel();
        frmBpmExtractor.getContentPane().add(panel_Input, BorderLayout.CENTER);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths    = new int[] {150, 450, 50, 50, 50};
        //gridBagLayout.rowHeights    = new int[] {0, 0, 0, 0, 0};
        panel_Input.setLayout(gridBagLayout);

        JButton btnChooseInputDir = new JButton("Choose");
        btnChooseInputDir.setAlignmentX(RIGHT_ALIGNMENT);
        btnChooseInputDir.setToolTipText("Choose the input directory were the raw data exists.");
        btnChooseInputDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = inputFileChooser.showOpenDialog(BPMExtractorUI.this);
                
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    inputDirectory = inputFileChooser.getSelectedFile();
                    txtInputDir.setText(inputDirectory.getAbsolutePath());
                    System.out.println("Input Directory: " + inputDirectory.getAbsolutePath() + ".");
                    inputFiles = inputDirectory.listFiles();

                    int len = inputFiles.length;
                    if (len <= 0) {
                        JOptionPane.showMessageDialog(panel_Input, "Folder contains no files !!", "Empty Folder", JOptionPane.WARNING_MESSAGE);
                    }
                    
                    outputFiles = new File[len];
                    
                    for (int i = 0; i < len; i++) {
                        System.out.println(inputFiles[i].getPath());
                        outputFiles[i] = new File(inputFiles[i].getPath() + ".xml");
                        System.out.println(outputFiles[i].getPath());
                    }

                } else {
                    System.out.println("Input directory selection was cancelled by user.");
                }
            }
        });
        GridBagConstraints gbc_btnChooseInputDir = new GridBagConstraints();
        gbc_btnChooseInputDir.insets = new Insets(0, 0, 1, 1);
        gbc_btnChooseInputDir.gridx = 2;
        gbc_btnChooseInputDir.gridy = 0;
        panel_Input.add(btnChooseInputDir, gbc_btnChooseInputDir);
        
        JButton btnChooseOutputDir = new JButton("Choose");
        btnChooseOutputDir.setAlignmentX(RIGHT_ALIGNMENT);
        btnChooseOutputDir.setToolTipText("Choose the output directory were the generated BPMN files will be stored.");
        btnChooseOutputDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = outputFileChooser.showOpenDialog(BPMExtractorUI.this);
                
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    outputDirectory = outputFileChooser.getSelectedFile();
                    txtOutputDir.setText(outputDirectory.getAbsolutePath());
                    
                    System.out.println("Output Directory: " + outputDirectory.getAbsolutePath());
                } else {
                    System.out.println("Output directory selection was cancelled by user.");
                }
            }
        });
        GridBagConstraints gbc_btnChooseOutputDir = new GridBagConstraints();
        gbc_btnChooseOutputDir.insets = new Insets(0, 0, 1, 1);
        gbc_btnChooseOutputDir.gridx = 2;
        gbc_btnChooseOutputDir.gridy = 1;
        panel_Input.add(btnChooseOutputDir, gbc_btnChooseOutputDir);
        
        JLabel lblInputDir = new JLabel("Input Directory");
        GridBagConstraints gbc_lblInputDir = new GridBagConstraints();
        gbc_lblInputDir.insets = new Insets(0, 0, 5, 5);
        gbc_lblInputDir.anchor = GridBagConstraints.EAST;
        gbc_lblInputDir.gridx = 0;
        gbc_lblInputDir.gridy = 0;
        panel_Input.add(lblInputDir, gbc_lblInputDir);
        
        txtInputDir = new JTextField();
        txtInputDir.setText(inputDirectory.getPath());
        GridBagConstraints gbc_txtInputDir = new GridBagConstraints();
        gbc_txtInputDir.insets = new Insets(0, 0, 5, 5);
        gbc_txtInputDir.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtInputDir.gridx = 1;
        gbc_txtInputDir.gridy = 0;
        panel_Input.add(txtInputDir, gbc_txtInputDir);
        txtInputDir.setColumns(20);
        
        JLabel lblOutputDir = new JLabel("Output Directory");
        GridBagConstraints gbc_lblOutputDir = new GridBagConstraints();
        gbc_lblOutputDir.insets = new Insets(0, 0, 5, 5);
        gbc_lblOutputDir.anchor = GridBagConstraints.EAST;
        gbc_lblOutputDir.gridx = 0;
        gbc_lblOutputDir.gridy = 1;
        panel_Input.add(lblOutputDir, gbc_lblOutputDir);
        
        txtOutputDir = new JTextField();
        txtOutputDir.setText(outputDirectory.getPath());
        GridBagConstraints gbc_txtOutputfolder = new GridBagConstraints();
        gbc_txtInputDir.insets = new Insets(0, 0, 5, 5);
        gbc_txtOutputfolder.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtOutputfolder.gridx = 1;
        gbc_txtOutputfolder.gridy = 1;
        panel_Input.add(txtOutputDir, gbc_txtOutputfolder);
        txtOutputDir.setColumns(20);
        
        JButton btnCreateBPMNOnly = new JButton("Create BPMN Only");
        btnCreateBPMNOnly.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                for (int i = 0; i < inputFiles.length; i++) {
                    BpmnExtractorEngine pt = new BpmnExtractorEngine(inputFiles[i].getPath(), outputFiles[i].getPath());
                    pt.processText();
                }
            }
        });
        btnCreateBPMNOnly.setToolTipText("Create BPMN Only");
        GridBagConstraints gbc_btnCreateBPMNOnly = new GridBagConstraints();
        gbc_btnCreateBPMNOnly.insets = new Insets(0, 0, 5, 5);
        gbc_btnCreateBPMNOnly.gridx = 1;
        gbc_btnCreateBPMNOnly.gridy = 3;
        panel_Input.add(btnCreateBPMNOnly, gbc_btnCreateBPMNOnly);
        
        JButton btnCreateMergedBPMN = new JButton("Create Merged BPMN");
        btnCreateMergedBPMN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                ArrayList<Process> processes = new ArrayList<>();
                Process mergedProcess = new Process("Merged");
                
                for (int i = 0; i < inputFiles.length; i++) {
                    BpmnExtractorEngine extractor = new BpmnExtractorEngine(inputFiles[i].getPath(), outputFiles[i].getPath());
                    processes.add(extractor.processText());
                }
                
                AlphaMatrix matrix = new AlphaMatrix(mergedProcess);
                for (Process process : processes) {
                    matrix.addProcess(process);
                }
                
                matrix.toString();
                matrix.getProcess().writeBMPNFile(outputDirectory + "mergedprocess.xml");
            }
        });
        btnCreateMergedBPMN.setToolTipText("Create BPMN and Merge all processes in one master process.");        
        GridBagConstraints gbc_btnCreateMergedBPMNOnly = new GridBagConstraints();
        gbc_btnCreateMergedBPMNOnly.insets = new Insets(0, 0, 5, 5);
        gbc_btnCreateMergedBPMNOnly.gridx = 2;
        gbc_btnCreateMergedBPMNOnly.gridy = 3;
        panel_Input.add(btnCreateMergedBPMN, gbc_btnCreateMergedBPMNOnly);
        
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	frmBpmExtractor.dispose();
            }
        });
        btnClose.setToolTipText("Close.");        
        GridBagConstraints gbc_btnClose = new GridBagConstraints();
        gbc_btnClose.insets = new Insets(0, 0, 5, 5);
        gbc_btnClose.gridx = 3;
        gbc_btnClose.gridy = 3;
        panel_Input.add(btnClose, gbc_btnClose);
        
    }

    private void initOutputPane() {
        JPanel panel_Output = new JPanel();
        frmBpmExtractor.getContentPane().add(panel_Output, BorderLayout.SOUTH);
        panel_Output.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        JTextArea textArea = new JTextArea(5, 150);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollV = new JScrollPane (textArea);
        JScrollPane scrollH = new JScrollPane (textArea);
        scrollV.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollH.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        //Forward the outputStream to the text area
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        System.setOut(printStream);
        System.setErr(printStream);
        
        panel_Output.add(textArea);        
    }
    
    public class CustomOutputStream extends OutputStream {
        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            textArea.append(String.valueOf((char)b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
            textArea.update(textArea.getGraphics());
        }
    }
}
