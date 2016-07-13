package uk.sacko.m2m.etl.trace;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.epsilon.etl.dom.TransformationRule;
import org.eclipse.epsilon.etl.trace.TransformationList;
import org.eclipse.epsilon.etl.trace.TransformationTrace;

/**
 * A transformation trace for use with ETL that uses the class
 * NestedTransformation rather than ETLs own Transformation.
 * 
 * Rebuild due to scope issues on the original.
 * 
 * @author John T. Saxon
 * @version 1.0
 */
public class NestedTransformationTrace extends TransformationTrace {
	protected final Map<Object, TransformationList> cache = new ConcurrentHashMap<>();
	protected final TransformationList transformations = new TransformationList();
	
	@Override
	public void add(Object source, Collection<Object> targets, TransformationRule rule) {
		// DIFF
		NestedTransformation transformation = new NestedTransformation();
		// END DIFF
		transformation.setSource(source);
		transformation.setTargets(targets);
		transformation.setRule(rule);
		this.transformations.add(transformation);
		
		TransformationList transformations = this.cache.get(source);
		if(transformations == null) {
			transformations = new TransformationList();
			transformations.add(transformation);
			this.cache.put(source, transformations);
		} else {
			transformations.add(transformation);
		}
	}

	public TransformationList getTransformations() {
		return this.transformations;
	}
	
	public TransformationList getTransformations(Object source) {
		if(this.cache.containsKey(source)) {
			return this.cache.get(source);
		} else {
			return new TransformationList();
		}
	}
}
