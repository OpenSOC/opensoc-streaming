/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opensoc.enrichment.adapters.threat;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensoc.enrichment.interfaces.EnrichmentAdapter;

public abstract class AbstractThreatAdapter implements EnrichmentAdapter,Serializable{

	
	private static final long serialVersionUID = 1524030932856141771L;
	protected static final Logger LOG = LoggerFactory
			.getLogger(AbstractThreatAdapter.class);
	
	abstract public boolean initializeAdapter();

}
