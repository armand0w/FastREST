package com.armandow.rest.enums;

public enum CharsetType {
    US_ASCII("US-ASCII"),
    ISO_8859_1("ISO-8859-1"),
    UTF_8("UTF-8"),
    UTF_16BE("UTF-16BE"),
    UTF_16LE("UTF-16LE"),
    UTF_16("UTF_16"),
    NONE(null);

    private final String name;

    CharsetType(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
