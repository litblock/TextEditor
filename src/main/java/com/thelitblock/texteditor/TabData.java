package com.thelitblock.texteditor;

import org.fxmisc.richtext.CodeArea;

public class TabData {
    CodeArea codeArea;
    boolean isChanged;

    TabData(CodeArea codeArea) {
        this.codeArea = codeArea;
        this.isChanged = false;
    }
}
