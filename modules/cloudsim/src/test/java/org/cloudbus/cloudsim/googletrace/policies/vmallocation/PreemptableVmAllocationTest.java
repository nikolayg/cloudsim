package org.cloudbus.cloudsim.googletrace.policies.vmallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.googletrace.GoogleHost;
import org.cloudbus.cloudsim.googletrace.GoogleVm;
import org.cloudbus.cloudsim.googletrace.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.googletrace.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.googletrace.util.PriorityHostComparator;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PreemptableVmAllocationTest {

	SortedSet<GoogleHost> sortedHosts;
	HostSelectionPolicy hostSelector;
	GoogleHost host1, host2;
	PreemptableVmAllocationPolicy preemptablePolicy;
	
	@Before
	public void setUp() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 1);
		
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		host2 = new GoogleHost(2, peList2,
				new VmSchedulerMipsBased(peList2), 1);
		
		List<GoogleHost> hosts = new ArrayList<GoogleHost>();
		hosts.add(host1);
		hosts.add(host2);
		
		sortedHosts = new TreeSet<GoogleHost>(new PriorityHostComparator(0));
		
		for (GoogleHost googleHost : hosts) {
			sortedHosts.add(googleHost);
		}		

		hostSelector = Mockito.mock(HostSelectionPolicy.class);

		preemptablePolicy = new PreemptableVmAllocationPolicy(hosts, hostSelector);
		
		Map<Integer, SortedSet<GoogleHost>> priorityToSortesHosts = new HashMap<Integer, SortedSet<GoogleHost>>();
		priorityToSortesHosts.put(0, sortedHosts);
		preemptablePolicy.setPriorityToSortedHost(priorityToSortesHosts);
	}
	
	@Test
	public void testAllocateHostForVm() {
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);

		// checking
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		//asserting that vm1 is allocated on host1 looking at VmTable
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
	}

	@Test
	public void testAllocateHostForVm2() {
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);
		Mockito.when(hostSelector.select(sortedHosts, vm2)).thenReturn(host2);

		// checking
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		
		// allocating the 2nd vm
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2));
		
		// checking 
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());

		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());

		// Asserting VmTable after allocations
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host2, preemptablePolicy.getVmTable().get(vm2.getUid()));
	}


	@Test
	public void testAllocateVMsToSameHost(){
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0, 0);
		GoogleVm vm3 = new GoogleVm(3, 1, 1.0, 1.0, 0, 0, 0);
		GoogleVm vm4 = new GoogleVm(4, 1, 1.0, 1.0, 0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);
		Mockito.when(hostSelector.select(sortedHosts, vm2)).thenReturn(host1);
		Mockito.when(hostSelector.select(sortedHosts, vm3)).thenReturn(host1);
		Mockito.when(hostSelector.select(sortedHosts, vm4)).thenReturn(host2);

		// checking vm1 allocation
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));

		// checking vm2 allocation
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2));
		Assert.assertEquals(host1, vm2.getHost());
		Assert.assertEquals(2, host1.getVmList().size());
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));

		// checking vm3 allocation
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm3));
		Assert.assertEquals(host1, vm3.getHost());
		Assert.assertEquals(3, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(3, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm3.getUid()));


		// checking vm4 allocation
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4));
		Assert.assertEquals(host2, vm4.getHost());
		Assert.assertEquals(3, host1.getVmList().size());
		Assert.assertEquals(1, host2.getVmList().size());
		Assert.assertEquals(4, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm3.getUid()));
		Assert.assertEquals(host2, preemptablePolicy.getVmTable().get(vm4.getUid()));

	}
	
	@Test
	public void testDeallocateHostForVm() {
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// deallocating
		preemptablePolicy.deallocateHostForVm(vm1);

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}
	
	
	@Test
	public void testDeallocateHostForVm2() {
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host2.vmCreate(vm2));
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		vmTable.put(vm2.getUid(), host2);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());

		// deallocating vm1
		preemptablePolicy.deallocateHostForVm(vm1);

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		
		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// deallocating vm2
		preemptablePolicy.deallocateHostForVm(vm2);

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());

		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());

		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testDeallocateVMNonExistent(){
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(preemptablePolicy.getVmTable().isEmpty());
		preemptablePolicy.deallocateHostForVm(vm1);
		Assert.assertTrue(preemptablePolicy.getVmTable().isEmpty());
	}

	@Test
	public void testDeallocateVMNonexistentAfterDeallocateExistingVM(){
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);

		// checking
		Assert.assertTrue(preemptablePolicy.getVmTable().isEmpty());
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertFalse(preemptablePolicy.getVmTable().isEmpty());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));

		// deallocate existing VM1
		preemptablePolicy.deallocateHostForVm(vm1);
		Assert.assertTrue(preemptablePolicy.getVmTable().isEmpty());
		Assert.assertNull(preemptablePolicy.getVmTable().get(vm1.getUid()));

		// trying to deallocate VM1 again
		preemptablePolicy.deallocateHostForVm(vm1);
		Assert.assertTrue(preemptablePolicy.getVmTable().isEmpty());
		Assert.assertNull(preemptablePolicy.getVmTable().get(vm1.getUid()));

	}

	@Test
	public void testDeallocateMoreThanOneVMFromSameHost(){
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));

		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm2));

		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		vmTable.put(vm2.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(host1, vm2.getHost());
		Assert.assertEquals(2, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(preemptablePolicy.getHost(vm1), vm1.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm2), vm2.getHost());

		// deallocating VM1
		preemptablePolicy.deallocateHostForVm(vm1);
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm1), vm1.getHost());
		Assert.assertNull(preemptablePolicy.getHost(vm1));
		Assert.assertEquals(host1, vm2.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// deallocationg VM2
		preemptablePolicy.deallocateHostForVm(vm2);
		Assert.assertEquals(preemptablePolicy.getHost(vm2), vm2.getHost());
		Assert.assertNull(preemptablePolicy.getHost(vm2));
		Assert.assertNull(vm2.getHost());
		Assert.assertTrue(host1.getVmList().isEmpty());
		Assert.assertTrue(host2.getVmList().isEmpty());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());

	}
	
	@Test
	public void testOptimizeAllocation() {
		Assert.assertNull(preemptablePolicy
				.optimizeAllocation(new ArrayList<Vm>()));
	}
	
	@Test
	public void testPreempt() {
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host2.vmCreate(vm2));
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		vmTable.put(vm2.getUid(), host2);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());

		// preempting
		Assert.assertTrue(preemptablePolicy.preempt(vm1));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm1), vm1.getHost());
		Assert.assertNull(preemptablePolicy.getHost(vm1));
		Assert.assertEquals(0, host1.getVmList().size());

		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting
		Assert.assertTrue(preemptablePolicy.preempt(vm2));
		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm2), vm2.getHost());
		Assert.assertNull(preemptablePolicy.getHost(vm2));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());

		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());

		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testPreemptInvalidVm() {
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0, 0);
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting invalid vm
		Assert.assertFalse(preemptablePolicy.preempt(vm2));
		
		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testPreemptSameVmMoreThanOneTime() {
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting vm1
		Assert.assertTrue(preemptablePolicy.preempt(vm1));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());

		// preempting vm1 again
		Assert.assertFalse(preemptablePolicy.preempt(vm1));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());

		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}


	@Test
	public void preemptMoreThanOneVmFromSameHost(){
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));

		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm2));

		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		vmTable.put(vm2.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(host1, vm2.getHost());
		Assert.assertEquals(2, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());

		// preempting vm1
		Assert.assertTrue(preemptablePolicy.preempt(vm1));
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting vm2
		Assert.assertTrue(preemptablePolicy.preempt(vm2));
		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());

		// trying preempt two Vms again
		Assert.assertFalse(preemptablePolicy.preempt(vm1));
		Assert.assertFalse(preemptablePolicy.preempt(vm2));
	}

	@Test
	public void testAllocateVMAtNullHost(){
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(null);

		// checking
		Assert.assertFalse(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testAllocateAtSpecificHost(){
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0, 0);
		GoogleVm vm3 = new GoogleVm(3, 1, 1.0, 1.0, 0, 0, 0);
		GoogleVm vm4 = new GoogleVm(4, 1, 1.0, 1.0, 0, 0, 0);

		// checking vm1 allocation at null host
		Assert.assertFalse(preemptablePolicy.allocateHostForVm(vm1, null));
		Assert.assertNull(vm1.getHost());
		Assert.assertNull(preemptablePolicy.getHost(vm1));
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());

		// checking vm1 allocation at host 1
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1, host1));
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm1), vm1.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm1), host1);
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));

		// checking vm2 allocation at host 1
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host1));
		Assert.assertEquals(host1, vm2.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm2), vm2.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm2), host1);
		Assert.assertEquals(2, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));

		// checking vm3 allocation at host 1
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm3, host1));
		Assert.assertEquals(host1, vm3.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm3), vm3.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm3), host1);
		Assert.assertEquals(3, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(3, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm3.getUid()));

		// checking vm4 allocation at host 2
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4, host2));
		Assert.assertEquals(host2, vm4.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm4), vm4.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm4), host2);
		Assert.assertEquals(3, host1.getVmList().size());
		Assert.assertEquals(1, host2.getVmList().size());
		Assert.assertEquals(4, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm3.getUid()));
		Assert.assertEquals(host2, preemptablePolicy.getVmTable().get(vm4.getUid()));
	}

	@Test
	public void testGetHostByUserId(){
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0, 0);
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0, 0);
		GoogleVm vm3 = new GoogleVm(3, 2, 1.0, 1.0, 0, 0, 0);
		GoogleVm vm4 = new GoogleVm(4, 3, 1.0, 1.0, 0, 0, 0);

		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1, host1));
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host1));
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm3, host1));
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4, host2));

		Assert.assertEquals(host1, preemptablePolicy.getHost(1, 1));
		Assert.assertEquals(host1, preemptablePolicy.getHost(2, 1));
		Assert.assertEquals(host1, preemptablePolicy.getHost(3, 2));
		Assert.assertEquals(host2, preemptablePolicy.getHost(4, 3));
		
		// checking invalid parameters
		Assert.assertNull(preemptablePolicy.getHost(5, 3));
		Assert.assertNull(preemptablePolicy.getHost(2, 4));
	}

	@Test
	public void testPriorityToSortedHostSkinsMap(){

	}
}
