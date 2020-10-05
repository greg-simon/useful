package au.id.simo.useful.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class that represents an Xml tag.
 */
public class Tag implements Map<String, String> {

    private final String name;
    private final StringBuilder content;
    private final Map<String, String> attributes;
    private final Tag parent;
    private final Collection<Tag> children;
    private boolean significantContent = false;

    /**
     * Will register this instance as a child to the parent Tag (if not null).
     * 
     * @param name
     * @param parent 
     */
    public Tag(String name, Tag parent) {
        this.name = name;
        this.parent = parent;
        this.content = new StringBuilder();
        this.attributes = new LinkedHashMap<>();
        this.children = new ArrayList<>();
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public String getXPath() {
        StringBuilder sb = new StringBuilder(200);
        Tag p = this;
        do {
            sb.insert(0, p.getName());
            sb.insert(0, "/");
        } while ((p = p.getParent()) != null);
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Tests if current tag has any parent with the given attribute and
     * attribute value.
     *
     * @param key name of attribute to test each parent tag with.
     * @param value value of the attribute to test each parent tag with.
     * @return true if any ancestor of this tag contains the given attribute and
     * attribute value.
     */
    public boolean hasParentWith(String key, String value) {
        if (hasParent()) {
            if (parent.containsKey(key) && value.equals(parent.get(key))) {
                return true;
            } else {
                return parent.hasParentWith(key, value);
            }
        } else {
            return false;
        }
    }

    public Tag getParent() {
        return parent;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    public Collection<Tag> getChildren() {
        return new ArrayList<>(this.children);
    }

    public void appendContent(String content) {
        char[] cnt = content.toCharArray();
        this.appendContent(cnt, 0, cnt.length);
    }

    public void appendContent(char[] ch, int start, int length) {
        if (!significantContent) {
            for (int i = start; i < start + length; i++) {
                switch (ch[i]) {
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        break;
                    default:
                        significantContent = true;
                }
            }
        }
        content.append(ch, start, length);
    }

    public String getContent() {
        return content.toString();
    }

    /**
     * Check if tag has content with more than just whitespace.
     *
     * @return
     */
    public boolean hasSignificantContent() {
        return significantContent;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tag)) {
            return false;
        }
        Tag tObj = (Tag) obj;
        // attributes
        if (this.attributes.size() != tObj.attributes.size()) {
            return false;
        }
        for (String key : this.attributes.keySet()) {
            if (!this.attributes.get(key).equals(tObj.attributes.get(key))) {
                return false;
            }
        }
        // xpath
        if (!this.getXPath().equals(tObj.getXPath())) {
            return false;
        }
        // content
        if (this.hasSignificantContent() || tObj.hasSignificantContent()) {
            if (!this.getContent().equals(tObj.getContent())) {
                return false;
            }
        }
        return true;
    }

    //============================
    // attribute Map methods
    //============================
    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return attributes.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return attributes.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return attributes.get(key);
    }

    @Override
    public String put(String key, String value) {
        return attributes.put(key, value);
    }

    @Override
    public String remove(Object key) {
        return attributes.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        attributes.putAll(m);
    }

    @Override
    public void clear() {
        attributes.clear();
    }

    @Override
    public Set<String> keySet() {
        return attributes.keySet();
    }

    @Override
    public Collection<String> values() {
        return attributes.values();
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return attributes.entrySet();
    }
}
