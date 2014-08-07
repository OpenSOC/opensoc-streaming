package com.opensoc.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.GraphiteReporter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MetricReporter {

	final MetricRegistry metrics = new MetricRegistry();
	private ConsoleReporter consoleReporter = null;
	private JmxReporter jmxReporter = null;
	private GraphiteReporter graphiteReporter = null;

	private Set<String> known_counters = new HashSet<String>();
	private Map<String, Counter> counterMap = new HashMap<String, Counter>();

	public void initialize(Properties props, Class klas) {

		for (Object key : props.keySet()) {

			if (props.getProperty((String) key) == "true")
				known_counters.add((String) key);

			counterMap.put((String) key,
					metrics.counter(MetricRegistry.name(klas, (String) key)));

			this.start(props);

		}
	}

	public void incCounter(String counterName) {

		if (known_counters.contains(counterName))
			counterMap.get(counterName).inc();

	}

	public void decCounter(String counterName) {

		if (known_counters.contains(counterName))
			counterMap.get(counterName).dec();

	}

	public void start(Properties props) {
		if (props.getProperty(
				"com.opensoc.metrics.TelemetryParserBolt.reporter.jmx").equals(
				"true"))
			jmxReporter.start();

		if (props.getProperty(
				"com.opensoc.metrics.TelemetryParserBolt.reporter.console")
				.equals("true"))
			consoleReporter.start(1, TimeUnit.SECONDS);

		if (props.getProperty(
				"com.opensoc.metrics.TelemetryParserBolt.reporter.graphite")
				.equals("true"))
			graphiteReporter.start(1, TimeUnit.SECONDS);

	}

}
