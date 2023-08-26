package au.id.simo.useful.xml;

@FunctionalInterface
public interface TagHandler {
    /**
     * This method is called when the provided tag has closed, and the fully
     * constructed {@link Tag} object is passed in as an argument.
     *
     * @param tag The tag that has closed in the endElement SAX event.
     */
    void endTag(Tag tag);

    /**
     * Run just after an opening tag has been read.
     *
     * @param tag the opening tag. It will not contain any text as text will not
     * have been read yet. But it will contain any tag attributes present.
     */
    default void startTag(Tag tag){}
}
