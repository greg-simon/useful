package au.id.simo.useful.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class TagTest {
    @Test
    public void testGetXPath() {
        Tag root = new Tag("root", null);
        Tag leaf = new Tag("leaf", root);
        assertEquals("/root", root.getXPath());
        assertEquals("/root/leaf", leaf.getXPath());
    }

    @Test
    public void testGetName() {
        Tag named = new Tag("named", null);
        assertEquals("named", named.getName());
    }

    @Test
    public void testHasParent() {
        Tag root = new Tag("root", null);
        Tag leaf = new Tag("leaf", root);
        assertFalse(root.hasParent());
        assertTrue(leaf.hasParent());
    }

    @Test
    public void testHasParentWith() {
        Tag root = new Tag("root", null);
        root.put("attrName", "attrValue");
        Tag branch = new Tag("branch", root);
        Tag leaf = new Tag("leaf", branch);
        
        assertTrue(leaf.hasParentWith("attrName", "attrValue"));
        assertFalse(leaf.hasParentWith("attr", "should not exist"));
        assertFalse(leaf.hasParentWith("attrName", "value should not exist"));
    }

    @Test
    public void testGetParent() {
        Tag root = new Tag("root", null);
        Tag branch = new Tag("branch", root);
        Tag leaf = new Tag("leaf", branch);
        
        assertTrue(leaf.getParent() == branch);
        assertTrue(branch.getParent() == root);
        assertNull(root.getParent());
    }

    @Test
    public void testHasChildren() {
        Tag root = new Tag("root", null);
        Tag branch = new Tag("branch", root);
        Tag leaf = new Tag("leaf", branch);
        
        assertTrue(root.hasChildren());
        assertTrue(branch.hasChildren());
        assertFalse(leaf.hasChildren());
    }

    @Test
    public void testGetChildren() {
        Tag root = new Tag("root", null);
        Tag branch = new Tag("branch", root);
        Tag leaf1 = new Tag("leaf1", branch);
        Tag leaf2 = new Tag("leaf2", branch);
        
        Collection<Tag> rootChildren = root.getChildren();
        Collection<Tag> branchChildren = branch.getChildren();
        Collection<Tag> leaf1Children = leaf1.getChildren();
        Collection<Tag> leaf2Children = leaf2.getChildren();
        
        assertEquals(1, rootChildren.size());
        assertTrue(rootChildren.iterator().next() == branch);
        assertEquals(2, branchChildren.size());
        Iterator<Tag> branchItr = branchChildren.iterator();
        assertTrue(branchItr.next() == leaf1);
        assertTrue(branchItr.next() == leaf2);
        assertFalse(branchItr.hasNext());
        
        assertEquals(0, leaf1Children.size());
        assertEquals(0, leaf2Children.size());
    }

    @Test
    public void testAppendContent_String() {
        Tag tag = new Tag("tag", null);
        assertEquals("", tag.getContent());
        
        tag.appendContent("This is a ");
        assertEquals("This is a ", tag.getContent());
        
        tag.appendContent("String");
        assertEquals("This is a String", tag.getContent());
    }

    @Test
    public void testAppendContent_3args() {
        Tag tag = new Tag("tag", null);
        assertEquals("", tag.getContent());

        char[] charArray = "This is a String".toCharArray();
        
        tag.appendContent(charArray, 0, 10);
        assertEquals("This is a ", tag.getContent());
        
        tag.appendContent(charArray, 10, 6);
        assertEquals("This is a String", tag.getContent());
    }

    @Test
    public void testHasSignificantContent() {
        Tag tag = new Tag("tag", null);
        tag.appendContent(" \t \n \r\n    \t");
        assertFalse(tag.hasSignificantContent());
        
        tag = new Tag("tag", null);
        tag.appendContent("\n \r\n \t\t significant");
        assertTrue(tag.hasSignificantContent());
    }

    @Test
    public void testEquals() {
        Tag parent = new Tag("parent", null);
        
        Tag tag1 = new Tag("tag", null);
        Tag tag2 = new Tag("tag2", null);
        assertFalse(tag1.equals(null));
        assertNotEquals(tag1, tag2);
        assertNotEquals(tag1, new Object());
        tag2 = new Tag("tag", null);
        assertEquals(tag1, tag2);
        
        // add parents
        tag1 = new Tag("tag", parent);
        tag2 = new Tag("tag", null);
        assertNotEquals(tag1, tag2);
        tag2 = new Tag("tag", parent);
        assertEquals(tag1, tag2);
        
        tag1.put("attr1", "value");
        assertNotEquals(tag1, tag2);
        tag2.put("attr1", "not same value");
        assertNotEquals(tag1, tag2);
        tag2.put("attr1", "value");
        assertEquals(tag1, tag2);
        
        tag1.appendContent(" \t\n");
        assertEquals(tag1, tag2);
        tag2.appendContent(" \t\n");
        assertEquals(tag1, tag2);
        tag1.appendContent("blah");
        assertNotEquals(tag1, tag2);
        assertNotEquals(tag2, tag1);
        tag2.appendContent("blah");
        assertEquals(tag1, tag2);
    }

    @Test
    public void testSize() {
        Tag tag = new Tag("tag", null);
        assertEquals(0, tag.size());
        tag.put("attr", "value");
        assertEquals(1, tag.size());
    }

    @Test
    public void testIsEmpty() {
        Tag tag = new Tag("tag", null);
        assertTrue(tag.isEmpty());
        tag.put("attr", "value");
        assertFalse(tag.isEmpty());
    }

    @Test
    public void testContainsKey() {
        Tag tag = new Tag("tag", null);
        tag.put("attr", "value");
        assertTrue(tag.containsKey("attr"));
        assertFalse(tag.containsKey("attr2"));
    }

    @Test
    public void testContainsValue() {
        Tag tag = new Tag("tag", null);
        tag.put("attr", "value");
        assertTrue(tag.containsValue("value"));
        assertFalse(tag.containsValue("attr"));
    }

    @Test
    public void testGet() {
        Tag tag = new Tag("tag", null);
        assertNull(tag.get("attr"));
        tag.put("attr", "value");
        assertEquals("value",tag.get("attr"));
    }

    @Test
    public void testRemove() {
        Tag tag = new Tag("tag", null);
        tag.put("attr", "value");
        assertEquals(1, tag.size());
        
        assertEquals("value", tag.remove("attr"));
        assertEquals(0, tag.size());
    }

    @Test
    public void testPutAll() {
        Tag tag = new Tag("tag", null);
        
        Map<String, String> map = new LinkedHashMap<>();
        map.put("attr", "value");
        map.put("attrNew", "value");
        tag.putAll(map);
        
        assertTrue(tag.containsKey("attr"));
        assertTrue(tag.containsKey("attrNew"));
    }

    @Test
    public void testClear() {
        Tag tag = new Tag("tag", null);
        tag.put("attr", "value");
        assertEquals(1, tag.size());
        tag.clear();
        assertEquals(0,tag.size());
    }

    @Test
    public void testKeySet() {
        Tag tag = new Tag("tag", null);
        tag.put("attr", "value");
        
        Set<String> keySet = tag.keySet();
        assertEquals(1, keySet.size());
        assertTrue(keySet.contains("attr"));
    }

    @Test
    public void testValues() {
        Tag tag = new Tag("tag", null);
        tag.put("attr", "value");
        
        Collection<String> values = tag.values();
        assertEquals(1, values.size());
        assertEquals("value",values.iterator().next());
    }

    @Test
    public void testEntrySet() {
        Tag tag = new Tag("tag", null);
        tag.put("attr", "value");
        
        Set<Map.Entry<String,String>> entrySet = tag.entrySet();
        assertEquals(1, entrySet.size());
        Map.Entry<String,String> entry = entrySet.iterator().next();
        assertEquals("attr",entry.getKey());
        assertEquals("value",entry.getValue());
    }
}
