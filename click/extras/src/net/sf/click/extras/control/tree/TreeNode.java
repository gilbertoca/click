/*
 * Copyright 2007 Malcolm A. Edgar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.click.extras.control.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * This class is the implementation of the {@link net.sf.click.extras.control.tree.Tree} model.
 *
 * <p/>TreeNode's are used to store hierarchical data representations for example,
 * directories, subdirectories and files.
 *
 * <p/>Nodes must have a id and can store a user defined value of type {@link java.lang.Object}.
 *
 * <p/>Nodes contain a reference to their parent nodes as well as their children.
 *
 * <p/><strong>Note:</strong> Each TreeNode must have a unique id within the graph.
 * If an id is not provided at creation time, a default id is generated using
 * an instance of {@link java.util.Random}.
 *
 * @author Bob Schellink
 */
public class TreeNode implements Serializable {

    // -------------------------------------------------------------- Constants

    /** default serial version id. */
    private static final long serialVersionUID = 1L;

    /** Used internally to generate unique id's for tree nodes where id's are
     * not explicitly provided. */
    private final static Random RANDOM = new Random();


    // ----------------------------------------------------- Instance Variables

    /** Each node must have a unique id in the tree. If a node is not provided
     * an id, one is generated using the java.util.Random class.*/
    private String id;

    /** Indicates if the treeNode is currently selected. */
    private boolean selected = false;

    /** Indicates if the treeNode is currently expanded. */
    private boolean expanded = false;

    /** Indicates if the treeNode supports children or not. This is useful to
     * differentiate between files and directories with no children. */
    private boolean childrenSupported = false;

    /** User provided value of this node. */
    private Object value;

    /** Specifies the depth of this tree. */
    private int treeDepth = -1;

    /** Each node except the top level node will have a parent. */
    private TreeNode parent;

    /** List containing this nodes children. */
    private List children = new ArrayList();


    // ---------------------------------------------------- Public Constructors

    /**
     * Creates a default node with no value or id.
     *
     * <p/><strong>Note:</strong> a default random id is generated using a static
     * instance of {@link java.util.Random}.
     */
    public TreeNode() {
        setId(generateId());
    }

    /**
     * Creates a node and sets the value to the specified argument.
     *
     * <p/><strong>Note:</strong> a default random id is generated using a static
     * instance of {@link java.util.Random}.
     *
     * @param value the nodes value
     */
    public TreeNode(Object value) {
        setValue(value);
        setId(generateId());
    }

    /**
     * Creates a node and sets the value and id to the specified arguments.
     *
     * @param value the nodes value
     * @param id the nodes id
     */
    public TreeNode(Object value, String id) {
        setValue(value);
        setId(id);
    }

    /**
     * Creates a node and sets the value, id and parent to the specified arguments.
     *
     * @param value the nodes value
     * @param id the nodes id
     * @param parent specifies the parent of this node
     */
    public TreeNode(Object value, String id, TreeNode parent) {
        setValue(value);
        setId(id);
        parent.add(this);
    }


    // --------------------------------------------- Public Getters and Setters

    /**
     * Returns this node's parent object or null if parent is not specified.
     *
     * @return TreeNode this node's parent or null if parent is not specified
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * Sets this node's parent to the specified argument.
     *
     * @param parent this node's parent object
     */
    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

       /**
     * Return this node's value.
     *
     * @return this node's value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set this node's value.
     *
     * @param value the new value of this node
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns true if this node supports children, false otherwise.
     *
     * @return true if this node supports children, false otherwise.
     */
    public boolean isChildrenSupported() {
        return childrenSupported;
    }

    /**
     * Sets whether this node supports children nodes or not.
     *
     * @param childrenSupported wheter this node supports children or not
     */
    public void setChildrenSupported(boolean childrenSupported) {
        this.childrenSupported = childrenSupported;
    }

    /**
     * Returns this node's id value.
     *
     * @return this node's id value
     */
    public String getId() {
        return id;
    }

    /**
     * Set this node's new id value.
     *
     * @param id this node's new id value
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns a unmodifiable list of this nodes children.
     *
     * @return the unmodifiable list of children.
     */
    public List getChildren() {
        return Collections.unmodifiableList(children);
    }


    // -------------------------------------------------------- Public Behavior

    /**
     * Adds the specified node as a child of this node and sets the childs parent
     * to this node.
     *
     * @param child child node to add
     * @throws IllegalArgumentException if the argument is null
     */
    public void add(TreeNode child) {
        if (child == null) {
            throw new IllegalArgumentException("null child specified");
        }
        getMutableChildren().add(child);
        child.setParent(this);
    }

    /**
     * Removes the specified node from the list of children and sets childs parent
     * node to null.
     *
     * @param child child node to remove from this nodes children
     * @throws IllegalArgumentException if the argument is null
     */
    public void remove(TreeNode child) {
        if (child == null) {
            throw new IllegalArgumentException("null child specified");
        }
        getMutableChildren().remove(child);
        child.setParent(null);
    }

    /**
     * Returns true if this node is the root node. The root is the node with a
     * null parent.
     *
     * @return boolean true if this node is root, false otherwise
     */
    public boolean isRoot() {
        return getParent() == null;
    }

    /**
     * Returns true if this node has any children nodes false otherwise.
     *
     * @return true if this node has any children false otherwise
     */
    public boolean hasChildren() {
        return getChildren().size() > 0;
    }

    /**
     * Returns true if either this node is the last child of its parent, or this
     * node is the root node. Else it returns false.
     *
     * @return true if this node is the last child of its parent or the
     * root node, false otherwise.
     */
    public boolean isLastChild() {
        if (isRoot()) {
            return true;
        }
        return getParent().isLastChild(this);
    }

     /**
     * Calculcate and return the depth of the tree where this node is set as the
     * root of the calculation.
     *
     * <p/>This method guarantees to recalculate the depth of the tree and will
     * not use the cached value if it was set using {@link #cacheTreeDepth(int)}.
     *
     * <p/><strong>Time complexity:</strong> This method performs in O(n) where n is the number of nodes in the tree.
     * In other words no optimized algorithm is used by this method.
     *
     * @return depth of the tree
     * @see #cacheTreeDepth(int)
     */
    public int calcTreeDepth() {
        return calcTreeDepth(false);
    }

    /**
     * Calculcate and return the depth of the tree where this node is set as the
     * root of the calculation.
     *
     * <p/>If the argument useCacheIfSet is true, this method will return the cached
     * value that was set by {@link #cacheTreeDepth(int)}. If useCacheIfSet is false
     * this method will recalculate the depth of the tree.
     *
     * <p/><strong>Time complexity:</strong> This method performs in O(n) where n is the number of nodes in the tree.
     * In other words no optimized algorithm is used by this method.
     *
     * @param useCacheIfSet if true use the cached value if it was set,
     * else recalculate the depth
     * @return depth of the tree
     * @see #cacheTreeDepth(int)
     */
    public int calcTreeDepth(boolean useCacheIfSet) {
        //lookup from cache. this will only be true if user explicitly sets the tree's
        //depth using cacheTreeDepth(int)
        if (useCacheIfSet && treeDepth > -1) {
            return treeDepth;
        }

        //Iterate over the tree using a breadth first iterator. The level of the
        //last node found will indicate the depth of the tree.
        Object o = null;
        for (Iterator it = new Tree.BreadthTreeIterator(this); it.hasNext();) {
            o = it.next();
        }
        TreeNode node = (TreeNode) o;
        //depth = level of last node - this node's level
        return node.getLevel() - getLevel();
    }

    /**
     * Stores the specified depth in a variable for fast retrieval.
     * The cached depth can be retrieved by calling {@link #calcTreeDepth(boolean)}
     * with 'true'.
     *
     * @param depth this node's depth to cache
     * @see #calcTreeDepth(boolean)
     */
    public void cacheTreeDepth(int depth) {
        this.treeDepth = depth;
    }

    /**
     * Returns this node's level in the tree structure.
     *
     * @return int indicating this node's level
     */
    public int getLevel() {
        int level = 0;
        TreeNode parent = this;
        while ((parent = parent.getParent()) != null) {
            level++;
        }
        return level;
    }

    /**
     * Checks if this node's id is equals to the specified node's id. Two
     * tree node's are the same if they have the same id.
     *
     * @param thatObject the specified object to check for equality
     * @return true if this node's id is equal to the specified node's id
     *
     * @see Object#equals(Object)
     */
    public boolean equals(Object thatObject) {
        if (thatObject == this) {
            return true;
        }

        if (!(thatObject instanceof TreeNode)) {
            return false;
        }

        TreeNode that = (TreeNode) thatObject;

        return this.getId() == null ? that.getId() == null : this.getId().equals(that.getId());
    }

    /**
     * Returns the hashCode value for this node. The hashCode is calculated
     * from the node's id.
     *
     * @return a hash code value for this object.
     * @see Object#hashCode()
     */
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Renders a string representation of this node.
     *
     * @return string representation of this node
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("TreeNode -> [");
        buffer.append("id=").append(getId()).append(", ");
        buffer.append("value=").append(getValue()).append(", ");
        buffer.append("expanded=").append(isExpanded()).append(", ");
        buffer.append("selected=").append(isSelected());
        buffer.append("]");
        return buffer.toString();
    }


    // ----------------------------------------------------- Protected Behavior

    /**
     * Returns the list of mutable children of this node.
     *
     * @return mutable children of this node
     */
    List getMutableChildren() {
        return children;
    }


    // ----------------------------------------------- Package-private Behavior

    /**
     * Returns true if this node is currently in the selected state, false otherwise.
     *
     * @return true if this node is currently selected, false otherwise.
     */
    boolean isSelected() {
        return selected;
    }

    /**
     * Sets this node to the specified selected state.
     *
     * @param selected new value for this node's selected state
     */
    void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Returns true if this node is currently in the expanded state, false otherwise.
     *
     * @return true if this node is currently expanded, false otherwise.
     */
    boolean isExpanded() {
        return expanded;
    }

    /**
     * Sets this node to the specified expanded state.
     *
     * @param expanded new value for this node's expanded state
     */
    void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    // ------------------------------------------------------- Private Behavior

    /**
     * Checks if the specified node is the last child of this node. If this
     * node does not have any children, this method returns false.
     *
     * <p/><strong>Note:</strong> The check is done comparing references -> node1 == node2,
     * and not equality -> node1.equals(node2).
     *
     * @param child the node to check if its the last child or not
     * @return true if the specified node is the last child of this node, false otherwise
     * @throws NullPointerException if the specified child is null
     */
    private boolean isLastChild(TreeNode child) {
        int size = getChildren().size();
        if (size == 0) {
            return false;
        }
        Object lastChild = getChildren().get(size - 1);
        return lastChild == child;
    }

    /**
     * Returns a randomized id for this node.
     *
     * @return a randomized id for this node
     * @see java.util.Random
     */
    private String generateId() {
        return Integer.toString(RANDOM.nextInt());
    }
}
