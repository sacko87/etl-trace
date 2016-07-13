package uk.sacko.m2m.etl.execute.listeners;

import java.util.NoSuchElementException;

import org.eclipse.epsilon.common.parse.AST;
import org.eclipse.epsilon.eol.dom.NewInstanceExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.control.IExecutionListener;
import org.eclipse.epsilon.etl.execute.context.IEtlContext;
import org.eclipse.epsilon.etl.strategy.ITransformationStrategy;

import uk.sacko.m2m.etl.strategy.TransformationStrategy;

/**
 * An execution listener that intercepts objects that are not created
 * by the ETL engine itself. It then uses the new transformation strategy
 * that exposes an execution stack of transformations that allow us to
 * retain this as a target object.
 * 
 * @author John T. Saxon
 * @version 1.0
 */
public class OrphanCaptureListener implements IExecutionListener {
	@Override
	public void aboutToExecute(AST ast, IEolContext context) {
		
	}

	@Override
	public void finishedExecuting(AST ast, Object result, IEolContext context) {
		// is this a new instance?
		if(NewInstanceExpression.class.isInstance(ast)) {
			// are we being called in an ETL context?
			if(IEtlContext.class.isInstance(context)) {
				IEtlContext etlContext = IEtlContext.class.cast(context);
				// are we using our strategy? i.e. with an exposed stack?
				ITransformationStrategy strategy = etlContext.getTransformationStrategy();
				if(TransformationStrategy.class.isInstance(strategy)) {
					TransformationStrategy sitraStrategy =
							TransformationStrategy.class.cast(strategy);
					
					try {
						// add `result' to trace, via IEtlContext's trace strategy.
						sitraStrategy.getExecutionStack().getFirst().getTargets().add(result);
					} catch(NoSuchElementException e) {
						// should never happen, as even the first transformation would
						// be the first element on the stack.
					}
				}
			}
		}
	}

	@Override
	public void finishedExecutingWithException(AST ast, EolRuntimeException exception, IEolContext context) {

	}
}
