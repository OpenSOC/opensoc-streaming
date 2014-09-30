 package com.opensoc.pcap;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.krakenapps.pcap.packet.PcapPacket;

// TODO: Auto-generated Javadoc
/**
 * The Class PcapMerger.
 * 
 * @author sheetal
 * @version $Revision: 1.0 $
 */
public final class PcapMerger {

  /** The Constant LOG. */
  private static final Logger LOG = Logger.getLogger(PcapMerger.class);

  /**
   * Instantiates a new pcap merger.
   */
  private PcapMerger() { // $codepro.audit.disable emptyMethod
  }

  /**
   * Merge two pcap byte arrays.
   * 
   * @param baos
   *          the baos
   * @param pcaps
   *          the pcaps
   * 
   * @throws IOException
   *           if there is no byte array, no access permission, or other io
   *           related problems.
   */
  // public static void merge(byte[] to, byte[] from) throws IOException {
  // PcapByteInputStream is = null;
  // PcapByteOutputStream os = null;
  // ByteArrayOutputStream baos = null;
  // try {
  // is = new PcapByteInputStream(from);
  // baos = new ByteArrayOutputStream();
  // os = new PcapByteOutputStream(baos, is.getGlobalHeader());
  //
  // writePacket(is, os);
  // } finally {
  // closeInput(is);
  // if (baos != null) {
  // baos.close();
  // }
  // closeOutput(os);
  // }
  // }

  public static void merge(ByteArrayOutputStream baos, List<byte[]> pcaps)
      throws IOException {
    PcapByteInputStream is = null;
    PcapByteOutputStream os = null;
    try {
      int i = 1;
      for (byte[] pcap : pcaps) {
        is = new PcapByteInputStream(pcap);
        if (i == 1) {
          os = new PcapByteOutputStream(baos, is.getGlobalHeader());
        }

        writePacket(is, os);
        i++;
        closeInput(is);
      }
    } finally {
      if (baos != null) {
        baos.close();
      }
      closeOutput(os);
    }
  }

  /**
   * Merge byte array1 with byte array2, and write to output byte array. It
   * doesn't hurt original pcap dump byte arrays.
   * 
   * @param baos
   *          the baos
   * @param pcaps
   *          the pcaps
   * 
   * @throws IOException
   *           if there are no source byte arrays, have no read and/or write
   *           permissions, or anything else.
   */
  public static void merge(ByteArrayOutputStream baos, byte[]... pcaps) // $codepro.audit.disable
                                                                        // overloadedMethods
      throws IOException {
    merge(baos, Arrays.asList(pcaps));

  }

  /**
   * Write packet.
   * 
   * @param is
   *          the is
   * @param os
   *          the os
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private static void writePacket(PcapByteInputStream is,
      PcapByteOutputStream os) throws IOException {
    PcapPacket packet = null;
    try {
      while (true) {
        packet = is.getPacket();
        if (packet == null) {
          break;
        }
        os.write(packet);
      }
    } catch (EOFException e) {
      LOG.debug("Ignorable exception ", e);
    }

  }

  /**
   * Close input.
   * 
   * @param is
   *          the is
   */
  private static void closeInput(PcapByteInputStream is) {
    if (is == null) {
      return;
    }
    try {
      is.close(); // $codepro.audit.disable closeInFinally
    } catch (IOException e) {
      LOG.error("Failed to close input stream", e);
    }
  }

  /**
   * Close output.
   * 
   * @param os
   *          the os
   */
  private static void closeOutput(PcapByteOutputStream os) {
    if (os == null) {
      return;
    }
    try {
      os.close();
    } catch (IOException e) {
      LOG.error("Failed to close output stream", e);

    }
  }

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void main(String[] args) throws IOException {
    byte[] b1 = FileUtils.readFileToByteArray(new File(
        "/Users/sheetal/Downloads/constructedTcpDump.1.pcap"));
    byte[] b2 = FileUtils.readFileToByteArray(new File(
        "/Users/sheetal/Downloads/constructedTcpDump.2.pcap"));
    byte[] b3 = FileUtils.readFileToByteArray(new File(
        "/Users/sheetal/Downloads/constructedTcpDump.3.pcap"));

    ByteArrayOutputStream boas = new ByteArrayOutputStream(); // $codepro.audit.disable
                                                              // closeWhereCreated
    PcapMerger.merge(boas, b1, b2, b3);

    FileUtils.writeByteArrayToFile(new File(
        "/Users/sheetal/Downloads/constructedTcpDump.automerged.1.2.pcap"),
        boas.toByteArray(), false);

  }
}
