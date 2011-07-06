/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package kanjava.aws;

import kanjava.aws.action.ApiAction;
import kanjava.aws.action.HelloAction;
import kanjava.aws.action.IndexAction;
import net.sf.oval.localization.context.OValContextRenderer;
import net.sf.oval.localization.message.MessageResolver;

import org.seasar.cubby.plugins.guice.CubbyModule;
import org.seasar.cubby.plugins.guice.FileUploadModule;
import org.seasar.cubby.plugins.oval.validation.RequestLocaleMessageResolver;
import org.seasar.cubby.plugins.oval.validation.RequestLocaleOValContextRenderer;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new ServletModule());
		install(new CubbyModule());
		install(new FileUploadModule());
		install(new AWSModule());

		bind(IndexAction.class);
		bind(HelloAction.class);
		bind(ApiAction.class);

		configureOVal();
	}

	protected void configureOVal() {
		bind(MessageResolver.class).to(RequestLocaleMessageResolver.class);
		bind(OValContextRenderer.class).to(
				RequestLocaleOValContextRenderer.class);
	}

}
