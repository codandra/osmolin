package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

/**
 *
 * @author sutter
 */
public class CrossDomainFilter implements Filter {
	public void init(FilterConfig filterConfig)
          throws ServletException {
	}
	
	public void doFilter(ServletRequest request,
              ServletResponse response,
              FilterChain chain)
              throws IOException,
                     ServletException {
		
		chain.doFilter(request, response);
		
		if(response instanceof HttpServletResponse) {
			((HttpServletResponse)response).addHeader("Access-Control-Allow-Origin", "*");
			((HttpServletResponse)response).addHeader("Access-Control-Allow-Headers", "*");
		}
	}
	
	public void destroy() {
		
	}
}
