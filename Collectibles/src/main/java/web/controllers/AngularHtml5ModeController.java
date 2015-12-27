package web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AngularHtml5ModeController {
		
	@RequestMapping(value = {
			"/app/products/",
			"/app/product/{id}",
			"/app/products/create/",
			"/app/hierarchy/create/"
	})
	public String forwardHello() {
	    return "forward:/app/index.html";
	}
}
