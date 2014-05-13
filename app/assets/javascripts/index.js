
var VALID_COUNTIES = ['New York County', 'Kings County', 'Queens County', 'Richmond County', 'Bronx County'];
var NYC_CENTER = new google.maps.LatLng(40.7692907, -73.8931637);

function showValidationError(msg) {
    console.log("validation error: " + msg);
}

function arrayStartsWith(arr, expected) {
    return arr.length >= 1 && arr[0] === expected;
}

function findAddressComponent(place, componentType) {
    var addresses = place.address_components;

    for (var i = 0; i < addresses.length; i++) {
        var address = addresses[i];
        if (arrayStartsWith(address.types, componentType)) {
            return address;
        }
    }
    return false;
}

function validatePlace(place) {

    if (!arrayStartsWith(place.types, 'street_address')) {
        showValidationError("Please choose a valid building address.");
        return false;
    }

    var county = findAddressComponent(place, 'administrative_area_level_2');
    if (!county || $.inArray(county.long_name, VALID_COUNTIES) === -1) {
        showValidationError("Please choose an address in New York City."); // TODO: suggest one!
        return false;
    }

    return true;
}

function loadPlace(place) {
    document.location = jsRoutes.controllers.BuildingController.infoPage(place.reference).url;
}

$(function() {

    var input = $('#pac-input')[0];

    var autocomplete = new google.maps.places.Autocomplete(input, {
        bounds: new google.maps.LatLngBounds(NYC_CENTER, NYC_CENTER),
        types: ['geocode']
    });

    google.maps.event.addListener(autocomplete, 'place_changed', function() {
        var place = autocomplete.getPlace();
        if (!place.geometry) {
            return;
        }

        if (!validatePlace(place)) {
            return;
        }

        loadPlace(place);
    });
});
