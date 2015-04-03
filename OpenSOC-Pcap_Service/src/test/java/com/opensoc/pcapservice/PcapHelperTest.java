/**
 * 
 */
package com.opensoc.pcapservice;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.core.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.opensoc.pcapservice.PcapHelper;
import com.opensoc.pcapservice.PcapHelper.TimeUnit;

// TODO: Auto-generated Javadoc
/**
 * The Class PcapHelperTest.
 * 
 * @author Sayi
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PcapHelper.class)
public class PcapHelperTest {

  /**
   * Sets the up.
   * 
   * @throws Exception
   *           the exception
   */
  @Before
  public void setUp() throws Exception {
    PowerMockito.spy(PcapHelper.class);
  }

  /**
   * Tear down.
   * 
   * @throws Exception
   *           the exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Input time is in SECONDS and data creation time is in SECONDS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_seconds_seconds() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.SECONDS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 1111122222L; // input time in seconds
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(1111122222L == time);
  }

  /**
   * Input time is in MILLIS and data creation time is in SECONDS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_millis_seconds() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.SECONDS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 1111122222333L; // input time in millis
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(1111122222L == time);
  }

  /**
   * Input time is in MICROS and data creation time is in SECONDS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_micros_seconds() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.SECONDS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 1111122222333444L; // input time in micros
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(1111122222L == time);
  }

  /**
   * Input time is in SECONDS and data creation time is in MILLIS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_seconds_millis() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MILLIS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 1111122222L; // input time in seconds
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(1111122222000L == time);
  }

  /**
   * Input time is in MILLIS and data creation time is in MILLIS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_millis_millis() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MILLIS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 111112222233L; // input time in millis
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(111112222233L == time);
  }

  /**
   * Input time is in MICROS and data creation time is in MILLIS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_micros_millis() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MILLIS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 111112222233344L; // input time in micros
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(111112222233L == time);
  }

  /**
   * Input time is in SECONDS and data creation time is in MICROS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_seconds_micros() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MICROS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 1111122222L; // input time in seconds
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(1111122222000000L == time);
  }

  /**
   * Input time is in SECONDS and data creation time is in MICROS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_seconds_micros_random() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MICROS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 13388; // input time in seconds
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(13388000000L == time);
  }

  /**
   * Input time is in MILLIS and data creation time is in MICROS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_millis_micros() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MICROS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 111112222233L; // input time in millis
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(111112222233000L == time);
  }

  /**
   * Input time is in MICROS and data creation time is in MICROS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_micros_micros() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MICROS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 1111122222334444L; // input time in micros
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(1111122222334444L == time);
  }

  /**
   * Input time is in SECONDS and data creation time is in MICROS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_seconds_micros_0() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MICROS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 0; // input time in micros
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(0 == time);
  }

  /**
   * Input time is in SECONDS and data creation time is in MICROS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_seconds_micros_1() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MICROS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = 1; // input time in micros
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(1000000L == time);
  }

  /**
   * Input time is in SECONDS and data creation time is in MICROS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_seconds_micros_decimal() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MICROS);
    PowerMockito.verifyNoMoreInteractions();

    long inputTime = 13; // input time in seconds (double to long type casting)
    long time = PcapHelper.convertSecondsToDataCreationTimeUnit(inputTime);

    Assert.isTrue(13000000L == time);
  }

  /**
   * Input time is in SECONDS and data creation time is in MICROS.
   */
  @Test
  public void test_convertToDataCreationTimeUnit_() {
    PowerMockito.when(PcapHelper.getDataCreationTimeUnit()).thenReturn(
        TimeUnit.MICROS);
    PowerMockito.verifyNoMoreInteractions();

    long endTime = (long) 111.333; // input time in seconds (double to long type
                                   // casting)
    long time = PcapHelper.convertToDataCreationTimeUnit(endTime);

    Assert.isTrue(111000000L == time);
  }

  /**
   * Test_get data creation time unit.
   */
  @Test
  public void test_getDataCreationTimeUnit() {
    TimeUnit dataCreationTimeUnit = PcapHelper.getDataCreationTimeUnit();
    Assert.isTrue(TimeUnit.MILLIS == dataCreationTimeUnit);
  }

  /**
   * Test_reverse key_valid.
   */
  @Test
  public void test_reverseKey_valid() {
    String key = "162.242.152.24-162.242.153.12-TCP-38190-9092";
    String reversekey = PcapHelper.reverseKey(key);
    Assert.isTrue("162.242.153.12-162.242.152.24-TCP-9092-38190"
        .equals(reversekey));
  }

  /**
   * Test_reverse key_valid_with fragment.
   */
  @Test
  public void test_reverseKey_valid_withFragment() {
    String key = "162.242.152.24-162.242.153.12-TCP-38190-9092-fragmentId";
    String reversekey = PcapHelper.reverseKey(key);
    Assert.isTrue("162.242.153.12-162.242.152.24-TCP-9092-38190"
        .equals(reversekey));
  }

  /**
   * Test_reverse key_in valid.
   */
  @Test
  public void test_reverseKey_inValid() {
    String key = "162.242.152.24-162.242.153.12-TCP-38190-9092-ipId-fragmentId-extra";
    String reversekey = PcapHelper.reverseKey(key);
    Assert.isTrue("".equals(reversekey));
  }

  /**
   * Test_reverse key_as list.
   */
  @Test
  public void test_reverseKey_asList() {
    String[] keys = {
        "162.242.152.24-162.242.153.12-TCP-38190-9092-fragmentId",
        "162.242.152.24-162.242.153.12-UDP-38190-9092" };

    List<String> reverseKeys = PcapHelper.reverseKey(Arrays.asList(keys));

    Assert.isTrue("162.242.153.12-162.242.152.24-TCP-9092-38190"
        .equals(reverseKeys.get(0)));
    Assert.isTrue("162.242.153.12-162.242.152.24-UDP-9092-38190"
        .equals(reverseKeys.get(1)));
  }

}
