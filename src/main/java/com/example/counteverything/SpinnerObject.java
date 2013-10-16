package com.example.counteverything;

/**
 * Created by stylesuxx on 10/16/13.
 */
public class SpinnerObject {
  private int databaseId;
  private String databaseValue;

  public SpinnerObject ( int databaseId , String databaseValue ) {
      this.databaseId = databaseId;
      this.databaseValue = databaseValue;
  }

  public int getId () {
      return databaseId;
  }

  public String getValue () {
      return databaseValue;
  }

  @Override
  public String toString () {
      return databaseValue;
  }
}
