package iped.app.home;/*
 * @created 07/09/2022
 * @project IPED
 * @author Thiago S. Figueiredo
 */

import iped.app.home.style.StyleManager;

import javax.swing.*;
import java.awt.*;

public abstract class DefaultPanel extends JPanel {
    protected MainFrame mainFrame;
    private static final Insets defaultInsets = StyleManager.getDefaultPanelInsets();
    private Color currentBackGroundColor = Color.white;

    public DefaultPanel(MainFrame mainFrame) {
        super();
        this.setBorder(BorderFactory.createEmptyBorder(defaultInsets.top, defaultInsets.left, defaultInsets.bottom, defaultInsets.right));
        this.mainFrame = mainFrame;
        this.setBackground(currentBackGroundColor);
        this.createAndShowGUI();
    }

    public Color getCurrentBackGroundColor(){
        return currentBackGroundColor;
    }

    protected abstract void createAndShowGUI();

}
