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

public class BooleanCheckExecutor implements ICheckExecutor {
	private IntegrityCheck check;
	private DataIntegrityDAO dao;
	private String parameterValues;
	private List<Object[]> failedRecords;
	
	public BooleanCheckExecutor(DataIntegrityDAO dao) {
		this.dao = dao;
	}
	
	@SuppressWarnings("unchecked")
	public void executeCheck() throws Exception {
		try {
			boolean failDirective = Boolean.valueOf(this.check.getFailDirective());
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
				if (OpenmrsUtil.nullSafeEquals(
						check.getFailDirectiveOperator(),
						DataIntegrityConstants.FAILURE_OPERATOR_EQUALS)) {
					for (int i=0; i<resultList.size(); i++) {
						if (columnCount > 1) {
							Boolean[] boolArray = new Boolean[columnCount];
							for (int j=0; j<columnCount; j++) {
								Boolean value = (Boolean) resultList.get(i)[j];
								if (value == failDirective) {
									for (int k=0; k<columnCount; k++) {
										boolArray[k] = (Boolean) resultList.get(i)[k];
									}
									failedRecords.add(boolArray);
									break;
								}
							}
						} else {
							Object value = resultList.get(i);
							Boolean boolValue = (Boolean)value;
							if (boolValue == failDirective) {
								Boolean[] boolArray = new Boolean[1];
								boolArray[0] = boolValue;
								failedRecords.add(boolArray);
							}
						}
					}
				} else {
					throw new Exception("Specified failure operator is unsupported");
				}
			} else {
				throw new Exception("Query returned no results");
			}
			
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

	}

	public List<Object[]> getFailedRecords() {
		return this.failedRecords;
	}
	
	public boolean getCheckResult() {
		boolean checkPassed = (failedRecords.size() > 0) ? false : true;
		return checkPassed;
	}

	public void initializeExecutor(IntegrityCheck check,
			String parameterValues) {
		this.check = check;
		this.parameterValues = parameterValues;
		this.failedRecords = new ArrayList<Object[]>();
	}
}
