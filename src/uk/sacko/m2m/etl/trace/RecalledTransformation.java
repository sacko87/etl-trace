package uk.sacko.m2m.etl.trace;

import org.eclipse.epsilon.etl.trace.Transformation;

public class RecalledTransformation extends Transformation {
	protected Transformation recalled;

	public Transformation getRecalled() {
		return recalled;
	}

	public void setRecalled(Transformation recalled) {
		this.recalled = recalled;
	}
	
	public String toString() {
		return this.recalled.toString();
	}
}
