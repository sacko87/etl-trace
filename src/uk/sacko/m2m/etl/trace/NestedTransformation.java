package uk.sacko.m2m.etl.trace;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.epsilon.etl.trace.Transformation;

/**
 * A form of transformation trace that is tree like, i.e. can have
 * dependencies.
 * 
 * @author John T. Saxon
 * @version 1.0
 */
public class NestedTransformation extends Transformation {
	protected final Collection<Transformation> dependencies =
			Collections.synchronizedSet(new HashSet<Transformation>());

	public Collection<Transformation> getDependencies() {
		return this.dependencies;
	}
}
