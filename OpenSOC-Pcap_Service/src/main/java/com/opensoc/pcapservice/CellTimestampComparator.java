package com.opensoc.pcapservice;

import java.util.Comparator;

import org.apache.hadoop.hbase.Cell;

/**
 * Comparator created for sorting pcaps cells based on the timestamp (dsc).
 * 
 * @author Sayi
 */
public class CellTimestampComparator implements Comparator<Cell> {

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  
  public int compare(Cell o1, Cell o2) {
    return Long.valueOf(o2.getTimestamp()).compareTo(o1.getTimestamp());
  }
}
