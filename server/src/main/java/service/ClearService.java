package service;

import dataaccess.*;

public class ClearService {
  private final DataAccess dataAccess;

  public ClearService(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public void clearApplication() throws DataAccessException {
    dataAccess.clear();
  }
}