package iped.app.home.newcase.tabs.caseinfo;/*
 * @created 08/09/2022
 * @project IPED
 * @author Thiago S. Figueiredo
 */

import iped.app.home.DefaultPanel;
import iped.app.home.MainFrame;
import iped.app.home.newcase.model.CaseInfo;
import iped.app.home.newcase.NewCaseContainerPanel;
import iped.app.home.newcase.model.ExistentCaseOptions;
import iped.app.home.newcase.model.IPEDProcess;
import iped.app.home.style.StyleManager;
import iped.app.home.utils.CasePathManager;
import iped.app.ui.Messages;
import iped.engine.config.ConfigurationManager;
import iped.engine.config.LocalConfig;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Case info TAB
 */
public class CaseInfoTab extends DefaultPanel {

    private LocalConfig localConfig;
    private IPEDProcess ipedProcess;

    private JTextField textFieldCaseOutput;
    private JCheckBox checkBoxOutputOnSSD;
    private JButton buttonSelectCaseOutput;
    private JTextField textFieldCaseNumber;
    private JTextField textFieldCaseName;
    private JTextArea textAreaInvestigatedNames;
    private JTextField textFieldRequestDate;
    private JTextField textFieldDemandant;
    private JTextField textFieldOrganization;
    private JTextArea textAreaExaminerNames;
    private JTextField textFieldContact;
    private JTextArea textAreaCaseNotes;
    private JPanel panelCaseOutputOptions;
    private JRadioButton radioCaseOutAdd;
    private JRadioButton radioCaseOutContinue;
    private JRadioButton radioCaseOutRestart;
    private ButtonGroup buttonGroupCaseOutOptions;
    private FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter("caseinfo.json", new String[]{"json", "JSON"});

    public CaseInfoTab(MainFrame mainFrame) {
        super(mainFrame);
    }

    /**
     * Prepare everything to be displayed
     */
    protected void createAndShowGUI(){
        ipedProcess = NewCaseContainerPanel.getInstance().getIpedProcess();
        this.setLayout( new BorderLayout() );
        this.add(createTitlePanel(), BorderLayout.NORTH);
        this.add(createFormPanel(), BorderLayout.CENTER);
        this.add(createNavigationButtonsPanel(), BorderLayout.SOUTH);
    }

    /**
     * Create a new JPanel instance containing the Page Title
     * @return - JPanel containing the Page Title
     */
    private JPanel createTitlePanel(){
        JPanel panelTitle = new JPanel();
        panelTitle.setBackground(Color.white);
        JLabel labelTitle = new JLabel(Messages.get("Home.CaseInformation"));
        labelTitle.setFont(StyleManager.getPageTitleFont());
        panelTitle.add(labelTitle);
        return panelTitle;
    }

    private void createFormComponentInstances(){
        localConfig = ConfigurationManager.get().findObject(LocalConfig.class);

        textFieldCaseNumber = new JTextField();
        textFieldCaseName = new JTextField();
        textAreaInvestigatedNames = new JTextArea();
        textAreaInvestigatedNames.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        textFieldRequestDate = new JTextField();
        textFieldDemandant = new JTextField();
        textFieldOrganization = new JTextField();
        textAreaExaminerNames = new JTextArea();
        textAreaExaminerNames.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        textFieldContact = new JTextField();
        textAreaCaseNotes = new JTextArea();
        textAreaCaseNotes.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        textAreaCaseNotes.setRows(5);
        textFieldCaseOutput = new JTextField();
        textFieldCaseOutput.setEditable(false);
        checkBoxOutputOnSSD = new JCheckBox(Messages.get("Home.NewCase.IsCaseOutputOnSSD"));
        checkBoxOutputOnSSD.setToolTipText(Messages.get("Home.NewCase.IsCaseOutputOnSSDToolTip"));
        checkBoxOutputOnSSD.setSelected(localConfig.isOutputOnSSD());
        checkBoxOutputOnSSD.setOpaque(false);
        checkBoxOutputOnSSD.addItemListener(e->{
            try {
                Boolean isEnableOutputSSD = (e.getStateChange() == ItemEvent.SELECTED);
                LocalConfig localConfig = ConfigurationManager.get().findObject(LocalConfig.class);
                localConfig.getPropertie().setProperty(LocalConfig.OUTPUT_ON_SSD, isEnableOutputSSD.toString());
                localConfig.getPropertie().saveOnFile(CasePathManager.getInstance().getLocalConfigFile());
                ConfigurationManager.get().reloadConfigurable(LocalConfig.class);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
        buttonSelectCaseOutput = new JButton("...");
        buttonSelectCaseOutput.addActionListener( e -> {
            JFileChooser fileChooserDestino = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            fileChooserDestino.setDialogTitle(Messages.get("Home.NewCase.ChooseCaseOutput"));
            fileChooserDestino.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooserDestino.setAcceptAllFileFilterUsed(false);
            if( fileChooserDestino.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                setCaseOutputValue(fileChooserDestino.getSelectedFile().toPath());
            }
        } );
    }

    private File showSaveCaseInfoFileChooser(String title){
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle(title);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(jsonFilter);
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getParentFile(), file.getName() + ".json");
            }
            return file;
        }
        return null;
    }

    private File showLoadCaseInfoFileChooser(String title){
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle(title);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(jsonFilter);
        int returnValue = fileChooser.showOpenDialog (this);
        if (returnValue == JFileChooser.APPROVE_OPTION){
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    private void setCaseOutputValue(Path caseOutput){
        //first check if exists a case on selected output folder
        boolean isExistsCase = Paths.get(caseOutput.toString(), "IPED-SearchApp.exe").toFile().exists();
        panelCaseOutputOptions.setVisible(isExistsCase);
        if(isExistsCase) {
            //check if the existent case is finished, if yes the user cannot choose the continue options
            boolean isCaseFinished = Paths.get(caseOutput.toString(), "iped", "data", "processing_finished").toFile().exists();
            radioCaseOutContinue.setVisible(! isCaseFinished);
        }
        //when user reselect other path wee need to unselect the previously selected radio button
        if( ! panelCaseOutputOptions.isVisible() ) {
            ipedProcess.setExistentCaseOption(null);
            buttonGroupCaseOutOptions.clearSelection();
        }

        IPEDProcess ipedProcess = NewCaseContainerPanel.getInstance().getIpedProcess();
        ipedProcess.setCaseOutputPath(caseOutput);
        textFieldCaseOutput.setText( caseOutput.toString() );
    }

    /**
     * Create a new JPanel instance containing all inputs
     * @return JPanel - A JPanel containing all data input form itens
     */
    private JPanel createFormPanel(){
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(super.getCurrentBackGroundColor());
        createFormComponentInstances();

        int column0 = 0;
        int column1 = 1;

        int column0Width = 1;
        int column1width = 2;
        int fullColumnWidth = column0Width + column1width;

        double noWeightx = 0;
        double fullWeightx = 1.0;

        int currentLine = 0;

        panelForm.add(new JLabel(Messages.get("Home.NewCase.CaseNumber")+":"), getGridBagConstraints(column0, currentLine, column0Width, noWeightx));
        panelForm.add(textFieldCaseNumber, getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        panelForm.add(new JLabel(Messages.get("Home.NewCase.CaseName")+":"), getGridBagConstraints(column0, currentLine, column0Width, noWeightx));
        panelForm.add(textFieldCaseName, getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        panelForm.add(new JLabel(Messages.get("Home.NewCase.Investigated")+":"), getGridBagConstraints(column0, currentLine, column0Width, noWeightx));
        panelForm.add(textAreaInvestigatedNames, getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        panelForm.add(new JLabel(Messages.get("Home.NewCase.RequestDate")+":"), getGridBagConstraints(column0, currentLine, column0Width, noWeightx));
        panelForm.add(textFieldRequestDate, getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        panelForm.add(new JLabel(Messages.get("Home.NewCase.Requester")+":"), getGridBagConstraints(column0, currentLine, column0Width, noWeightx));
        panelForm.add(textFieldDemandant, getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        panelForm.add(new JLabel(Messages.get("Home.NewCase.OrganizationName")+":"), getGridBagConstraints(column0, currentLine, column0Width, noWeightx));
        panelForm.add(textFieldOrganization, getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        panelForm.add(new JLabel(Messages.get("Home.NewCase.Examiners")+":"), getGridBagConstraints(column0, currentLine, column0Width, noWeightx));
        panelForm.add(textAreaExaminerNames, getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        panelForm.add(new JLabel(Messages.get("Home.NewCase.Contact")+":"), getGridBagConstraints(column0, currentLine, column0Width, noWeightx));
        panelForm.add(textFieldContact, getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        panelForm.add(new JLabel(Messages.get("Home.NewCase.Notes")+":"), getGridBagConstraints(column0, currentLine, column0Width, noWeightx));
        panelForm.add(textAreaCaseNotes, getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        //----------------Case data buttons-----------------
        panelForm.add(createCaseDataPanel(), getGridBagConstraints(column0, currentLine++, fullColumnWidth, fullWeightx, new Insets(10,10,0,10)));

        //----------------Line Separator-----------------
        panelForm.add(new JSeparator(), getGridBagConstraints(column0, currentLine++, fullColumnWidth, fullWeightx, new Insets(10,10,10,10)));

        panelForm.add(new JLabel(Messages.get("Home.NewCase.CaseOutput")+":"), getGridBagConstraints(column0, currentLine, column0Width, noWeightx));
        panelForm.add(createSetCaseOutputPanel(), getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        panelForm.add(checkBoxOutputOnSSD, getGridBagConstraints(column1, currentLine++, column1width, fullWeightx));

        panelCaseOutputOptions = createCaseOutputOptionsPanel();
        panelForm.add(panelCaseOutputOptions, getGridBagConstraints(column0, currentLine, fullColumnWidth, fullWeightx, new Insets(10,10,0,10)));

        return panelForm;

    }

    /**
     * Create a JPanel with one textfield and a button to set the case output
     * @return JPanel with a textfield and a button to set case output
     */
    private JPanel createSetCaseOutputPanel(){
        JPanel panelCaseOutput = new JPanel();
        panelCaseOutput.setLayout(new BoxLayout(panelCaseOutput, BoxLayout.LINE_AXIS));
        panelCaseOutput.setBackground(Color.white);
        panelCaseOutput.add(textFieldCaseOutput);
        panelCaseOutput.add(buttonSelectCaseOutput);
        return panelCaseOutput;
    }

    private JPanel createCaseOutputOptionsPanel(){
        buttonGroupCaseOutOptions = new ButtonGroup();
        radioCaseOutAdd = new JRadioButton(Messages.get("Home.AppendExistentCase"), false);
        radioCaseOutAdd.setBackground(super.getCurrentBackGroundColor());
        radioCaseOutContinue = new JRadioButton(Messages.get("Home.ContinueExistentCase"), false);
        radioCaseOutContinue.setBackground(super.getCurrentBackGroundColor());
        radioCaseOutRestart = new JRadioButton(Messages.get("Home.RestartExistentCase"), false);
        radioCaseOutRestart.setBackground(super.getCurrentBackGroundColor());
        buttonGroupCaseOutOptions.add(radioCaseOutAdd);
        buttonGroupCaseOutOptions.add(radioCaseOutContinue);
        buttonGroupCaseOutOptions.add(radioCaseOutRestart);

        JPanel panel = new JPanel();
        panel.setVisible(false);
        panel.setBackground(super.getCurrentBackGroundColor());
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JLabel labelCaseOptionMessage = new JLabel(Messages.get("Home.ExistentCaseAlert"));
        labelCaseOptionMessage.setForeground(Color.RED);
        panel.add(labelCaseOptionMessage);
        panel.add(radioCaseOutAdd);
        panel.add(radioCaseOutContinue);
        panel.add(radioCaseOutRestart);
        return panel;
    }

    /**
     * Create a JPanel with Save and load buttons
     * @return A new JPanel with Case Button
     */
    private JPanel createCaseDataPanel(){
        JPanel panelCaseData = new JPanel();
        panelCaseData.setBackground(super.getCurrentBackGroundColor());
        JButton buttonSaveCaseData = new JButton(Messages.get("Home.NewCase.SaveCaseData"));
        buttonSaveCaseData.addActionListener( e -> {
            File destinationFile = showSaveCaseInfoFileChooser(Messages.get("Home.NewCase.ChooseCaseInfoFileOutput"));
            populateCaseInfo();
            new CaseInfoManager().saveCaseInfo(ipedProcess.getCaseInfo(), destinationFile);
        });
        JButton buttonLoadCaseData = new JButton(Messages.get("Home.NewCase.LoadCaseData"));
        buttonLoadCaseData.addActionListener(e -> {
            File caseInfoSourceFile = showLoadCaseInfoFileChooser( Messages.get("Home.NewCase.ChooseCaseInfoFile") );
            CaseInfo caseInfo = null;
            try {
                caseInfo = new CaseInfoManager().loadCaseInfo(caseInfoSourceFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if(caseInfo == null)
                return;
            ipedProcess.setCaseInfo(caseInfo);
            populateFormCaseInfo();
        });
        panelCaseData.add(buttonSaveCaseData);
        panelCaseData.add(buttonLoadCaseData);
        return panelCaseData;
    }

    /**
     * Create a new GridBagConstraints to be used on this page Form
     * @param tableColumnIndex - The index number of table column
     * @param tableLineIndex - The index number of table line
     * @param cellWidth - The table Cell Width
     * @param weightX - Cell Weight X
     * @return GridBagConstraints - a new GridBagConstraints instance containing all parameter passed
     */
    private GridBagConstraints getGridBagConstraints(int tableColumnIndex, int tableLineIndex, int cellWidth, double weightX) {
        GridBagConstraints gbcons = new GridBagConstraints();
        gbcons.fill = GridBagConstraints.HORIZONTAL;
        gbcons.weightx = weightX;
        gbcons.gridx = tableColumnIndex;
        gbcons.gridy = tableLineIndex;
        gbcons.gridwidth = cellWidth;
        gbcons.gridheight = 1;
        gbcons.insets = new Insets(2,10,2,10);
        return gbcons;
    }

    /**
     * Create a new GridBagConstraints to be used on this page Form
     * @param tableColumnIndex - The index number of table column
     * @param tableLineIndex - The index number of table line
     * @param cellWidth - The table Cell Width
     * @param weightX - Cell Weight X
     * @param insets - the Insets
     * @return GridBagConstraints - a new GridBagConstraints instance containing all parameter passed
     */
    private GridBagConstraints getGridBagConstraints(int tableColumnIndex, int tableLineIndex, int cellWidth, double weightX, Insets insets){
        GridBagConstraints gbcons = getGridBagConstraints(tableColumnIndex, tableLineIndex, cellWidth, weightX);
        gbcons.insets = insets;
        return gbcons;
    }

    /**
     * A JPanel containing "Cancel" and "Next" buttons
     * @return JPanel - a new JPanel instance containing the bottom page Button
     */
    private JPanel createNavigationButtonsPanel() {
        JPanel panelButtons = new JPanel();
        panelButtons.setBackground(Color.white);
        JButton buttoCancel = new JButton(Messages.get("Home.Cancel"));
        buttoCancel.addActionListener( e -> NewCaseContainerPanel.getInstance().goHome());
        JButton buttonNext = new JButton(Messages.get("Home.Next"));
        buttonNext.addActionListener( e -> navigateToNextTab() );
        panelButtons.add(buttoCancel);
        panelButtons.add(buttonNext);
        return panelButtons;
    }

    private void navigateToNextTab(){
        Path casePath = ipedProcess.getCaseOutputPath();
        //check if output path exists
        if( (casePath == null) || ( ! Files.isDirectory(casePath) ) ){
            JOptionPane.showMessageDialog(this, Messages.get("Home.NewCase.CaseOutputRequired"), Messages.get("Home.NewCase.CaseOutputRequiredTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        //Check case output permissions
        if( (! Files.isReadable(casePath)) || (! Files.isWritable(casePath)) ){
            JOptionPane.showMessageDialog(this, Messages.get("Home.NewCase.CaseOutputPermission"), Messages.get("Home.NewCase.CaseOutputPermissionTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        if( panelCaseOutputOptions.isVisible() && (buttonGroupCaseOutOptions.getSelection() == null ) ){
            JOptionPane.showMessageDialog(this, Messages.get("Home.NewCase.CaseOutPutOptionsRequired"), Messages.get("Home.NewCase.CaseOutputRequiredTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        setCaseOutputOptions();
        populateCaseInfo();
        NewCaseContainerPanel.getInstance().goToNextTab();
    }

    private void setCaseOutputOptions(){
        if(radioCaseOutAdd.isSelected())
            ipedProcess.setExistentCaseOption(ExistentCaseOptions.APPEND);
        if (radioCaseOutContinue.isSelected())
            ipedProcess.setExistentCaseOption(ExistentCaseOptions.CONTINUE);
        if( radioCaseOutRestart.isSelected())
            ipedProcess.setExistentCaseOption(ExistentCaseOptions.RESTART);
    }

    private void populateCaseInfo(){
        CaseInfo caseInfo = ipedProcess.getCaseInfo();
        caseInfo.setCaseNumber(textFieldCaseNumber.getText());
        caseInfo.setCaseName(textFieldCaseName.getText());
        caseInfo.setInvestigatedNames(new ArrayList<>(Arrays.asList(textAreaInvestigatedNames.getText().split("\n") )));
        caseInfo.setRequestDate(textFieldRequestDate.getText());
        caseInfo.setRequester(textFieldDemandant.getText());
        caseInfo.setOrganizationName(textFieldOrganization.getText());
        caseInfo.setExaminers(new ArrayList<>( Arrays.asList(textAreaExaminerNames.getText().split("\n")) ));
        caseInfo.setContact(textFieldContact.getText());
        caseInfo.setCaseNotes(textAreaCaseNotes.getText());
    }

    private void populateFormCaseInfo(){
        CaseInfo caseInfo = ipedProcess.getCaseInfo();
        textFieldCaseNumber.setText( caseInfo.getCaseNumber() != null ? caseInfo.getCaseNumber() : textFieldCaseNumber.getText() );
        textFieldCaseName.setText(caseInfo.getCaseName() != null ? caseInfo.getCaseName() : textFieldCaseName.getText() );
        if( (caseInfo.getInvestigatedNames() != null) && (!caseInfo.getInvestigatedNames().isEmpty()) ){
            String investigatedNames = caseInfo.getInvestigatedNames().stream().map(String::trim).collect(Collectors.joining("\n"));
            textAreaInvestigatedNames.setText(investigatedNames);
        }
        textFieldRequestDate.setText(caseInfo.getRequestDate() != null ? caseInfo.getRequestDate() : textFieldRequestDate.getText() );
        textFieldDemandant.setText(caseInfo.getRequester() != null? caseInfo.getRequester() : textFieldDemandant.getText() );
        textFieldOrganization.setText(caseInfo.getOrganizationName() != null ? caseInfo.getOrganizationName() : textFieldOrganization.getText() );
        if( (caseInfo.getExaminers() != null) && (!caseInfo.getExaminers().isEmpty())){
            String examinerNames = caseInfo.getExaminers().stream().map(String::trim).collect(Collectors.joining("\n"));
            textAreaExaminerNames.append(examinerNames);
        }
        textFieldContact.setText(caseInfo.getContact() != null ? caseInfo.getContact() : textFieldContact.getText() );
        textAreaCaseNotes.setText(caseInfo.getCaseNotes() != null ? caseInfo.getCaseNotes() : textAreaCaseNotes.getText() );
    }

}