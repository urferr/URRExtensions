import java.nio.charset.StandardCharsets;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.compxc.server.remote.proxy.config.XentisHttpGatewayConnectionConfiguration.RequestLoggerExchangeFilterFunction;

import reactor.core.publisher.Mono;

public class RequestLoggerExchangeFilterFunction implements ExchangeFilterFunction {
		private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggerExchangeFilterFunction.class);

		@Override
		public Mono<ClientResponse> filter(ClientRequest theRequest, ExchangeFunction theNext) {
			logRequest(theRequest);
			return theNext
					.exchange(interceptBody(theRequest))
					.doOnNext(this::logResponse)
					.map(this::interceptBody);
		}

		private ClientRequest interceptBody(ClientRequest theRequest) {
			return ClientRequest.from(theRequest)
					.body((theOutputMessage, theContext) -> theRequest.body().insert(new ClientHttpRequestDecorator(theOutputMessage) {
						@Override
						public Mono<Void> writeWith(Publisher<? extends DataBuffer> theBody) {
							return super.writeWith(
									Mono.from(theBody)
											.doOnNext(theDataBuffer -> logRequestBody(theDataBuffer)));
						}
					}, theContext))
					.build();
		}

		private ClientResponse interceptBody(ClientResponse theResponse) {
			return theResponse.mutate()
					.body(theData -> theData.doOnNext(this::logResponseBody))
					.build();
		}

		private void logRequest(ClientRequest theRequest) {
			LOGGER.info("DOWNSTREAM REQUEST: METHOD {}, URI: {}, HEADERS: {}", theRequest.method(), theRequest.url(), theRequest.headers());
		}

		private void logRequestBody(DataBuffer theDataBuffer) {
			LOGGER.info("DOWNSTREAM REQUEST: BODY: {}", theDataBuffer.toString(StandardCharsets.UTF_8));
		}

		private void logResponse(ClientResponse theResponse) {
			LOGGER.info("DOWNSTREAM RESPONSE: STATUS: {}, HEADERS: {}", theResponse.rawStatusCode(), theResponse.headers().asHttpHeaders());
		}

		private void logResponseBody(DataBuffer theDataBuffer) {
			LOGGER.info("DOWNSTREAM RESPONSE: BODY: {}", theDataBuffer.toString(StandardCharsets.UTF_8));
		}

	}
