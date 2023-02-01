package iped.app.home.configurables;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.xml.sax.SAXException;

import iped.app.home.MainFrame;
import iped.app.ui.controls.textarea.XmlEditorKit;
import iped.configuration.Configurable;import iped.engine.task.carver.XMLCarverConfiguration;

public class XMLCarverConfigurablePanel extends TextConfigurablePanel {
    XMLCarverConfiguration config;

    protected XMLCarverConfigurablePanel(Configurable<XMLCarverConfiguration> configurable, MainFrame mainFrame) {
        super(configurable, mainFrame);
        config=configurable.getConfiguration();
    }

    @Override
    public void createConfigurableGUI() {
        super.createConfigurableGUI();
        XmlEditorKit xek = new XmlEditorKit();
        xek.setSchema(XMLCarverConfiguration.xsdFile);
        textArea.setEditorKitForContentType("text/xml", xek);
        textArea.setContentType("text/xml");
        textArea.getDocument().removeDocumentListener(this);
        textArea.setText(config.getXMLString());
        textArea.getDocument().addDocumentListener(this);

    }

    @Override
    public void applyChanges() throws ConfigurableValidationException{
        try {
            config.loadXMLConfigFile(textArea.getText());
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new ConfigurableValidationException("Erro de sintaxe no XML", e);
        }        
    }

}
