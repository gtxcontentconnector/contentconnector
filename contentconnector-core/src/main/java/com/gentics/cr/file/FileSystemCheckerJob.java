package com.gentics.cr.file;

/**
 * Job to check for updates in the file system for the {@link FileSystemChecker}
 * which controls the jobs.
 * @author bigbear3001
 *
 */
public class FileSystemCheckerJob extends Thread {

  /**
   * {@link FileSystemChecker} we are currently monitoring.
   */
  private static FileSystemChecker fileSystemChecker;

  /**
   * when set to true the update check stops.
   */
  private boolean stopped = false;

  /**
   * initialize a new {@link FileSystemCheckerJob} for the specified
   * {@link FileSystemChecker}.
   * @param givenFileSystemChecker {@link FileSystemChecker} we should check the
   * entries for.
   */
  protected FileSystemCheckerJob(
      final FileSystemChecker givenFileSystemChecker) {
    fileSystemChecker = givenFileSystemChecker;
  }

  /**
   * initialize a new {@link FileSystemCheckerJob} for the specified
   * {@link FileSystemChecker}.
   * @param givenFileSystemChecker {@link FileSystemChecker} we should check the
   * entries for.
   * @param name name to set for the {@link Thread}
   */
  protected FileSystemCheckerJob(
      final FileSystemChecker givenFileSystemChecker, final String name) {
    this(givenFileSystemChecker);
    setName(name);
  }

  @Override
  /**
   * get the next root element to check an there get the next element.
   */
  public final void run() {
    while (!stopped) {
      ResolvableFileBean rootElement =
        fileSystemChecker.getNextElementToIndex();
        //Fill children of the root element if not already done.
      rootElement.getChildren();
      if (!stopped) {
        ResolvableFileBean updateElement =
          rootElement.getNextDescendantToCheck();
        updateElement.getChildren();
      }
    }
  }

  /**
   * interrupt current {@link Thread} and stop it.
   */
  public final void stopUpdateCheck() {
    Thread current = Thread.currentThread();
    current.interrupt();
    stopped = true;
  }
}
