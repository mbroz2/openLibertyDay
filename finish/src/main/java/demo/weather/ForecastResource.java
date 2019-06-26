package demo.weather;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
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

		if (zip == null || zip.isEmpty()) {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("error", "zip value is null. Specify with query param ?zip=00000");
			return builder.build();
		}

		ZipToGeoPoint zipData = new ZipToGeoPoint(zip);
		zipData.resolve();

		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("zip", zip);
		builder.add("city", zipData.getCity());
		builder.add("geopoint", zipData.getGeopoint());
		getTodaysForecast(zipData.getGeopoint(), builder);

		return builder.build();
	}

	private void getTodaysForecast(String geopoint, JsonObjectBuilder builder) {
		// e.g. https://api.weather.gov/points/30.5039,-97.8242
		// good : "https://api.weather.gov/points/30.5013,-97.8309";
		String targetUrl = "http://api.weather.gov/points/" + geopoint;
		log.info("resolving points URL: " + targetUrl);

		Client client = RestClientFactory.getRestClient();

		Response response = client.target(targetUrl).request().get();
		JsonObject jObj = response.readEntity(JsonObject.class);

		response.close();
		client.close();

		builder.add("cwa", jObj.getJsonObject("properties").get("cwa"));
		String forecastURL = getForecastURL(jObj);
		log.info("resolving forecast URL: " + forecastURL);
		getForecastFromURL(forecastURL, builder);
	}

	private String getForecastURL(JsonObject jObj) {
		return jObj.getJsonObject("properties").getString("forecast");
	}

	private void getForecastFromURL(String forecastURL, JsonObjectBuilder builder) {
		Client client = RestClientFactory.getRestClient();

		Response response = client.target(forecastURL).request().get();
		JsonObject jObj = response.readEntity(JsonObject.class);

		response.close();
		client.close();

		JsonArray periods = jObj.getJsonObject("properties").getJsonArray("periods");
		builder.add("periods", periods);
	}

}