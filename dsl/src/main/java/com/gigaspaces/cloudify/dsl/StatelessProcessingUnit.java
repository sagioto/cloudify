package com.gigaspaces.cloudify.dsl;

import com.gigaspaces.cloudify.dsl.internal.CloudifyDSLEntity;

/**
 * Defines an elastic processing unit deployment that does not contain a space.
 * The stateless Processing unit configuration POJO is initialized by 
 * the service groovy DSL and holds all of the required information regarding 
 * the deployment of stateless processing units.
 *  
 * In order to deploy mirror based services, use this processing unit type.
 *  
 * @see ElasticStatelessProcessingUnitDeployment
 * 
 * @author adaml
 *
 */
@CloudifyDSLEntity(name="stateless", clazz=StatelessProcessingUnit.class, allowInternalNode = true, allowRootNode = false, parent = "service")
public class StatelessProcessingUnit extends ServiceProcessingUnit{
	private String binaries;
	
	/**
	 * can be a folder, or a jar/war file.
	 * @return - a String containing the folder path or the jar file name.
	 */
	public String getBinaries() {
		return binaries;
	}
	
	public void setBinaries(String binaries) {
		this.binaries = binaries;
	}
	

}
