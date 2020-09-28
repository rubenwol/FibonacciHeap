
//created by: Ruben Wolhandler  rubenw       
//            Daniel Malash    danielmalash  

/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap
{	
	int size;
	int totalMarked;
	static int totalCuts = 0;
	static int totalLinks = 0;
	private HeapNode min;
	private HeapNode first;
	
	FibonacciHeap(){
		size = 0;
		totalMarked = 0;
		min = null;
		first = null;
	}


/**
    * public boolean isEmpty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    *   
    */
    public boolean isEmpty()
    {
    	if(size==0)
    		return true;
    	return false;
    }
		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap. 
    */
    public HeapNode insert(int key)
    {   
    	HeapNode insertNode = new HeapNode(key);
    	    	
    	addToFirst(insertNode);//insert the new Node
    	    	    	
    	size++;//update size
    	
    	return insertNode;
    }
    
    /**
     * private void addToFirst(HeapNode insertNode) 
     *
     * inserts the insertNode to the first place in the heap.
     *
     */ 
   private void addToFirst(HeapNode insertNode) {
	   
	   if(size == 0) {
		   //single node in heap has links to itself
		   //and it is min
		   first = insertNode;
		   min = insertNode;
		   insertNode.next = insertNode.prev = insertNode;
		   insertNode.setParent(null);
		   return;
	   }
	   HeapNode firstPrev = first;
	   first = insertNode;
	   first.next = firstPrev;
	   first.prev = firstPrev.prev;
	   first.prev.next = first;
	   firstPrev.prev = first;
	   insertNode.setParent(null);
	   
	   //if its key is minimal it will be min
	   if(insertNode.getKey() < min.getKey()) {
		   min = insertNode;
	   }
   }
   

   /**
    * private int numRootNodes()
    *
    * The method returns the number of root nodes.
    *
    */ 
   private int numRootNodes() {
	   if(size == 0) {
		   return 0;
	   }
	   int num = 1;
	   HeapNode node = min;
	   //all root nodes has loop connected link
	   while(node.next != min) {
		   node = node.next;
		   num++;
	   }
	   return num;
   }

/**
    * public void deleteMin()
    *
    * Delete the node containing the minimum key.
    *
    */
   public void deleteMin() {
	   if(isEmpty() || findMin() == null) {
		   return;
	   }
	   if(min.rank > 0) {//min has child
		   
		   //put all child of min to root nodes and connect with min 
		   HeapNode NextMin = min.next;
		   min.next = min.child;
		   min.child.prev.next = NextMin;
		   NextMin.prev = min.child.prev;
		   min.child.prev = min;
		   min.child.setParent(null);
		   
	   } 
	   
	   if(min.prev == min && min.next == min) {//min was single node
		   min = null;
		   first = null;
		   size = 0;
		   return;   
	   }
	  
	   HeapNode nextMin = min.next;
	   
	   //exclude min from root nodes
	   
	   excludeNodeFromLink(min);

	   if(min == first) {
		   first = nextMin;
	   }
	   
	  //define next node as min 
	  //real min will find in consolidate()
	   min = nextMin;
	   
	   consolidate();
	   
	   //reduce size of heap
	   size--;	   
   }
   
/**
    * public void consolidate()
    *
    * The method will be used in deleteMin() only.
    * It recreates the heap.
    * 
    * postcondition: the heap contains at most log(n) trees, at most one of each degree
    *
    */    
   private void consolidate()
   {
	   if(size < 2) {
		   return;
	   }
	   //maximal number of root nodes to allocate new constructed tree
	   int bucketSize = 1 + (int) (((Math.log(size) / Math.log(2)))*1.4404);
	   
	   HeapNode[] b = new HeapNode[bucketSize];
	   
	   //construct new trees and put it in box table b
	   toBuckets(b);
	   
	   //put new trees to heap and make connections between them 
	   fromBuckets(b);	
   }
   
 
   /**
    *  private void toBuckets(HeapNode[] b)
    *
    * construct new trees (by linking trees with the same degree) and put them in box table b.
    *
    */ 
   private void toBuckets(HeapNode[] b) {
	   
	   int num = numRootNodes();
	   
	   HeapNode node, nodeNext = first;
	  
	   //loop by all linked root nodes
	   for(int i = 0; i < num; i++) {
		   //loop begin with first and go through all connected root nodes
		   node = nodeNext;
		   nodeNext = nodeNext.next;
		   
		   //if table in place with the same rank has some tree
		   //we make new compound tree from these two tree
		   while(b[node.rank] != null) {
			   
			 //we link 2 nodes with the same rank to new tree
			   node = link(node,b[node.rank]);
			   
			   //now rank of linked node  of new tree is increased by 1
			   //mark that table does not have tree with previous rank
			   b[node.rank-1] = null;
		   }
		   //last linked node of new tree put to box having its rank
		   b[node.rank] = node; 
	   }
   }
   
  
   /**
    * private void fromBuckets(HeapNode[] b)
    *
    * put new trees from table b to the heap and make connections between them. select new minimum. 
    *
    */ 
   private void fromBuckets(HeapNode[] b) {
	    min = null;
	    //on every step of loop we know first and last trees 
	    HeapNode node, lastNode = null;
	    first = null;
	    for(int i = 0; i < b.length; i++) {
	    	//if table has tree with rank equal to index of table
	    	if(b[i] != null) {
	    		//take new tree	
	    		node = b[i];
	    		node.parent = null;
	    		//if it is first tree all links point to itself 
	    		if(first == null) {
	    			node.prev = node.next = node;
	    			min = node;	
	    			first = node;
	    			lastNode = node;
	    		}
	    		//else we put new tree after last and before first
	    		else {
	    			lastNode.next = node;
	    			node.prev = lastNode;
	    			node.next = first;
	    			first.prev = node;
	    			
	    			//verify min and change
	    			if(node.getKey() < min.getKey()) {
    				   min = node;
    			   }
	    		}
	    		//new tree now will last tree
	    		lastNode = node;
	    	}
	    }
   }
   

   
   /**
    * private HeapNode link(HeapNode Parent, HeapNode Child)
    *
    * link two nodes by parent and child relation.
    *
    */ 
   private HeapNode link(HeapNode Parent, HeapNode Child) {
	  	  
	   HeapNode temp;
	   //select parent and child 
	   if(Parent.key > Child.key) { 
		   temp = Parent;
		   Parent = Child;
		   Child = temp;
	   }
	
	   //exclude node that will be child from his links with other nodes
	   excludeNodeFromLink(Child);
	    
	   //isolate child node 
	   Child.next = Child;
	   Child.prev = Child;
	   Child.parent = Parent;
	   
	   if(Parent.child == null) {
		   Parent.child = Child;
	   }
	   else {
		   //make link from node Child with all child of Parent
		   Child.next = Parent.child;
		   Child.prev = Parent.child.prev;
		   Child.prev.next = Child;
		   Child.next.prev = Child;
		   Parent.child = Child;
	   }
	   
	   //update Rank and totalLinks
	   Parent.rank += 1; 
	   totalLinks++;
	   
	   return Parent;
   }
   
   
   /**
    * private HeapNode excludeNodeFromLink(HeapNode node)
    *
    * exclude node from linked nodes
    * return the next node or null if excluded node was single
    */ 
   private HeapNode excludeNodeFromLink(HeapNode node) {
	   	HeapNode Prev = node.prev;
  	   	HeapNode Next = node.next;
  	   	
  	   	if(Prev == node && Next == node) {
  	   		//we have single node
  	   		return null;
  	   	}
  	   	if(Prev == Next) {
  	   		//we have two nodes, return second node
  	   		Next.next = Next.prev = Next;
  	   		return Next;
  	   	}
  	   	//connect prev and next of excluded node, return next
  	   	Prev.next = Next;
  	   	Next.prev = Prev;
  	   	return Next;
   }
  

/**
    * public HeapNode findMin()
    *
    * Return the node of the heap whose key is minimal. 
    *
    */
    public HeapNode findMin()
    {
    	return min;
    } 
    
   
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Meld the heap with heap2
    *
    */
    public void meld(FibonacciHeap heap2)
    {
    	if(heap2.size() == 0) {
    		return;
    	}
    	if(size == 0) {
    		min = heap2.findMin();
    		size = heap2.size();
    		totalMarked += heap2.totalMarked;
    	}
    	else {

    		size += heap2.size(); //update size
        	totalMarked += heap2.totalMarked;
        	HeapNode last = first.prev;
        	HeapNode last2 = heap2.first.prev;
        	//update min
        	if(findMin().getKey()>heap2.findMin().getKey()) {
        		min = heap2.findMin();
        	}
        	//meld to last
        	last.next = heap2.first;
        	last2.next = first;
        	first.prev = last2;
        	heap2.first.prev = last;
    }
    }
	
    
   /**
    * public int size()
    *
    * Return the number of elements in the heap
    *   
    */
    public int size()
    {
    	return size; 
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
    * 
    */
    public int[] countersRep()
    {
    	if(isEmpty()) {
    		return null;
    	}
    	
    	int[] arr = new int[1 + maxRank()];
    	
    	arr[min.rank] = 1;
    	
    	HeapNode next = min.next;
    	    	
    	while(next != min) {
    		arr[next.rank] += 1;
    		next = next.next;
    	}
        return arr;
    }
    
	
    /**
     * private HeapNode nodeMaxRank()
     *
     * precondition: heap is no empty
     * 
     * return the root node with the max rank.
     *
     */ 
    private HeapNode nodeMaxRank() {
 	   
 	   int maxrank = min.rank;
 	   HeapNode next = min.next;
 	   
 	   //loop on the root nodes
 	   while(next != min) {
 		   if(next.rank > maxrank) {
 			  //select maximum rank
 			  maxrank = next.rank;
 		   }
 		   next = next.next;
 	   }
 	   return next;
 	}
    
    /**
     * private int maxRank()
     *
     * return the max rank in the heap.
     *
     */  
   private int maxRank() {
	   HeapNode node  = nodeMaxRank();
	   return node.rank;
	}

/**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap. 
    *
    */
    public void delete(HeapNode node) 
    {  
    	if(node == null) {
    		return;
    	}
   	
       	int keyNode = node.getKey();
       	
       	//define delta for decreaseKey() to make minimum of key value
       	if(keyNode >= 0) {
       		keyNode = Integer.MAX_VALUE - keyNode - 1;
       	}
       	else {
       		keyNode = Integer.MAX_VALUE + keyNode - 1;
       	}
       	//change key value of node to possible minimum
    	decreaseKey(node, keyNode);
    	
    	//now node is minimum of heap and we will use constructed before function
    	deleteMin();
    	    	
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
    */
    public void decreaseKey(HeapNode node, int delta)
    {   
    	int newKey = node.getKey() - delta;
    	
    	//update the key
    	node.setKey(newKey);
        	
    	//keep the heap rules
    	HeapNode parent = node.getParent();
    	if(parent != null && newKey < parent.getKey()) {
    		
    		//node will cut and cascading cut of parent
			cut(node);
			cascadingCuts(parent);
    	}
    		
    	//update min
    	if(findMin() == null || newKey < findMin().getKey()) {
    		min = node;
    	}
    }
    

    /**
     * private void cut(HeapNode node)
     *
     * The method cuts node from its parent and makes it a root node.
     * 
     *
     */ 
    private void cut(HeapNode node) {
 	   
 	   HeapNode parent = node.getParent();
 	   if(parent == null) {
 		   return;
 	   }
 	   //remove node from node link 
 	   node.prev.next = node.next;
 	   node.next.prev = node.prev;
 	    	   
 	   if(node.next == node)
 		   //parent of node had single child, link to it will cut
 		   parent.child = null;
 	   else {
 		   //parent change connection with child to next node in child link
 		   parent.child = node.next;
 		   parent.child.parent = parent;
 	   }
 	   
 	   //decrease rank of node parent
 	   parent.rank--;
 	   
 	   //change mark of cutting node if it was marked
 	   //and decrease counter of marked nodes
 	   if(node.mark == true) {
 		   totalMarked--;
 	   }
 	   node.mark = false;
 	   
 	   //add node to link of root nodes
 	   addToFirst(node);
 	   
 	   //increase counter of cutting nodes
 	   totalCuts ++;
    }
    

    /**
     * private void cascadingCuts(HeapNode node)
     *
     * Recursive method that will cut child from parent until parent is not marked or we got root node.
     *
     */ 
   private void cascadingCuts(HeapNode node) {
	   HeapNode parent = node.parent;
	   //if node does not have parent it is root node
	   if(parent == null ) {
		   return;
	   }
	   //if node did not cut child before, we mark about cutting and return
	   if(node.mark == false) {
		   node.mark = true;
		   totalMarked++;
		   return;
	   }
	   //else we proceed cutting
	   if(node.mark == true) {
		   totalMarked--;
	   }
	   node.mark = false;
	   cut(node);
	   cascadingCuts(parent);
   }


/**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap. 
    */
    public int potential() 
    {    
    	return numRootNodes() + 2*totalMarked; 
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the run-time of the program.
    * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
    * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
    * in its root.
    */
    public static int totalLinks()
    {    
    	return totalLinks; 
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the run-time of the program.
    * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
    */
    public static int totalCuts()
    {    
    	return totalCuts; 
    }

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k minimal elements in a binomial tree H.
    * The function should run in O(k(logk + deg(H)). 
    */ 
    public static int[] kMin(FibonacciHeap H, int k)
    {       
    	if(H.size() == 0 || H.size() < k) {
    		return null;
    	}
		//allocate returned array 
	    int[] minKeyArray = new int[k];
	    
	    HeapNode node, child, nodeHelp;
	    FibonacciHeap help = new FibonacciHeap();
	    
	    //first node will put to help
	    node = H.findMin();
	    nodeHelp = help.insert(node.getKey());
	    nodeHelp.kmin = node;
	    
	    //index is place in output minKeyArray
	    int index = 0;
	    
	    while(index<k) {
	    	node = help.findMin();
	    	minKeyArray[index++] = node.getKey();
	    	
	    	//all child of min help will add to help
	    	if(node.kmin.getChild() != null) {
	    		child = node.kmin.getChild();
	    		for(int j =0; j< node.kmin.rank;j++) {
	    			nodeHelp = help.insert(child.getKey());
	    			nodeHelp.kmin = child;
	    			child = child.next;
	    		}
	    	}
	    	//node with minimal key will be removed from help
	    	help.deleteMin();
	    }
        return minKeyArray;
    }
    

   
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in 
    * another file 
    *  
    */
    public class HeapNode{

		public HeapNode prev;
		public HeapNode next;
		private HeapNode parent;
		private HeapNode child;
		private int key;
		private boolean mark;
		public int rank;
		private HeapNode kmin = null;
	
	  	public HeapNode(int key) {
		    this.key = key;
		    prev = this;
		    next = this;
		    parent = null;
		    child = null;
		    mark = false;
		    rank = 0;
	      }
	
	  	public HeapNode getParent() {
			return parent;
		}
		public void setParent(HeapNode parent) {
			this.parent = parent;
		}
		public HeapNode getChild() {
			return child;
		}
	
		public int getKey() {
		    return key;
	    }
	  	
	  	public void setKey(int key) {
	  		this.key = key;
	  	}
	
	
    }
}
   
