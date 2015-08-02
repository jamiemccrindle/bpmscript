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

package org.bpmscript.timeout.quartz;

import java.util.Date;

import org.bpmscript.timeout.ITimeoutListener;
import org.bpmscript.timeout.ITimeoutManager;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

/**
 * A Quartz Timeout Manager
 */
public class QuartzTimeoutManager implements ITimeoutManager {

    public static class TimeoutJob implements Job {

        /* (non-Javadoc)
         * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
         */
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDetail jobDetail = context.getJobDetail();
            try {
                QuartzTimeoutManager manager = (QuartzTimeoutManager) context.getScheduler().getContext().get("timeoutManager");
                manager.triggerTimeout(jobDetail.getJobDataMap().get("data"));
            } catch (SchedulerException e) {
                throw new JobExecutionException(e);
            }
        }
        
    }
    
    private ITimeoutListener listener;
    private Scheduler scheduler;

    /* (non-Javadoc)
     * @see org.bpmscript.timeout.ITimeoutManager#addTimeout(java.lang.Object, long)
     */
    public void addTimeout(Object data, long delay) {
        long startTime = System.currentTimeMillis() + delay;

        SimpleTrigger trigger = new SimpleTrigger("timeout",
                                                  null,
                                                  new Date(startTime),
                                                  null,
                                                  0,
                                                  0L);
        JobDetail jobDetail = new JobDetail();
        jobDetail.getJobDataMap().put("data", data);
        jobDetail.setJobClass(TimeoutJob.class);
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param object
     */
    public void triggerTimeout(Object object) {
        this.listener.timedOut(object);
    }

    /* (non-Javadoc)
     * @see org.bpmscript.timeout.ITimeoutManager#setTimeoutListener(org.bpmscript.timeout.ITimeoutListener)
     */
    public void setTimeoutListener(ITimeoutListener listener) {
        this.listener = listener;
    }

    /* (non-Javadoc)
     * @see org.bpmscript.ILifeCycle#start()
     */
    public void start() throws Exception {
    }

    /* (non-Javadoc)
     * @see org.bpmscript.ILifeCycle#stop()
     */
    public void stop() throws Exception {
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

}
