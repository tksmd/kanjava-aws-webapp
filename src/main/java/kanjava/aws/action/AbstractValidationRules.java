package kanjava.aws.action;

import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.validator.DefaultValidationRules;
import org.seasar.cubby.validator.Validator;
import org.seasar.cubby.validator.validators.RequiredValidator;

public abstract class AbstractValidationRules extends DefaultValidationRules {

	private static final RequiredValidator REQUIRED = new RequiredValidator();

	protected Validator required() {
		return REQUIRED;
	}

	@Override
	public ActionResult fail(String errorPage) {
		
		
		

		return super.fail(errorPage);
	}

}
