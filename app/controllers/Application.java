package controllers;

import java.io.IOException;

import models.Description;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

	public static Result index() {
		return ok("Your new application is ready.");
	}

	public static Result descriptions() {
		return ok(views.html.index.render(Description.all()));
	}

	public static Result refresh() throws IOException{
		Description._refresh();
		return ok("ok");
	}
}