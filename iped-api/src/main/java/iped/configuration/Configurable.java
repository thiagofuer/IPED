package iped.configuration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * This interface is used to indicate a configurable object that holds
 * configuration for some module.
 *
 * @param <AppConfig>
 *            the type used for the application configuration
 * @param <UserConfig>
 *            the type used for the user configuration
 * @author Bruno W. P. Hoelz
 * @author Luis Nassif
 */
public interface Configurable<T> extends Serializable {

    /**
     * Returns a filter to be used for resource lookup on the configuration
     * directory system
     * 
     * @return the filter to be used
     */
    public DirectoryStream.Filter<Path> getResourceLookupFilter();

    /**
     * Process the configuration resources found after applying the lookup filter.
     * 
     * @param resources
     *            the filtered configuration resources.
     */
    default public void processConfigs(List<Path> resources) throws IOException {
        for (Iterator<Path> iterator = resources.iterator(); iterator.hasNext();) {
            Path path = iterator.next();
            processConfig(path);
        }
    }

    /**
     * Process a configuration resource found after applying the lookup filter.
     * 
     * @param resource
     *            a configuration resource.
     */
    public void processConfig(Path resource) throws IOException;

    /**
     * Gets the configuration object.
     *
     * @return the configuration object.
     */
    T getConfiguration();

    /**
     * Sets the configuration object.
     *
     * @param config
     *            the configuration object.
     */
    void setConfiguration(T config);

    /**
     * Writes a configuration resource found after applying the lookup filter.
     * 
     * @param resource
     *            a configuration path where to save the necessary data.
     */
    public void save(Path resource);


    /**
     * Resets the configurable state. Configurables that implements loading tecniques from multiple
     * sources should use this to reset before reload, clearing any old loaded source.
     * 
     * @param resource
     *            a configuration path where to save the necessary data.
     */
    public void reset();

}
