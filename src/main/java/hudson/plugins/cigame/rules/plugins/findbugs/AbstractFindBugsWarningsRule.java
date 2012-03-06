package hudson.plugins.cigame.rules.plugins.findbugs;

import java.util.List;

import hudson.maven.MavenBuild;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.checkstyle.CheckStyleMavenResultAction;
import hudson.plugins.checkstyle.CheckStyleResultAction;
import hudson.plugins.cigame.model.AggregatableRule;
import hudson.plugins.cigame.model.RuleResult;
import hudson.plugins.cigame.util.ActionRetriever;
import hudson.plugins.findbugs.FindBugsMavenResultAction;
import hudson.plugins.findbugs.FindBugsResultAction;

public abstract class AbstractFindBugsWarningsRule implements AggregatableRule<Integer> {
	
	protected static final RuleResult<Integer> EMPTY_RESULT = new RuleResult<Integer>(0.0, "", Integer.valueOf(0));
	
	protected Priority priority;

	protected AbstractFindBugsWarningsRule(Priority priority) {
		this.priority = priority;
	}
	
    protected boolean hasNoErrors(List<FindBugsResultAction> actions) {
        for (FindBugsResultAction action : actions) {
            if (action.getResult().hasError()) {
                return false;
            }
        }
        return true;
    }
    
    protected int getNumberOfAnnotations(List<FindBugsResultAction> list) {
        int numberOfAnnotations = 0;
        for (FindBugsResultAction action : list) {
            numberOfAnnotations += action.getResult().getNumberOfAnnotations(priority);
        }
        return numberOfAnnotations;
    }
    
    protected boolean hasMavenNoErrors(List<FindBugsMavenResultAction> actions) {
        for (FindBugsMavenResultAction action : actions) {
            if (action.getResult().hasError()) {
                return false;
            }
        }
        return true;
    }
    
    protected int getMavenNumberOfAnnotations(List<FindBugsMavenResultAction> list) {
        int numberOfAnnotations = 0;
        for (FindBugsMavenResultAction action : list) {
            numberOfAnnotations += action.getResult().getNumberOfAnnotations(priority);
        }
        return numberOfAnnotations;
    }
    
	@Override
	public final RuleResult<Integer> evaluate(AbstractBuild<?, ?> previousBuild,
			AbstractBuild<?, ?> build) {
    	if (build != null && build.getResult() != null && build.getResult().isWorseOrEqualTo(Result.FAILURE)) {
    		return EMPTY_RESULT;
    	}
    	
    	if (previousBuild == null) {
    		if ( !(build instanceof MavenBuild)) {
    			// backward compatibility
    			return EMPTY_RESULT;
    		}
    	} else if (previousBuild.getResult().isWorseOrEqualTo(Result.FAILURE)) {
    		return EMPTY_RESULT;
    	}
    	
    	boolean isMavenProject=false;
        int currentAnnotations = 0;
        int previousAnnotations = 0;
        
        if(isMavenProject)
        {
            List<FindBugsMavenResultAction> currentActions = ActionRetriever.getResult(build, Result.UNSTABLE, FindBugsMavenResultAction.class);
            List<FindBugsMavenResultAction> previousActions=ActionRetriever.getResult(previousBuild, Result.UNSTABLE, FindBugsMavenResultAction.class);
            
            if (!hasMavenNoErrors(currentActions)) {
                return RuleResult.EMPTY_INT_RESULT;
            }
            currentAnnotations = getMavenNumberOfAnnotations(currentActions);
                

            if (!hasMavenNoErrors(previousActions)) {
                return RuleResult.EMPTY_INT_RESULT; 
            }
            previousAnnotations = getMavenNumberOfAnnotations(previousActions);
        }
        else
        {                                           
            List<FindBugsResultAction> currentActions = ActionRetriever.getResult(build, Result.UNSTABLE, FindBugsResultAction.class);
            List<FindBugsResultAction> previousActions=ActionRetriever.getResult(previousBuild, Result.UNSTABLE, FindBugsResultAction.class);
            
            
           if (!hasNoErrors(currentActions)) {
                return RuleResult.EMPTY_INT_RESULT;
            }
            currentAnnotations = getNumberOfAnnotations(currentActions);
                

            if (!hasNoErrors(previousActions)) {
                return RuleResult.EMPTY_INT_RESULT; 
            }
            previousAnnotations = getNumberOfAnnotations(previousActions);
        }
    	
    	return evaluate(previousAnnotations, currentAnnotations);
	}
	
	protected abstract RuleResult<Integer> evaluate(int previousAnnotations, int currentAnnotations);
}
