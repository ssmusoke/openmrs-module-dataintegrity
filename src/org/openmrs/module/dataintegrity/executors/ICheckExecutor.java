package org.openmrs.module.dataintegrity.executors;

import java.util.List;

import org.openmrs.module.dataintegrity.IntegrityCheck;

public interface ICheckExecutor {
	/**
	 * 
	 * Method to initialize the check executing class.
	 * 
	 * @param check: The Integrity Check to be executed
	 * @param parameterValues: Parameter values for the parameter fields
	 */
	public void initializeExecutor(IntegrityCheck check, String parameterValues);
	
	/**
	 * 
	 * Executes the check according to the result type
	 * 
	 * @throws Exception
	 */
	public void executeCheck() throws Exception;
	
	/**
	 * 
	 * Returns the records that fail the check.
	 * 
	 * @param <T>: The type of the returned failed records List
	 * @return: The list of failed records
	 */
	public <T> List<T[]> getFailedRecords();
	
	/**
	 * 
	 * Returns the result of the check
	 * 
	 * @return whether the check passed or not
	 */
	public boolean getCheckResult();
}
