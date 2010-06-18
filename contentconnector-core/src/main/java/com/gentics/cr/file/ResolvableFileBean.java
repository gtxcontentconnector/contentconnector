package com.gentics.cr.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.util.StringUtils;

/**
 * ResolvableFileBean is used to wrap a File into a {@link CRResolvableBean}.
 * @author bigbear3001
 *
 */
public class ResolvableFileBean extends CRResolvableBean {

  /**
   * Log4 logger for error messages.
   */
  private static Logger logger = Logger.getLogger(ResolvableFileBean.class);

  /**
   * Generated serial version uid.
   */
  private static final long serialVersionUID = 4674065796284318422L;

  /**
   * contained {@link File} represented by the {@link ResolvableFileBean}.
   */
  private File file;

  /**
   * parent {@link ResolvableFileBean} set in the constructor.
   */
  private ResolvableFileBean parent = null;

  /**
   * direct children of the {@link ResolvableFileBean}.
   */
  private ResolvableFileBean[] children = null;

  /**
   * {@link Collection} with all descendants of this {@link ResolvableFileBean}.
   */
  private Collection<ResolvableFileBean> descendants = null;

  /**
   * {@link Iterator} to get an order for the descendants to check.
   */
  private Iterator<ResolvableFileBean> descendantsToCheck = null;

  /**
   * Object type for files.
   */
  private static final String FILEOBJTYPE = "10008";

  /**
   * Object type for directory.
   */
  private static final String DIROBJTYPE = "10002";

  /**
   * Generate a new {@link ResolvableFileBean} for the specified {@link File}.
   * @param fileForPath {@link File} to create the {@link ResolvableFileBean}
   * for
   */
  public ResolvableFileBean(final File fileForPath) {
    super();
    file = fileForPath;
  }

  /**
   * Generate a new {@link ResolvableFileBean} for the specified {@link File}.
   * @param fileForPath {@link File} to create the {@link ResolvableFileBean}
   * for
   * @param givenParent {@link ResolvableFileBean} creating the object.
   */
  public ResolvableFileBean(final File fileForPath,
      final ResolvableFileBean givenParent) {
    this(fileForPath);
    parent = givenParent;
  }

  /**
   * gets the parent {@link ResolvableFileBean} if available.
   * @return the parent {@link ResolvableFileBean}, null if not available.
   */
  public final ResolvableFileBean getParent() {
    return parent;
  }

  /**
   * Get all direct children of the {@link ResolvableFileBean}.
   * @return direct children of the {@link ResolvableFileBean}
   */
  public final ResolvableFileBean[] getChildren() {
    if (!file.isDirectory()) {
      return new ResolvableFileBean[0];
    } else {
      if (!checkForUpdate() && children == null) {
        File[] fetchedChildren = file.listFiles();
        children = new ResolvableFileBean[fetchedChildren.length];
        for (int i = 0; i < fetchedChildren.length; i++) {
          ResolvableFileBean resolvableChild =
            new ResolvableFileBean(fetchedChildren[i], this);
          children[i] = resolvableChild;
          registerDescendant(resolvableChild);
        }
      }
      return children;
    }
  }

  /**
   * get all registered descendants.
   * @return registered descendants
   */
  public final Collection<ResolvableFileBean> getRegisteredDescendants() {
    return descendants;
  }

  /**
   * register the given {@link ResolvableFileBean} as a descendant to this
   * {@link ResolvableFileBean} an all parents.
   * @param descendantToRegister {@link ResolvableFileBean} to register
   */
  protected final void registerDescendant(
      final ResolvableFileBean descendantToRegister) {
    if (descendants == null) {
      descendants = new Vector<ResolvableFileBean>(children.length);
    }
    descendants.add(descendantToRegister);
    if (parent != null) {
      parent.registerDescendant(descendantToRegister);
    }
  }

  /**
   * delete the {@link ResolvableFileBean} from the children and all desendant
   * relations.
   */
  public void deleteFromIndex() {
    //TODO delete from index.
  }

  /**
   * check if the information represented in the {@link ResolvableFileBean} is
   * up to date.
   * @return <code>true</code> if the information is up to date, otherwise it
   * returns <code>false</code>.
   */
  private boolean checkForUpdate() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * get the next descendant in the queue to check for an update.
   * @return descendant that should be checked for an update.
   */
  protected final ResolvableFileBean getNextDescendantToCheck() {
    if (descendantsToCheck == null || !descendantsToCheck.hasNext()) {
      descendantsToCheck =
        new Vector<ResolvableFileBean>(descendants).iterator();
    }
    if (descendantsToCheck.hasNext()) {
      return descendantsToCheck.next();
    }
    return null;
  }

  @Override
  /**
   * equals method to test if two {@link ResolvableFileBean}s are the same.
   * @param objectToCompare Object that should be compared with the
   * {@link ResolvableFileBean}
   * @return true if:<br />
   * - object is an implementation of {@link ResolvableFileBean}
   * - and the {@link File} represented by the object to compare is the same
   * {@link File} as the file represented by the actual object (this).
   * <br /><br />
   * Otherwise the method will return false.
   */
  public final boolean equals(final Object objectToCompare) {
    if (objectToCompare instanceof ResolvableFileBean) {
      ResolvableFileBean resolvableFileBean =
        (ResolvableFileBean) objectToCompare;
      return resolvableFileBean.file.equals(file);
    } else {
      return false;
    }
  }

  @Override
  public final int hashCode() {
    return file.hashCode();
  }

  @Override
  /**
   * show the name of the {@link File} as identifier.
   * @return name of the contained {@link File}
   */
  public final String toString() {
    return file.toString();
  }

  @Override
  /**
   * get an attribute from the file
   */
  public Object get(final String propertyName) {
    if ("binarycontent".equals(propertyName)) {
      return getBinaryContent();
    } else if ("obj_type".equals(propertyName)) {
      return getObjType();
    }
    return super.get(propertyName);
  }

  @Override
  /**
   * get the binary data from the file
   */
  public byte[] getBinaryContent() {
    if (FILEOBJTYPE.equals(getObjType())) {
      try {
        FileInputStream fileReader = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        for (int i = 0; i < buffer.length; i++) {
          buffer[i] = (byte) fileReader.read();
        }
        return buffer;
      } catch (FileNotFoundException e) {
        logger.error("File not found: " + file, e);
        //TODO should we remove the file from the indexes?
      } catch (IOException e) {
        logger.error("Error reading file " + file + ".");
      }
    }
    return null;
  }

  /**
   * get object type.
   * @return if file is a directory it returns "10002" else it returns
   * "10008".
   */
  public String getObjType() {
    if (file.isDirectory()) {
      return DIROBJTYPE;
    } else {
      return FILEOBJTYPE;
    }
  }

  /**
   * get unique id for file.
   * @return md5sum for absolute path of file.
   */
  public String getContentid(){
    return StringUtils.md5sum(file.getAbsolutePath());
  }

}
