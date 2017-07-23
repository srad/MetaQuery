package com.github.srad.textimager.reader.type;

import java.util.HashMap;

final public class Char extends ElementType {
    public Char(final Sofa sofa, final String id, final int begin, final int end) {
        super(sofa, new HashMap<String, String>() {{
            put("id", id);
            put("begin", String.valueOf(end));
            put("end", String.valueOf(end));
        }});
    }
    public Char(final Sofa sofa, final HashMap<String, String> attr) {
        super(sofa, attr);
    }

    @Override
    public String getText() { return sofa.getCharAt(begin).toLowerCase(); }
}
