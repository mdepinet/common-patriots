package org.commonpatriots.frontend;

import com.google.inject.servlet.ServletModule;

public class CPServletModule extends ServletModule {

	@Override
	protected void configureServlets() {
		serve("/account", "/account/*").with(AccountManagementServlet.class);
		serve("/admin", "/admin/*").with(AdminServlet.class);
		serve("/polygon", "/polygon/*", "/polygons", "/polygons/*").with(PolygonServlet.class);
		serve("/_ah/mail/*").with(EmailServlet.class);
		serve("/serviceUnit", "/serviceUnits", "/serviceUnit/*", "/serviceUnits/*").with(ServiceUnitServlet.class);
		// Everything but _ah requests -> Allows login/logout when debugging locally
		serveRegex("^(?!.*_ah).*").with(CommonPatriotsServlet.class);
	  }
}
