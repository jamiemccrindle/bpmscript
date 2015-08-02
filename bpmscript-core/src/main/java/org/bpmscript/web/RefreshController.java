package org.bpmscript.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Refresh all of the application contexts when this URL is hit
 */
public class RefreshController extends AbstractController implements ApplicationContextAware {

    /**
     * Refreshes all of the application contexts and returns a "Refreshed" message if it succeeds.
     */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) getApplicationContext().getParent();
		configurableApplicationContext.refresh();
		((ConfigurableApplicationContext) getApplicationContext()).refresh();

		return new ModelAndView(new View() {
		
			@SuppressWarnings("unchecked")
			public void render(Map model, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				response.getWriter().write("<html><body>Refreshed... hopefully</body></html>");
				response.getWriter().flush();
				response.getWriter().close();
			}
		
			public String getContentType() {
				return "text/html";
			}
		
		});
	}

}
