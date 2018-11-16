package org.springframework.http.server.reactive;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

public class OSGiJettyHttpHandlerAdapter extends JettyHttpHandlerAdapter {
	private final String baseServletPath;

	public OSGiJettyHttpHandlerAdapter(HttpHandler theHttpHandler, String theBaseServletPath) {
		super(theHttpHandler);
		baseServletPath = theBaseServletPath;
	}

	@Override
	public String getServletPath() {
		return baseServletPath;
	}

	@Override
	public void init(ServletConfig theConfig) {
		// do nothing
	}

	@Override
	protected ServletServerHttpRequest createRequest(HttpServletRequest request, AsyncContext context)
			throws IOException,
			URISyntaxException {
		Assert.notNull(getServletPath(), "Servlet path is not initialized");
		return new OSGiJettyServerHttpRequest(request, context, getServletPath(), getDataBufferFactory(), getBufferSize());
	}

	@Override
	protected ServletServerHttpResponse createResponse(
			HttpServletResponse response,
			AsyncContext context,
			ServletServerHttpRequest request)
			throws IOException {

		return new OSGiJettyServerHttpResponse(
				response,
				context,
				getDataBufferFactory(),
				getBufferSize(),
				request);
	}

	private static final class OSGiJettyServerHttpRequest extends ServletServerHttpRequest {
		OSGiJettyServerHttpRequest(
				HttpServletRequest request,
				AsyncContext asyncContext,
				String servletPath,
				DataBufferFactory bufferFactory,
				int bufferSize)
				throws IOException,
				URISyntaxException {

			super(createHeaders(request), request, asyncContext, servletPath, bufferFactory, bufferSize);
		}

		private static HttpHeaders createHeaders(HttpServletRequest request) {
			HttpFields fields = ((Request) ((ServletRequestWrapper) request).getRequest()).getMetaData().getFields();
			return new HttpHeaders(new JettyHeadersAdapter(fields));
		}
	}

	private static final class OSGiJettyServerHttpResponse extends ServletServerHttpResponse {

		OSGiJettyServerHttpResponse(
				HttpServletResponse response,
				AsyncContext asyncContext,
				DataBufferFactory bufferFactory,
				int bufferSize,
				ServletServerHttpRequest request)
				throws IOException {

			super(createHeaders(response), response, asyncContext, bufferFactory, bufferSize, request);
		}

		private static HttpHeaders createHeaders(HttpServletResponse response) {
			HttpFields fields = ((Response) ((ServletResponseWrapper) response).getResponse()).getHttpFields();
			return new HttpHeaders(new JettyHeadersAdapter(fields));
		}

		@Override
		protected void applyHeaders() {
			MediaType contentType = getHeaders().getContentType();
			HttpServletResponse response = getNativeResponse();
			if (response.getContentType() == null && contentType != null) {
				response.setContentType(contentType.toString());
			}
			Charset charset = (contentType != null ? contentType.getCharset() : null);
			if (response.getCharacterEncoding() == null && charset != null) {
				response.setCharacterEncoding(charset.name());
			}
			long contentLength = getHeaders().getContentLength();
			if (contentLength != -1) {
				response.setContentLengthLong(contentLength);
			}
		}

		@Override
		protected int writeToOutputStream(DataBuffer dataBuffer) throws IOException {
			ByteBuffer input = dataBuffer.asByteBuffer();
			int len = input.remaining();
			ServletResponse response = getNativeResponse();
			((HttpOutput) ((Response) ((ServletResponseWrapper) response).getResponse()).getOutputStream()).write(input);
			return len;
		}
	}
}
