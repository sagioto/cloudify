package com.gigaspaces.cloudify.dsl.context;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.cluster.ClusterInfo;

import com.gigaspaces.cloudify.dsl.Service;
import com.gigaspaces.cloudify.dsl.internal.CloudifyConstants;
import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
import com.gigaspaces.cloudify.dsl.utils.ServiceUtils.FullServiceName;

/**
 * 
 * 
 * @author barakme
 * @since 1.0
 */
public class ServiceContext {

	private com.gigaspaces.cloudify.dsl.Service service;
	private Admin admin;
	private String serviceDirectory;
	private ClusterInfo clusterInfo;
	private boolean initialized = false;

	private String serviceName;

	private String applicationName;

	public ServiceContext() {

	}

	public void init(final Service service, final Admin admin,
			final String dir, ClusterInfo clusterInfo) {
		this.service = service;
		this.admin = admin;
		this.serviceDirectory = dir;
		this.clusterInfo = clusterInfo;

		if (clusterInfo == null) {
			this.applicationName = CloudifyConstants.DEFAULT_APPLICATION_NAME;
			this.serviceName = service.getName();
		} else {
			logger.info("Parsing full service name from PU name: "
					+ clusterInfo.getName());
			FullServiceName fullServiceName = ServiceUtils
					.getFullServiceName(clusterInfo.getName());
			logger.info("Got full service name: " + fullServiceName);
			this.serviceName = fullServiceName.getServiceName();
			this.applicationName = fullServiceName.getApplicationName();

		}
		if (admin != null) {
			boolean found = this.admin.getLookupServices().waitFor(1, 5,
					TimeUnit.SECONDS);
			if (!found) {
				throw new IllegalStateException(
						"A service context could not be created as the Admin API could not find a lookup service in the network, using groups: "
								+ Arrays.toString(admin.getGroups())
								+ " and locators: "
								+ Arrays.toString(admin.getLocators()));
			}
		}
		initialized = true;

	}

	public void initInIntegratedContainer(final Service service,
			final String dir) {
		this.service = service;
		this.serviceDirectory = dir;
		this.clusterInfo = new ClusterInfo(null, 1, 0, 1, 0);
		if (service != null) {
			this.clusterInfo.setName(service.getName());
		}

		this.applicationName = CloudifyConstants.DEFAULT_APPLICATION_NAME;
		this.serviceName = service.getName();

		initialized = true;

	}

	private void checkInitialized() {
		if (!this.initialized) {
			throw new IllegalStateException(
					"The Service Context has not been initialized yet. It can only be used after the Service file has been fully evaluated");
		}
	}

	/**
	 * Returns the instance ID of this instance in the cluster.
	 * 
	 * @return the instance ID.
	 */
	public int getInstanceId() {
		checkInitialized();

		return clusterInfo.getInstanceId();
	}

	/********
	 * Waits for the specified period of time until the service with the given
	 * name becomes available.
	 * 
	 * @param name
	 *            the service name.
	 * @param timeout
	 *            the timeout.
	 * @param unit
	 *            the unit of time used with the timeout.
	 * @return the Service.
	 */
	public com.gigaspaces.cloudify.dsl.context.Service waitForService(
			String name, int timeout, TimeUnit unit) {
		checkInitialized();

		if (this.admin != null) {
			final String puName = ServiceUtils.getAbsolutePUName(
					this.applicationName, name);
			ProcessingUnit pu = waitForProcessingUnitFromAdmin(puName, timeout,
					unit);
			if (pu == null) {
				return null;
			} else {
				return new com.gigaspaces.cloudify.dsl.context.Service(pu);
			}
		}

		// running in integrated container
		if (name.equals(this.service.getName())) {
			return new com.gigaspaces.cloudify.dsl.context.Service(name,
					service.getNumInstances());
		}

		throw new IllegalArgumentException(
				"When running in the integrated container, Service Context only includes the running service");

	}

	private static final java.util.logging.Logger logger = java.util.logging.Logger
			.getLogger(ServiceContext.class.getName());

	private ProcessingUnit waitForProcessingUnitFromAdmin(String name,
			long timeout, TimeUnit unit) {

		final ProcessingUnit pu = admin.getProcessingUnits().waitFor(name,
				timeout, unit);
		if (pu == null) {
			logger.warning("Processing unit with name: "
					+ name
					+ " was not found in the cluster. Are you running in an IntegratedProcessingUnitContainer? If not, consider extending the timeout.");
		}

		return pu;
	}

	/**
	 * The service folder for the current service instance.
	 * 
	 * @return
	 */
	public String getServiceDirectory() {
		checkInitialized();

		return serviceDirectory;
	}

	/**
	 * Returns the Admin Object the underlies the Service Context. Note: this is
	 * intended as a debugging aid, and should not be used by most application.
	 * Only power users, familiar with the details of the Admin API, should use
	 * it.
	 * 
	 * @return
	 */
	public Admin getAdmin() {
		return admin;
	}

	/**
	 * 
	 * @param service
	 */
	void setService(Service service) {
		this.service = service;
	}

	/**
	 * Returns the cluster info object used to initialize this service context.
	 * 
	 * @return
	 */
	public ClusterInfo getClusterInfo() {
		return clusterInfo;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	@Override
	public String toString() {
		if (this.initialized) {
			return "ServiceContext [dir=" + serviceDirectory + ", clusterInfo="
					+ clusterInfo + "]";
		} else {
			return "ServiceContext [NOT INITIALIZED]";
		}
	}

}
