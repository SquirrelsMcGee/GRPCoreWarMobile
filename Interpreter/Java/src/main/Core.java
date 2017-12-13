package main;

public class Core {
	
	private CoreNode[] coreArray;
	
	public Core(int coreSize) {
		createCoreArray(new CoreNode[coreSize]);
	}

	public CoreNode[] getCoreArray() {
		return coreArray;
	}

	public void createCoreArray(CoreNode[] coreArray) {
		this.coreArray = coreArray;

		// Initialise array
		for (int i = 0; i < coreArray.length; i++) {
			coreArray[i] = new CoreNode();
		}
	}
	
	public CoreNode getNode(int index) {
		return coreArray[index];
	}

}
