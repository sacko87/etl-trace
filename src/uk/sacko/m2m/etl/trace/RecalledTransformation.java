package uk.sacko.m2m.etl.trace;

import org.eclipse.epsilon.etl.trace.Transformation;

/**
 * A form of callback of a transformation trace, allowing
 * a graph like structure.
 * 
 * @author John T. Saxon
 * @version 1.0
 */
public class RecalledTransformation extends Transformation {
	protected Transformation recalled;

	public Transformation getRecalled() {
		return recalled;
	}

	public void setRecalled(Transformation recalled) {
		this.recalled = recalled;
	}
}
