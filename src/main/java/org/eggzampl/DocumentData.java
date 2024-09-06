package org.eggzampl;

import org.apache.lucene.document.Document;

public class DocumentData {
    private Document document;
    private String snippet;

    public DocumentData(Document document, String snippet) {
        this.document = document;
        this.snippet = snippet;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}

