package ca.corefacility.bioinformatics.irida.config.services;

import ca.corefacility.bioinformatics.irida.plugins.IridaPlugin;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration file for loading IRIDA plugins
 */
@Configuration
public class IridaPluginConfig {

	private static final Logger logger = LoggerFactory.getLogger(IridaPluginConfig.class);

	private static final Path PIPELINE_PLUGIN_PATH = Paths.get("/etc/irida/plugins");

	/**
	 * Get the list of IRIDA pipeline plugins
	 *
	 * @return a {@link IridaPluginList} containing all the loaded
	 *         {@link IridaPlugin}
	 */
	@Bean(name = "iridaPipelinePlugins")
	public IridaPluginList iridaPipelinePlugins() {
		PluginManager pluginManager = new DefaultPluginManager(PIPELINE_PLUGIN_PATH);

		// TODO: Add exception management here to more gracefully handle failed pipeline
		// loads
		pluginManager.loadPlugins();
		pluginManager.startPlugins();

		List<IridaPlugin> extensions = getValidPlugins(pluginManager.getExtensions(IridaPlugin.class));

		logger.debug("Loaded " + extensions.size() + " pipeline plugins.");

		return new IridaPluginList(extensions);
	}

	/**
	 * Filters the passed list of {@link IridaPlugin}s to only include those plugins
	 * that are properly defined.
	 * 
	 * @param plugins The initial list of {@link IridaPlugin}s to search through.
	 * @return A list of {@link IridaPlugin}s that properly implement
	 *         {@link IridaPlugin}.
	 */
	private List<IridaPlugin> getValidPlugins(List<IridaPlugin> plugins) {
		List<IridaPlugin> validPlugins = Lists.newArrayList();

		// get all declared methods from IridaPlugin interface to use for verifying that the passed plugins implement all these methods
		// we are skipping the static method 'getWorkflowsPath()' and any default methods
		List<Method> iridaPluginInterfaceMethods = Arrays.stream(IridaPlugin.class.getDeclaredMethods())
				.filter(t -> !t.isDefault() && !t.getName().equals("getWorkflowsPath")).collect(Collectors.toList());

		// for each plugin, verify it implements all the required methods from the IridaPlugin interface
		for (IridaPlugin plugin : plugins) {
			boolean foundAllMethods = true;

			for (Method method : iridaPluginInterfaceMethods) {
				if (!isMethodImplemented(method, plugin.getClass())) {
					logger.trace("Method [" + method + "] is not implemented in class [" + plugin.getClass() + "]");
					foundAllMethods = false;
					break;
				}
			}

			if (foundAllMethods) {
				validPlugins.add(plugin);
			} else {
				logger.error("Plugin [" + plugin.getClass()
						+ "] does not properly implement all required methods. Disabling the plugin.");
			}
		}

		return validPlugins;
	}

	/**
	 * Checks if the given method is implemented in the passed class or any superclass.
	 * 
	 * @param method The method to check.
	 * @param clazz  The class to check.
	 * @return True if the method is defined, False otherwise.
	 */
	private boolean isMethodImplemented(Method method, Class<?> clazz) {
		// base case, if no class there is no method
		if (clazz == null) {
			return false;
		} else {
			try {
				Method pluginMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
				return pluginMethod != null;
			} catch (NoSuchMethodException | SecurityException e) {
				// if method is not implemented strictly on this class we need to move up the
				// hierarchy and check if method is implemented on superclass
				return isMethodImplemented(method, clazz.getSuperclass());
			}
		}
	}

	/**
	 * Class containing the list of all loaded {@link IridaPlugin}
	 */
	public class IridaPluginList {
		private List<IridaPlugin> plugins;

		public IridaPluginList(List<IridaPlugin> plugins) {
			this.plugins = plugins;
		}

		public List<IridaPlugin> getPlugins() {
			return plugins;
		}
	}

}
