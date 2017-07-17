package main.java.org.srad.textimager.reader.type;

final public class Char extends ElementType {
    public Char(final Sofa sofa, final String id, final int begin, final int end) {
        super(sofa, id, begin, end);
    }

    @Override
    public String getText() { return sofa.getCharAt(begin).toLowerCase(); }
}
