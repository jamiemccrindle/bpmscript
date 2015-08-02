/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bpmscript.process;

import java.sql.Timestamp;

/**
 * A convenience class for storing creation dates and last modified timestamps.
 * Typically used as part of composition for entities that implement this interface.
 */
public class TimeStampedIdentity extends Identity implements ITimeStampedIdentity {
	
	private static final long serialVersionUID = -2030995506141168229L;

	private Timestamp lastModified = new Timestamp(System.currentTimeMillis());
	private Timestamp creationDate = new Timestamp(System.currentTimeMillis());
	
	public TimeStampedIdentity(String id, Timestamp lastModified, Timestamp creationDate) {
		super(id);
		this.lastModified = lastModified;
		this.creationDate = creationDate;
	}
	
	public TimeStampedIdentity(ITimeStampedIdentity timeStampedIdentity) {
		this(timeStampedIdentity.getId(), timeStampedIdentity.getLastModified(), timeStampedIdentity.getCreationDate());
	}
	public Timestamp getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	public Timestamp getLastModified() {
		return lastModified;
	}
	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}
	public TimeStampedIdentity() {
		super();
	}
}
