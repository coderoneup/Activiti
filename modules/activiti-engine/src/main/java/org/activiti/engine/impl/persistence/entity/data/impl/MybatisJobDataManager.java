/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.persistence.entity.data.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.JobEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.JobDataManager;
import org.activiti.engine.runtime.Job;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class MybatisJobDataManager extends AbstractDataManager<JobEntity> implements JobDataManager {
  
  public MybatisJobDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends JobEntity> getManagedEntityClass() {
    return JobEntityImpl.class;
  }
  
  @Override
  public JobEntity create() {
    return new JobEntityImpl();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsToExecute(Page page) {
    return getDbSqlSession().selectList("selectJobsToExecute", null, page);
  }

  @Override
  public List<JobEntity> findJobsByExecutionId(final String executionId) {
    return getList("selectJobsByExecutionId", executionId, new CachedEntityMatcher<JobEntity>() {
      @Override
      public boolean isRetained(JobEntity jobEntity) {
        return jobEntity.getExecutionId() != null && jobEntity.getExecutionId().equals(executionId);
      }
    }, true);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByProcessInstanceId(final String processInstanceId) {
    return getDbSqlSession().selectList("selectJobsByProcessInstanceId", processInstanceId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<JobEntity> findExpiredJobs(Page page) {
    Date now = getClock().getCurrentTime();
    return getDbSqlSession().selectList("selectExpiredJobs", now, page);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    final String query = "selectJobByQueryCriteria";
    return getDbSqlSession().selectList(query, jobQuery, page);
  }
  
  @Override
  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectJobCountByQueryCriteria", jobQuery);
  }

  @Override
  public void updateJobTenantIdForDeployment(String deploymentId, String newTenantId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateJobTenantIdForDeployment", params);
  }
  
  @Override
  public void unacquireJob(String jobId) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("id", jobId);
    getDbSqlSession().update("unacquireJob", params);
  }
  
}