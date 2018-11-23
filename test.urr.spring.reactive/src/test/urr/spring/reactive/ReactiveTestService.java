package test.urr.spring.reactive;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.OSGiJettyHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.xnife.http.service.HttpService;

import test.urr.spring.reactive.example.DummyPersonRepository;
import test.urr.spring.reactive.example.PersonHandler;

public class ReactiveTestService implements InitializingBean, DisposableBean {
	private static final String BASE_SERVLET_PATH = "/reactive";

	private HttpService httpService;

	/**
	 * Sets the httpService.
	 *
	 * @param theHttpService the httpService to set
	 */
	public void setHttpService(HttpService theHttpService) {
		httpService = theHttpService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		RouterFunction<?> route = routingFunction();
		HttpHandler httpHandler = toHttpHandler(route);

		Servlet aServlet = new OSGiJettyHttpHandlerAdapter(httpHandler, BASE_SERVLET_PATH);
		Map<String, String> servletConfig = new HashMap<>();

		httpService.registerServlet(BASE_SERVLET_PATH, aServlet, servletConfig);
	}

	public RouterFunction<ServerResponse> routingFunction() {
		PersonRepository repository = new DummyPersonRepository();
		PersonHandler handler = new PersonHandler(repository);

		return nest(
				path(BASE_SERVLET_PATH + "/person"),
				nest(
						accept(APPLICATION_JSON, TEXT_EVENT_STREAM),
						route(GET("/{id}"), handler::getPerson)
								.andRoute(method(HttpMethod.GET), handler::listPeople)).andRoute(POST("/").and(contentType(APPLICATION_JSON)), handler::createPerson));
	}

	@Override
	public void destroy() throws Exception {
		httpService.unregister(BASE_SERVLET_PATH);
	}
}
