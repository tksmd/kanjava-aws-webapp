package kanjava.aws.action;

import javax.servlet.http.HttpServletRequest;

import org.seasar.cubby.action.ActionContext;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Json;
import org.seasar.cubby.internal.controller.ThreadContext;
import org.seasar.cubby.util.ActionUtils;
import org.seasar.cubby.validator.DefaultValidationRules;
import org.seasar.cubby.validator.Validator;
import org.seasar.cubby.validator.validators.RequiredValidator;

public abstract class BaseValidationRules extends DefaultValidationRules {

	private static final RequiredValidator REQUIRED = new RequiredValidator();

	protected Validator required() {
		return REQUIRED;
	}

	@Override
	public ActionResult fail(String errorPage) {
		if (isAjax()) {
			ActionContext context = ActionUtils.actionContext();
			return new Json(context.getActionErrors());
		}
		return super.fail(errorPage);
	}

	boolean isAjax() {
		final ThreadContext currentContext = ThreadContext.getCurrentContext();
		final HttpServletRequest request = currentContext.getRequest();
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}

}
