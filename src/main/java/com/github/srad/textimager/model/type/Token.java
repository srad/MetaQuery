package com.github.srad.textimager.model.type;

public class Token extends AbstractElement {
    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getMorph() {
        return morph;
    }

    public void setMorph(String morph) {
        this.morph = morph;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    private String lemma;
    private String morph;
    private String pos;

    public Token(String id, String begin, String end, String text, String lemma, String morph, String pos) {
        super(id, begin, end, text);
        this.lemma = lemma;
        this.morph = morph;
        this.pos = pos;
    }
}
