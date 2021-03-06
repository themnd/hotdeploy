package com.polopoly.ps.hotdeploy.xml.parser.cache;

import com.polopoly.ps.hotdeploy.util.SingleObjectHolder;
import com.polopoly.ps.hotdeploy.xml.parser.ParseCallback;
import com.polopoly.ps.hotdeploy.xml.parser.ParseContext;


public class ClassReferenceMemento extends SingleObjectHolder<String> implements SingleCallMemento {
    private String klass;

    public ClassReferenceMemento(String klass) {
        super(klass);
        this.klass = klass;
    }

    public void replay(ParseContext context, SingleCallMemento memento,
            ParseCallback parseCallback) {
        parseCallback.classReferenceFound(context.getFile(), klass);
    }
}
