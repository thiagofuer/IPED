package iped.app.home;

/*
 * @created 05/09/2022
 * @project IPED
 * @author Thiago S. Figueiredo
 */

import iped.app.home.style.StyleManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Objects;

/**
 * This is the Home Panel - the App start page
 * here we got the app logo and first options
 */
public class HomePanel extends DefaultPanel {

    public HomePanel(MainFrame mainFrame) {
        super(mainFrame);
    }

    /**
     * Prepare everything to be displayed
     */
    protected void createAndShowGUI(){
        this.setLayout(new GridBagLayout());
        this.setBackground(Color.white);
        createAndSetupAppLogo();
        createAndSetupOptionsButtons();
    }

    /**
     * Create and setup IPED Application Logo panel
     */
    private void createAndSetupAppLogo(){
        String IPEDLogoFilename = "IPED-logo_lupa.png";
        ImageIcon imgLogo = createNewButtonIcon(IPEDLogoFilename, new Dimension(758, 151));
        JLabel labelLogo = new JLabel(imgLogo, JLabel.CENTER);
        labelLogo.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (labelLogo.getIcon() != null) {
                    int padding = 300;
                    //Image size proportion to calculate resize maintaining image proportion
                    double proportion = 0.199;
                    int newWidth = labelLogo.getWidth() - padding;
                    int maxWidht = 1200;
                    newWidth = Math.min(maxWidht, newWidth);
                    double height = (newWidth * proportion );
                    Dimension resizedDimension = new Dimension(newWidth, (int) height);
                    labelLogo.setIcon( createNewButtonIcon(IPEDLogoFilename, resizedDimension) );
                }
            }
        });
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_START;
        c.weightx = 0.5;
        c.weighty=0.2;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.gridheight = 1;
        Insets inset = StyleManager.getDefaultPanelInsets();
        c.insets = new Insets(inset.top,inset.left,2,inset.right);
        this.add(labelLogo, c);
    }

    /**
     * Create and set up the application home buttons (new case, open case and config button)
     */
    private void createAndSetupOptionsButtons(){
        //default app inset
        Insets inset = StyleManager.getDefaultPanelInsets();
        GridBagConstraints c = getOptionsButtonsGridBagConstraints(GridBagConstraints.LINE_START, 0, inset.left, 2);
        this.add( getNewOptionsButton("INICIAR NOVO CASO", "newcase.png", MainFrameCardsNames.NEW_CASE), c );
        c = getOptionsButtonsGridBagConstraints(GridBagConstraints.CENTER, 1,2 ,2);
        this.add(getNewOptionsButton("ABRIR CASO", "opencase.png", MainFrameCardsNames.OPEN_CASE), c);
        c = getOptionsButtonsGridBagConstraints(GridBagConstraints.LINE_END, 2, 2, inset.right);
        this.add(getNewOptionsButton("CONFIGURAÇÕES", "config.png", MainFrameCardsNames.CONFIG), c);
    }

    /**
     * Create a GridBagConstraints instance to be used with option buttons
     * @param anchor - the item anchor positioning
     * @param gridx - grid x positioning
     * @return GridBagConstraints - A GridBagConstraints instance
     */
    private GridBagConstraints getOptionsButtonsGridBagConstraints(int anchor, int gridx, int paddingLeft, int paddingRight) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = anchor;
        c.fill = GridBagConstraints.BOTH;
        //Don`t know why, but center cell (cell number 1) is not resized equally if used same weight
        c.weightx = (gridx == 1 )? 0.5 : 0.3;
        c.weighty = 0.3;
        c.gridx = gridx;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(2,paddingLeft,100,paddingRight);
        return c;
    }

    /**
     * Create a new JButton with default style and configuration for HomePanel
     * @param buttonText - The text to be displayed on JButton
     * @param iconName - The icon filename  to be used on JButton
     * @param destination - The card destination of button @see MainFrameCardsNames
     * @return - a new JButton to be used as HomePanel Button
     */
    private JButton getNewOptionsButton(String buttonText, String iconName , MainFrameCardsNames destination){
        JButton optionButton = new JButton(buttonText);
        optionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        optionButton.setBorderPainted(false);
        optionButton.setFont(StyleManager.getHomeButtonFont());
        //Button label alignment
        optionButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        optionButton.setHorizontalTextPosition(SwingConstants.CENTER);
        //set button click action
        optionButton.addActionListener( e -> mainFrame.showPanel(destination));
        //set button icon with default size
        optionButton.setIcon( createNewButtonIcon(iconName, new Dimension(152,152)) );
        //A listener to resize button icon as size as panel size
        optionButton.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (optionButton.getIcon() != null && optionButton.getIcon().getIconHeight() != optionButton.getHeight()) {
                    int padding = 150;
                    //we use the smaller button size to determine the icon size because the button not necessarily is redimensioned with same width and height
                    int smallerButtonSize = Math.min(optionButton.getHeight(), optionButton.getWidth());
                    Dimension resizedDimension = new Dimension(smallerButtonSize - padding,smallerButtonSize - padding);
                    optionButton.setIcon( createNewButtonIcon(iconName, resizedDimension) );
                }
            }
        });
        return optionButton;
    }


    /**
     * create a new ImageIcon instance with proper size to the HomePanel
     * @param imageFilename - The icon filename  to be used on JButton
     * @return ImageIcon - A new ImageIcon instance with proper size to the HomePanel
     */
    private ImageIcon createNewButtonIcon(String imageFilename, Dimension iconDimension){
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(imageFilename)));
        Image resizedImage = icon.getImage().getScaledInstance( iconDimension.width, iconDimension.height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

}