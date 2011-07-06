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

package kanjava.aws.action;

import java.util.List;

import kanjava.aws.service.EC2Service;

import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Path;

import com.amazonaws.services.ec2.model.Instance;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
@ActionClass
@Path("/")
public class IndexAction {

	@Inject
	private EC2Service ec2Service;

	private List<Instance> instances;

	public List<Instance> getInstances() {
		return instances;
	}

	public ActionResult index() {
		return new Forward("index.jsp");
	}

	public ActionResult status() {
		this.instances = ec2Service.getRunnningInstances();
		return new Forward("status.jsp");
	}
	
	public ActionResult editor(){
		return new Forward("editor.jsp");
	}
	
}