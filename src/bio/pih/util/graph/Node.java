package bio.pih.util.graph;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author albrecht
 * 
 * @param <T>
 *            internal content of each node. Is not permitted different kinds connected.
 */
public class Node<T> {

	T content;
	Node<T> parent;
	List<Node<T>> connected = Lists.newLinkedList();

	private Node(T content) {
		this.content = content;
	}
	
	/**
	 * Create a new Node
	 * 
	 * @param <T>
	 * @param content
	 * @return the new node
	 */
	public static <T> Node<T> newNode(T content) {
		return new Node<T>(content);
	}

	
	/**
	 * Connect and guarantee that will have only one connection with the n
	 * @param n
	 */
	public void connectUnique(Node<T> n) {
		if (!this.connected.contains(n)) {
			this.connect(n);
		}
	}
	
	/**
	 * Connect this node in other, but not the inverse.
	 * 
	 * @param n
	 */
	public void connect(Node<T> n) {
		this.connected.add(n);
	}

	/**
	 * Connect this node in other, and connect other in this.
	 * 
	 * @param n
	 */
	public void biConnect(Node<T> n) {
		this.connected.add(n);
		n.connect(this);
	}

	/**
	 * Connect this node in other and set this node as its parent.
	 * 
	 * @param n
	 */
	public void connectAsSon(Node<T> n) {
		this.connect(n);
		n.setParent(this);

	}

	/**
	 * Set node n as parent.
	 * 
	 * @param n
	 */
	public void setParent(Node<T> n) {
		this.parent = n;
	}

	/**
	 * @return the parent.
	 */
	public Node<T> getParent() {
		return parent;
	}

	/**
	 * @return the content inside this node.
	 */
	public T getContent() {
		return content;
	}	
	
	/**
	 * @return nodes that this node is connected
	 */
	public List<Node<T>> getConnected() {
		return connected;
	}
	
	@Override
	public String toString() {
		return "(" + content.toString() + ")";
	}
}
