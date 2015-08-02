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
package org.bpmscript.web;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Formatter utility class, formats things like dates and strings
 */
public class Formatter {
	
	private String dateFormat = "{0,date,dd/MM/yy hh:mm:ss}";
	
	/**
	 * Shorted a string if it is over a particular size and replace any removed
	 * characters with elipses e.g.
	 * 
	 * shorten("Hello World!", "...", 5) will return "Hello...", while
	 * shorten("Hello", "...", 5) will return "Hello".
	 * @param value
	 * @param elipses
	 * @param max
	 * @return
	 */
	public String shorten(String value, String elipses, int max) {
		if(value == null) return value;
		if(value.length() < max) return value;
		return value.substring(0, max) + elipses;
	}
	
	/**
	 * Format a string using a message format
	 * @param pattern the message format pattern
	 * @param value the value place in the pattern
	 * @return the formatted string
	 * @see MessageFormat#format(String, Object...)
	 */
	public String format(String pattern, Object value) {
		return MessageFormat.format(pattern, value);
	}
	/**
	 * Format a date using a default date format
	 * @param date the date to format
	 * @return the formatted date
	 */
	public String formatDate(Date date) {
		return MessageFormat.format(dateFormat, date);
	}
	
	/**
	 * Set up the date format to use
	 * @param dateFormat the date format to use for "formatDate"
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
}
