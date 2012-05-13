package controllers;

import java.io.IOException;

import models.Description;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

	public static Result index() {
		return ok("Your new application is ready.");
	}

	public static Result descriptions() {
		return ok(views.html.desc.render(Description.all()));
	}

	public static Result rest() {
		ObjectNode result = Json.newObject();
		ArrayNode descriptions = Json.newObject().arrayNode();
		for(Description desc : Description.find.all()){
			ObjectNode value = Json.newObject();
			value.put("url", desc.url);
			descriptions.add(value);
		}
		result.put("descriptions", descriptions);
		return ok(result);
	}
	public static Result refresh() throws IOException{
		Description._refresh();
		return redirect(routes.Application.descriptions());
	}
}