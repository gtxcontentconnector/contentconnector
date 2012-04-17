package com.gentics.cr.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

import com.gentics.cr.util.AccessibleBean;

/**
 * Stores a Configuration tree.
 *     - Keys are stored in Upper Case
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class GenericConfiguration extends AccessibleBean implements Serializable {

	/**
	 * Version ID for Serialization.
	 */
	private static final long serialVersionUID = 2984152538932729448L;

	/**
	 * Properties container.
	 */
	private Properties properties;

	/**
	 * Defines how the keys in the configuration should be converted (default = uppercase).
	 */
	public enum KeyConversion {
		/**
		 * These are the values which a key can be converted to.
		 */
		UPPER_CASE, LOWER_CASE, UNCHANGED
	};

	/**
	 * Defines how the key-values are stored in the configuration.
	 */
	private KeyConversion keyHandling = KeyConversion.UPPER_CASE;

	/**
	 * Container for subconfigs.
	 */
	private Hashtable<String, GenericConfiguration> subconfigs;

	/**
	 * Create new instance of Generic Configuration.
	 */
	public GenericConfiguration() {
	}

	/**
	 * Returns the containing properties as flat Properties class.
	 *       - will not resolve to sub configurations
	 * @return properties or null if there are no containing properties
	 */
	public final Properties getProperties() {
		return this.properties;
	}

	/**
	 * Sets the properties of this configuration instance.
	 * @param props as <code>Properties</code>
	 */
	public final void setProperties(final Properties props) {
		this.properties = props;
	}

	/**
	 * This is used to modify the key-management of the configuration.
	 * 
	 * @param conversion
	 *            A KeyConversion-Attribute (either uppercase, lowercase or
	 *            unchanged).
	 */
	public final void setKeyConversion(final KeyConversion conversion) {
		if (conversion != null) {
			this.keyHandling = conversion;
		} else {
			this.keyHandling = KeyConversion.UPPER_CASE;
		}
	}

	/**
	 * Returns rebuilt property tree as flat property file.
	 * @return flattened property file
	 */
	public final Properties getRebuiltPropertyTree() {
		Properties ret = new Properties();
		if (this.properties != null) {
			for (Entry<Object, Object> e : this.properties.entrySet()) {
				ret.put(e.getKey(), e.getValue());
			}
		}
		if (this.subconfigs != null) {
			for (Entry<String, GenericConfiguration> e : this.subconfigs.entrySet()) {
				String subConfKey = e.getKey();
				GenericConfiguration subVal = e.getValue();

				for (Entry<Object, Object> se : subVal.getRebuiltPropertyTree().entrySet()) {
					String key = subConfKey + "." + se.getKey();
					ret.put(key, se.getValue());
				}
			}
		}
		return ret;
	}

	/**
	 * Returns the containing sub configurations.
	 *       
	 * @return hashtable of configurations with keys or null 
	 * if there are no containing sub configs
	 */
	public final Hashtable<String, GenericConfiguration> getSubConfigs() {
		return subconfigs;
	}

	/**
	 * Set the sub configurations for this configuration instance.
	 * @param newSubConfigs sub configurations
	 */
	public final void setSubConfigs(final Hashtable<String, GenericConfiguration> newSubConfigs) {
		subconfigs = newSubConfigs;
	}

	/**
	 * get the sub configuration with the specified key.
	 * @param key - key of the sub configuration
	 * @return sub configuration for the specified key
	 * @throws NullPointerException - in case the key doesn't exist.
	 */
	public final GenericConfiguration getSubConfig(String key) throws NullPointerException {
		return subconfigs.get(convertKey(key));
	}

	/**
	 * Set a sub configuration
	 * @param key - key to set for the sub configuration
	 * @param config - sub configuration to add with the given key
	 */
	public void setSubConfig(String key, GenericConfiguration config) {
		if (subconfigs == null) {
			subconfigs = new Hashtable<String, GenericConfiguration>();
		}
		subconfigs.put(convertKey(key), config);
	}

	/**
	 * @param key - key of the sub configuration
	 * @return true if the configuration has a sub configuration with the given key that is not null.
	 */
	public boolean hasSubConfig(String key) {
		return key != null && subconfigs != null && subconfigs.containsKey(convertKey(key))
				&& subconfigs.get(convertKey(key)) != null;
	}

	/**
	 * Returns the containing sub configurations sorted by key.
	 * @return hashtable of configurations with keys or null 
	 * if there are no containing sub configs
	 */
	public final synchronized Map<String, GenericConfiguration> getSortedSubconfigs() {
		Map<String, GenericConfiguration> ret = null;

		if (this.subconfigs != null) {
			ret = Collections.synchronizedMap(new LinkedHashMap<String, GenericConfiguration>(this.subconfigs.size()));

			Vector<String> v = new Vector<String>(this.subconfigs.keySet());
			Collections.sort(v);
			for (String key : v) {
				ret.put(key, this.subconfigs.get(key));
			}
		}
		return ret;

	}

	/**
	 * Returns size of properties in this instance. - does not count sub
	 * configurations
	 * 
	 * @return count
	 */
	public final int getPropertySize() {
		if (this.properties != null) {
			return this.properties.size();
		}
		return 0;
	}

	/**
	 * Returns size of sub configs in this instance. - does not count properties
	 * 
	 * @return count
	 */
	public final int getSubConfigSize() {
		if (this.subconfigs != null) {
			return this.subconfigs.size();
		}
		return 0;
	}

	/**
	 * Returns size of properties in this instance. - does count sub
	 * configurations
	 * 
	 * @return count
	 */
	public final int getChildSize() {
		return getSubConfigSize() + getPropertySize();
	}

	/**
	 * Gets flat properties in the resolved instance as sorted collection.
	 * 
	 * @param key for resolving the desired config instance.
	 * @return collection of properties or null if no properties set or resolved
	 *         object is not able to contain properties
	 */
	public final ArrayList<String> getPropertiesAsSortedCollection(final String key) {
		Object obj = get(key);
		if (obj != null && obj instanceof GenericConfiguration) {
			return ((GenericConfiguration) obj).getPropertiesAsSortedCollection();
		}
		return null;
	}

	/**
	 * Gets flat properties in this instance as sorted collection.
	 * 
	 * @return Collection of String or null if there are no properties
	 */
	@SuppressWarnings("unchecked")
	public final ArrayList<String> getPropertiesAsSortedCollection() {
		if (this.properties != null) {
			ArrayList<String> ret = new ArrayList<String>(this.properties.size());
			Vector v = new Vector(this.properties.keySet());
			Collections.sort(v);
			for (Object k : v) {
				ret.add(this.properties.getProperty((String) k));
			}
			return ret;
		}
		return null;

	}

	/**
	 * Gets the property to the given key. &nbsp;- will resolve sub properties
	 * like "conf1.A.1.1" to<br />
	 * &nbsp;&nbsp;this config<br />
	 * &nbsp;&nbsp;&nbsp;-config config1<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;-config A<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-config 1<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-config 1<br />
	 * &nbsp;- will resolve properties like "conf1" to <br />
	 * &nbsp;&nbsp;this config<br />
	 * &nbsp;&nbsp;&nbsp;-property config1<br />
	 * 
	 * @param resolvingKey to resolve the desired object
	 * @return property value as string or a GenericConfiguration object if key
	 *         points to a config
	 */
	public final Object get(final String resolvingKey) {
		String key = convertKey(resolvingKey);
		if (isSubKey(key)) {
			if (this.subconfigs != null) {
				GenericConfiguration subConf = this.subconfigs.get(getSubConfigKey(key));
				if (subConf != null) {
					return subConf.get(getSubKey(key));
				}
			}
		} else if (this.properties != null || this.subconfigs != null) {
			Object prop = null;
			if (properties != null) {
				prop = properties.getProperty(key);
			}
			if (prop == null && subconfigs != null) {
				prop = subconfigs.get(key);
			}
			return prop;
		}
		return null;
	}

	/**
	 * Sets the property value to the given key.<br />
	 * &nbsp;- will resolve sub properties like "conf1.A.1.1" to<br />
	 * &nbsp;&nbsp;this config<br />
	 * &nbsp;&nbsp;&nbsp;-config config1<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;-config A<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-config 1<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-config 1<br />
	 * &nbsp;- will resolve properties like "conf1" to<br />
	 * &nbsp;&nbsp;this config<br />
	 * &nbsp;&nbsp;&nbsp;-property config1<br />
	 * 
	 * @param resolvingKey
	 *            Property key as string
	 * @param value
	 *            Property value as string
	 * 
	 */
	public final void set(final String resolvingKey, final String value) {
		String key = convertKey(resolvingKey);
		if (isSubKey(key)) {
			if (this.subconfigs == null) {
				this.subconfigs = new Hashtable<String, GenericConfiguration>();
			}
			String confKey = getSubConfigKey(key);
			if (this.subconfigs.get(confKey) == null) {
				GenericConfiguration subconf = new GenericConfiguration();
				subconf.setKeyConversion(this.keyHandling);
				this.subconfigs.put(confKey, subconf);
			}
			this.subconfigs.get(confKey).set(getSubKey(key), value);
		} else {
			if (this.properties == null) {
				this.properties = new Properties();
			}
			this.properties.setProperty(key, value);
		}
	}

	/**
	 * Returns true if key is resolving to a sub configuration.
	 * 
	 * @param key resolvingKey
	 * @return true if key is resolving to a sub configuration
	 */
	private boolean isSubKey(final String key) {
		boolean ret = false;
		if (key != null) {
			String[] arr = key.split("\\.");
			if (arr != null && arr.length > 1) {
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * Gets the sub configuration key.
	 * 
	 * @param key resolvingKey
	 * @return the sub configuration key as string, null if the key does not
	 *         resolve to a sub configuration
	 */
	private String getSubKey(final String key) {
		String sub = null;

		if (key != null) {
			String[] arr = key.split("\\.");
			if (arr != null && arr.length > 1) {
				int firstpos = key.indexOf(".") + 1;
				if (key.length() > firstpos) {
					sub = key.substring(firstpos);
				}
			}
		}
		return sub;
	}

	/**
	 * Gets the key under which the sub configuration is stored in the
	 * subconfigs hashmap.
	 * 
	 * @param key resolvingKey
	 * @return sub config key as string
	 */
	private String getSubConfigKey(final String key) {
		String sub = null;

		if (key != null) {
			String[] arr = key.split("\\.");
			if (arr != null && arr.length > 1) {
				sub = arr[0];
			}
		}
		return sub;
	}

	/**
	 * Prepares key for storage and fetching - converty key to upper case.
	 * 
	 * @param key resolving key
	 * @return <code>key</code> as upper case string
	 */
	protected String convertKey(final String key) {
		switch (this.keyHandling) {
			case UPPER_CASE:
				return key.toUpperCase();

			case LOWER_CASE:
				return key.toLowerCase();

			case UNCHANGED:
			default:
				return key;
		}
	}
}
