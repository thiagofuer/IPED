package iped.app.home.configurables;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import iped.app.home.DefaultPanel;
import iped.app.home.MainFrame;
import iped.app.home.configurables.bean.BeanConfigurablePanel;
import iped.configuration.Configurable;
import iped.engine.config.CategoryConfig;
import iped.engine.config.CategoryToExpandConfig;
import iped.engine.config.ExternalParsersConfig;
import iped.engine.config.MakePreviewConfig;
import iped.engine.config.ParsersConfig;
import iped.engine.config.RegexTaskConfig;
import iped.engine.task.carver.XMLCarverConfiguration;
import iped.parsers.external.ExternalParser;
import iped.utils.UTF8Properties;

/**
 * @created 10/11/2022
 * @project IPED
 * @author Patrick Dalla Bernardina
 */

public abstract class ConfigurablePanel extends DefaultPanel implements DocumentListener{
    protected Configurable<?> configurable;
    protected SpringLayout layout;
    protected boolean changed=false;
    
    List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();    

    protected ConfigurablePanel(Configurable<?> configurable, MainFrame mainFrame) {
        super(mainFrame);
        this.configurable = configurable;
    }

    /**
     * Factory method to instantiate an ConfigurablePanel suitable to the configurable object
     * @param configurable - the configurable object that the created ConfigurablePanel will handle.
     * @param mainFrame - the main frame of the panel.
     */
    public static ConfigurablePanel createConfigurablePanel(Configurable<?> configurable, MainFrame mainFrame) {
        Object config = configurable.getConfiguration();
        ConfigurablePanel result=null;
        
        if(config instanceof UTF8Properties) {
            result = new UTF8PropertiesConfigurablePanel((Configurable<UTF8Properties>)configurable, mainFrame);
        }else if(configurable instanceof ParsersConfig) {
            result = new ParsersConfigurablePanel((ParsersConfig) configurable, mainFrame);
        }else if(configurable instanceof ExternalParsersConfig) {
            result = new ParsersConfigurablePanel((ExternalParsersConfig) configurable, mainFrame);
        }else if(configurable instanceof CategoryConfig) {
            result = new SetCategoryConfigurablePanel((CategoryConfig) configurable, mainFrame);
        }else if(config instanceof String) {
            /*try to see if it is a json object*/
            boolean isJson = false;
            String strConfig = (String) config;
            if(strConfig.trim().startsWith("{")) {
                JSONParser parser = new JSONParser();
                try {
                    parser.parse(strConfig);
                    isJson=true;
                } catch (ParseException e) {
                }
            }
            
            if(isJson) {
                result = new JSONConfigurablePanel((Configurable<String>)configurable, mainFrame);
            }else {
                /*try to see if it is a xml object*/
                try {
                    if(strConfig.trim().startsWith("<?xml")) {
                        result = new XMLConfigurablePanel((Configurable<String>)configurable, mainFrame);
                    }
                }finally {
                    if(result==null) {
                        result = new TextConfigurablePanel((Configurable<String>)configurable, mainFrame);
                    }
                }
            }
        }else if(configurable instanceof CategoryToExpandConfig) {
            result = new CategoryToExpandConfigPanel((CategoryToExpandConfig) configurable, mainFrame);
        }else if(configurable instanceof MakePreviewConfig) {
            result = new MakePreviewConfigurablePanel((MakePreviewConfig) configurable, mainFrame);
        }else if(config instanceof XMLCarverConfiguration) {
            result = new XMLCarverConfigurablePanel((Configurable<XMLCarverConfiguration>)configurable, mainFrame);
        }else if(configurable.getClass().equals(RegexTaskConfig.class)) {
            result = new RegexConfigurablePanel((Configurable<?>)configurable, mainFrame);
        }else if(config instanceof Collection<?>) {
            Type type;
            try {
                type = configurable.getClass().getMethod("getConfiguration").getGenericReturnType();
                if(type instanceof ParameterizedType) {
                    ParameterizedType ptype = (ParameterizedType) type;
                    Type[] typeArguments = ptype.getActualTypeArguments();
                    if(typeArguments[0].getTypeName().equals(String.class.getCanonicalName())) {
                        result = new StringSetConfigurablePanel((Configurable<HashSet<String>>)configurable, mainFrame);
                    }
                }
            } catch (NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
        if(result==null) {
            result = new BeanConfigurablePanel((Configurable<?>)configurable, mainFrame);
        }

        return result;
    }

    /**
     * Creates the UI objects of the panel.
     * Every editable UI must install "this" object as a DocumentListener to keep track of changes
     */
    abstract public void createConfigurableGUI();

    /**
     * Applies the changes made on UI objects to the underlying configurable object
     */
    abstract public void applyChanges() throws ConfigurableValidationException;

    @Override
    protected void createAndShowGUI() {
        layout = new SpringLayout();

        this.setLayout(layout);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changed=true;
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changed=true;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        changed=true;
    }

    public boolean hasChanged() {
        return changed;
    }

    public Configurable<?> getConfigurable() {
        return configurable;
    }

    public void setConfigurable(Configurable<?> configurable) {
        this.configurable = configurable;
    }
    
    public void fireChangeListener(ChangeEvent e) {
        for (Iterator iterator = changeListeners.iterator(); iterator.hasNext();) {
            ChangeListener changeListener = (ChangeListener) iterator.next();
            changeListener.stateChanged(e);            
        }
    }
    
    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }
}