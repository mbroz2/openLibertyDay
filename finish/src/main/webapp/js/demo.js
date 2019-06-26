function getCoordinates() {
    document.getElementById("city").style.removeProperty("display");
    document.getElementById("city").innerHTML = "City: Retrieving.....";
    document.getElementById("coordinates").style.removeProperty("display");
    document.getElementById("coordinates").innerHTML = "Coordinates: Retrieving.....";
    var table = document.getElementsByTagName('table')[0];
    if (table) {
        document.getElementById("main").removeChild(table);
    }

    var zip = document.getElementById("zipCode").value;
    var url = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=us-zip-code-latitude-and-longitude&facet=state&facet=timezone&facet=dst&q="
        + zip;
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            var response = JSON.parse(this.responseText);
            var city = response.records[0].fields.city;
            var coordinates = response.records[0].fields.geopoint;
            document.getElementById("city").innerHTML = "City: " + city;
            document.getElementById("coordinates").innerHTML = "Coordinates: "
                + coordinates;
            getStation(coordinates);
        } else if (this.readyState == 4 && this.status != 200) {
            document.getElementById("city").innerHTML = "City: Unavailable, try again later.";
            document.getElementById("coordinates").innerHTML = "Coordinates: Unavailable, try again later.";
        }
    };
    xhttp.open("GET", url, true);
    xhttp.send();
};

function getStation(cooridnates) {
    document.getElementById("cwa").style.removeProperty("display");
    document.getElementById("cwa").innerHTML = "Center Weather Advisory: Retrieving.....";
    var url = "https://api.weather.gov/points/" + cooridnates;
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            var response = JSON.parse(this.responseText);
            var cwa = response.properties.cwa;
            var forecastURL = response.properties.forecast;
            document.getElementById("cwa").innerHTML = "Center Weather Advisory: "
                + cwa;
            getWeeklyForecast(forecastURL);
        } else if (this.readyState == 4 && this.status != 200) {
            document.getElementById("cwa").innerHTML = "Center Weather Advisory: Unavailable, try again later.";
        }
    };
    xhttp.open("GET", url, true);
    xhttp.send();
};

function getWeeklyForecast(forecastURL) {
    var url = forecastURL;
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            // Create forecast table
            table = document.createElement('table');
            trHeader = document.createElement('tr');
            trTemp = document.createElement('tr');
            trIcon = document.createElement('tr');

            var response = JSON.parse(this.responseText);
            for (period in response.properties.periods) {
                var day = response.properties.periods[period].name;
                var temp = response.properties.periods[period].temperature + " "
                    + response.properties.periods[period].temperatureUnit;
                var forecastText = response.properties.periods[period].detailedForecast;
                var icon = response.properties.periods[period].icon;

                header = document.createElement('th');
                header.textContent = day;
                trHeader.appendChild(header);

                tdTemp = document.createElement('td');
                tdTemp.textContent = temp;
                trTemp.appendChild(tdTemp);

                tdIcon = document.createElement('td');
                iconImg = document.createElement('img');
                iconImg.src = icon;
                tdIcon.appendChild(iconImg);
                trIcon.appendChild(tdIcon);

                table.appendChild(trHeader);
                table.appendChild(trTemp);
                table.appendChild(trIcon);

                document.getElementsByTagName('main')[0].appendChild(table);
            }
        } else if (this.readyState == 4 && this.status != 200) {
            document.getElementById("cwa").innerHTML = "Unavailable, try again later.";
        }
    };
    xhttp.open("GET", url, true);
    xhttp.send();
}