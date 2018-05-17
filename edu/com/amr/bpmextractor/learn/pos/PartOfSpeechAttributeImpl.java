package com.amr.bpmextractor.learn.pos;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public final class PartOfSpeechAttributeImpl extends AttributeImpl implements PartOfSpeechAttribute {

	private PartOfSpeech pos = PartOfSpeech.Unknown;

	public void setPartOfSpeech(PartOfSpeech pos) {
		this.pos = pos;
	}

	public PartOfSpeech getPartOfSpeech() {
		return pos;
	}

	@Override
	public void clear() {
		pos = PartOfSpeech.Unknown;
	}

	@Override
	public void copyTo(AttributeImpl target) {
		((PartOfSpeechAttribute) target).setPartOfSpeech(pos);
	}

	@Override
	public void reflectWith(AttributeReflector target) {
		((PartOfSpeechAttribute) target).setPartOfSpeech(pos);
	}
}
