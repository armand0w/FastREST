package com.armandow.rest.enums;

public enum ContentType {
    NONE(null),

    APPLICATION_ATOM_XML("application/atom+xml"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_JSON("application/json"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_SVG_XML("application/svg+xml"),
    APPLICATION_XHTML_XML("application/xhtml+xml"),
    APPLICATION_XML("application/xml"),
    APPLICATION_PDF("application/pdf"),
    APPLICATION_ZIP("application/zip"),
    APPLICATION_GZIP("application/gzip"),
    APPLICATION_CSV("application/csv"),

    MULTIPART_FORM_DATA("multipart/form-data"),

    TEXT_HTML("text/html"),
    TEXT_PLAIN("text/plain"),
    TEXT_XML("text/xml"),
    TEXT_CSS("text/css"),

    IMAGE_PNG("image/png"),
    IMAGE_JPG("image/jpg"),
    IMAGE_SVG("image/svg+xml");

    private final String name;

    ContentType(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
