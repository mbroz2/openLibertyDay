package demo.weather;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("forecast")
public class ForecastResource {
	private final static Logger log = Logger.getLogger("demo.weather");

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getForecast(@QueryParam("zip") String zip) {
		// Turn on all logging for development
		log.setLevel(Level.ALL);

		String geopoint = new ZipToGeoPoint(zip).resolve();
		String forecast = getTodaysForecast(geopoint);

		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("forecast", forecast);

		return builder.build();
	}

	private String getTodaysForecast(String geopoint) {
		// e.g. https://api.weather.gov/points/30.5039,-97.8242
		// good : "https://api.weather.gov/points/30.5013,-97.8309";
		String targetUrl = "http://api.weather.gov/points/" + geopoint;
		log.info("resolving points URL: " + targetUrl);

		Client client = RestClientFactory.getRestClient();

		Response response = client.target(targetUrl).request().get();
		JsonObject jObj = response.readEntity(JsonObject.class);

		response.close();
		client.close();

		String forecastURL = getForecastURL(jObj);
		log.info("resolving forecast URL: " + forecastURL);

		return getForecastFromURL(forecastURL);
	}

	private String getForecastFromURL(String forecastURL) {
		Client client = RestClientFactory.getRestClient();

		Response response = client.target(forecastURL).request().get();
		JsonObject jObj = response.readEntity(JsonObject.class);

		response.close();
		client.close();

		JsonObject curForecast = jObj.getJsonObject("properties").getJsonArray("periods").getJsonObject(0);
		String forecast = curForecast.getString("name") + " - " + curForecast.getString("detailedForecast");
		System.out.println("Current forecast: " + forecast);
		return forecast;
	}

	private String getForecastURL(JsonObject jObj) {
		return jObj.getJsonObject("properties").getString("forecast");
	}

}