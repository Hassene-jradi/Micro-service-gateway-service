package org.sid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableHystrix
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

	@Bean
	RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
		return builder.routes().route(r -> r.path("/publicCountries/**")
				.filters(f -> f.addRequestHeader("x-rapidapi-host", "restcountries-v1.p.rapidapi.com")
						.addRequestHeader("x-rapidapi-key", "6531ce0694msh721004932f964a0p1d7efejsn009a36949d46")
						.rewritePath("/publicCountries/(?<segment>.*)", "/${segment}")
						.hystrix(h -> h.setName("countries").setFallbackUri("forward:/defaultCountries")))

				.uri("https://restcountries-v1.p.rapidapi.com").id("r1"))
				.route(r -> r.path("/exchange/**")
						.filters(f -> f.addRequestHeader("x-rapidapi-host", "currency-exchange.p.rapidapi.com")
								.addRequestHeader("x-rapidapi-key",
										"6531ce0694msh721004932f964a0p1d7efejsn009a36949d46")
								.rewritePath("/exchange/(?<segment>.*)", "/${segment}")
								.hystrix(h->h.setName("exchange").setFallbackUri("forward:/defaultExchange")))
						.uri("https://currency-exchange.p.rapidapi.com").id("r2"))

				.build();
	}

	@Bean
	DiscoveryClientRouteDefinitionLocator dynamicRoutes(ReactiveDiscoveryClient dc, DiscoveryLocatorProperties dlp) {
		return new DiscoveryClientRouteDefinitionLocator(dc, dlp);
	}

}
@RestController
class CircuitBreakerRestContoller {
	@GetMapping("/defaultCountries")
	Map<String, String> countries() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("Message", "defaultCountries");
		data.put("Countries", "France, Italie, Espagne,...");
		return data;
	}
	
	@GetMapping("/defaultExchange")
	String exchange() {
		return "exchange rate not avalaible ";
	}
}