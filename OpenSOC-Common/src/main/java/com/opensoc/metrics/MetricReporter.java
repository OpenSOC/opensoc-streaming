package com.opensoc.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MetricReporter {

	final MetricRegistry metrics = new MetricRegistry();
	private ConsoleReporter consoleReporter = null;
	private JmxReporter jmxReporter = null;
	private GraphiteReporter graphiteReporter = null;

	private Class _klas;

	public void initialize(Map config, Class klas) {

		System.out.println("===========Initializing Reporter");
		this._klas = klas;
		this.start(config);

	}

	public Counter registerCounter(String countername) {
		return metrics.counter(MetricRegistry.name(_klas, countername));
	}

	public void start(Map config) {
		try {
			if (config.get("reporter.jmx").equals("true")) {
				jmxReporter = JmxReporter.forRegistry(metrics).build();
				jmxReporter.start();
			}

			if (config.get("reporter.console").equals("true")) {
				consoleReporter = ConsoleReporter.forRegistry(metrics).build();
				consoleReporter.start(1, TimeUnit.SECONDS);
			}

			if (config.get("reporter.graphite").equals("true")) {
				String address = (String) config.get("graphite.address");
				int port = Integer.parseInt((String) config
						.get("graphite.port"));

				System.out.println("===========Graphite ADDRESS: " + address
						+ ":" + port);

				Graphite graphite = new Graphite(new InetSocketAddress(address,
						port));
				graphiteReporter = GraphiteReporter.forRegistry(metrics).build(
						graphite);
				System.out
						.println("---------******STARTING GRAPHITE*********---------");
				graphiteReporter.start(1, TimeUnit.SECONDS);
			} else
				System.out
						.println("---------******GRAPHITE DISABLED*********---------");
		} catch (Exception e) {
			e.printStackTrace();

			for (Object key : config.values())
				System.out.println(key + "---" + config.get(key));
		}

	}

}
