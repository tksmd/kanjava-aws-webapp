/**
 * editor.js implements CloudEditor
 * 
 * @require jQuery 1.6.1 or later
 * @require jQuery UI 1.8.14 or later
 * @require underscore.js 1.1.6 or later
 * @require filter.js 0.0.1
 */
(function() {
	var root = this;

	// jQuery の Event オブジェクトのプロパティとして dataTransfer にアクセス出来るようにする
	jQuery.event.props.push("dataTransfer");

	var CloudEditor = function(canvas, basePath) {
		return setupContext(canvas, basePath);
	};
	CloudEditor.VERSION = "0.0.1";

	root.CloudEditor = CloudEditor;

	var setupContext = function(canvas, basePath) {

		var ctx = new Context(canvas, basePath), j$canvas = $(canvas);

		j$canvas.mousedown(function(evt) {
			if (ctx.state) {
				ctx.state.onMouseDown(evt, evt.offsetX, evt.offsetY);
			}
		});

		j$canvas.mouseup(function(evt) {
			if (ctx.state) {
				ctx.state.onMouseUp(evt, evt.offsetX, evt.offsetY);
			}
		});

		j$canvas.mousemove(function(evt) {
			if (ctx.state) {
				ctx.state.onMouseMove(evt, evt.offsetX, evt.offsetY);
			}
		});

		j$canvas.click(function(evt) {
			if (ctx.state) {
				ctx.state.onClick(evt);
			}
		});

		j$canvas.dblclick(function(evt) {
			if (ctx.state) {
				ctx.state.onDoubleClick(evt, evt.offsetX, evt.offsetY);
			}
		});

		j$canvas.bind("dragenter dragover", function(evt) {
			evt.preventDefault();
			return false;
		});

		j$canvas.bind("drop", function(evt) {
			if (ctx.state) {
				ctx.state.onDrop(evt, evt.offsetX, evt.offsetY);
			}
			return false;
		});

		$(document).keydown(function(evt) {
			if (ctx.state) {
				ctx.state.onKeyDown(evt);
			}
		});

		ctx.toDefaultState(); // start context
		return ctx;
	};

	/** Context Object for maintain CloudEditor state */
	var Context = function(canvas, basePath) {
		this.drawer = new Drawer(canvas);
		this.j$canvas = $(canvas);

		this.manager = new ControllerManager(basePath);
		this.manager.register("ec2", EC2Controller);
		this.manager.register("ebs", EBSController);
		this.manager.register("elb", ELBController);
		this.manager.register("connect", ConnectController);

		this._bind();
	};

	Context.prototype = {
		transit : function(state) {
			if (this.state) {
				this.state.end();
			}
			this.state = state;
			this.drawer.state = state;
			this.state.start();
			console.log("[Start] " + this.state.name);
		},
		toDefaultState : function() {
			this.transit(new NormalState(this));
		},
		dragItem : function(evt) {
			this.transit(new ItemDragState(this, evt));
		},
		startConnect : function() {
			this.transit(new ConnectStartState(this));
		},
		setDialog : function(dialog) {
			this.dialog = dialog.dialog({
				autoOpen : false,
				modal : true,
				resizable : false,
				width : 300,
				height : 300
			});
		},
		_bind : function() {
			var self = this;
			this.drawer.bind("viewAdded", function(evt, view, opts) {
				var controller = self.manager.create(view, opts);
				controller.onAdd();
			});
			this.drawer.bind("viewRemoved", function(evt, view) {
				var controller = self.manager.find(view), refresh = false;
				controller.onRemove();
				self.manager.remove(view);
				
				// connector を削除
				_.each(controller.connections,function(connector){
					self.manager.remove(connector.view);
					self.drawer.remove(connector.view, true);
					refresh = true;
				});			
				if(refresh){
					var f = _.bind(self.drawer.refresh,self.drawer);
					_.delay(f,1000);					
				}				
			});
			this.manager.bind("modelActivated", function(evt, model) {
				self.drawer.refresh();
			});
			this.manager.bind("viewInvalidated", function(evt, view) {
				// イベントがループしないように
				self.drawer.remove(view, true);
			});
		},
		_showDialog : function(view, x, y) {
			if (this.dialog == null) {
				return;
			}
			var controller = this.manager.find(view);
			var pos = this.j$canvas.offset();
			controller.showDialog(this.dialog, x + pos.left, y + pos.top - 300);
		}
	};

	var Drawer = function(canvas) {
		this.g = canvas.getContext("2d");
		this.width = canvas.width;
		this.height = canvas.height;
		this.j$canvas = $(canvas);
		this.views = [];
		this.state = null;
	};

	Drawer.prototype = {
		refresh : function() {
			var g = this.g;
			g.clearRect(0, 0, this.width, this.height);
			_.each(this.views, function(v) {
				v.draw(g);
				if (v.active) {
					v.drawActive(g);
				}
			});
			if (this.state) {
				this.state.draw(g);
			}
		},
		add : function(view, opts) {
			this.views.push(view);
			this.refresh();
			this.j$canvas.trigger("viewAdded", [ view, opts ]);
		},
		remove : function(view, suppress) {
			var suppress = suppress || false;
			this.views = _.reject(this.views, function(v) {
				return v.id == view.id;
			});
			this.refresh();
			if (!suppress) {
				this.j$canvas.trigger("viewRemoved", [ view ]);
			}
		},
		find : function(x, y) {
			return _.detect(this.views, function(v) {
				return v.hitTest(x, y);
			});
		},
		select : function(view) {
			if (!view) {
				this.refresh();
				return;
			}
			view.drawSelect(this.g);
		},
		getCacheImage : function(x, y, w, h) {
			var g = this.g;
			var src = g.getImageData(x, y, w, h);
			return Filter(g, src).blur();
		},
		bind : function(evtType, listener) {
			this.j$canvas.bind(evtType, listener);
		}
	};

	var ControllerManager = function(basePath) {
		this.factory = {};
		this.controllers = {};
		this.basePath = basePath || "";
	};

	ControllerManager.prototype = {
		create : function(view, opts) {
			var constructor = this.factory[view.key];
			var controller = new constructor(view, opts);
			controller.manager = this;
			this.controllers[view.id] = controller;
			return controller;
		},
		find : function(view) {
			return this.controllers[view.id];
		},
		remove : function(view) {
			delete this.controllers[view.id];
		},
		register : function(key, constructor) {
			this.factory[key] = constructor;
		},
		ajax : function(path, settings) {
			_.defaults(settings, {
				dataType : "json",
				error : function(data) {
					console.log("------> ajax error");
					console.log(data);
				}
			});
			$.ajax(this.basePath + path, settings);
		},
		trigger : function(evtType, args) {
			$(document.body).trigger(evtType, args);
		},
		bind : function(evtType, listener) {
			$(document.body).bind(evtType, listener);
		}
	};

	/** CloudEditor states */
	var State = function() {
	};

	State.prototype = {
		start : function() {
		},
		end : function() {
		},
		draw : function(g) {
		},
		onClick : function(evt) {
		},
		onDoubleClick : function(evt, x, y) {
		},
		onMouseDown : function(evt, x, y) {
		},
		onMouseUp : function(evt, x, y) {
		},
		onMouseMove : function(evt, x, y) {
		},
		onDrop : function(evt, x, y) {
		},
		onKeyDown : function(evt) {
		}
	};

	var NormalState = function(ctx) {
		this.ctx = ctx;
		this.name = "Normal";
	};

	_.extend(NormalState.prototype, State.prototype, {
		start : function() {
			this.ctx.drawer.select(null);
		},
		onMouseDown : function(evt, x, y) {
			var view = this.ctx.drawer.find(x, y);
			if (view) {
				this.ctx.transit(new SelectedState(this.ctx, view));
			}
		}
	});

	var SelectedState = function(ctx, selected) {
		this.ctx = ctx;
		this.selected = selected;
		this.name = "Selected";
	};

	_.extend(SelectedState.prototype, State.prototype, {
		start : function() {
			this.ctx.drawer.select(this.selected);
		},
		onDoubleClick : function(evt, x, y) {
			this.ctx._showDialog(this.selected, x, y);
		},
		onMouseDown : function(evt, x, y) {
			var view = this.ctx.drawer.find(x, y);
			if (view) {
				if (view.id != this.selected.id) {
					this.ctx.drawer.refresh();
					this.ctx.transit(new SelectedState(this.ctx, view));
				} else {
					this.ctx.transit(new SelectedDragState(this.ctx, view));
				}
			} else {
				this.ctx.toDefaultState();
			}
		},
		onKeyDown : function(evt) {
			if (evt.keyCode == 8 || evt.keycode == 46) {
				this.ctx.drawer.remove(this.selected);
				this.ctx.toDefaultState();
				evt.preventDefault();
			}
		}
	});

	var SelectedDragState = function(ctx, selected) {
		this.ctx = ctx;
		this.selected = selected;
		this.name = "SelectedDrag";
	};

	_.extend(SelectedDragState.prototype, State.prototype, {
		onMouseUp : function(evt, x, y) {
			this.ctx.transit(new SelectedState(this.ctx, this.selected));
		},
		onMouseMove : function(evt, x, y) {
			this.ctx.transit(new MoveState(this.ctx, this.selected, x, y));
		}
	});

	var MoveState = function(ctx, selected, x, y) {
		this.ctx = ctx;
		this.selected = selected;
		this.name = "Move";
		this.startX = x;
		this.startY = y;
		this.dx = this.dy = 0;
		this.cache = null;
	};

	_.extend(MoveState.prototype, State.prototype, {
		start : function() {
			var view = this.selected;
			this.cache = this.ctx.drawer.getCacheImage(view.x, view.y,
					view.width, view.height);
		},
		onMouseUp : function(evt, x, y) {
			var dx = x - this.startX;
			var dy = y - this.startY;
			this.selected.move(dx, dy);
			this.cache = null;
			this.ctx.drawer.refresh();
			this.ctx.transit(new SelectedState(this.ctx, this.selected));
		},
		onMouseMove : function(evt, x, y) {
			this.dx = x - this.startX;
			this.dy = y - this.startY;
			this.ctx.drawer.refresh();
		},
		draw : function(g) {
			if (!this.cache) {
				return;
			}
			var view = this.selected, nx = view.x + this.dx, ny = view.y
					+ this.dy;
			g.putImageData(this.cache, nx, ny);
		}
	});

	var ItemDragState = function(ctx, evt) {
		this.ctx = ctx;
		evt.dataTransfer.setData("text", evt.target.id);
		this.offsets = {};
		this.offsets[evt.target.id] = {
			x : evt.offsetX,
			y : evt.offsetY
		};
		this.name = "ItemDrag";
	};

	_.extend(ItemDragState.prototype, State.prototype, {
		onDrop : function(evt, x, y) {
			var id = evt.dataTransfer.getData("text"), j$item = $("#" + id);
			var props = $.data(j$item[0], "props"), src = j$item.attr("src");
			var view = new ImageView(src, props.key);
			view.x = x - this.offsets[id].x;
			view.y = y - this.offsets[id].y;
			this.ctx.drawer.add(view, props.opts);
			this.ctx.transit(new SelectedState(this.ctx, view));
		}
	});

	var ConnectStartState = function(ctx) {
		this.ctx = ctx;
		this.name = "ConnectStart";
	};

	_.extend(ConnectStartState.prototype, State.prototype, {
		start : function() {
			this.ctx.drawer.select(null);
		},
		onMouseDown : function(evt, x, y) {
			var view = this.ctx.drawer.find(x, y);
			if (view) {
				this.ctx.transit(new ConnectSearchState(this.ctx, view));
			} else {
				this.ctx.toDefaultState();
			}
		}
	});

	var ConnectSearchState = function(ctx, from) {
		this.ctx = ctx;
		this.from = from;
		this.name = "ConnectSearch";
		this.toX = from.x;
		this.toY = from.y;
	};

	_.extend(ConnectSearchState.prototype, State.prototype, {
		onMouseUp : function(evt, x, y) {
			var to = this.ctx.drawer.find(x, y);
			if (to) {
				if (this._connectable(this.from, to)) {
					var view = new ConnectView(this.from, to);
					this.ctx.drawer.add(view);
				}
			}
			this.ctx.toDefaultState();
		},
		onMouseMove : function(evt, x, y) {
			this.toX = x;
			this.toY = y;
			this.ctx.drawer.refresh();
		},
		draw : function(g) {
			if (this.from.hitTest(this.toX, this.toY)) {
				return;
			}
			var s = this.from._center(), to = new Point(this.toX, this.toY);
			var line = Line.create(s, to);
			var cand1 = this.from._intersects(line);
			var pair = ConnectView.search(cand1, [ to ]);

			g.beginPath();
			g.moveTo(pair.start.x, pair.start.y);
			g.lineTo(pair.end.x, pair.end.y);
			g.stroke();
		},
		_connectable : function(from, to) {
			// 双方異なっていて、かつアクティブであること
			// これ以上の複雑な条件判定はコントローラ側で行う
			return from.id != to.id && from.active && to.active;
		}
	});

	var View = function() {
		this.reset();
	};

	View.prototype = {
		reset : function() {
			this.x = 0;
			this.y = 0;
			this.width = 0;
			this.height = 0;
			this.active = false;
			this.id = _.uniqueId("view");
		},
		move : function(dx, dy) {
			this.x += dx;
			this.y += dy;
		},
		draw : function(g) {
		},
		_delayDraw : function(func, g, count) {
			var count = count || 0;
			if (count == 3) {
				console.log("3 times called,  give up...");
				return;
			}
			count++;
			var f = _.bind(func, this);
			_.delay(f, 200, g, count);
		},
		drawSelect : function(g, count) {
			if (this.width == 0 && this.height == 0) {
				this._delayDraw(arguments.callee, g, count);
				return;
			}
			g.fillStyle = 'rgba(155, 187, 89, 0.8)';
			_.each(this._bounds(), function(p) {
				g.fillRect(p.x - 4, p.y - 4, 8, 8);
			});
		},
		drawActive : function(g, count) {
			if (this.width == 0 && this.height == 0) {
				this._delayDraw(arguments.callee, g, count);
				return;
			}
			g.fillStyle = 'rgba(192, 80, 77, 0.2)';
			g.fillRect(this.x, this.y, this.width, this.height);
		},
		_bounds : function() {
			return [ new Point(this.x, this.y),
					new Point(this.x + this.width, this.y),
					new Point(this.x + this.width, this.y + this.height),
					new Point(this.x, this.y + this.height) ];
		},
		_center : function() {
			return new Point(this.x + this.width / 2, this.y + this.height / 2);
		},
		_intersects : function(line) {
			var bounds = [ new Line(1, 0, -this.x),
					new Line(1, 0, -(this.x + this.width)),
					new Line(0, 1, -this.y),
					new Line(0, 1, -(this.y + this.height)) ];
			var self = this;
			return _(bounds).chain().map(function(b) {
				return Line.intersect(b, line);
			}).select(
					function(p) {
						return p != null
								&& self.hitTest(Math.round(p.x), Math
										.round(p.y));
					}).value();
		},
		hitTest : function(x, y) {
			return (this.x <= x && x <= (this.x + this.width))
					&& (this.y <= y && y <= (this.y + this.height));
		}
	};

	var ImageView = function(src, key) {
		this.src = src;
		this.key = key;
		this.loaded = null;
		this.connectable = true;
		this.reset();
	};

	_.extend(ImageView.prototype, View.prototype, {
		draw : function(g) {
			if (this.loaded) {
				g.drawImage(this.loaded, this.x, this.y);
				return;
			}
			var img = new Image(), self = this;
			img.onload = function() {
				self.width = img.width;
				self.height = img.height;
				self.loaded = img;
				g.drawImage(img, self.x, self.y);
			}
			img.src = self.src + "?" + new Date().getTime();
		}
	});

	var ConnectView = function(from, to) {
		this.from = from;
		this.to = to;
		this.reset();
		this.key = "connect";
	};

	ConnectView.search = function(cand1, cand2) {
		return _(cand1).chain().map(function(p1) {
			return _(cand2).map(function(p2) {
				return {
					"start" : p1,
					"end" : p2,
					"distance" : Point.distance(p1, p2)
				};
			});
		}).flatten().min(function(v) {
			return v.distance;
		}).value();
	};

	_.extend(ConnectView.prototype, View.prototype, {
		draw : function(g) {
			var l = Line.create(this.from._center(), this.to._center());
			var cand1 = this.from._intersects(l);
			var cand2 = this.to._intersects(l);
			var pair = ConnectView.search(cand1, cand2);

			g.beginPath();
			g.moveTo(pair.start.x, pair.start.y);
			g.lineTo(pair.end.x, pair.end.y);
			g.stroke();
		}
	});

	var Controller = function(view) {
		this.view = view;
		this.reset();
	};

	Controller.prototype = {
		reset : function() {
			this.id = _.uniqueId("controller");
			this.connections = [];
		},
		onAdd : function() {
		},
		onRemove : function() {
		},
		onActive : function() {
			// reload 時に既に active の状態で呼ばれることもあるため
			if (!this.view.active) {
				this.view.active = true;
				this.manager.trigger("modelActivated", [ this.model ]);
			}
		},
		ajax : function(path, settings) {
			this.manager.ajax(path, settings);
		},
		delay : function(func, arg, millis) {
			var f = _.bind(func, this), millis = millis || 10000;
			_.delay(f, millis, arg);
		},
		showDialog : function(dialog, x, y) {
			if (!this.view.active) {
				return;
			}
			dialog.dialog("option", {
				"position" : [ x, y ],
				"title" : this.dialogTitle
			});
			this.renderModel(dialog);
			dialog.dialog("open");
		},
		renderModel : function(dialog) {
			var tr = "<% _.each(model, function(value, key) { %> <tr><td><%= key %></td><td><%= value %></td></tr> <% }); %>";
			var compiled = _.template(tr);
			dialog.html("<table>" + compiled({
				"model" : this.model
			}) + "</table>");
		},
		connect : function(controller) {
			this.connections.push(controller);
		},
		disconnect : function(connected) {
			this.connections = _.reject(this.connections, function(controller) {
				return connected.id == controller.id;
			});
		}
	};

	var EC2Controller = function(view, opts) {
		this.view = view;
		this.opts = opts;
		this.dialogTitle = "EC2 インスタンス";
		this.type = "ec2";
		this.connectableTypes = [ "ebs", "elb" ];
		this.deviceCount = 0;
		this.reset();
	};

	_.extend(EC2Controller.prototype, Controller.prototype, {
		onAdd : function() {
			var path = "/api/ec2/run/" + this.opts["type"];
			this.ajax(path, {
				context : this,
				success : function(data) {
					this.model = data;
					this.delay(this.reload, 0, 8000);
				}
			});
		},
		onRemove : function() {
			if (this.model == null) {
				return;
			}
			var path = "/api/ec2/terminate/" + this.model.instanceId;
			this.ajax(path, {
				context : this,
				success : function(data) {
					this.model = null;
				}
			});
		},
		reload : function(count) {
			if (this.model == null || this.model.instanceId == null) {
				return;
			}
			if (count == 6) {
				console.log("retry count reached maxium.");
				return;
			}
			var path = "/api/ec2/instance/" + this.model.instanceId;
			this.ajax(path, {
				context : this,
				success : function(data) {
					this.model = data;
					if (this.model.state.code != 16) {
						count++;
						this.delay(this.reload, count, 8000);
					} else {
						this.onActive();
					}
				}
			});
		},
		getAvailableDevice : function() {
			var start = 'f';
			var suffix = String.fromCharCode(start.charCodeAt(0)
					+ this.deviceCount);
			this.deviceCount++;
			return "/dev/sd" + suffix;
		}
	});

	var EBSController = function(view, opts) {
		this.view = view;
		this.opts = opts;
		this.dialogTitle = "EBS ボリューム";
		this.type = "ebs";
		this.connectableTypes = [ "ec2" ];
		this.reset();
	};

	_.extend(EBSController.prototype, Controller.prototype, {
		onAdd : function() {
			var path = "/api/ebs/create";
			this.ajax(path, {
				context : this,
				success : function(data) {
					this.model = data;
					this.delay(this.reload, 0, 5000);
				}
			});
		},
		onRemove : function() {
			if (this.model == null) {
				return;
			}
			var path = "/api/ebs/delete/" + this.model.volumeId;
			this.ajax(path, {
				context : this,
				success : function(data) {
					this.model = null;
				}
			});
		},
		reload : function(count) {
			if (this.model == null || this.model.volumeId == null) {
				return;
			}
			if (count == 6) {
				console.log("retry count reached maxium.");
				return;
			}
			var path = "/api/ebs/volume/" + this.model.volumeId;
			this.ajax(path, {
				context : this,
				success : function(data) {
					this.model = data;
					if (!_.include(["available","in-use"],this.model.state)) {
						count++;
						this.delay(this.reload, count, 5000);
					} else {
						this.onActive();
					}
				}
			});
		}
	});

	var ELBController = function(view, opts) {
		this.view = view;
		this.opts = opts;
		this.dialogTitle = "ELB";
		this.type = "elb";
		this.connectableTypes = [ "ec2" ];
		this.reset();
	};

	_.extend(ELBController.prototype, Controller.prototype, {
		onAdd : function() {
			var path = "/api/elb/create/elb-" + this.view.id;
			this.ajax(path, {
				context : this,
				success : function(data) {
					this.model = data;
					this.onActive();
				}
			});
		},
		onRemove : function() {
			if (this.model == null) {
				return;
			}
			var path = "/api/elb/delete/" + this.model.loadBalancerName;
			this.ajax(path, {
				context : this,
				success : function(data) {
					this.model = null;
				}
			});
		},
		reload : function(count) {
			var path = "/api/elb/balancer/" + this.model.loadBalancerName;
			this.ajax(path, {
				context : this,
				success : function(data) {
					this.model = data;
				}
			});
		}
	});

	var ConnectController = function(view) {
		this.view = view;
		this.dialogTitle = "";
		this.type = "connect";
		this.connectableTypes = [];
		this.reset();
	};

	_.extend(ConnectController.prototype, Controller.prototype, {
		onAdd : function() {
			var from = this.manager.find(this.view.from);
			var to = this.manager.find(this.view.to);
			var conn = Connection.create(from, to);
			if (conn && conn.connectable()) {
				this.model = conn;
				conn.parent = this;
				conn.onAdd();
			} else {
				this.manager.trigger("viewInvalidated", this.view);
			}
		},
		onRemove : function() {
			if (this.model) {
				this.model.onRemove();
				this.model = null;
			}
		}
	});

	var Connection = function() {
		this.reset();
	}

	Connection.prototype = {
		reset : function() {
			this.id = _.uniqueId("connection");
			this.parent = null;
			this.model = null;
		},
		connectable : function() {
			return false;
		},
		onAdd : function() {
		},
		onRemove : function() {
		},
		ajax : function(path,settings){
			// まだ有効な connection でない場合 controller が割り当てられていない
			if(this.parent == null){
				return;
			}
			this.parent.ajax(path,settings);
		}
	};

	Connection.create = function(a, b) {
		if (!_.include(a.connectableTypes, b.type)
				|| !_.include(b.connectableTypes, a.type)) {
			return null;
		}
		if (a.type == "ebs" || b.type == "ebs") {
			return new EBSConnection(a, b);
		} else if (a.type == "elb" || b.type == "elb") {
			return new ELBConnection(a, b);
		}
		return null;
	};

	Connection.selectType = function(args, type) {
		var getSelector = function(type) {
			return function(controller) {
				return controller.type == type;
			}
		};
		return _(args).chain().select(getSelector(type)).first().value();
	}

	var EBSConnection = function(a, b) {
		this.ec2 = Connection.selectType([ a, b ], "ec2");
		this.ebs = Connection.selectType([ a, b ], "ebs");
		this.reset();
	};

	_.extend(EBSConnection.prototype, Connection.prototype, {
		connectable : function() {
			// 既に接続されていないかどうか
			return this.ebs.model.state == "available";
		},
		onAdd : function() {
			var path = "/api/ebs/attach/" + this.ebs.model.volumeId;
			this.ajax(path, {
				context : this,
				data : {
					instanceId : this.ec2.model.instanceId,
					device : this.ec2.getAvailableDevice()
				},
				success : function(data) {
					this.model = data;
					this.ec2.connect(this.parent);
					this.ebs.connect(this.parent);
					this.ec2.reload();
					this.ebs.reload();
				}
			});
		},
		onDelete : function() {
			var path = "/api/ebs/detach/" + this.ebs.model.volumeId;
			this.ajax(path, {
				context : this,
				success : function(data) {
					this.model = null;
					this.ec2.disconnect(this.parent);
					this.ebs.disconnect(this.parent);
					this.ec2.reload();
					this.ebs.reload();
				}
			});
		}
	});

	var ELBConnection = function(a, b) {
		this.ec2 = Connection.selectType([ a, b ], "ec2");
		this.elb = Connection.selectType([ a, b ], "elb");
		this.reset();
	};

	_.extend(ELBConnection.prototype, Connection.prototype, {
		connectable : function() {
			var self = this;
			return !_.any(this.elb.instances, function(i) {
				return i.instanceId == self.ec2.model.instanceId;
			});
		},
		onAdd : function() {
			var name = this.elb.model.loadBalancerName;
			var path = "/api/elb/register/" + name;
			this.ajax(path, {
				context : this,
				data : {
					instanceId : this.ec2.model.instanceId
				},
				success : function(data) {
					this.model = data;
					this.ec2.connect(this.parent);
					this.elb.connect(this.parent);
					this.elb.reload();
				}
			});
		},
		onDelete : function() {
			var name = this.elb.model.loadBalancerName;
			var path = "/api/elb/deregister/" + name;
			this.ajax(path, {
				context : this,
				data : {
					instanceId : this.ec2.model.instanceId
				},
				success : function(data) {
					this.model = null;
					this.ec2.disconnect(this.parent);
					this.elb.disconnect(this.parent);
					this.elb.reload();
				}
			});
		}
	});

	var Line = function(a, b, c) {
		this.a = a || 0;
		this.b = b || 0;
		this.c = c || 0;
	};

	Line.create = function(p1, p2) {
		var line = new Line();
		if (p1.x == p2.x) {
			line.a = 1;
			line.b = 0;
			line.c = -p1.x;
		} else if (p1.y == p2.y) {
			line.a = 0;
			line.b = 1;
			line.c = -p1.y;
		} else {
			var denom = p1.x - p2.x;
			line.a = (p1.y - p2.y) / denom;
			line.b = -1;
			line.c = (p1.x * p2.y - p2.x * p1.y) / denom;
		}
		return line;
	};

	Line.isParallel = function(l1, l2) {
		return (l1.a == 0 && l2.a == 0) || (l1.b == 0 && l2.b == 0)
				|| (l1.a == l2.a && l1.b == l2.b);
	};

	Line.intersect = function(l1, l2) {
		if (Line.isParallel(l1, l2)) {
			return null;
		}
		var denom = (l1.a * l2.b - l2.a * l1.b);
		var x = (l2.c * l1.b - l1.c * l2.b) / denom;
		var y = -(l2.c * l1.a - l1.c * l2.a) / denom;
		return new Point(x, y);
	};

	var Point = function(x, y) {
		this.x = x;
		this.y = y;
	};

	Point.distance = function(p1, p2) {
		return Math.sqrt(Math.pow((p1.x - p2.x), 2)
				+ Math.pow((p1.y - p2.y), 2));
	};

})();
