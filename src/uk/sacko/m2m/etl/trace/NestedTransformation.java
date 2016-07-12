package uk.sacko.m2m.etl.trace;

import java.util.Collection;

import org.eclipse.epsilon.common.util.CollectionUtil;
import org.eclipse.epsilon.etl.trace.Transformation;

public class NestedTransformation extends Transformation {
	protected final Collection<Transformation> dependencies =
			CollectionUtil.createDefaultList();

	public Collection<Transformation> getDependencies() {
		return this.dependencies;
	}
}
