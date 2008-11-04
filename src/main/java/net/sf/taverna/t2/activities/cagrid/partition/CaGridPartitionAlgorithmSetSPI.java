package net.sf.taverna.t2.activities.cagrid.partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class CaGridPartitionAlgorithmSetSPI extends ActivityPartitionAlgorithmSet {

	public CaGridPartitionAlgorithmSetSPI() {
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("operation"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("use"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("style"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("url"));
	}
}
