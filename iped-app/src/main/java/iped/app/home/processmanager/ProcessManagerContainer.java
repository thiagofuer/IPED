package iped.app.home.processmanager;

/*
 * @created 27/09/2022
 * @project IPED
 * @author Thiago S. Figueiredo
 */

import iped.app.home.DefaultPanel;
import iped.app.home.MainFrame;
import iped.app.home.MainFrameCardsNames;
import iped.app.home.newcase.NewCaseContainerPanel;
import iped.app.home.newcase.model.IPEDProcess;
import iped.app.home.newcase.tabs.caseinfo.CaseInfoManager;
import iped.app.home.style.StyleManager;
import iped.app.ui.Messages;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;
import java.util.Objects;

public class ProcessManagerContainer extends DefaultPanel implements ProcessListener {

    private IPEDProcess ipedProcess;
    private JLabel labelTitle;
    private ImageIcon startingIcon;
    private ImageIcon errorIcon;
    private ImageIcon successIcon;
    private ImageIcon runningIcon;
    private JLabel currentLabelIcon;
    private JPanel errorOptionsButtonPanel;
    private JPanel successOptionsButtonPanel;
    private JPanel processRunningPanel;
    private IpedStartException ipedStartException;
    private JTextArea logTextArea;
    private JButton buttonOpenCase;

    //Constants to change content view
    private final String STARTING_PROCESS = "startingProcess";
    private final String RUNNING_PROCESS = "runningProcess";
    private final String FAILED_PROCESS = "failedProcess";
    private final String FINISHED_PROCESS = "finishedProcess";

    public ProcessManagerContainer(MainFrame mainFrame) {
        super(mainFrame);
    }

    @Override
    protected void createAndShowGUI() {
        ipedProcess = NewCaseContainerPanel.getInstance().getIpedProcess();
        this.setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );
        this.add(createTitlePanel());
        createLabelIcons();
        currentLabelIcon = new JLabel(startingIcon, JLabel.CENTER);
        currentLabelIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(currentLabelIcon);
        this.add(createProcessRunningPanel());
        this.add(createErrorOptionsButtonpanel());
        this.add(createSuccessOptionsButtonpanel());
    }

    private JPanel createTitlePanel(){
        JPanel panelTitle = new JPanel();
        panelTitle.setBackground(Color.white);
        labelTitle = new JLabel(Messages.get("Home.ProcessManager.StartingProcess"));
        labelTitle.setFont(StyleManager.getPageTitleFont());
        panelTitle.add(labelTitle);
        return panelTitle;
    }

    private void createLabelIcons(){
        startingIcon = createNewButtonIcon("plug-in.png");
        runningIcon = createNewButtonIcon("pluged-in.png");
        errorIcon = createNewButtonIcon("plug_error.png");
        successIcon = createNewButtonIcon("success.png");
    }

    private JPanel createProcessRunningPanel(){
        processRunningPanel = new JPanel();
        processRunningPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        processRunningPanel.setBackground(super.getCurrentBackGroundColor());
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBorder(BorderFactory.createLineBorder(Color.black,1));
        processRunningPanel.add(logTextArea);
        processRunningPanel.setVisible(false);
        return processRunningPanel;
    }

    private JPanel createErrorOptionsButtonpanel(){
        errorOptionsButtonPanel = new JPanel();
        errorOptionsButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorOptionsButtonPanel.setBackground(super.getCurrentBackGroundColor());
        JButton buttonBack = new JButton(Messages.get("Home.ProcessManager.BackToCaseInfo"));
        buttonBack.addActionListener(e->mainFrame.showPanel(MainFrameCardsNames.NEW_CASE) );
        errorOptionsButtonPanel.add(buttonBack);
        JButton buttonShowLog = new JButton(Messages.get("Home.ProcessManager.ShowTerminalLog"));
        buttonShowLog.addActionListener(e->{
            if(ipedStartException != null) {
                JTextArea textArea = new JTextArea();
                textArea.setText(ExceptionUtils.getStackTrace(ipedStartException));
                JOptionPane.showMessageDialog(this, textArea, ipedStartException.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        });
        errorOptionsButtonPanel.add(buttonShowLog);
        errorOptionsButtonPanel.setVisible(false);
        return errorOptionsButtonPanel;
    }

    private JPanel createSuccessOptionsButtonpanel(){
        successOptionsButtonPanel = new JPanel();
        successOptionsButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        successOptionsButtonPanel.setBackground(super.getCurrentBackGroundColor());

        JButton buttonBackCase = new JButton(Messages.get("Home.ProcessManager.BackToCaseInfo"));
        buttonBackCase.addActionListener(e->mainFrame.showPanel(MainFrameCardsNames.NEW_CASE));
        successOptionsButtonPanel.add(buttonBackCase);

        buttonOpenCase = new JButton(Messages.get("Home.ProcessManager.OpenCase"));
        buttonOpenCase.addActionListener(e->{
            Thread t = new Thread(() -> {
                ProcessManager pm = new ProcessManager();
                pm.addProcessListener(ProcessManagerContainer.this);
                pm.openSingleCase(ipedProcess.getCaseOutputPath());
            });
            t.start();
        });
        successOptionsButtonPanel.add(buttonOpenCase);

        JButton buttonShowLog = new JButton(Messages.get("Home.ProcessManager.ShowTerminalLog"));
        buttonShowLog.addActionListener(e-> JOptionPane.showMessageDialog(this, logTextArea, Messages.get("Home.ProcessManager.TerminalLog"), JOptionPane.ERROR_MESSAGE));
        successOptionsButtonPanel.add(buttonShowLog);

        JButton buttonExit = new JButton(Messages.get("Home.ProcessManager.ExitApplication"));
        buttonExit.addActionListener(e-> System.exit(0));
        successOptionsButtonPanel.add(buttonExit);

        successOptionsButtonPanel.setVisible(false);
        return successOptionsButtonPanel;
    }


    private void saveCaseInfoJsonOnCaseOutputPath(){
        CaseInfoManager ciManager = new CaseInfoManager();
        //Populate caseinfo materials with evidences info
        ciManager.castEvidenceListToMaterialsList(ipedProcess.getCaseInfo(), ipedProcess.getEvidenceList());
        //Save the CaseInfo.json on case output
        ciManager.saveCaseInfo(ipedProcess.getCaseInfo(), Paths.get(ipedProcess.getCaseOutputPath().toString(), "CaseInfo.json").toFile());
    }

    public void startProcess(){
        switchPanelTo(STARTING_PROCESS);
        saveCaseInfoJsonOnCaseOutputPath();
        Thread t = new Thread(() -> {
            try {
                ProcessManager processManager = new ProcessManager();
                processManager.addProcessListener(ProcessManagerContainer.this);
                processManager.startIpedProcess(ipedProcess, logTextArea);
            } catch (IpedStartException e) {
                ipedStartException = e;
                switchPanelTo(FAILED_PROCESS);
                e.printStackTrace();
            }
        });
        t.start();
    }

    private void switchPanelTo(String panelName){
        switch (panelName){
            case STARTING_PROCESS: {
                labelTitle.setText(Messages.get("Home.ProcessManager.StartingProcess"));
                currentLabelIcon.setIcon(startingIcon);
                processRunningPanel.setVisible(false);
                errorOptionsButtonPanel.setVisible(false);
                successOptionsButtonPanel.setVisible(false);
                break;
            }
            case RUNNING_PROCESS: {
                labelTitle.setText(Messages.get("Home.ProcessManager.ProcessRunning"));
                currentLabelIcon.setIcon(runningIcon);
                processRunningPanel.setVisible(true);
                errorOptionsButtonPanel.setVisible(false);
                successOptionsButtonPanel.setVisible(false);
                break;
            }
            case FAILED_PROCESS: {
                labelTitle.setText(Messages.get("Home.ProcessManager.ProcessFailed"));
                currentLabelIcon.setIcon(errorIcon);
                processRunningPanel.setVisible(false);
                errorOptionsButtonPanel.setVisible(true);
                successOptionsButtonPanel.setVisible(false);
                break;
            }
            case FINISHED_PROCESS: {
                labelTitle.setText(Messages.get("Home.ProcessManager.ProcessFinished"));
                currentLabelIcon.setIcon(successIcon);
                processRunningPanel.setVisible(false);
                errorOptionsButtonPanel.setVisible(false);
                successOptionsButtonPanel.setVisible(true);
                break;
            }
        }
    }

    private ImageIcon createNewButtonIcon(String imageFilename){
        Dimension iconDimension = new Dimension(400,400);
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(imageFilename)));
        Image resizedImage = icon.getImage().getScaledInstance( iconDimension.width, iconDimension.height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    /**
     * This method will be fired by ProcessManager when Iped process start
     */
    @Override
    public void processStarted() {
        try {
            Thread.sleep(3000);
            switchPanelTo(RUNNING_PROCESS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method will be fired by ProcessManager when Iped process finish
     */
    @Override
    public void processFinished() {
        switchPanelTo(FINISHED_PROCESS);
    }

    @Override
    public void caseIsOpening() {
        buttonOpenCase.setEnabled(false);
        buttonOpenCase.setText(Messages.get("Home.ProcessManager.OpenCaseMsg"));
    }

    @Override
    public void caseWasClosed() {
        buttonOpenCase.setEnabled(true);
        buttonOpenCase.setText(Messages.get("Home.ProcessManager.OpenCase"));
    }
}
