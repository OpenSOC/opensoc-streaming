package com.opensoc.hbase;



import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;


import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

/**
 * A Storm bolt for putting data into HBase.
 * <p>
 * By default works in batch mode by enabling HBase's client-side write buffer. Enabling batch mode
 * is recommended for high throughput, but it can be disabled in {@link TupleTableConfig}.
 * <p>
 * The HBase configuration is picked up from the first <tt>hbase-site.xml</tt> encountered in the
 * classpath
 * @see TupleTableConfig
 * @see HTableConnector
 */
@SuppressWarnings("serial")
public class HBaseBolt implements IRichBolt {
  private static final Logger LOG = Logger.getLogger(HBaseBolt.class);

  protected OutputCollector collector;
  protected HTableConnector connector;
  protected TupleTableConfig conf;
  protected boolean autoAck = true;

  public HBaseBolt(TupleTableConfig conf) {
    this.conf = conf;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("rawtypes")
  
  public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    this.collector = collector;

    try {
      this.connector = new HTableConnector(conf);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    LOG.info("Preparing HBaseBolt for table: " + this.conf.getTableName());
  }

  /** {@inheritDoc} */
  
  public void execute(Tuple input) {
    try {
      this.connector.getTable().put(conf.getPutFromTuple(input));
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    if (this.autoAck) {
      this.collector.ack(input);
    }
  }

  /** {@inheritDoc} */
  
  public void cleanup() {
    this.connector.close();
  }

  /** {@inheritDoc} */
  
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
  }

  /** {@inheritDoc} */
  
  public Map<String, Object> getComponentConfiguration() {
    return null;
  }

  /**
   * @return the autoAck
   */
  public boolean isAutoAck() {
    return autoAck;
  }

  /**
   * @param autoAck the autoAck to set
   */
  public void setAutoAck(boolean autoAck) {
    this.autoAck = autoAck;
  }
}