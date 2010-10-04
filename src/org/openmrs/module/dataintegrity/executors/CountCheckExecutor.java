package org.openmrs.module.dataintegrity.executors;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.module.dataintegrity.IntegrityCheck;
import org.openmrs.module.dataintegrity.DataIntegrityConstants;
import org.openmrs.module.dataintegrity.IntegrityCheckUtil;
import org.openmrs.module.dataintegrity.db.DataIntegrityDAO;
import org.openmrs.util.OpenmrsUtil;

public class CountCheckExecutor implements ICheckExecutor {
	private IntegrityCheck check;
	private DataIntegrityDAO dao;
	private String parameterValues;
	private List<Object[]> failedRecords;
	private boolean checkPassed;
	
	public CountCheckExecutor(DataIntegrityDAO dao) {
		this.dao = dao;
	}

	@SuppressWarnings("unchecked")
	public void executeCheck() throws Exception {
		try {
			int failDirective = Integer.valueOf(this.check.getFailDirective());
			String checkCode = IntegrityCheckUtil.getModifiedCheckCode(
					this.check.getCheckCode(),
					this.check.getRepairParameters(), this.parameterValues);
			SessionFactory factory = this.dao.getSessionFactory();
			SQLQuery query = factory.getCurrentSession().createSQLQuery(checkCode);
			List<Object[]> resultList = query.list();
			
			if (resultList.size() > 0) {
				int columnCount;
				try {
					Object[] records = resultList.get(0);
					columnCount = records.length;
				} catch (Exception e) {
					columnCount = 1;
				}
				for (int i=0; i<resultList.size(); i++) {
					if (columnCount > 1) {
						Object[] objectArray = new Object[columnCount];
						for (int j=0; j<columnCount; j++) {
							Object value = resultList.get(i)[j];
							objectArray[j] = value;
						}
						failedRecords.add(objectArray);
					} else {
						Object value = resultList.get(i);
						Object[] objectArray = new Object[1];
						objectArray[0] = value;
						failedRecords.add(objectArray);
					}
				}
				if (OpenmrsUtil.nullSafeEquals(
						check.getFailDirectiveOperator(),
						DataIntegrityConstants.FAILURE_OPERATOR_LESS_THAN)) {
					if (resultList.size() < failDirective) {
						checkPassed = false;
					}
				} else if (OpenmrsUtil.nullSafeEquals(
						check.getFailDirectiveOperator(),
						DataIntegrityConstants.FAILURE_OPERATOR_GREATER_THAN)) {
					if (resultList.size() > failDirective) {
						checkPassed = false;
					}
				} else if (OpenmrsUtil.nullSafeEquals(
						check.getFailDirectiveOperator(),
						DataIntegrityConstants.FAILURE_OPERATOR_EQUALS)) {
					if (resultList.size() == failDirective) {
						checkPassed = false;
					}
				} else if (OpenmrsUtil.nullSafeEquals(
						check.getFailDirectiveOperator(),
						DataIntegrityConstants.FAILURE_OPERATOR_NOT_EQUALS)) {
					if (resultList.size() != failDirective) {
						checkPassed = false;
					}
				} else {
					throw new Exception("Specified failure operator is unsupported");
				}
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

	}
	
	public List<Object[]> getFailedRecords() {
		return this.failedRecords;
	}

	public void initializeExecutor(IntegrityCheck check,
			String parameterValues) {
		this.check = check;
		this.parameterValues = parameterValues;
		this.failedRecords = new ArrayList<Object[]>();
		this.checkPassed = true;
	}

	public boolean getCheckResult() {
		return checkPassed;
	}
}
